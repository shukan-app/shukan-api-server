CREATE TABLE task_creation_sources
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL
);

CREATE TABLE task_status
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL
);

CREATE TABLE task_types
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL
);

CREATE TABLE tasks
(
    id                 UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id            UUID        NOT NULL REFERENCES users (id),
    title              TEXT        NOT NULL,
    company_id         UUID        NOT NULL REFERENCES companies (id),
    type_id            UUID        NOT NULL REFERENCES task_types (id),
    status_id          UUID        NOT NULL REFERENCES task_status (id),
    creation_source_id UUID        NOT NULL REFERENCES task_creation_sources (id),
    email_url          TEXT        NOT NULL,
    deadline           TIMESTAMPTZ          DEFAULT NULL,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at         TIMESTAMPTZ          DEFAULT NULL
);