-- Create client table in PostgreSQL database
CREATE TABLE IF NOT EXISTS client (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INTEGER NOT NULL
);

-- Create index on name for better query performance
CREATE INDEX IF NOT EXISTS idx_client_name ON client(name);