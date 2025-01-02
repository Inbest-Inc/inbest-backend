-- 1. StockMetrics Table
CREATE TABLE StockMetrics (
                              stock_id INT NOT NULL,
                              date TIMESTAMP NOT NULL,
                              metric_name VARCHAR(100) NOT NULL,
                              current_price DECIMAL(10, 2) NOT NULL,
                              daily_return DECIMAL(10, 4),
                              monthly_return DECIMAL(10, 4),
                              yearly_return DECIMAL(10, 4),
                              last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT pk_stockmetrics PRIMARY KEY (stock_id, date),  -- Composite primary key
                              CONSTRAINT fk_stock FOREIGN KEY (stock_id) REFERENCES Stock (stock_id) ON DELETE CASCADE
);

-- 2. PortfolioMetrics Table
CREATE TABLE PortfolioMetrics (
                                  portfolio_id INT NOT NULL,
                                  date TIMESTAMP NOT NULL,
                                  metric_name VARCHAR(100) NOT NULL,
                                  total_value DECIMAL(15, 2) NOT NULL,
                                  monthly_return DECIMAL(10, 4),
                                  yearly_return DECIMAL(10, 4),
                                  ytd_return DECIMAL(10, 4),
                                  total_return DECIMAL(10, 4),
                                  average_return DECIMAL(10, 4),
                                  follower_count INT DEFAULT 0,
                                  holdings_count INT DEFAULT 0,
                                  last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  CONSTRAINT pk_portfoliometrics PRIMARY KEY (portfolio_id, date),
                                  CONSTRAINT fk_portfolio FOREIGN KEY (portfolio_id) REFERENCES Portfolio (portfolio_id) ON DELETE CASCADE
);

-- 3. PositionMetrics Table
CREATE TABLE PositionMetrics (
                                 portfolio_id INT NOT NULL,
                                 stock_id INT NOT NULL,
                                 date TIMESTAMP NOT NULL,
                                 quantity INT NOT NULL,
                                 average_cost DECIMAL(10, 2) NOT NULL,
                                 current_value DECIMAL(15, 2) NOT NULL,
                                 total_return DECIMAL(10, 4),
                                 position_weight DECIMAL(10, 4),
                                 last_transaction_type VARCHAR(10),
                                 last_transaction_date TIMESTAMP,
                                 last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT pk_positionmetrics PRIMARY KEY (portfolio_id, stock_id, date),
                                 CONSTRAINT fk_portfolio FOREIGN KEY (portfolio_id) REFERENCES Portfolio (portfolio_id) ON DELETE CASCADE,
                                 CONSTRAINT fk_stock FOREIGN KEY (stock_id) REFERENCES Stock (stock_id) ON DELETE CASCADE
);

-- 4. TradeMetrics Table (No partitioning needed)
CREATE TABLE TradeMetrics (
                              trade_id SERIAL PRIMARY KEY,
                              portfolio_id INT NOT NULL,
                              stock_id INT NOT NULL,
                              entry_date TIMESTAMP NOT NULL,
                              exit_date TIMESTAMP,
                              entry_price DECIMAL(10, 2) NOT NULL,
                              exit_price DECIMAL(10, 2),
                              quantity INT NOT NULL,
                              total_return DECIMAL(10, 4),
                              is_best_trade BOOLEAN DEFAULT FALSE,
                              is_worst_trade BOOLEAN DEFAULT FALSE,
                              last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT fk_portfolio FOREIGN KEY (portfolio_id) REFERENCES Portfolio (portfolio_id) ON DELETE CASCADE,
                              CONSTRAINT fk_stock FOREIGN KEY (stock_id) REFERENCES Stock (stock_id) ON DELETE CASCADE
);

-- 5. Partitioning - Create Hypertables
SELECT create_hypertable('StockMetrics', 'date', chunk_time_interval => INTERVAL '1 month');
SELECT create_hypertable('PortfolioMetrics', 'date', chunk_time_interval => INTERVAL '1 month');
SELECT create_hypertable('PositionMetrics', 'date', chunk_time_interval => INTERVAL '1 month');

-- 6. Compression Policy
ALTER TABLE StockMetrics SET (timescaledb.compress, timescaledb.compress_segmentby = 'stock_id');
SELECT add_compression_policy('StockMetrics', INTERVAL '7 days');

ALTER TABLE PortfolioMetrics SET (timescaledb.compress, timescaledb.compress_segmentby = 'portfolio_id');
SELECT add_compression_policy('PortfolioMetrics', INTERVAL '7 days');

ALTER TABLE PositionMetrics SET (timescaledb.compress, timescaledb.compress_segmentby = 'portfolio_id, stock_id');
SELECT add_compression_policy('PositionMetrics', INTERVAL '7 days');

-- 7. Retention Policy
SELECT add_retention_policy('StockMetrics', INTERVAL '2 years');
SELECT add_retention_policy('PortfolioMetrics', INTERVAL '2 years');
SELECT add_retention_policy('PositionMetrics', INTERVAL '2 years');

-- 8. Optimized Indexes for Queries
CREATE INDEX idx_stockmetrics_latest ON StockMetrics (stock_id, date DESC);
CREATE INDEX idx_portfoliometrics_latest ON PortfolioMetrics (portfolio_id, date DESC);
CREATE INDEX idx_positionmetrics_latest ON PositionMetrics (portfolio_id, stock_id, date DESC);
CREATE INDEX idx_trademetrics_portfolio ON TradeMetrics (portfolio_id, is_best_trade, is_worst_trade);

-- 9. Materialized View for Latest Metrics
CREATE MATERIALIZED VIEW latest_portfolio_view AS
WITH latest_metrics AS (
    SELECT DISTINCT ON (portfolio_id) portfolio_id, date, metric_name, total_value, monthly_return, yearly_return
    FROM PortfolioMetrics
    ORDER BY portfolio_id, date DESC
),
     latest_positions AS (
         SELECT DISTINCT ON (portfolio_id, stock_id) portfolio_id, stock_id, quantity, current_value, total_return
         FROM PositionMetrics
         ORDER BY portfolio_id, stock_id, date DESC
     )
SELECT m.*, p.stock_id, p.quantity, p.current_value, p.total_return
FROM latest_metrics m
         LEFT JOIN latest_positions p USING (portfolio_id);

-- 10. Index on Materialized View
CREATE UNIQUE INDEX idx_latest_portfolio_view ON latest_portfolio_view (portfolio_id, stock_id);

-- 11. Refresh Function for Materialized View
CREATE OR REPLACE FUNCTION refresh_latest_portfolio_view()
    RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY latest_portfolio_view;
END;
$$ LANGUAGE plpgsql;
