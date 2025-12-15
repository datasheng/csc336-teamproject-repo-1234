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
DROP TABLE IF EXISTS event_tag CASCADE;
DROP TABLE IF EXISTS tag CASCADE;
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
-- Tag table
-- Stores available event tags for categorization
-- ============================================================================
CREATE TABLE tag (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

COMMENT ON TABLE tag IS 'Stores available event tags for categorization';
COMMENT ON COLUMN tag.id IS 'Auto-generated unique identifier';
COMMENT ON COLUMN tag.name IS 'Tag name (e.g., Tech, Entertainment, Sports)';

-- ============================================================================
-- Event-Tag junction table
-- Many-to-many relationship between events and tags
-- ============================================================================
CREATE TABLE event_tag (
    event_id INTEGER NOT NULL,
    tag_id INTEGER NOT NULL,
    PRIMARY KEY (event_id, tag_id),
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
);

COMMENT ON TABLE event_tag IS 'Links events to their tags';
COMMENT ON COLUMN event_tag.event_id IS 'Reference to event table';
COMMENT ON COLUMN event_tag.tag_id IS 'Reference to tag table';


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

-- Event tag queries
CREATE INDEX idx_event_tag_event ON event_tag(event_id);
CREATE INDEX idx_event_tag_tag ON event_tag(tag_id);

-- ============================================================================
-- Sample seed data
-- ============================================================================

-- Insert sample cities
INSERT INTO city (city, country) VALUES 
    ('Cambridge', 'USA'),
    ('Stanford', 'USA'),
    ('New Haven', 'USA'),
    ('Berkeley', 'USA'),
    ('New York', 'USA'),
    ('Albany', 'USA'),
    ('Buffalo', 'USA'),
    ('Stony Brook', 'USA'),
    ('Binghamton', 'USA'),
    ('Los Angeles', 'USA'),
    ('Chicago', 'USA'),
    ('Princeton', 'USA'),
    ('Philadelphia', 'USA'),
    ('Durham', 'USA'),
    ('Ithaca', 'USA'),
    ('Providence', 'USA'),
    ('Evanston', 'USA'),
    ('Ann Arbor', 'USA'),
    ('Atlanta', 'USA'),
    ('Boston', 'USA');

-- Insert sample campuses
INSERT INTO campus (name, address, zip_code, city) VALUES 
    -- Original 4
    ('Harvard University', 'Massachusetts Hall, Cambridge, MA', '02138', 'Cambridge'),
    ('Stanford University', '450 Serra Mall, Stanford, CA', '94305', 'Stanford'),
    ('Yale University', '149 Elm St, New Haven, CT', '06510', 'New Haven'),
    ('UC Berkeley', '200 California Hall, Berkeley, CA', '94720', 'Berkeley'),
    
    -- CUNY Schools
    ('CUNY Baruch College', '55 Lexington Ave, New York, NY', '10010', 'New York'),
    ('CUNY Brooklyn College', '2900 Bedford Ave, Brooklyn, NY', '11210', 'New York'),
    ('CUNY City College', '160 Convent Ave, New York, NY', '10031', 'New York'),
    ('CUNY Hunter College', '695 Park Ave, New York, NY', '10065', 'New York'),
    ('CUNY Queens College', '65-30 Kissena Blvd, Queens, NY', '11367', 'New York'),
    ('CUNY John Jay College', '524 W 59th St, New York, NY', '10019', 'New York'),
    ('CUNY Lehman College', '250 Bedford Park Blvd W, Bronx, NY', '10468', 'New York'),
    ('CUNY College of Staten Island', '2800 Victory Blvd, Staten Island, NY', '10314', 'New York'),
    
    -- SUNY Schools
    ('SUNY Albany', '1400 Washington Ave, Albany, NY', '12222', 'Albany'),
    ('SUNY Buffalo', '12 Capen Hall, Buffalo, NY', '14260', 'Buffalo'),
    ('SUNY Stony Brook', '100 Nicolls Rd, Stony Brook, NY', '11794', 'Stony Brook'),
    ('SUNY Binghamton', '4400 Vestal Pkwy E, Binghamton, NY', '13902', 'Binghamton'),
    
    -- Other Well-Known Universities
    ('Columbia University', '116th St & Broadway, New York, NY', '10027', 'New York'),
    ('New York University', '70 Washington Square S, New York, NY', '10012', 'New York'),
    ('MIT', '77 Massachusetts Ave, Cambridge, MA', '02139', 'Cambridge'),
    ('UCLA', '405 Hilgard Ave, Los Angeles, CA', '90095', 'Los Angeles'),
    ('USC', '3551 Trousdale Pkwy, Los Angeles, CA', '90089', 'Los Angeles'),
    ('University of Chicago', '5801 S Ellis Ave, Chicago, IL', '60637', 'Chicago'),
    ('Princeton University', '1 Nassau Hall, Princeton, NJ', '08544', 'Princeton'),
    ('University of Pennsylvania', '3451 Walnut St, Philadelphia, PA', '19104', 'Philadelphia'),
    ('Duke University', '103 Allen Building, Durham, NC', '27708', 'Durham'),
    ('Cornell University', '300 Day Hall, Ithaca, NY', '14853', 'Ithaca'),
    ('Brown University', '1 Prospect St, Providence, RI', '02912', 'Providence'),
    ('Northwestern University', '633 Clark St, Evanston, IL', '60208', 'Evanston'),
    ('University of Michigan', '500 S State St, Ann Arbor, MI', '48109', 'Ann Arbor'),
    ('Georgia Tech', '225 North Ave NW, Atlanta, GA', '30332', 'Atlanta'),
    ('Boston University', '1 Silber Way, Boston, MA', '02215', 'Boston'),
    ('Northeastern University', '360 Huntington Ave, Boston, MA', '02115', 'Boston'),
    ('Boston College', '140 Commonwealth Ave, Chestnut Hill, MA', '02467', 'Boston');

-- Insert sample organizations
INSERT INTO organization (name, description) VALUES
    ('Computer Science Club', 'A community for CS students to learn, collaborate, and build projects together'),
    ('Student Government Association', 'Official student government representing student interests'),
    ('Cultural Arts Society', 'Promoting diversity through cultural events and performances'),
    ('Entrepreneurship Club', 'Fostering innovation and startup culture on campus'),
    ('Music & Arts Collective', 'Supporting musicians, artists, and creative expression'),
    ('Sports & Recreation Club', 'Organizing intramural sports and fitness activities'),
    ('Environmental Action Group', 'Advocating for sustainability and environmental awareness'),
    ('Pre-Med Society', 'Supporting students pursuing careers in medicine'),
    ('Finance & Investment Club', 'Learning about markets, trading, and financial literacy'),
    ('Debate & Public Speaking', 'Developing communication and argumentation skills'),
    ('Gaming & Esports Association', 'Competitive gaming and community events'),
    ('Film & Photography Club', 'Creating and appreciating visual media'),
    ('Volunteer & Community Service', 'Making a positive impact in local communities'),
    ('International Students Association', 'Supporting international students and cultural exchange'),
    ('Engineering Society', 'Connecting engineering students across disciplines');

-- Insert sample tags
INSERT INTO tag (name) VALUES
    ('Tech'),
    ('Entertainment'),
    ('Sports'),
    ('Academic'),
    ('Career'),
    ('Networking'),
    ('Music'),
    ('Art'),
    ('Culture'),
    ('Food'),
    ('Workshop'),
    ('Competition'),
    ('Social'),
    ('Gaming'),
    ('Health'),
    ('Sustainability'),
    ('Film'),
    ('Community Service'),
    ('Business'),
    ('Science');

-- Insert fee periods
INSERT INTO fee (id, start_time, end_time, fee_percent) VALUES
    (1, '2024-01-01 00:00:00', '2025-12-31 23:59:59', 0.05),
    (2, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 0.06);

-- Insert sample events (using future dates from 2025-2026)
INSERT INTO event (organizer_id, campus_id, capacity, description, start_time, end_time) VALUES
    -- Harvard events (campus_id = 1)
    (1, 1, 200, 'Harvard CS Hackathon 2025 - Build innovative solutions in 24 hours! Food and prizes provided.', '2025-12-20 09:00:00', '2025-12-21 17:00:00'),
    (5, 1, 150, 'Winter Jazz Concert - Harvard Jazz Band performs holiday classics and contemporary pieces', '2025-12-18 19:00:00', '2025-12-18 21:30:00'),
    (2, 1, 500, 'Spring Career Fair 2026 - Meet top employers from tech, finance, and consulting', '2026-02-15 10:00:00', '2026-02-15 16:00:00'),
    
    -- Stanford events (campus_id = 2)
    (4, 2, 100, 'Startup Pitch Night - Watch student entrepreneurs pitch their ideas to real VCs', '2025-12-19 18:00:00', '2025-12-19 21:00:00'),
    (1, 2, 80, 'AI/ML Workshop Series: Introduction to Large Language Models', '2026-01-10 14:00:00', '2026-01-10 17:00:00'),
    (6, 2, 300, 'Stanford vs Cal Basketball Watch Party', '2026-01-25 17:00:00', '2026-01-25 20:00:00'),
    
    -- Yale events (campus_id = 3)
    (3, 3, 250, 'International Food Festival - Taste cuisines from around the world', '2025-12-21 12:00:00', '2025-12-21 16:00:00'),
    (10, 3, 120, 'Yale Debate Championship - Watch top debaters compete', '2026-02-08 13:00:00', '2026-02-08 18:00:00'),
    
    -- UC Berkeley events (campus_id = 4)
    (7, 4, 400, 'Earth Day Festival 2026 - Sustainability fair with speakers and activities', '2026-04-22 10:00:00', '2026-04-22 17:00:00'),
    (15, 4, 150, 'Berkeley Engineering Expo - Student projects and research showcase', '2026-03-15 11:00:00', '2026-03-15 16:00:00'),
    
    -- CUNY Baruch events (campus_id = 5)
    (9, 5, 200, 'Wall Street 101: Introduction to Finance Careers', '2025-12-17 18:00:00', '2025-12-17 20:00:00'),
    (4, 5, 80, 'Baruch Startup Weekend - Build a business in 54 hours', '2026-02-21 18:00:00', '2026-02-23 21:00:00'),
    
    -- CUNY Hunter events (campus_id = 8)
    (8, 8, 100, 'Pre-Med Information Session - MCAT prep and application tips', '2026-01-15 17:00:00', '2026-01-15 19:00:00'),
    (13, 8, 150, 'Hunter Community Service Day', '2026-03-20 08:00:00', '2026-03-20 15:00:00'),
    
    -- Columbia events (campus_id = 17)
    (1, 17, 250, 'Columbia Tech Summit 2026 - Industry speakers and networking', '2026-03-05 09:00:00', '2026-03-05 18:00:00'),
    (5, 17, 300, 'Spring Musical: Les Misérables', '2026-04-10 19:30:00', '2026-04-10 22:30:00'),
    
    -- NYU events (campus_id = 18)
    (12, 18, 100, 'NYU Film Festival - Student short films screening', '2026-01-20 18:00:00', '2026-01-20 22:00:00'),
    (3, 18, 200, 'Lunar New Year Celebration', '2026-01-29 17:00:00', '2026-01-29 21:00:00'),
    (11, 18, 150, 'NYU Esports Tournament - League of Legends Championship', '2026-02-14 12:00:00', '2026-02-14 20:00:00'),
    
    -- MIT events (campus_id = 19)
    (15, 19, 300, 'MIT Robot Competition 2026', '2026-04-05 10:00:00', '2026-04-05 17:00:00'),
    (1, 19, 500, 'HackMIT 2026 - Annual hackathon with $50k in prizes', '2026-09-14 12:00:00', '2026-09-15 18:00:00'),
    
    -- UCLA events (campus_id = 20)
    (6, 20, 400, 'UCLA Spring Sports Festival', '2026-04-18 09:00:00', '2026-04-18 18:00:00'),
    (3, 20, 250, 'Bruin Cultural Night - Celebrating campus diversity', '2026-02-28 18:00:00', '2026-02-28 22:00:00'),
    
    -- University of Chicago events (campus_id = 22)
    (9, 22, 80, 'Economics Research Symposium', '2026-03-12 09:00:00', '2026-03-12 17:00:00'),
    (10, 22, 100, 'Chicago Debate Open - Regional competition', '2026-02-22 08:00:00', '2026-02-22 18:00:00'),
    
    -- Cornell events (campus_id = 25)
    (15, 25, 200, 'Cornell Engineering Career Fair', '2026-02-10 10:00:00', '2026-02-10 15:00:00'),
    (7, 25, 150, 'Sustainability in Agriculture Conference', '2026-04-08 09:00:00', '2026-04-08 17:00:00'),
    
    -- Georgia Tech events (campus_id = 29)
    (1, 29, 300, 'Georgia Tech Hackathon - HackGT 2026', '2026-10-10 18:00:00', '2026-10-12 12:00:00'),
    (4, 29, 120, 'Atlanta Startup Ecosystem Panel', '2026-03-25 17:00:00', '2026-03-25 20:00:00'),
    
    -- Boston University events (campus_id = 30)
    (5, 30, 200, 'BU Symphony Orchestra Spring Concert', '2026-04-15 19:00:00', '2026-04-15 21:30:00'),
    (14, 30, 100, 'International Coffee Hour - Meet students from around the world', '2025-12-16 15:00:00', '2025-12-16 17:00:00'),
    
    -- Northeastern events (campus_id = 31)
    (4, 31, 150, 'Co-op & Career Success Workshop', '2026-01-22 14:00:00', '2026-01-22 16:00:00'),
    (11, 31, 200, 'Northeastern Gaming Convention', '2026-03-08 10:00:00', '2026-03-08 22:00:00');

-- Insert ticket costs for events (mix of free, paid, and multi-tier pricing)
INSERT INTO cost (type, event_id, cost) VALUES
    -- Harvard CS Hackathon (free)
    ('General', 1, 0.00),
    
    -- Winter Jazz Concert (paid)
    ('General', 2, 15.00),
    ('Student', 2, 8.00),
    ('VIP', 2, 35.00),
    
    -- Spring Career Fair (free)
    ('General', 3, 0.00),
    
    -- Startup Pitch Night (low cost)
    ('General', 4, 5.00),
    ('Student', 4, 0.00),
    
    -- AI/ML Workshop (paid workshop)
    ('General', 5, 25.00),
    ('Student', 5, 10.00),
    
    -- Basketball Watch Party (free)
    ('General', 6, 0.00),
    
    -- International Food Festival (low cost)
    ('General', 7, 10.00),
    ('Student', 7, 5.00),
    
    -- Yale Debate Championship (free to watch)
    ('General', 8, 0.00),
    
    -- Earth Day Festival (free)
    ('General', 9, 0.00),
    
    -- Berkeley Engineering Expo (free)
    ('General', 10, 0.00),
    
    -- Wall Street 101 (free educational)
    ('General', 11, 0.00),
    
    -- Baruch Startup Weekend (paid)
    ('General', 12, 50.00),
    ('Student', 12, 25.00),
    
    -- Pre-Med Information Session (free)
    ('General', 13, 0.00),
    
    -- Hunter Community Service Day (free)
    ('General', 14, 0.00),
    
    -- Columbia Tech Summit (paid conference)
    ('General', 15, 75.00),
    ('Student', 15, 25.00),
    ('VIP', 15, 150.00),
    
    -- Spring Musical (paid)
    ('General', 16, 30.00),
    ('Student', 16, 15.00),
    ('Premium', 16, 50.00),
    
    -- NYU Film Festival (low cost)
    ('General', 17, 8.00),
    ('Student', 17, 0.00),
    
    -- Lunar New Year Celebration (free)
    ('General', 18, 0.00),
    
    -- NYU Esports Tournament (low cost)
    ('Spectator', 19, 5.00),
    ('Participant', 19, 15.00),
    
    -- MIT Robot Competition (free to watch)
    ('General', 20, 0.00),
    ('Team Registration', 20, 100.00),
    
    -- HackMIT (free)
    ('General', 21, 0.00),
    
    -- UCLA Spring Sports Festival (free)
    ('General', 22, 0.00),
    
    -- Bruin Cultural Night (low cost)
    ('General', 23, 12.00),
    ('Student', 23, 5.00),
    
    -- Economics Research Symposium (paid)
    ('General', 24, 40.00),
    ('Student', 24, 15.00),
    
    -- Chicago Debate Open (free to watch)
    ('Spectator', 25, 0.00),
    ('Competitor', 25, 30.00),
    
    -- Cornell Engineering Career Fair (free)
    ('General', 26, 0.00),
    
    -- Sustainability Conference (paid)
    ('General', 27, 35.00),
    ('Student', 27, 10.00),
    
    -- HackGT (free)
    ('General', 28, 0.00),
    
    -- Atlanta Startup Panel (low cost)
    ('General', 29, 10.00),
    ('Student', 29, 0.00),
    
    -- BU Symphony Concert (paid)
    ('General', 30, 20.00),
    ('Student', 30, 8.00),
    ('Premium', 30, 40.00),
    
    -- International Coffee Hour (free)
    ('General', 31, 0.00),
    
    -- Co-op Workshop (free)
    ('General', 32, 0.00),
    
    -- Gaming Convention (paid)
    ('Day Pass', 33, 15.00),
    ('Student', 33, 8.00),
    ('VIP', 33, 35.00);

-- Insert event tags (linking events to tags)
-- Tag IDs: 1=Tech, 2=Entertainment, 3=Sports, 4=Academic, 5=Career, 6=Networking, 7=Music, 8=Art, 9=Culture, 10=Food
-- Tag IDs: 11=Workshop, 12=Competition, 13=Social, 14=Gaming, 15=Health, 16=Sustainability, 17=Film, 18=Community Service, 19=Business, 20=Science
INSERT INTO event_tag (event_id, tag_id) VALUES
    -- Harvard CS Hackathon (Tech, Competition)
    (1, 1), (1, 12),
    -- Winter Jazz Concert (Music, Entertainment)
    (2, 7), (2, 2),
    -- Spring Career Fair (Career, Networking)
    (3, 5), (3, 6),
    -- Startup Pitch Night (Business, Networking, Tech)
    (4, 19), (4, 6), (4, 1),
    -- AI/ML Workshop (Tech, Workshop, Academic)
    (5, 1), (5, 11), (5, 4),
    -- Basketball Watch Party (Sports, Social, Entertainment)
    (6, 3), (6, 13), (6, 2),
    -- International Food Festival (Food, Culture, Social)
    (7, 10), (7, 9), (7, 13),
    -- Yale Debate Championship (Academic, Competition)
    (8, 4), (8, 12),
    -- Earth Day Festival (Sustainability, Community Service)
    (9, 16), (9, 18),
    -- Berkeley Engineering Expo (Tech, Academic, Career)
    (10, 1), (10, 4), (10, 5),
    -- Wall Street 101 (Career, Business, Academic)
    (11, 5), (11, 19), (11, 4),
    -- Baruch Startup Weekend (Business, Tech, Competition)
    (12, 19), (12, 1), (12, 12),
    -- Pre-Med Information Session (Health, Academic, Career)
    (13, 15), (13, 4), (13, 5),
    -- Hunter Community Service Day (Community Service, Social)
    (14, 18), (14, 13),
    -- Columbia Tech Summit (Tech, Career, Networking)
    (15, 1), (15, 5), (15, 6),
    -- Spring Musical (Entertainment, Music, Art)
    (16, 2), (16, 7), (16, 8),
    -- NYU Film Festival (Film, Art, Entertainment)
    (17, 17), (17, 8), (17, 2),
    -- Lunar New Year Celebration (Culture, Food, Social)
    (18, 9), (18, 10), (18, 13),
    -- NYU Esports Tournament (Gaming, Competition, Entertainment)
    (19, 14), (19, 12), (19, 2),
    -- MIT Robot Competition (Tech, Competition, Science)
    (20, 1), (20, 12), (20, 20),
    -- HackMIT (Tech, Competition)
    (21, 1), (21, 12),
    -- UCLA Spring Sports Festival (Sports, Social)
    (22, 3), (22, 13),
    -- Bruin Cultural Night (Culture, Entertainment, Social)
    (23, 9), (23, 2), (23, 13),
    -- Economics Research Symposium (Academic, Business)
    (24, 4), (24, 19),
    -- Chicago Debate Open (Academic, Competition)
    (25, 4), (25, 12),
    -- Cornell Engineering Career Fair (Career, Tech, Networking)
    (26, 5), (26, 1), (26, 6),
    -- Sustainability Conference (Sustainability, Academic)
    (27, 16), (27, 4),
    -- HackGT (Tech, Competition)
    (28, 1), (28, 12),
    -- Atlanta Startup Panel (Business, Networking, Career)
    (29, 19), (29, 6), (29, 5),
    -- BU Symphony Concert (Music, Entertainment, Art)
    (30, 7), (30, 2), (30, 8),
    -- International Coffee Hour (Culture, Social, Networking)
    (31, 9), (31, 13), (31, 6),
    -- Co-op Workshop (Career, Workshop)
    (32, 5), (32, 11),
    -- Gaming Convention (Gaming, Entertainment, Social)
    (33, 14), (33, 2), (33, 13);

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
