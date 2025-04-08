ALTER TABLE investmentactivity
    ALTER COLUMN stock_quantity TYPE DECIMAL(10,2)
        USING stock_quantity::DECIMAL(10,2);

ALTER TABLE portfoliostock
    ALTER COLUMN quantity TYPE DECIMAL(10,2)
        USING quantity::DECIMAL(10,2);

ALTER TABLE trademetrics
    ALTER COLUMN quantity TYPE DECIMAL(10,2)
        USING quantity::DECIMAL(10,2);

DROP MATERIALIZED VIEW IF EXISTS latest_portfolio_view;

-- Step 1: Remove compression policy and disable compression
SELECT remove_compression_policy('PositionMetrics');
ALTER TABLE PositionMetrics SET (timescaledb.compress = false);

-- Step 2: Alter the column type
ALTER TABLE PositionMetrics
    ALTER COLUMN quantity TYPE DECIMAL(10,2)
        USING quantity::DECIMAL(10,2);

-- Step 3: Re-enable compression
ALTER TABLE PositionMetrics SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'portfolio_id, stock_id'
    );
SELECT add_compression_policy('PositionMetrics', INTERVAL '7 days');