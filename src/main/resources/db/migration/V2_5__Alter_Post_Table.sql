ALTER TABLE Posts DROP COLUMN title;

ALTER TABLE Posts ADD COLUMN investment_activity_id INT;

ALTER TABLE Posts 
    ADD CONSTRAINT fk_investment_activity 
    FOREIGN KEY (investment_activity_id) 
    REFERENCES InvestmentActivity(activity_id) 
    ON DELETE CASCADE;

ALTER TABLE Posts ALTER COLUMN investment_activity_id SET NOT NULL;

CREATE INDEX idx_investment_activity ON Posts(investment_activity_id);

ALTER TABLE Posts ALTER COLUMN content SET NOT NULL;

ALTER TABLE Posts ALTER COLUMN created_at SET NOT NULL; 