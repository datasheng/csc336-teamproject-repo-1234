CREATE OR REPLACE PROCEDURE reset_database()
LANGUAGE plpgsql
AS $$
BEGIN
    DROP TABLE IF EXISTS ticket CASCADE;
    DROP TABLE IF EXISTS cost CASCADE;
    DROP TABLE IF EXISTS event CASCADE;
    DROP TABLE IF EXISTS org_leadership CASCADE;
    DROP TABLE IF EXISTS organization CASCADE;
    DROP TABLE IF EXISTS "user" CASCADE;
    DROP TABLE IF EXISTS campus CASCADE;
    DROP TABLE IF EXISTS city CASCADE;
    DROP TABLE IF EXISTS fee CASCADE;


    CREATE TABLE city (
        city VARCHAR(100) PRIMARY KEY,
        country VARCHAR(100) NOT NULL
    );

    CREATE TABLE campus (
        id SERIAL PRIMARY KEY,
        name VARCHAR(200) NOT NULL,
        address VARCHAR(300) NOT NULL,
        zip_code VARCHAR(20) NOT NULL,
        city VARCHAR(100) NOT NULL,
        FOREIGN KEY (city) REFERENCES city(city)
    );

    CREATE TABLE "user" (
        id SERIAL PRIMARY KEY,
        first_name VARCHAR(100) NOT NULL,
        last_name VARCHAR(100) NOT NULL,
        email VARCHAR(255) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL,
        campus_id INTEGER NOT NULL,
        FOREIGN KEY (campus_id) REFERENCES campus(id)
    );

    CREATE TABLE organization (
        id SERIAL PRIMARY KEY,
        name VARCHAR(200) NOT NULL,
        description TEXT
    );

    CREATE TABLE org_leadership (
        user_id INTEGER NOT NULL,
        org_id INTEGER NOT NULL,
        PRIMARY KEY (user_id, org_id),
        FOREIGN KEY (user_id) REFERENCES "user"(id),
        FOREIGN KEY (org_id) REFERENCES organization(id)
    );

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

    CREATE TABLE cost (
        type VARCHAR(50) NOT NULL,
        event_id INTEGER NOT NULL,
        cost DECIMAL(10,2) NOT NULL,
        PRIMARY KEY (type, event_id),
        FOREIGN KEY (event_id) REFERENCES event(id)
    );

    CREATE TABLE fee (
        id INTEGER PRIMARY KEY,
        start_time TIMESTAMP NOT NULL,
        end_time TIMESTAMP NOT NULL,
        fee_percent FLOAT NOT NULL
    );

    CREATE TABLE ticket (
        user_id INTEGER NOT NULL,
        event_id INTEGER NOT NULL,
        type VARCHAR(50) NOT NULL,
        time_period INTEGER NOT NULL,
        PRIMARY KEY (user_id, event_id, type),
        FOREIGN KEY (user_id) REFERENCES "user"(id),
        FOREIGN KEY (event_id) REFERENCES event(id),
        FOREIGN KEY (type, event_id) REFERENCES cost(type, event_id),
        FOREIGN KEY (time_period) REFERENCES fee(id)
    );

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


-- Insert fee periods
INSERT INTO fee (id, start_time, end_time, fee_percent) VALUES
    (1, '2024-01-01 00:00:00', '2025-12-31 23:59:59', 0.05),
    (2, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 0.06);

END;
$$;


