# CI/CD Pipeline

## Pipeline Overview

```
Git Push → GitHub Actions → Build → Test → Package → Deploy
                                                 ↓
                                          Staging → Production
```

## GitHub Actions Workflows

### Main CI Pipeline

```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

jobs:
  # ============================================
  # Backend Checks
  # ============================================
  backend:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_DB: contextos_test
          POSTGRES_USER: test
          POSTGRES_PASSWORD: test
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

      redis:
        image: redis:7-alpine
        ports:
          - 6379:6379
        options: >-
          --health-cmd "redis-cli ping"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v4

      - name: Setup Java 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Lint and compile
        run: |
          ./mvnw compile -q

      - name: Run unit tests
        run: |
          ./mvnw test -q

      - name: Run integration tests
        env:
          DB_URL: jdbc:postgresql://localhost:5432/contextos_test
          DB_USERNAME: test
          DB_PASSWORD: test
          REDIS_HOST: localhost
        run: |
          ./mvnw verify -Pintegration-test -q

      - name: Check vulnerability
        run: |
          ./mvnw org.owasp:dependency-check-maven:check

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: backend-test-results
          path: backend/target/surefire-reports/

  # ============================================
  # Frontend Checks
  # ============================================
  frontend:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Setup Node 20
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: npm
          cache-dependency-path: frontend/package-lock.json

      - name: Install dependencies
        run: npm ci

      - name: Type check
        run: npm run typecheck

      - name: Lint
        run: npm run lint

      - name: Run unit tests
        run: npm run test -- --coverage

      - name: Build
        run: npm run build

      - name: Upload build artifacts
        uses: actions/upload-artifact@v4
        with:
          name: frontend-build
          path: frontend/dist/

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: frontend-test-results
          path: frontend/coverage/

  # ============================================
  # Docker Image Build
  # ============================================
  docker:
    needs: [backend, frontend]
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'

    steps:
      - uses: actions/checkout@v4

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Login to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}

      - name: Build and push API image
        uses: docker/build-push-action@v5
        with:
          context: ./backend
          push: true
          tags: |
            ${{ secrets.DOCKER_REGISTRY }}/contextos-api:latest
            ${{ secrets.DOCKER_REGISTRY }}/contextos-api:${{ github.sha }}
          cache-from: type=gha
          cache-to: type=gha,mode=max

      - name: Build and push Frontend image
        uses: docker/build-push-action@v5
        with:
          context: ./frontend
          push: true
          tags: |
            ${{ secrets.DOCKER_REGISTRY }}/contextos-frontend:latest
            ${{ secrets.DOCKER_REGISTRY }}/contextos-frontend:${{ github.sha }}
          cache-from: type=gha
          cache-to: type=gha,mode=max
```

### Deploy Pipeline

```yaml
# .github/workflows/deploy.yml
name: Deploy

on:
  workflow_run:
    workflows: ["CI"]
    types:
      - completed
    branches: [main]

jobs:
  deploy-staging:
    runs-on: ubuntu-latest
    if: ${{ github.event.workflow_run.conclusion == 'success' }}
    environment: staging

    steps:
      - uses: actions/checkout@v4

      - name: Deploy to Staging
        uses: appleboy/ssh-action@v1.0.3
        with:
          host: ${{ secrets.STAGING_HOST }}
          username: ${{ secrets.STAGING_USER }}
          key: ${{ secrets.STAGING_SSH_KEY }}
          script: |
            cd /opt/contextos
            docker compose pull
            docker compose up -d --remove-orphans
            docker system prune -f

      - name: Run smoke tests
        run: |
          sleep 30
          curl -f https://staging.contextos.dev/api/v1/health || exit 1
          curl -f https://staging.contextos.dev/ || exit 1

  deploy-production:
    needs: deploy-staging
    runs-on: ubuntu-latest
    environment: production
    if: github.ref == 'refs/heads/main'

    steps:
      - uses: actions/checkout@v4

      - name: Deploy to Production
        uses: appleboy/ssh-action@v1.0.3
        with:
          host: ${{ secrets.PROD_HOST }}
          username: ${{ secrets.PROD_USER }}
          key: ${{ secrets.PROD_SSH_KEY }}
          script: |
            cd /opt/contextos
            docker compose pull
            docker compose up -d --remove-orphans
            docker system prune -f

      - name: Health check
        run: |
          for i in 1 2 3 4 5; do
            if curl -sf https://contextos.app/api/v1/health; then
              echo "Deployment successful!"
              exit 0
            fi
            echo "Waiting for service... ($i/5)"
            sleep 15
          done
          echo "Deployment failed - health check timeout"
          exit 1

      - name: Notify success
        if: success()
        uses: slackapi/slack-github-action@v1.26.0
        with:
          payload: |
            {
              "text": "✅ ContextOS deployed to production successfully!\nVersion: ${{ github.sha }}\nDeployed by: ${{ github.actor }}"
            }
        env:
          SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK }}

      - name: Notify failure
        if: failure()
        uses: slackapi/slack-github-action@v1.26.0
        with:
          payload: |
            {
              "text": "❌ ContextOS production deployment FAILED!\nCommit: ${{ github.sha }}\nAuthor: ${{ github.actor }}"
            }
        env:
          SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK }}
```

