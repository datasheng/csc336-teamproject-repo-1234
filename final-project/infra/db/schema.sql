-- ============================================================================
-- Campus Events Platform - Database Schema
-- ============================================================================
-- This schema is designed for PostgreSQL (Neon)
-- Run this file against your Neon database to create all tables
-- 
-- IMPORTANT: This project does NOT use an ORM.
-- All queries are written as raw SQL with prepared statements.
-- ============================================================================

-- Drop existing tables if they exist (in reverse dependency order)
DROP TABLE IF EXISTS ticket CASCADE;
DROP TABLE IF EXISTS cost CASCADE;
DROP TABLE IF EXISTS event CASCADE;
DROP TABLE IF EXISTS org_leadership CASCADE;
DROP TABLE IF EXISTS organization CASCADE;
DROP TABLE IF EXISTS "user" CASCADE;
DROP TABLE IF EXISTS campus CASCADE;
DROP TABLE IF EXISTS city CASCADE;
DROP TABLE IF EXISTS fee CASCADE;

-- ============================================================================
-- City table
-- Stores city and country information for campuses
-- ============================================================================
CREATE TABLE city (
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    PRIMARY KEY (city)
);

COMMENT ON TABLE city IS 'Stores city and country information for campus locations';
COMMENT ON COLUMN city.city IS 'City name - serves as primary key';
COMMENT ON COLUMN city.country IS 'Country where the city is located';

-- ============================================================================
-- Campus table
-- Stores university/campus information
-- ============================================================================
CREATE TABLE campus (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(300) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    city VARCHAR(100) NOT NULL,
    FOREIGN KEY (city) REFERENCES city(city)
);

COMMENT ON TABLE campus IS 'Stores university/campus information';
COMMENT ON COLUMN campus.id IS 'Auto-generated unique identifier';
COMMENT ON COLUMN campus.name IS 'Full name of the university/campus';
COMMENT ON COLUMN campus.address IS 'Street address of the campus';
COMMENT ON COLUMN campus.zip_code IS 'Postal/ZIP code';
COMMENT ON COLUMN campus.city IS 'Reference to city table';

-- ============================================================================
-- User table
-- Note: "user" is a reserved keyword in PostgreSQL, so we quote it
-- ============================================================================
CREATE TABLE "user" (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    campus_id INTEGER NOT NULL,
    FOREIGN KEY (campus_id) REFERENCES campus(id)
);

COMMENT ON TABLE "user" IS 'Stores user account information';
COMMENT ON COLUMN "user".id IS 'Auto-generated unique identifier';
COMMENT ON COLUMN "user".first_name IS 'User first name';
COMMENT ON COLUMN "user".last_name IS 'User last name';
COMMENT ON COLUMN "user".email IS 'Unique email address for login';
COMMENT ON COLUMN "user".password IS 'BCrypt hashed password';
COMMENT ON COLUMN "user".campus_id IS 'Reference to the campus the user belongs to';

-- ============================================================================
-- Organization table
-- Stores student organizations that can host events
-- ============================================================================
CREATE TABLE organization (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT
);

COMMENT ON TABLE organization IS 'Stores student organizations that can host events';
COMMENT ON COLUMN organization.id IS 'Auto-generated unique identifier';
COMMENT ON COLUMN organization.name IS 'Name of the organization';
COMMENT ON COLUMN organization.description IS 'Description of the organization and its activities';

-- ============================================================================
-- Organization leadership table
-- Many-to-many relationship between users and organizations
-- Users in this table can create/manage events for the organization
-- ============================================================================
CREATE TABLE org_leadership (
    user_id INTEGER NOT NULL,
    org_id INTEGER NOT NULL,
    PRIMARY KEY (user_id, org_id),
    FOREIGN KEY (user_id) REFERENCES "user"(id),
    FOREIGN KEY (org_id) REFERENCES organization(id)
);

COMMENT ON TABLE org_leadership IS 'Links users to organizations they lead';
COMMENT ON COLUMN org_leadership.user_id IS 'Reference to user table';
COMMENT ON COLUMN org_leadership.org_id IS 'Reference to organization table';

-- ============================================================================
-- Event table
-- Stores campus events organized by organizations
-- ============================================================================
CREATE TABLE event (
    id SERIAL PRIMARY KEY,
    organizer_id INTEGER NOT NULL,
    campus_id INTEGER NOT NULL,
    capacity INTEGER NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    FOREIGN KEY (organizer_id) REFERENCES organization(id),
    FOREIGN KEY (campus_id) REFERENCES campus(id)
);

COMMENT ON TABLE event IS 'Stores campus events';
COMMENT ON COLUMN event.id IS 'Auto-generated unique identifier';
COMMENT ON COLUMN event.organizer_id IS 'Organization hosting the event';
COMMENT ON COLUMN event.campus_id IS 'Campus where the event takes place';
COMMENT ON COLUMN event.capacity IS 'Maximum number of attendees';
COMMENT ON COLUMN event.description IS 'Event description and details';
COMMENT ON COLUMN event.start_time IS 'Event start date and time';
COMMENT ON COLUMN event.end_time IS 'Event end date and time';

