-- Refactored schema for executions using inheritance

-- Rename existing execution_results table to executions
ALTER TABLE execution_results RENAME TO executions;

-- Add execution_type column to executions table
ALTER TABLE executions ADD COLUMN execution_type VARCHAR(20);

-- Update existing records to have DEFAULT execution type
UPDATE executions SET execution_type = 'DEFAULT';

-- Create API executions table
CREATE TABLE api_executions (
                                execution_id UUID PRIMARY KEY REFERENCES executions(id) ON DELETE CASCADE,
                                status_code INTEGER,
                                response_headers JSONB,
                                response_body JSONB,
                                response_time_ms BIGINT,
                                successful BOOLEAN
);

-- Create indexes for improved query performance
CREATE INDEX idx_executions_template_id ON executions(template_id);
CREATE INDEX idx_executions_user_id ON executions(user_id);
CREATE INDEX idx_executions_status ON executions(status);
CREATE INDEX idx_executions_start_time ON executions(start_time);
CREATE INDEX idx_executions_end_time ON executions(end_time);
CREATE INDEX idx_executions_execution_type ON executions(execution_type);
CREATE INDEX idx_api_executions_status_code ON api_executions(status_code);
CREATE INDEX idx_api_executions_successful ON api_executions(successful);
CREATE INDEX idx_api_executions_response_time_ms ON api_executions(response_time_ms);