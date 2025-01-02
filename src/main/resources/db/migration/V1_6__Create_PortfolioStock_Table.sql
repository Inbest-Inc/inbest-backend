CREATE TABLE PortfolioStock (
    portfolio_stock_id SERIAL PRIMARY KEY,
    portfolio_id INT NOT NULL,
    stock_id INT NOT NULL,
    quantity INT NOT NULL,
    CONSTRAINT fk_portfolio FOREIGN KEY (portfolio_id) REFERENCES Portfolio (portfolio_id) ON DELETE CASCADE,
    CONSTRAINT fk_stock FOREIGN KEY (stock_id) REFERENCES Stock (stock_id) ON DELETE CASCADE
);
