-- Add category to expenses table
ALTER TABLE expenses ADD COLUMN category VARCHAR(20) NOT NULL DEFAULT 'OTHER';

-- Add index
CREATE INDEX idx_expenses_category ON expenses(category);
