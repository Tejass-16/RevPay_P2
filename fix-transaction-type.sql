-- Fix transaction_type column for RevPay
-- This script fixes the database schema issue where transaction_type column
-- was created with wrong size, causing "data truncated" errors

USE revpay3;

-- First, drop the problematic column if it exists
SET @sql_mode = '';
ALTER TABLE transactions DROP COLUMN IF EXISTS transaction_type;

-- Recreate the column with correct size for single character enum values
ALTER TABLE transactions ADD COLUMN transaction_type VARCHAR(1) NOT NULL DEFAULT 'S';

-- Set the column to allow NULL temporarily for existing records
ALTER TABLE transactions MODIFY COLUMN transaction_type VARCHAR(1) NULL;

-- Update existing records to have proper enum values
UPDATE transactions SET transaction_type = 'S' WHERE transaction_type IS NULL OR transaction_type = '';

-- Show the result
DESCRIBE transactions;

COMMIT;
