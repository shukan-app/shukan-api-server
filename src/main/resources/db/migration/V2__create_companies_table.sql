CREATE TABLE contact_types
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL
);

CREATE TABLE application_routes
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL
);

CREATE TABLE company_status
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL
);

CREATE TABLE companies
(
    id                   UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id              UUID        NOT NULL REFERENCES users (id),
    name                 TEXT        NOT NULL,
    applied_role         TEXT        NOT NULL,
    status_id            UUID        NOT NULL REFERENCES company_status (id),
    application_route_id UUID        NOT NULL REFERENCES application_routes (id),
    contact_type_id      UUID        NOT NULL REFERENCES contact_types (id),
    email_url            TEXT        NOT NULL,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at           TIMESTAMPTZ          DEFAULT NULL
);