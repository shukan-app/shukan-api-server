CREATE TABLE event_format_types
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL
);

CREATE TABLE event_status
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL
);

CREATE TABLE event_types
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL
);

CREATE TABLE events
(
    id             UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id        UUID        NOT NULL REFERENCES users (id),
    title          TEXT        NOT NULL,
    company_id     UUID REFERENCES companies (id),
    type_id        UUID        NOT NULL REFERENCES event_types (id),
    status_id      UUID        NOT NULL REFERENCES event_status (id),
    format_type_id UUID        NOT NULL REFERENCES event_format_types (id),
    location       TEXT,
    email_url      TEXT        NOT NULL,
    begin_at       TIMESTAMPTZ,
    end_at         TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at     TIMESTAMPTZ          DEFAULT NULL
);