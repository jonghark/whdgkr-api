CREATE TABLE trips (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at DATETIME(3) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE participants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    trip_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    created_at DATETIME(3) NOT NULL,
    INDEX idx_trip_id (trip_id),
    CONSTRAINT fk_participants_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE expenses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    trip_id BIGINT NOT NULL,
    title VARCHAR(120) NOT NULL,
    occurred_at DATETIME(3) NOT NULL,
    total_amount INT NOT NULL,
    created_at DATETIME(3) NOT NULL,
    INDEX idx_trip_id (trip_id),
    INDEX idx_occurred_at (occurred_at),
    CONSTRAINT fk_expenses_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE expense_payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    expense_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    amount INT NOT NULL,
    INDEX idx_expense_id (expense_id),
    INDEX idx_participant_id (participant_id),
    CONSTRAINT fk_payments_expense FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE,
    CONSTRAINT fk_payments_participant FOREIGN KEY (participant_id) REFERENCES participants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE expense_shares (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    expense_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    amount INT NOT NULL,
    INDEX idx_expense_id (expense_id),
    INDEX idx_participant_id (participant_id),
    CONSTRAINT fk_shares_expense FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE,
    CONSTRAINT fk_shares_participant FOREIGN KEY (participant_id) REFERENCES participants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
