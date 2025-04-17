-- Create UUID extension if not exists
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table (for authorization - simplified for Phase 1)
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       active BOOLEAN DEFAULT TRUE
);

-- Create enum types
CREATE TYPE template_type AS ENUM ('API', 'DATABASE', 'FILE', 'INTEGRATION', 'CUSTOM');
CREATE TYPE execution_status AS ENUM ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'TIMEOUT', 'CANCELLED');

-- Tool Templates table
CREATE TABLE tool_templates (
                                id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                name VARCHAR(100) NOT NULL,
                                description TEXT,
                                version VARCHAR(20) NOT NULL DEFAULT '1.0.0',
                                type template_type NOT NULL,
                                properties JSONB,
                                created_by UUID NOT NULL,
                                created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                last_modified_by UUID,
                                last_modified_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                active BOOLEAN DEFAULT TRUE
);

-- Template Tags table
CREATE TABLE template_tags (
                               template_id UUID NOT NULL REFERENCES tool_templates(id) ON DELETE CASCADE,
                               tag_name VARCHAR(50) NOT NULL,
                               PRIMARY KEY (template_id, tag_name)
);

-- Template Versions table
CREATE TABLE template_versions (
                                   id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                   template_id UUID NOT NULL REFERENCES tool_templates(id) ON DELETE CASCADE,
                                   version VARCHAR(20) NOT NULL,
                                   content JSONB NOT NULL,
                                   created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                   created_by UUID NOT NULL,
                                   UNIQUE (template_id, version)
);

-- Execution Results table
CREATE TABLE execution_results (
                                   id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                   template_id UUID NOT NULL REFERENCES tool_templates(id),
                                   user_id UUID NOT NULL,
                                   status execution_status NOT NULL DEFAULT 'PENDING',
                                   start_time TIMESTAMP WITH TIME ZONE,
                                   end_time TIMESTAMP WITH TIME ZONE,
                                   result JSONB,
                                   error_message TEXT,
                                   metrics JSONB,
                                   created_by UUID NOT NULL,
                                   created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                   last_modified_by UUID,
                                   last_modified_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                   active BOOLEAN DEFAULT TRUE
);

-- Create indexes for better query performance
CREATE INDEX idx_tool_templates_name ON tool_templates(name);
CREATE INDEX idx_tool_templates_type ON tool_templates(type);
CREATE INDEX idx_tool_templates_created_by ON tool_templates(created_by);
CREATE INDEX idx_execution_results_template_id ON execution_results(template_id);
CREATE INDEX idx_execution_results_user_id ON execution_results(user_id);
CREATE INDEX idx_execution_results_status ON execution_results(status);
CREATE INDEX idx_template_tags_template_id ON template_tags(template_id);
CREATE INDEX idx_template_tags_tag_name ON template_tags(tag_name);

-- Insert initial admin user (password: admin)
INSERT INTO users (id, username, email, password_hash, created_at, updated_at)
VALUES (
           '00000000-0000-0000-0000-000000000001',
           'admin',
           'admin@example.com',
           '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP
       );