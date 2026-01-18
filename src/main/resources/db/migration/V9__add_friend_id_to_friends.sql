-- 친구의 로그인 ID를 저장하는 컬럼 추가
ALTER TABLE friends
    ADD COLUMN friend_id VARCHAR(50) NOT NULL DEFAULT '' AFTER phone;

-- friend_id 인덱스 추가 (owner_id + friend_id 조합으로 조회 최적화)
CREATE INDEX idx_friends_owner_friend_id ON friends(owner_member_id, friend_id);

-- 동일한 owner가 같은 friend_id를 중복 등록하지 못하도록 유니크 제약 추가
ALTER TABLE friends
    ADD CONSTRAINT uk_friends_owner_friend_id UNIQUE (owner_member_id, friend_id, delete_yn);