-- ============================================================================
-- Cost table (ticket types and prices)
-- Defines different ticket types for each event with their prices
-- ============================================================================
CREATE TABLE cost (
    type VARCHAR(50) NOT NULL,
    event_id INTEGER NOT NULL,
    cost DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (type, event_id),
    FOREIGN KEY (event_id) REFERENCES event(id)
);

COMMENT ON TABLE cost IS 'Defines ticket types and prices for events';
COMMENT ON COLUMN cost.type IS 'Ticket type (e.g., General, VIP, Student, Early Bird)';
COMMENT ON COLUMN cost.event_id IS 'Reference to the event';
COMMENT ON COLUMN cost.cost IS 'Price in dollars (0.00 for free tickets)';

-- ============================================================================
-- Fees per time table
-- Stores what fee percent we had in each time period
-- ============================================================================

CREATE TABLE fee (
    id INTEGER NOT NULL PRIMARY KEY,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    fee_percent float NOT NULL
);

COMMENT ON TABLE fee IS 'Stores fee policy at different times';
COMMENT ON COLUMN fee.id IS 'Id of time period';
COMMENT ON COLUMN fee.start_time IS 'Starting time';
COMMENT ON COLUMN fee.end_time IS 'Ending time';
COMMENT ON COLUMN fee.fee_percent IS 'Decimal value(?) of fee we take';


-- ============================================================================
-- Ticket table
-- Stores purchased tickets linking users to events
-- ============================================================================
CREATE TABLE ticket (
    user_id INTEGER NOT NULL,
    event_id INTEGER NOT NULL,
    type VARCHAR(50) NOT NULL,
    time_period INTEGER NOT NULL,
    PRIMARY KEY (user_id, event_id, type),
    FOREIGN KEY (user_id) REFERENCES "user"(id),
    FOREIGN KEY (event_id) REFERENCES event(id),
    FOREIGN KEY (time_period) REFERENCES fee(id),
    FOREIGN KEY (type, event_id) REFERENCES cost(type, event_id)
);

COMMENT ON TABLE ticket IS 'Stores purchased tickets';
COMMENT ON COLUMN ticket.user_id IS 'User who purchased the ticket';
COMMENT ON COLUMN ticket.event_id IS 'Event the ticket is for';
COMMENT ON COLUMN ticket.type IS 'Type of ticket purchased';


-- ============================================================================
-- Indexes for common queries
-- ============================================================================

-- User lookups by email (for login)
CREATE INDEX idx_user_email ON "user"(email);

-- Event queries by campus
CREATE INDEX idx_event_campus ON event(campus_id);

-- Event queries by organizer
CREATE INDEX idx_event_organizer ON event(organizer_id);

-- Event queries by date/time
CREATE INDEX idx_event_start_time ON event(start_time);

-- Ticket queries by user (my tickets)
CREATE INDEX idx_ticket_user ON ticket(user_id);

-- Ticket queries by event (event attendees)
CREATE INDEX idx_ticket_event ON ticket(event_id);

-- Organization leadership queries
CREATE INDEX idx_org_leadership_user ON org_leadership(user_id);
CREATE INDEX idx_org_leadership_org ON org_leadership(org_id);

-- ============================================================================
-- Sample seed data
-- ============================================================================

-- Insert sample cities
INSERT INTO city (city, country) VALUES 
    ('Cambridge', 'USA'),
    ('Stanford', 'USA'),
    ('New Haven', 'USA'),
    ('Berkeley', 'USA');

-- Insert sample campuses
INSERT INTO campus (name, address, zip_code, city) VALUES 
    ('Harvard University', 'Massachusetts Hall, Cambridge, MA', '02138', 'Cambridge'),
    ('Stanford University', '450 Serra Mall, Stanford, CA', '94305', 'Stanford'),
    ('Yale University', '149 Elm St, New Haven, CT', '06510', 'New Haven'),
    ('UC Berkeley', '200 California Hall, Berkeley, CA', '94720', 'Berkeley');

-- Note: In production, you would NOT include sample users with plain text passwords
-- The password below is 'password123' hashed with BCrypt
-- INSERT INTO "user" (first_name, last_name, email, password, campus_id) VALUES 
--     ('John', 'Doe', 'john.doe@harvard.edu', '$2a$10$...', 1);

-- ============================================================================
-- Useful queries for testing (commented out)
-- ============================================================================

-- Get all events for a campus:
-- SELECT e.*, o.name as organizer_name 
-- FROM event e 
-- JOIN organization o ON e.organizer_id = o.id 
-- WHERE e.campus_id = 1;

-- Get all tickets for a user:
-- SELECT t.*, e.description, c.cost 
-- FROM ticket t 
-- JOIN event e ON t.event_id = e.id 
-- JOIN cost c ON t.type = c.type AND t.event_id = c.event_id 
-- WHERE t.user_id = 1;

-- Get remaining capacity for an event:
-- SELECT e.capacity - COALESCE(COUNT(t.user_id), 0) as remaining
-- FROM event e 
-- LEFT JOIN ticket t ON e.id = t.event_id 
-- WHERE e.id = 1
-- GROUP BY e.id, e.capacity;
