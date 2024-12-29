CREATE TABLE Stock (
    stock_id SERIAL PRIMARY KEY,
    ticker_symbol VARCHAR(10) NOT NULL UNIQUE,
    stock_name VARCHAR(100) NOT NULL,
    current_price DECIMAL(10, 2) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
