-- SQL Schema for MySQL / PostgreSQL
CREATE TABLE IF NOT EXISTS transactions (
    tx_id VARCHAR(64) PRIMARY KEY,
    account_id VARCHAR(64) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    city VARCHAR(100),
    country VARCHAR(100),
    tx_type VARCHAR(10) NOT NULL,
    card_number VARCHAR(32),
    is_international BOOLEAN,
    vpa_id VARCHAR(100),
    status VARCHAR(20) NOT NULL
);
