CREATE TABLE users (
    id UUID PRIMARY KEY,
    employee_no VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    password_hash VARCHAR(120) NOT NULL,
    role VARCHAR(16) NOT NULL,
    invited_by_user_id UUID REFERENCES users(id),
    invite_quota_used INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE invite_codes (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    created_by_user_id UUID REFERENCES users(id),
    used_by_user_id UUID REFERENCES users(id),
    status VARCHAR(16) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE devices (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    imei1 VARCHAR(15) NOT NULL UNIQUE,
    imei2 VARCHAR(15),
    owner_user_id UUID NOT NULL REFERENCES users(id),
    current_holder_user_id UUID REFERENCES users(id),
    status VARCHAR(24) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE loan_records (
    id UUID PRIMARY KEY,
    device_id UUID NOT NULL REFERENCES devices(id),
    borrower_user_id UUID NOT NULL REFERENCES users(id),
    previous_holder_user_id UUID REFERENCES users(id),
    owner_user_id UUID NOT NULL REFERENCES users(id),
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP,
    status VARCHAR(16) NOT NULL
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    recipient_user_id UUID NOT NULL REFERENCES users(id),
    type VARCHAR(32) NOT NULL,
    title VARCHAR(120) NOT NULL,
    content VARCHAR(500) NOT NULL,
    related_device_id UUID REFERENCES devices(id),
    related_loan_record_id UUID REFERENCES loan_records(id),
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE auth_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    access_token VARCHAR(120) NOT NULL UNIQUE,
    refresh_token VARCHAR(120) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE app_events (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    session_id VARCHAR(120) NOT NULL,
    event_name VARCHAR(120) NOT NULL,
    screen VARCHAR(120) NOT NULL,
    action VARCHAR(120) NOT NULL,
    result VARCHAR(24) NOT NULL,
    severity VARCHAR(16) NOT NULL,
    context_json TEXT,
    app_version VARCHAR(40),
    device_model VARCHAR(120),
    os_version VARCHAR(80),
    created_at TIMESTAMP NOT NULL
);
