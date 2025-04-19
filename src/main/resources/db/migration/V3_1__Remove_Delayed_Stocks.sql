DELETE FROM stock
WHERE ticker_symbol IN ('ANSS', 'AIZ', 'AZO', 'BKNG', 'FICO', 'JKHY', 'NVR', 'STE', 'TPL', 'TYL');

INSERT INTO stock (ticker_symbol, stock_name, current_price) VALUES
                                                                 ('GC=F', 'Gold Jun 25', 0),
                                                                 ('SPY', 'SPDR S&P 500 ETF', 0);
