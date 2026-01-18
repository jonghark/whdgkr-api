-- Make friend_id nullable for auto-registered friends
-- Auto-registered friends from trip participants won't have a loginId initially
ALTER TABLE friends MODIFY COLUMN friend_id VARCHAR(50) NULL;

-- Remove the unique constraint on owner+friendId since friendId can be null
-- Note: MySQL doesn't support partial indexes, so we'll handle uniqueness in application code
ALTER TABLE friends DROP INDEX uk_friends_owner_friend_id;
