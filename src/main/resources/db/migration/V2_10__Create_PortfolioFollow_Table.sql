-- Create PortfolioFollow table
CREATE TABLE PortfolioFollow (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    portfolio_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (portfolio_id) REFERENCES portfolio(portfolio_id) ON DELETE CASCADE,
    UNIQUE (user_id, portfolio_id)
);