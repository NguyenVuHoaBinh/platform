-- Refactored schema for templates using inheritance

-- Rename existing tool_templates table to templates
ALTER TABLE tool_templates RENAME TO templates;

-- Create API templates table
CREATE TABLE api_templates (
                               template_id UUID PRIMARY KEY REFERENCES templates(id) ON DELETE CASCADE,
                               endpoint VARCHAR(1000) NOT NULL,
                               http_method VARCHAR(10) NOT NULL,
                               headers JSONB,
                               query_params JSONB,
                               request_body JSONB,
                               content_type VARCHAR(100),
                               timeout INTEGER,
                               follow_redirects BOOLEAN DEFAULT TRUE
);

-- Create indexes for improved query performance
CREATE INDEX idx_templates_name ON templates(name);
CREATE INDEX idx_templates_type ON templates(template_type);
CREATE INDEX idx_templates_created_by ON templates(created_by);
CREATE INDEX idx_templates_active ON templates(active);
CREATE INDEX idx_api_templates_http_method ON api_templates(http_method);
CREATE INDEX idx_api_templates_endpoint ON api_templates(endpoint);

-- Update template_tags foreign key
ALTER TABLE template_tags
DROP CONSTRAINT template_tags_template_id_fkey,
    ADD CONSTRAINT template_tags_template_id_fkey
    FOREIGN KEY (template_id) REFERENCES templates(id) ON DELETE CASCADE;

-- Update template_versions foreign key
ALTER TABLE template_versions
DROP CONSTRAINT template_versions_template_id_fkey,
    ADD CONSTRAINT template_versions_template_id_fkey
    FOREIGN KEY (template_id) REFERENCES templates(id) ON DELETE CASCADE;