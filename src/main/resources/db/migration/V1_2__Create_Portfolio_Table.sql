CREATE TABLE Portfolio (
    portfolio_id SERIAL PRIMARY KEY,
    Portfolio_name VARCHAR(100) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id INT NOT NULL,
    visibility VARCHAR(10) DEFAULT 'private',
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE
);
