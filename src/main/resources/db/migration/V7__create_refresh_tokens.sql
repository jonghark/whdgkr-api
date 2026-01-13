CREATE TABLE refresh_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at DATETIME(3) NOT NULL,
    revoked_yn CHAR(1) NOT NULL DEFAULT 'N',
    revoked_at DATETIME(3),
    user_agent VARCHAR(500),
    ip_address VARCHAR(50),
    created_at DATETIME(3) NOT NULL,
    CONSTRAINT fk_refresh_tokens_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_refresh_tokens_member_id ON refresh_tokens(member_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
