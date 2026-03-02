-- Clean up existing transaction data that might cause issues
-- Remove any transactions with invalid or too long transaction types

DELETE FROM transactions WHERE transaction_type IS NULL OR LENGTH(transaction_type) > 10;

-- Set default values for any problematic transactions
UPDATE transactions SET transaction_type = 'SEND' WHERE transaction_type IS NULL OR transaction_type = '';
