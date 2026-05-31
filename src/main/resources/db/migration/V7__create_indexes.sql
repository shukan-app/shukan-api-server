CREATE INDEX idx_companies_user_active ON companies (user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_tasks_user_active ON tasks (user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_events_user_active ON events (user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_scouts_user_active ON scouts (user_id) WHERE deleted_at IS NULL;

CREATE INDEX idx_tasks_company_id ON tasks (company_id);
CREATE INDEX idx_events_company_id ON events (company_id);

CREATE INDEX idx_tasks_deadline ON tasks (deadline) WHERE deleted_at IS NULL;
CREATE INDEX idx_events_period ON events (begin_at, end_at) WHERE deleted_at IS NULL;

CREATE INDEX idx_companies_status_id ON companies (status_id);
CREATE INDEX idx_companies_application_route_id ON companies (application_route_id);
CREATE INDEX idx_companies_contact_type_id ON companies (contact_type_id);

CREATE INDEX idx_tasks_status_id ON tasks (status_id);
CREATE INDEX idx_tasks_type_id ON tasks (type_id);
CREATE INDEX idx_tasks_creation_source_id ON tasks (creator_kind_id);

CREATE INDEX idx_events_status_id ON events (status_id);
CREATE INDEX idx_events_type_id ON events (type_id);
CREATE INDEX idx_events_format_type_id ON events (format_type_id);

CREATE INDEX idx_scouts_status_id ON scouts (status_id);
CREATE INDEX idx_scouts_recruiting_platform_id ON scouts (recruiting_platform_id);
