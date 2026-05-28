CREATE TABLE recruiting_platforms
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL
);

CREATE TABLE scout_status
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL
);

CREATE TABLE scouts
(
    id                     UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id                UUID        NOT NULL REFERENCES users (id),
    title                  TEXT        NOT NULL,
    company_name           TEXT        NOT NULL,
    recruiting_platform_id UUID        NOT NULL REFERENCES recruiting_platforms (id),
    details_url            TEXT,
    status_id              UUID        NOT NULL REFERENCES scout_status (id),
    created_at             TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at             TIMESTAMPTZ          DEFAULT NULL
);