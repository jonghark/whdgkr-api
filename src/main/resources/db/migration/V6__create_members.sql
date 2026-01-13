CREATE TABLE members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    login_id VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    delete_yn CHAR(1) NOT NULL DEFAULT 'N',
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_members_login_id ON members(login_id);
CREATE INDEX idx_members_email ON members(email);
