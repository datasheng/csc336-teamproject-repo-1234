const { Client } = require('pg');

const client = new Client({
  connectionString: 'postgresql://neondb_owner:npg_y7Whd1lfQZpH@ep-orange-hall-a4je5i96-pooler.us-east-1.aws.neon.tech/neondb?sslmode=require',
  ssl: { rejectUnauthorized: false }
});

async function checkAdmin() {
  try {
    await client.connect();
    console.log('Connected to database');
    
    const result = await client.query(
      'SELECT id, email, first_name, last_name, is_admin, password FROM "user" WHERE email = $1',
      ['admin@gmail.com']
    );
    
    if (result.rows.length === 0) {
      console.log('❌ Admin account NOT FOUND in database');
      console.log('The AdminInitializer should create it on startup.');
    } else {
      const admin = result.rows[0];
      console.log('✓ Admin account found:');
      console.log('  ID:', admin.id);
      console.log('  Email:', admin.email);
      console.log('  Name:', admin.first_name, admin.last_name);
      console.log('  Is Admin:', admin.is_admin);
      console.log('  Password hash:', admin.password.substring(0, 20) + '...');
    }
    
    await client.end();
  } catch (error) {
    console.error('Error:', error.message);
    await client.end();
    process.exit(1);
  }
}

checkAdmin();

