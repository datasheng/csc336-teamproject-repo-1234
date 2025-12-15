// Simple script to run the admin migration
const { Client } = require('pg');

const client = new Client({
  connectionString: 'postgresql://neondb_owner:npg_y7Whd1lfQZpH@ep-orange-hall-a4je5i96-pooler.us-east-1.aws.neon.tech/neondb?sslmode=require',
  ssl: { rejectUnauthorized: false }
});

const migrationSQL = `
-- Add is_admin column to user table
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS is_admin BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN "user".is_admin IS 'Indicates if the user has admin privileges';

-- Create admin account (password: admin123)
-- The AdminInitializer will create this with proper BCrypt hash on startup
-- This is just to ensure the column exists
INSERT INTO "user" (first_name, last_name, email, password, campus_id, is_admin)
VALUES (
    'Admin',
    'User',
    'admin@gmail.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    1,
    TRUE
)
ON CONFLICT (email) DO UPDATE SET is_admin = TRUE;

-- Create index for admin lookups
CREATE INDEX IF NOT EXISTS idx_user_is_admin ON "user"(is_admin) WHERE is_admin = TRUE;
`;

async function runMigration() {
  try {
    await client.connect();
    console.log('Connected to database');
    
    await client.query(migrationSQL);
    console.log('Migration completed successfully!');
    
    await client.end();
    process.exit(0);
  } catch (error) {
    console.error('Migration failed:', error.message);
    await client.end();
    process.exit(1);
  }
}

runMigration();

