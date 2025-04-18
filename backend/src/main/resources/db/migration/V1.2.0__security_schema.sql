-- V1.2.0__security_schema.sql
-- Schema for security-related entities (users, roles)

-- Roles table
CREATE TABLE roles (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       name VARCHAR(20) NOT NULL UNIQUE,
                       description VARCHAR(255)
);

-- Update users table to add additional fields
ALTER TABLE users
    ADD COLUMN full_name VARCHAR(100);

-- User-Role mapping table
CREATE TABLE user_roles (
                            user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role_id UUID NOT NULL REFERENCES roles(id),
                            PRIMARY KEY (user_id, role_id)
);

-- Create indexes
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- Insert default roles
INSERT INTO roles (id, name, description) VALUES
                                              (uuid_generate_v4(), 'ROLE_USER', 'Regular user with basic permissions'),
                                              (uuid_generate_v4(), 'ROLE_MANAGER', 'Manager with template management permissions'),
                                              (uuid_generate_v4(), 'ROLE_ADMIN', 'Administrator with full system access');

-- Update admin user with ADMIN role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.id = '00000000-0000-0000-0000-000000000001'
  AND r.name = 'ROLE_ADMIN';