-- V2: Soft delete and progress tracking for containers
ALTER TABLE containers ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE containers ADD COLUMN progress INTEGER;
