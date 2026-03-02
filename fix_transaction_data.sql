-- Fix transaction type data
UPDATE transactions SET transaction_type = 'SEND' WHERE transaction_type = 'S';
UPDATE transactions SET transaction_type = 'RECEIVE' WHERE transaction_type = 'R';
UPDATE transactions SET transaction_type = 'ADD_FUNDS' WHERE transaction_type = 'A';
UPDATE transactions SET transaction_type = 'WITHDRAW' WHERE transaction_type = 'W';

-- Clean up problematic data
DELETE FROM transactions WHERE transaction_type IS NULL OR LENGTH(transaction_type) > 10;

-- Set default for empty transaction types
UPDATE transactions SET transaction_type = 'SEND' WHERE transaction_type IS NULL OR transaction_type = '';