### Dependency Update (Dependabot)

```yaml
# .github/dependabot.yml
version: 2
updates:
  - package-ecosystem: "maven"
    directory: "/backend"
    schedule:
      interval: "weekly"
      day: "monday"
      time: "09:00"
      timezone: "America/New_York"
    open-pull-requests-limit: 10
    labels:
      - "dependencies"
      - "backend"

  - package-ecosystem: "npm"
    directory: "/frontend"
    schedule:
      interval: "weekly"
      day: "monday"
      time: "09:00"
      timezone: "America/New_York"
    open-pull-requests-limit: 10
    labels:
      - "dependencies"
      - "frontend"

  - package-ecosystem: "docker"
    directory: "/"
    schedule:
      interval: "monthly"
    open-pull-requests-limit: 5
```

## Environment Configuration

```yaml
# .github/environments.yml
environments:
  development:
    url: http://localhost:5173
    deployment_branch: develop
    protection_rules: []
    
  staging:
    url: https://staging.contextos.dev
    deployment_branch: main
    protection_rules:
      - required_reviewers: 1
      - wait_timer: 0
    
  production:
    url: https://contextos.app
    deployment_branch: main
    protection_rules:
      - required_reviewers: 2
      - wait_timer: 10  # 10 minute delay for rollback chance
    secrets:
      - PROD_HOST
      - PROD_USER
      - PROD_SSH_KEY
      - JWT_SECRET
      - DB_PASSWORD
      - SLACK_WEBHOOK
```

## Deployment Script

```bash
#!/bin/bash
# scripts/deploy.sh
set -euo pipefail

ENV=${1:-staging}
VERSION=${2:-latest}

echo "🚀 Deploying ContextOS ($VERSION) to $ENV..."

# Pull latest images
docker compose -f docker-compose.yml -f "docker-compose.$ENV.yml" pull

# Start services
docker compose -f docker-compose.yml -f "docker-compose.$ENV.yml" up -d --remove-orphans

# Wait for health check
echo "⏳ Waiting for services..."
for i in {1..30}; do
  if curl -sf "http://localhost:8080/api/v1/health"; then
    echo "✅ API is healthy!"
    break
  fi
  sleep 2
done

# Run database migrations
echo "📦 Running migrations..."
docker compose exec -T api ./mvnw flyway:migrate -q

# Clean up old images
echo "🧹 Cleaning up..."
docker system prune -f --filter "until=24h"

echo "✅ Deployment complete!"
```

## Rollback Procedure

```bash
#!/bin/bash
# scripts/rollback.sh
set -euo pipefail

VERSION=${1:-previous}

echo "⏪ Rolling back to version: $VERSION"

# Stop current services
docker compose down

# Restore database backup if migration issues
if [ "${ROLLBACK_DB:-false}" = "true" ]; then
  echo "📦 Restoring database..."
  docker compose up -d postgres
  sleep 10
  gunzip -c "backups/contextos_$VERSION.sql.gz" | docker compose exec -T postgres psql -U contextos contextos
fi

# Start previous version
VERSION=$VERSION docker compose up -d

echo "✅ Rollback complete"
```
