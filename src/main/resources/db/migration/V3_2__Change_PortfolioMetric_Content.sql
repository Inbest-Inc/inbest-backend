-- Drop existing table first
DROP TABLE IF EXISTS portfoliometrics;

-- Create the portfolio_metrics table with all required columns
CREATE TABLE portfoliometrics (
                                   portfolio_metrics_id SERIAL PRIMARY KEY,
                                   portfolio_id INTEGER NOT NULL,
                                   last_updated_date TIMESTAMP NOT NULL,
                                   hourly_return NUMERIC,
                                   daily_return NUMERIC,
                                   monthly_return NUMERIC,
                                   total_return NUMERIC,
                                   beta NUMERIC,
                                   sharpe_ratio NUMERIC,
                                   volatility NUMERIC,
                                   portfolio_value NUMERIC,
                                   risk_score NUMERIC,
                                   risk_category VARCHAR(20),
                                   CONSTRAINT fk_portfolio
                                       FOREIGN KEY(portfolio_id)
                                           REFERENCES portfolio(portfolio_id)
);

-- Create an index for faster queries
CREATE INDEX idx_portfolio_metrics_portfolio_id_date
    ON portfoliometrics (portfolio_id, last_updated_date);
