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
