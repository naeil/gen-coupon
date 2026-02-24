INSERT IGNORE INTO config (config_key, config_value)
VALUES
('minimum_amount', '20000'),
('minimum_count', '10'),
('collect_period', 'now'),
('collect_time', '24h'),
('blocked_suppliers', '국민돈까스,[샘플발송]국민돈까스,내일그룹_하이프리,내일그룹_샘플');


INSERT IGNORE INTO shop (shop_code, shop_name)
VALUES
('A000', '직접입력');