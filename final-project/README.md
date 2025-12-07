# Campus Events Platform

A full-stack campus events ticketing platform built with React, TypeScript, Java Spring Boot, and PostgreSQL.

## Tech Stack

- **Frontend**: React 18 + TypeScript + Vite + Tailwind CSS
- **Backend**: Java 17 + Spring Boot 3 (NO ORM - Raw SQL only)
- **Database**: PostgreSQL on Neon
- **Cloud**: Google Cloud Run + Google Cloud Pub/Sub
- **Containers**: Docker

## Project Structure

- `/frontend` - React TypeScript application
- `/backend` - Spring Boot REST API
- `/infra` - Infrastructure code and database migrations

## Local Development Setup

### Prerequisites

- Node.js 18+
- Java 17+
- Maven 3.9+
- Docker
- Neon PostgreSQL account

### Database Setup

1. Create a Neon PostgreSQL database
2. Run the migration: `psql <connection-string> -f infra/db/schema.sql`
3. Copy `.env.example` to `.env` and fill in your database credentials

### Backend Setup

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend will run on http://localhost:8080

### Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

Frontend will run on http://localhost:5173

### Docker Build

**Backend:**
```bash
cd backend
docker build -t campus-events-backend .
docker run -p 8080:8080 --env-file ../.env campus-events-backend
```

**Frontend:**
```bash
cd frontend
docker build -t campus-events-frontend .
docker run -p 80:80 campus-events-frontend
```

## Google Cloud Platform Setup (Future Milestone)

This application will be deployed to:
- **Cloud Run** for both frontend and backend containers
- **Cloud Pub/Sub** for real-time event updates
- **Neon PostgreSQL** for database

See `/infra/gcp/setup-instructions.md` for deployment instructions.

## Important: No ORM Policy

This project explicitly does NOT use any ORM framework. All database operations use raw SQL with prepared statements via Spring JDBC Template. This is a critical architectural decision for learning and performance reasons.

## API Documentation

Base URL: `http://localhost:8080/api`

**Health Check:**
- `GET /health` - Returns system status

(More endpoints will be added in future milestones)
