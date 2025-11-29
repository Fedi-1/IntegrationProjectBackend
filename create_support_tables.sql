-- SQL Script to Create Support System Tables
-- Run this on your Render.com PostgreSQL database if tables are not auto-created

-- Create support_tickets table
CREATE TABLE IF NOT EXISTS support_tickets (
    id BIGSERIAL PRIMARY KEY,
    subject VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ticket_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

-- Create support_messages table
CREATE TABLE IF NOT EXISTS support_messages (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    is_admin_reply BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_ticket FOREIGN KEY (ticket_id) REFERENCES support_tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_ticket_created_by ON support_tickets(created_by);
CREATE INDEX IF NOT EXISTS idx_ticket_status ON support_tickets(status);
CREATE INDEX IF NOT EXISTS idx_ticket_priority ON support_tickets(priority);
CREATE INDEX IF NOT EXISTS idx_ticket_updated_at ON support_tickets(updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_message_ticket ON support_messages(ticket_id);
CREATE INDEX IF NOT EXISTS idx_message_sender ON support_messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_message_created_at ON support_messages(created_at);

-- Verify tables were created
SELECT 'support_tickets' as table_name, COUNT(*) as record_count FROM support_tickets
UNION ALL
SELECT 'support_messages' as table_name, COUNT(*) as record_count FROM support_messages;
