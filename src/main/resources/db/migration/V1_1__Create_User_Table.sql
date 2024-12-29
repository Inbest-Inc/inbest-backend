CREATE TABLE Users (
    id SERIAL PRIMARY KEY,         -- Unique identifier for the user
    username VARCHAR(50) NOT NULL UNIQUE,    -- Username for the user
    email VARCHAR(255) NOT NULL UNIQUE, -- Email address (must be unique)
    password_hash VARCHAR(255) NOT NULL, -- Hashed password
    date_joined TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Date user joined
    name VARCHAR(50) NOT NULL,                  -- User's first name
    surname VARCHAR(50) NOT NULL,                -- User's last name
    role VARCHAR(20) NOT NULL DEFAULT 'USER'
);
