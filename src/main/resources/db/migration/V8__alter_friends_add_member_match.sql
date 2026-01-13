ALTER TABLE friends
    ADD COLUMN owner_member_id BIGINT AFTER id,
    ADD COLUMN friend_member_id BIGINT,
    ADD COLUMN matched_yn CHAR(1) NOT NULL DEFAULT 'N',
    ADD COLUMN matched_at DATETIME(3),
    ADD COLUMN delete_yn CHAR(1) NOT NULL DEFAULT 'N';

CREATE INDEX idx_friends_owner_member_id ON friends(owner_member_id);
CREATE INDEX idx_friends_friend_member_id ON friends(friend_member_id);
