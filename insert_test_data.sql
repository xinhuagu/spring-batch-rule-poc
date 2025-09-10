-- Insert test data into client table
INSERT INTO client (name, age) VALUES
    ('Alice Johnson', 28),
    ('Bob Smith', 35),
    ('Charlie Brown', 42),
    ('Diana Wilson', 31),
    ('Edward Davis', 29),
    ('Fiona Miller', 38),
    ('George Taylor', 45),
    ('Helen Anderson', 33),
    ('Ivan Martinez', 27),
    ('Julia Garcia', 39),
    ('Kevin Lee', 32),
    ('Linda Wang', 26),
    ('Michael Chen', 41),
    ('Nancy Rodriguez', 36),
    ('Oliver Thompson', 30);

-- Verify the data insertion
SELECT COUNT(*) as total_records FROM client;
SELECT * FROM client ORDER BY name;