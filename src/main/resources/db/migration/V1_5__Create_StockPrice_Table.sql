CREATE TABLE StockPrice (
    stock_id INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    date TIMESTAMP NOT NULL,
    CONSTRAINT pk_stockprice PRIMARY KEY (stock_id, date),  -- Composite primary key
    CONSTRAINT fk_stock FOREIGN KEY (stock_id) REFERENCES Stock (stock_id) ON DELETE CASCADE
);

SELECT create_hypertable('StockPrice', 'date', chunk_time_interval => INTERVAL '1 month');
