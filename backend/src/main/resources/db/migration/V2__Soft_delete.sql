-- V2: Soft delete for containers
ALTER TABLE containers ADD COLUMN deleted_at TIMESTAMP;
