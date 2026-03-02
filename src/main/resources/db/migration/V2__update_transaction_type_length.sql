-- Update transaction_type column to accommodate longer strings
-- This migration handles the transition from enum to string-based transaction types

ALTER TABLE transactions MODIFY COLUMN transaction_type VARCHAR(10) NOT NULL;

-- Update any existing enum values to string equivalents
UPDATE transactions SET transaction_type = 'SEND' WHERE transaction_type = 'S';
UPDATE transactions SET transaction_type = 'RECEIVE' WHERE transaction_type = 'R';
UPDATE transactions SET transaction_type = 'ADD_FUNDS' WHERE transaction_type = 'A';
UPDATE transactions SET transaction_type = 'WITHDRAW' WHERE transaction_type = 'W';
