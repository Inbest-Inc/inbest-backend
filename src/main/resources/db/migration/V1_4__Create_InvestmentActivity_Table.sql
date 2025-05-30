CREATE TABLE InvestmentActivity (
    activity_id SERIAL PRIMARY KEY,
    portfolio_id INT NOT NULL,
    stock_id INT NOT NULL,
    action_type VARCHAR(10) NOT NULL,
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    amount DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL,
    CONSTRAINT fk_portfolio FOREIGN KEY (portfolio_id) REFERENCES Portfolio (portfolio_id) ON DELETE CASCADE,
    CONSTRAINT fk_stock FOREIGN KEY (stock_id) REFERENCES Stock (stock_id) ON DELETE CASCADE
);
