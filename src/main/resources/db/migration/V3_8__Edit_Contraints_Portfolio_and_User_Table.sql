ALTER TABLE portfoliometrics DROP CONSTRAINT fk_portfolio;

ALTER TABLE portfoliometrics
ADD CONSTRAINT fk_portfolio
FOREIGN KEY (portfolio_id)
REFERENCES portfolio(portfolio_id)
ON DELETE CASCADE;
