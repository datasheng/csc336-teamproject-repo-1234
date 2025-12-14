CREATE OR REPLACE PROCEDURE reset_database() --doesn't add any sample info currently
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
