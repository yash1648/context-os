# Backup Strategy

## Backup Scope

| Component | Backup Method | Frequency | Retention | RPO | RTO |
|---|---|---|---|---|---|
| PostgreSQL | pg_dump + WAL archiving | Daily + Continuous | 30 days | 5 min | 1 hour |
| Redis | RDB snapshots | Every 6 hours | 7 days | 6 hours | 30 min |
| Application Config | Git + Docker volumes | On change | Perpetual | — | — |
| User Uploads | Volume snapshots | Daily | 30 days | 1 day | 2 hours |
| Vector Embeddings | Part of PostgreSQL backup | Daily | 30 days | 5 min | 1 hour |

## PostgreSQL Backup

```bash
#!/bin/bash
# scripts/backup-db.sh
set -euo pipefail

BACKUP_DIR="/backups/postgres"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="contextos"
DB_USER="contextos"
RETENTION_DAYS=30

mkdir -p "$BACKUP_DIR"

echo "📦 Starting PostgreSQL backup..."

# Full database dump (excluding vector embeddings initially for speed, then include)
pg_dump -U "$DB_USER" -d "$DB_NAME" \
  --format=custom \
  --compress=9 \
  --file="$BACKUP_DIR/${DB_NAME}_${DATE}.dump" \
  --verbose \
  --no-owner \
  --no-privileges

# Create plain SQL backup (for easy inspection)
pg_dump -U "$DB_USER" -d "$DB_NAME" \
  --format=plain \
  --compress=9 \
  --file="$BACKUP_DIR/${DB_NAME}_${DATE}.sql.gz" \
  --data-only \
  --exclude-table=containers_embedding \
  --verbose

echo "✅ Backup completed: $BACKUP_DIR/${DB_NAME}_${DATE}.dump"

# Archive old backups
find "$BACKUP_DIR" -name "*.dump" -mtime +$RETENTION_DAYS -delete
find "$BACKUP_DIR" -name "*.sql.gz" -mtime +$RETENTION_DAYS -delete

echo "🧹 Cleaned up backups older than $RETENTION_DAYS days"

# Upload to remote storage (S3-compatible)
if [ -n "${S3_BACKUP_BUCKET:-}" ]; then
  echo "☁️ Uploading to S3..."
  aws s3 cp "$BACKUP_DIR/${DB_NAME}_${DATE}.dump" \
    "s3://$S3_BACKUP_BUCKET/postgres/${DB_NAME}_${DATE}.dump" \
    --storage-class STANDARD_IA
  echo "✅ Uploaded to S3"
fi
```

## Database Restore

```bash
#!/bin/bash
# scripts/restore-db.sh
set -euo pipefail

BACKUP_FILE=${1:-}
if [ -z "$BACKUP_FILE" ]; then
  echo "Usage: $0 <backup-file.dump>"
  echo "Available backups:"
  ls -lh /backups/postgres/*.dump
  exit 1
fi

echo "⏪ Restoring PostgreSQL from: $BACKUP_FILE"

# Stop application
docker compose stop api

# Drop and recreate database
docker compose exec -T postgres psql -U contextos -c "DROP DATABASE IF EXISTS contextos;"
docker compose exec -T postgres psql -U contextos -c "CREATE DATABASE contextos;"

# Restore from dump
pg_restore -U contextos -d contextos \
  --clean \
  --if-exists \
  --no-owner \
  --verbose \
  "$BACKUP_FILE"

echo "✅ Restore complete"

# Start application
docker compose start api

echo "🚀 Application restarted"
```

## Point-in-Time Recovery

```sql
-- PostgreSQL WAL archiving setup
-- archive_mode = on
-- archive_command = 'cp %p /backups/postgres/wal/%f'

-- Recovery to specific point in time
-- Create recovery.signal in data directory
-- Add to postgresql.conf:
-- restore_command = 'cp /backups/postgres/wal/%f %p'
-- recovery_target_time = '2026-03-15 14:30:00 UTC'
```

## Redis Backup

```bash
#!/bin/bash
# scripts/backup-redis.sh
set -euo pipefail

BACKUP_DIR="/backups/redis"
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p "$BACKUP_DIR"

# Trigger RDB save
redis-cli SAVE

# Copy the RDB file
cp /data/dump.rdb "$BACKUP_DIR/redis_$DATE.rdb"

# Compress
gzip "$BACKUP_DIR/redis_$DATE.rdb"

# Cleanup old backups (7 days retention)
find "$BACKUP_DIR" -name "*.rdb.gz" -mtime +7 -delete

echo "✅ Redis backup completed"
```

## Backup Automation

```yaml
# docker-compose.backup.yml
services:
  backup:
    image: alpine:latest
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
      - ./backups:/backups
      - postgres-data:/var/lib/postgresql/data:ro
    entrypoint: |
      /bin/sh -c '
      apk add --no-cache postgresql-client aws-cli
      
      # Run backup script daily
      while true; do
        /scripts/backup-db.sh
        sleep 86400
      done
      '
    environment:
      - S3_BACKUP_BUCKET=${S3_BACKUP_BUCKET:-}
      - AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID:-}
      - AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY:-}
```

## Backup Verification

```bash
#!/bin/bash
# scripts/verify-backup.sh
set -euo pipefail

echo "🔍 Verifying latest backup..."

LATEST_BACKUP=$(ls -t /backups/postgres/*.dump | head -1)

if [ -z "$LATEST_BACKUP" ]; then
  echo "❌ No backup found"
  exit 1
fi

echo "Testing backup: $LATEST_BACKUP"

# Restore to temporary database
createdb contextos_verify
pg_restore -d contextos_verify --no-owner --verbose "$LATEST_BACKUP"

# Run basic verification queries
psql -d contextos_verify -c "SELECT COUNT(*) as container_count FROM containers;"
psql -d contextos_verify -c "SELECT COUNT(*) as user_count FROM users;"
psql -d contextos_verify -c "SELECT COUNT(*) as tag_count FROM tags;"

# Cleanup
dropdb contextos_verify

echo "✅ Backup verification complete"
```

## Disaster Recovery Plan

```yaml
Disaster Recovery Plan:

  Level 1 - Service Restart:
    Issue: API unresponsive
    Action: docker compose restart api
    RTO: 30 seconds

  Level 2 - Full Restart:
    Issue: Multiple services down
    Action: docker compose down && docker compose up -d
    RTO: 2 minutes

  Level 3 - Database Recovery:
    Issue: Data corruption
    Action: Restore from latest backup
    RTO: 1 hour
    RPO: 5 minutes (WAL)

  Level 4 - Complete Recovery:
    Issue: Server failure
    Action: Provision new server, restore from S3 backup
    RTO: 4 hours
    RPO: 24 hours (S3 backup)

  Level 5 - Disaster:
    Issue: Region failure
    Action: Deploy to secondary region, restore from cross-region backup
    RTO: 8 hours
    RPO: 1 hour (cross-region replication)
```

## Backup Schedule (Cron)

```cron
# Database backup - daily at 2 AM
0 2 * * * /scripts/backup-db.sh

# Redis backup - every 6 hours
0 */6 * * * /scripts/backup-redis.sh

# Backup verification - weekly on Sunday
0 3 * * 0 /scripts/verify-backup.sh

# S3 sync - after each backup
0 2 * * * /scripts/sync-to-s3.sh

# Cleanup temp files - daily
0 4 * * * find /tmp/backups* -mtime +1 -delete
```