CREATE OR REPLACE PROCEDURE insert_event(
    neworg_id INTEGER,
    newcampus_id INTEGER,
    newcapacity INTEGER,
    newdescription TEXT,
    newstart TIMESTAMP,
    newend TIMESTAMP
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO event (
        organizer_id,
        campus_id,
        capacity,
        description,
        start_time,
        end_time
    )
    VALUES (
        neworg_id,
        newcampus_id,
        newcapacity,
        newdescription,
        newstart,
        newend
    );
END;
$$;

-- ============================================================================
-- Procedure: Delete Event (with cascading deletes)
-- Deletes an event and all associated tickets, costs, and tags
-- Returns TRUE if successful, FALSE if event not found
-- ============================================================================
CREATE OR REPLACE FUNCTION delete_event_cascade(p_event_id INTEGER)
RETURNS BOOLEAN
LANGUAGE plpgsql
AS $$
BEGIN
    -- Check if event exists
    IF NOT EXISTS (SELECT 1 FROM event WHERE id = p_event_id) THEN
        RETURN FALSE;
    END IF;

    -- Delete associated tickets first (foreign key constraint)
    DELETE FROM ticket WHERE event_id = p_event_id;
    
    -- Delete associated costs
    DELETE FROM cost WHERE event_id = p_event_id;
    
    -- Delete associated tags
    DELETE FROM event_tag WHERE event_id = p_event_id;
    
    -- Delete the event
    DELETE FROM event WHERE id = p_event_id;
    
    RETURN TRUE;
END;
$$;

-- ============================================================================
-- Function: Get Event Analytics
-- Returns ticket counts and revenue breakdown for an event
-- ============================================================================
CREATE OR REPLACE FUNCTION get_event_analytics(p_event_id INTEGER)
RETURNS TABLE (
    ticket_type VARCHAR(50),
    ticket_count BIGINT,
    revenue DECIMAL(10,2)
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        t.type as ticket_type,
        COUNT(*)::BIGINT as ticket_count,
        COALESCE(SUM(c.cost), 0)::DECIMAL(10,2) as revenue
    FROM ticket t 
    JOIN cost c ON t.event_id = c.event_id AND t.type = c.type 
    WHERE t.event_id = p_event_id 
    GROUP BY t.type;
END;
$$;

-- ============================================================================
-- Function: Get Total Tickets Sold for an Event
-- ============================================================================
CREATE OR REPLACE FUNCTION get_tickets_sold(p_event_id INTEGER)
RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE
    ticket_count BIGINT;
BEGIN
    SELECT COUNT(*) INTO ticket_count 
    FROM ticket 
    WHERE event_id = p_event_id;
    
    RETURN COALESCE(ticket_count, 0);
END;
$$;

-- ============================================================================
-- Function: Check Event Capacity Available
-- Returns TRUE if there's capacity available for the event
-- ============================================================================
CREATE OR REPLACE FUNCTION check_event_capacity(p_event_id INTEGER)
RETURNS BOOLEAN
LANGUAGE plpgsql
AS $$
DECLARE
    v_capacity INTEGER;
    v_tickets_sold BIGINT;
BEGIN
    SELECT capacity INTO v_capacity FROM event WHERE id = p_event_id;
    
    IF v_capacity IS NULL THEN
        RETURN FALSE;
    END IF;
    
    SELECT COUNT(*) INTO v_tickets_sold FROM ticket WHERE event_id = p_event_id;
    
    RETURN v_tickets_sold < v_capacity;
END;
$$;

-- ============================================================================
-- Function: Purchase Ticket
-- Validates capacity and ticket type, then inserts the ticket
-- Returns: 'SUCCESS', 'EVENT_NOT_FOUND', 'SOLD_OUT', 'INVALID_TYPE', 'ALREADY_PURCHASED'
-- ============================================================================
CREATE OR REPLACE FUNCTION purchase_ticket(
    p_user_id INTEGER,
    p_event_id INTEGER,
    p_type VARCHAR(50),
    p_fee_period_id INTEGER
)
RETURNS TEXT
LANGUAGE plpgsql
AS $$
DECLARE
    v_capacity INTEGER;
    v_tickets_sold BIGINT;
BEGIN
    -- Check if event exists
    IF NOT EXISTS (SELECT 1 FROM event WHERE id = p_event_id) THEN
        RETURN 'EVENT_NOT_FOUND';
    END IF;
    
    -- Get event capacity
    SELECT capacity INTO v_capacity FROM event WHERE id = p_event_id;
    
    -- Get current tickets sold
    SELECT COUNT(*) INTO v_tickets_sold FROM ticket WHERE event_id = p_event_id;
    
    -- Check capacity
    IF v_tickets_sold >= v_capacity THEN
        RETURN 'SOLD_OUT';
    END IF;
    
    -- Check if ticket type exists
    IF NOT EXISTS (SELECT 1 FROM cost WHERE event_id = p_event_id AND type = p_type) THEN
        RETURN 'INVALID_TYPE';
    END IF;
    
    -- Check if user already has this ticket
    IF EXISTS (SELECT 1 FROM ticket WHERE user_id = p_user_id AND event_id = p_event_id AND type = p_type) THEN
        RETURN 'ALREADY_PURCHASED';
    END IF;
    
    -- Insert the ticket
    INSERT INTO ticket (user_id, event_id, type, time_period) 
    VALUES (p_user_id, p_event_id, p_type, p_fee_period_id);
    
    RETURN 'SUCCESS';
END;
$$;

-- ============================================================================
-- Function: Register User
-- Registers a new user with email uniqueness check
-- Returns: user ID on success, -1 if email exists, -2 if invalid campus
-- ============================================================================
CREATE OR REPLACE FUNCTION register_user(
    p_first_name VARCHAR(100),
    p_last_name VARCHAR(100),
    p_email VARCHAR(255),
    p_password VARCHAR(255),
    p_campus_id INTEGER
)
RETURNS INTEGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_user_id INTEGER;
BEGIN
    -- Check if email already exists
    IF EXISTS (SELECT 1 FROM "user" WHERE email = p_email) THEN
        RETURN -1;
    END IF;
    
    -- Check if campus exists
    IF NOT EXISTS (SELECT 1 FROM campus WHERE id = p_campus_id) THEN
        RETURN -2;
    END IF;
    
    -- Insert the user
    INSERT INTO "user" (first_name, last_name, email, password, campus_id)
    VALUES (p_first_name, p_last_name, p_email, p_password, p_campus_id)
    RETURNING id INTO v_user_id;
    
    RETURN v_user_id;
END;
$$;

-- ============================================================================
-- Function: Add Organization Leader
-- Adds a user as a leader of an organization with validation
-- Returns: 'SUCCESS', 'USER_NOT_FOUND', 'ORG_NOT_FOUND', 'ALREADY_LEADER'
-- ============================================================================
CREATE OR REPLACE FUNCTION add_org_leader(p_user_id INTEGER, p_org_id INTEGER)
RETURNS TEXT
LANGUAGE plpgsql
AS $$
BEGIN
    -- Check if user exists
    IF NOT EXISTS (SELECT 1 FROM "user" WHERE id = p_user_id) THEN
        RETURN 'USER_NOT_FOUND';
    END IF;
    
    -- Check if organization exists
    IF NOT EXISTS (SELECT 1 FROM organization WHERE id = p_org_id) THEN
        RETURN 'ORG_NOT_FOUND';
    END IF;
    
    -- Check if already a leader
    IF EXISTS (SELECT 1 FROM org_leadership WHERE user_id = p_user_id AND org_id = p_org_id) THEN
        RETURN 'ALREADY_LEADER';
    END IF;
    
    -- Add as leader
    INSERT INTO org_leadership (user_id, org_id) VALUES (p_user_id, p_org_id);
    
    RETURN 'SUCCESS';
END;
$$;

-- ============================================================================
-- Function: Get User Ticket Count for Event
-- Returns number of tickets a user has for a specific event
-- ============================================================================
CREATE OR REPLACE FUNCTION get_user_ticket_count(p_user_id INTEGER, p_event_id INTEGER)
RETURNS INTEGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_count 
    FROM ticket 
    WHERE user_id = p_user_id AND event_id = p_event_id;
    
    RETURN COALESCE(v_count, 0);
END;
$$;
