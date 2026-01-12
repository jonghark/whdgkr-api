-- 지출 정산완료 상태 필드 추가
ALTER TABLE expenses ADD COLUMN settled_yn CHAR(1) NOT NULL DEFAULT 'N';
ALTER TABLE expenses ADD COLUMN settled_at TIMESTAMP NULL;

-- 인덱스 추가 (정산 대상 조회 최적화)
CREATE INDEX idx_expenses_settled ON expenses(trip_id, settled_yn, delete_yn);
