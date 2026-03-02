-- Fix transaction_type enum values to match new single-character enum
-- This script updates existing transactions to use the new enum values

UPDATE transactions 
SET transaction_type = CASE 
    WHEN transaction_type = 'SEND' THEN 'S'
    WHEN transaction_type = 'REQUEST' THEN 'R' 
    WHEN transaction_type = 'PAYMENT' THEN 'P'
    WHEN transaction_type = 'ADD_FUNDS' THEN 'A'
    WHEN transaction_type = 'WITHDRAWAL' THEN 'W'
    WHEN transaction_type = 'INITIAL_DEPOSIT' THEN 'I'
    ELSE transaction_type
END;

-- Also update status enum if needed
UPDATE transactions 
SET status = CASE 
    WHEN status = 'PENDING' THEN 'PENDING'
    WHEN status = 'COMPLETED' THEN 'COMPLETED'
    WHEN status = 'FAILED' THEN 'FAILED'
    ELSE status
END;

-- Commit the changes
COMMIT;
