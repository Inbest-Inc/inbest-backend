ALTER TABLE TradeMetrics
DROP COLUMN entry_date;

ALTER TABLE TradeMetrics
RENAME COLUMN entry_price TO average_cost;