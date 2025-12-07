# Google Cloud Platform Setup Instructions

This document provides instructions for deploying the Campus Events Platform to Google Cloud Platform (GCP).

## Prerequisites

1. A Google Cloud Platform account with billing enabled
2. `gcloud` CLI installed and configured
3. Docker installed locally
4. Neon PostgreSQL database set up with schema applied

## GCP Services Used

### 1. Cloud Run
Serverless container hosting for both frontend and backend services.

- **Frontend Service**: `campus-events-frontend`
- **Backend Service**: `campus-events-backend`
- Auto-scaling based on traffic
- Pay only for what you use

### 2. Cloud Pub/Sub (Future Milestone)
Message queue for real-time event updates.

- **Topic**: `event-updates`
- **Subscription**: `event-updates-sub`
- Used for pushing updates to connected WebSocket clients

### 3. Container Registry / Artifact Registry
Docker image storage for deployment.

- Store both frontend and backend images
- Integrates with Cloud Build for CI/CD

### 4. Cloud Build
CI/CD pipeline for automated deployments.

- Triggered on pushes to main branch
- Builds and deploys both services

### 5. Secret Manager
Secure storage for sensitive environment variables.

- Database credentials
- JWT secrets
- API keys

### 6. Service Accounts
For backend to access GCP services.

- Pub/Sub publisher/subscriber permissions
- Secret Manager access

## Initial GCP Setup

### Step 1: Create GCP Project

```bash
# Set your project ID
export PROJECT_ID="campus-events-platform"

# Create the project
gcloud projects create $PROJECT_ID --name="Campus Events Platform"

# Set as default project
gcloud config set project $PROJECT_ID

# Enable billing (do this in the console)
# https://console.cloud.google.com/billing
```

### Step 2: Enable Required APIs

```bash
# Enable all required APIs
gcloud services enable \
    run.googleapis.com \
    containerregistry.googleapis.com \
    artifactregistry.googleapis.com \
    cloudbuild.googleapis.com \
    pubsub.googleapis.com \
    secretmanager.googleapis.com
```

### Step 3: Create Artifact Registry Repository

```bash
# Create a Docker repository
gcloud artifacts repositories create campus-events \
    --repository-format=docker \
    --location=us-central1 \
    --description="Campus Events Platform Docker images"
```

### Step 4: Store Secrets in Secret Manager

```bash
# Store database URL
echo -n "jdbc:postgresql://your-neon-host/dbname?sslmode=require" | \
    gcloud secrets create DB_URL --data-file=-

# Store database username
echo -n "your-username" | \
    gcloud secrets create DB_USERNAME --data-file=-

# Store database password
echo -n "your-password" | \
    gcloud secrets create DB_PASSWORD --data-file=-

# Store JWT secret (generate a long random string)
openssl rand -base64 64 | tr -d '\n' | \
    gcloud secrets create JWT_SECRET --data-file=-
```

### Step 5: Create Pub/Sub Topic and Subscription

```bash
# Create topic for event updates
gcloud pubsub topics create event-updates

# Create subscription
gcloud pubsub subscriptions create event-updates-sub \
    --topic=event-updates \
    --ack-deadline=60
```

### Step 6: Create Service Account

```bash
# Create service account
gcloud iam service-accounts create campus-events-backend \
    --display-name="Campus Events Backend Service"

# Grant Pub/Sub permissions
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:campus-events-backend@$PROJECT_ID.iam.gserviceaccount.com" \
    --role="roles/pubsub.publisher"

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:campus-events-backend@$PROJECT_ID.iam.gserviceaccount.com" \
    --role="roles/pubsub.subscriber"

# Grant Secret Manager access
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:campus-events-backend@$PROJECT_ID.iam.gserviceaccount.com" \
    --role="roles/secretmanager.secretAccessor"
```

## Building and Deploying

### Build and Push Docker Images

```bash
# Configure Docker for GCR
gcloud auth configure-docker us-central1-docker.pkg.dev

# Build and push backend
cd backend
docker build -t us-central1-docker.pkg.dev/$PROJECT_ID/campus-events/backend:latest .
docker push us-central1-docker.pkg.dev/$PROJECT_ID/campus-events/backend:latest

# Build and push frontend
cd ../frontend
docker build -t us-central1-docker.pkg.dev/$PROJECT_ID/campus-events/frontend:latest .
docker push us-central1-docker.pkg.dev/$PROJECT_ID/campus-events/frontend:latest
```

### Deploy to Cloud Run

