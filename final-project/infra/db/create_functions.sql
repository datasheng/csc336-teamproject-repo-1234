-- Function: Get Total Tickets Sold for an Event
CREATE OR REPLACE FUNCTION get_tickets_sold(p_event_id BIGINT)
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

-- Function: Check Event Capacity Available
CREATE OR REPLACE FUNCTION check_event_capacity(p_event_id BIGINT)
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

-- Function: Purchase Ticket
CREATE OR REPLACE FUNCTION purchase_ticket(
    p_user_id BIGINT,
    p_event_id BIGINT,
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
    IF NOT EXISTS (SELECT 1 FROM event WHERE id = p_event_id) THEN
        RETURN 'EVENT_NOT_FOUND';
    END IF;
    
    SELECT capacity INTO v_capacity FROM event WHERE id = p_event_id;
    SELECT COUNT(*) INTO v_tickets_sold FROM ticket WHERE event_id = p_event_id;
    
    IF v_tickets_sold >= v_capacity THEN
        RETURN 'SOLD_OUT';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM cost WHERE event_id = p_event_id AND type = p_type) THEN
        RETURN 'INVALID_TYPE';
    END IF;
    
    IF EXISTS (SELECT 1 FROM ticket WHERE user_id = p_user_id AND event_id = p_event_id AND type = p_type) THEN
        RETURN 'ALREADY_PURCHASED';
    END IF;
    
    INSERT INTO ticket (user_id, event_id, type, time_period) 
    VALUES (p_user_id, p_event_id, p_type, p_fee_period_id);
    
    RETURN 'SUCCESS';
END;
$$;

-- Procedure: Insert Event
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

-- Function: Delete Event (with cascading deletes)
CREATE OR REPLACE FUNCTION delete_event_cascade(p_event_id BIGINT)
RETURNS BOOLEAN
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM event WHERE id = p_event_id) THEN
        RETURN FALSE;
    END IF;

    DELETE FROM ticket WHERE event_id = p_event_id;
    DELETE FROM cost WHERE event_id = p_event_id;
    DELETE FROM event_tag WHERE event_id = p_event_id;
    DELETE FROM event WHERE id = p_event_id;
    
    RETURN TRUE;
END;
$$;

-- Function: Get Event Analytics
CREATE OR REPLACE FUNCTION get_event_analytics(p_event_id BIGINT)
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

-- Function: Register User
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
    IF EXISTS (SELECT 1 FROM "user" WHERE email = p_email) THEN
        RETURN -1;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM campus WHERE id = p_campus_id) THEN
        RETURN -2;
    END IF;
    
    INSERT INTO "user" (first_name, last_name, email, password, campus_id)
    VALUES (p_first_name, p_last_name, p_email, p_password, p_campus_id)
    RETURNING id INTO v_user_id;
    
    RETURN v_user_id;
END;
$$;

-- Function: Add Organization Leader
CREATE OR REPLACE FUNCTION add_org_leader(p_user_id BIGINT, p_org_id BIGINT)
RETURNS TEXT
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM "user" WHERE id = p_user_id) THEN
        RETURN 'USER_NOT_FOUND';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM organization WHERE id = p_org_id) THEN
        RETURN 'ORG_NOT_FOUND';
    END IF;
    
    IF EXISTS (SELECT 1 FROM org_leadership WHERE user_id = p_user_id AND org_id = p_org_id) THEN
        RETURN 'ALREADY_LEADER';
    END IF;
    
    INSERT INTO org_leadership (user_id, org_id) VALUES (p_user_id, p_org_id);
    
    RETURN 'SUCCESS';
END;
$$;

-- Function: Get User Ticket Count for Event
CREATE OR REPLACE FUNCTION get_user_ticket_count(p_user_id BIGINT, p_event_id BIGINT)
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
