-- Add soft delete and owner support

-- Add delete_yn to trips
ALTER TABLE trips ADD COLUMN delete_yn CHAR(1) NOT NULL DEFAULT 'N';
CREATE INDEX idx_trips_delete_yn ON trips(delete_yn);

-- Add is_owner, delete_yn, phone, email to participants
ALTER TABLE participants ADD COLUMN is_owner BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE participants ADD COLUMN delete_yn CHAR(1) NOT NULL DEFAULT 'N';
ALTER TABLE participants ADD COLUMN phone VARCHAR(20) NULL;
ALTER TABLE participants ADD COLUMN email VARCHAR(100) NULL;
CREATE INDEX idx_participants_delete_yn ON participants(delete_yn);
CREATE INDEX idx_participants_is_owner ON participants(is_owner);

-- Add delete_yn to expenses
ALTER TABLE expenses ADD COLUMN delete_yn CHAR(1) NOT NULL DEFAULT 'N';
CREATE INDEX idx_expenses_delete_yn ON expenses(delete_yn);

-- Add delete_yn to expense_payments
ALTER TABLE expense_payments ADD COLUMN delete_yn CHAR(1) NOT NULL DEFAULT 'N';
CREATE INDEX idx_expense_payments_delete_yn ON expense_payments(delete_yn);

-- Add delete_yn to expense_shares
ALTER TABLE expense_shares ADD COLUMN delete_yn CHAR(1) NOT NULL DEFAULT 'N';
CREATE INDEX idx_expense_shares_delete_yn ON expense_shares(delete_yn);