```bash
# Deploy backend
gcloud run deploy campus-events-backend \
    --image=us-central1-docker.pkg.dev/$PROJECT_ID/campus-events/backend:latest \
    --platform=managed \
    --region=us-central1 \
    --allow-unauthenticated \
    --service-account=campus-events-backend@$PROJECT_ID.iam.gserviceaccount.com \
    --set-secrets="DB_URL=DB_URL:latest,DB_USERNAME=DB_USERNAME:latest,DB_PASSWORD=DB_PASSWORD:latest,JWT_SECRET=JWT_SECRET:latest" \
    --set-env-vars="FRONTEND_URL=https://campus-events-frontend-xxxxx-uc.a.run.app"

# Get the backend URL
BACKEND_URL=$(gcloud run services describe campus-events-backend --platform=managed --region=us-central1 --format='value(status.url)')

# Deploy frontend
gcloud run deploy campus-events-frontend \
    --image=us-central1-docker.pkg.dev/$PROJECT_ID/campus-events/frontend:latest \
    --platform=managed \
    --region=us-central1 \
    --allow-unauthenticated

# Get the frontend URL
FRONTEND_URL=$(gcloud run services describe campus-events-frontend --platform=managed --region=us-central1 --format='value(status.url)')

echo "Frontend: $FRONTEND_URL"
echo "Backend: $BACKEND_URL"
```

### Update Backend with Frontend URL (for CORS)

```bash
gcloud run services update campus-events-backend \
    --platform=managed \
    --region=us-central1 \
    --set-env-vars="FRONTEND_URL=$FRONTEND_URL"
```

## CI/CD with Cloud Build

Create a `cloudbuild.yaml` file in the root of your repository:

```yaml
steps:
  # Build backend
  - name: 'gcr.io/cloud-builders/docker'
    args: ['build', '-t', 'us-central1-docker.pkg.dev/$PROJECT_ID/campus-events/backend:$COMMIT_SHA', './backend']
  
  # Build frontend
  - name: 'gcr.io/cloud-builders/docker'
    args: ['build', '-t', 'us-central1-docker.pkg.dev/$PROJECT_ID/campus-events/frontend:$COMMIT_SHA', './frontend']
  
  # Push images
  - name: 'gcr.io/cloud-builders/docker'
    args: ['push', 'us-central1-docker.pkg.dev/$PROJECT_ID/campus-events/backend:$COMMIT_SHA']
  
  - name: 'gcr.io/cloud-builders/docker'
    args: ['push', 'us-central1-docker.pkg.dev/$PROJECT_ID/campus-events/frontend:$COMMIT_SHA']
  
  # Deploy backend
  - name: 'gcr.io/google.com/cloudsdktool/cloud-sdk'
    args:
      - 'run'
      - 'deploy'
      - 'campus-events-backend'
      - '--image=us-central1-docker.pkg.dev/$PROJECT_ID/campus-events/backend:$COMMIT_SHA'
      - '--region=us-central1'
      - '--platform=managed'
    entrypoint: gcloud
  
  # Deploy frontend
  - name: 'gcr.io/google.com/cloudsdktool/cloud-sdk'
    args:
      - 'run'
      - 'deploy'
      - 'campus-events-frontend'
      - '--image=us-central1-docker.pkg.dev/$PROJECT_ID/campus-events/frontend:$COMMIT_SHA'
      - '--region=us-central1'
      - '--platform=managed'
    entrypoint: gcloud

images:
  - 'us-central1-docker.pkg.dev/$PROJECT_ID/campus-events/backend:$COMMIT_SHA'
  - 'us-central1-docker.pkg.dev/$PROJECT_ID/campus-events/frontend:$COMMIT_SHA'
```

## Cost Estimation

With Cloud Run's pay-per-use model, a small-scale campus events platform would cost approximately:

- **Cloud Run**: ~$0-50/month (depends on traffic)
- **Secret Manager**: ~$0.06/secret/month
- **Pub/Sub**: ~$0-10/month (depends on message volume)
- **Artifact Registry**: ~$0.10/GB/month

Total estimated cost: **$10-70/month** for a moderately active platform.

## Monitoring

Set up monitoring in the GCP Console:

1. Go to Cloud Monitoring
2. Create dashboards for:
   - Request latency
   - Error rates
   - Active connections
   - Container instance count

## Troubleshooting

### View Logs
```bash
# Backend logs
gcloud run services logs read campus-events-backend --region=us-central1

# Frontend logs
gcloud run services logs read campus-events-frontend --region=us-central1
```

### Check Service Status
```bash
gcloud run services describe campus-events-backend --region=us-central1
gcloud run services describe campus-events-frontend --region=us-central1
```

### Common Issues

1. **CORS errors**: Ensure FRONTEND_URL is correctly set in backend environment
2. **Database connection fails**: Verify DB_URL, DB_USERNAME, DB_PASSWORD secrets
3. **Cold start delays**: Consider setting minimum instances to 1

## Security Best Practices

1. Never commit secrets to Git
2. Use Secret Manager for all sensitive values
3. Enable VPC connectors for database access (if using private IPs)
4. Regularly rotate JWT secrets
5. Enable Cloud Armor for DDoS protection (production)
