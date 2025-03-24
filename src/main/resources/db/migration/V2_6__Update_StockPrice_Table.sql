DROP TABLE StockPrice;

CREATE TABLE StockPrice (
    ticker_symbol VARCHAR(10) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    date TIMESTAMP NOT NULL,
    CONSTRAINT pk_stockprice PRIMARY KEY (ticker_symbol, date),  -- Composite primary key
    CONSTRAINT fk_stock FOREIGN KEY (ticker_symbol) REFERENCES Stock (ticker_symbol) ON DELETE CASCADE
);
