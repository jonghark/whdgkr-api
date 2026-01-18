-- Add friend_id to participants table
-- To track which friend was used to create this participant
ALTER TABLE participants ADD COLUMN friend_id BIGINT NULL;

-- Add foreign key
ALTER TABLE participants ADD CONSTRAINT fk_participants_friend
    FOREIGN KEY (friend_id) REFERENCES friends(id);

-- Add index
CREATE INDEX idx_participants_friend_id ON participants(friend_id);
