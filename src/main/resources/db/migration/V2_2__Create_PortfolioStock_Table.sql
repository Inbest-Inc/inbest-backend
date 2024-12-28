CREATE TABLE PortfolioStock (
    PortfolioStockID SERIAL PRIMARY KEY,
    PortfolioID INT NOT NULL,
    StockID INT NOT NULL,
    Quantity INT NOT NULL,
    CONSTRAINT fk_portfolio FOREIGN KEY (PortfolioID) REFERENCES Portfolio (PortfolioID) ON DELETE CASCADE,
    CONSTRAINT fk_stock FOREIGN KEY (StockID) REFERENCES Stock (StockID) ON DELETE CASCADE
);
