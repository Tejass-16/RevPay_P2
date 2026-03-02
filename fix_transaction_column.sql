-- Fix transaction_type column length issue
ALTER TABLE transactions MODIFY COLUMN transaction_type VARCHAR(20);

-- Also fix the status column to be consistent
ALTER TABLE transactions MODIFY COLUMN status VARCHAR(20);
