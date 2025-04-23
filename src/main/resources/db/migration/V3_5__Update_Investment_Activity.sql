ALTER TABLE InvestmentActivity
    ADD COLUMN old_position_weight DECIMAL(10, 4),
    ADD COLUMN new_position_weight DECIMAL(10, 4);

ALTER TABLE InvestmentActivity
    DROP COLUMN amount;