-- V1.1.0__api_tool_schema.sql
-- Schema for API Tool entities

-- API Tool Templates table (specialization of Tool Templates)
CREATE TABLE api_tool_templates (
                                    id UUID PRIMARY KEY REFERENCES tool_templates(id) ON DELETE CASCADE,
                                    endpoint VARCHAR(1000) NOT NULL,
                                    http_method VARCHAR(10) NOT NULL,
                                    headers JSONB,
                                    query_params JSONB,
                                    request_body JSONB,
                                    content_type VARCHAR(100),
                                    timeout INTEGER,
                                    follow_redirects BOOLEAN DEFAULT TRUE
);

-- API Execution Results table (specialization of Execution Results)
CREATE TABLE api_execution_results (
                                       id UUID PRIMARY KEY REFERENCES execution_results(id) ON DELETE CASCADE,
                                       status_code INTEGER,
                                       response_headers JSONB,
                                       response_body JSONB,
                                       response_time_ms BIGINT,
                                       successful BOOLEAN
);

-- Create indexes for better query performance
CREATE INDEX idx_api_tool_templates_endpoint ON api_tool_templates(endpoint);
CREATE INDEX idx_api_tool_templates_http_method ON api_tool_templates(http_method);
CREATE INDEX idx_api_execution_results_status_code ON api_execution_results(status_code);
CREATE INDEX idx_api_execution_results_successful ON api_execution_results(successful);