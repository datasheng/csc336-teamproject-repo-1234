const { Client } = require('pg');
const bcrypt = require('bcrypt');

const client = new Client({
  connectionString: 'postgresql://neondb_owner:npg_y7Whd1lfQZpH@ep-orange-hall-a4je5i96-pooler.us-east-1.aws.neon.tech/neondb?sslmode=require',
  ssl: { rejectUnauthorized: false }
});

async function fixAdminPassword() {
  try {
    await client.connect();
    console.log('Connected to database');
    
    // Generate a proper BCrypt hash for "admin123"
    const saltRounds = 10;
    const hashedPassword = await bcrypt.hash('admin123', saltRounds);
    
    console.log('Generated new password hash');
    
    // Update the admin account with the correct password
    const result = await client.query(
      `UPDATE "user" 
       SET password = $1, is_admin = TRUE 
       WHERE email = $2`,
      [hashedPassword, 'admin@gmail.com']
    );
    
    if (result.rowCount === 0) {
      console.log('❌ Admin account not found - creating it...');
      // Create admin account if it doesn't exist
      await client.query(
        `INSERT INTO "user" (first_name, last_name, email, password, campus_id, is_admin)
         VALUES ($1, $2, $3, $4, $5, $6)
         ON CONFLICT (email) DO UPDATE SET password = $4, is_admin = $6`,
        ['Admin', 'User', 'admin@gmail.com', hashedPassword, 1, true]
      );
      console.log('✓ Admin account created');
    } else {
      console.log('✓ Admin password updated');
    }
    
    // Verify
    const verify = await client.query(
      'SELECT email, is_admin FROM "user" WHERE email = $1',
      ['admin@gmail.com']
    );
    console.log('Verified admin account:', verify.rows[0]);
    
    await client.end();
    console.log('Done!');
  } catch (error) {
    console.error('Error:', error.message);
    await client.end();
    process.exit(1);
  }
}

fixAdminPassword();

