-- Add currency column to expenses table
ALTER TABLE expenses ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'KRW';

-- Add unique constraint to phone column in friends table
ALTER TABLE friends ADD UNIQUE INDEX idx_friends_phone (phone);
