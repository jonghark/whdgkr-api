-- Add user_id to participants table
ALTER TABLE participants ADD COLUMN user_id BIGINT NULL;

-- Add foreign key to members
ALTER TABLE participants ADD CONSTRAINT fk_participants_user
    FOREIGN KEY (user_id) REFERENCES members(id);

-- Add index
CREATE INDEX idx_participants_user_id ON participants(user_id);
