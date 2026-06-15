# Docker Strategy

## Container Architecture

```
┌─────────────────────────────────────────────────┐
│                  Docker Host                      │
│                                                   │
│  ┌──────────┐  ┌──────────┐  ┌────────────────┐  │
│  │  Nginx    │  │  API     │  │  Frontend      │  │
│  │  :443     │──│  :8080   │  │  (static)      │  │
│  └──────────┘  └──────────┘  └────────────────┘  │
│       │                                            │
│  ┌────┴────┐  ┌──────────┐  ┌────────────────┐  │
│  │PostgreSQL│  │  Redis   │  │  RabbitMQ      │  │
│  │  :5432   │  │  :6379   │  │  :5672, :15672 │  │
│  └─────────┘  └──────────┘  └────────────────┘  │
│                                                   │
│  ┌────────────────────────────────────────────┐  │
│  │  Ollama (optional GPU passthrough)         │  │
│  │  :11434                                     │  │
│  └────────────────────────────────────────────┘  │
│                                                   │
│  ┌──────────┐  ┌──────────┐                      │
│  │Prometheus│  │ Grafana  │                      │
│  │  :9090   │  │  :3000   │                      │
│  └──────────┘  └──────────┘                      │
└─────────────────────────────────────────────────┘
```

## Docker Compose Configuration

```yaml
# docker-compose.yml
version: '3.8'

name: contextos

services:
  # ============================================
  # Reverse Proxy
  # ============================================
  nginx:
    image: nginx:1.25-alpine
    container_name: contextos-nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./docker/nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./docker/nginx/ssl:/etc/nginx/ssl:ro
      - ./frontend/dist:/usr/share/nginx/html:ro
    depends_on:
      - api
    networks:
      - contextos-net
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "nginx", "-t"]
      interval: 30s
      timeout: 10s
      retries: 3

  # ============================================
  # Backend API
  # ============================================
  api:
    build:
      context: ./backend
      dockerfile: Dockerfile
      args:
        - JAR_FILE=target/contextos-api-*.jar
    image: contextos/api:${VERSION:-latest}
    container_name: contextos-api
    environment:
      - SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=contextos
      - DB_USERNAME=${DB_USERNAME:-contextos}
      - DB_PASSWORD=${DB_PASSWORD:-contextos}
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - RABBITMQ_HOST=rabbitmq
      - RABBITMQ_PORT=5672
      - RABBITMQ_USERNAME=${RABBITMQ_USER:-guest}
      - RABBITMQ_PASSWORD=${RABBITMQ_PASS:-guest}
      - OLLAMA_URL=http://ollama:11434
      - JWT_SECRET=${JWT_SECRET}
      - API_URL=https://${DOMAIN:-localhost}
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_started
      rabbitmq:
        condition: service_healthy
    networks:
      - contextos-net
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 1G
        reservations:
          memory: 512M
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/v1/health"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 60s

  # ============================================
  # Frontend (build-time only, served by nginx)
  # ============================================
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
      target: production
    image: contextos/frontend:${VERSION:-latest}
    container_name: contextos-frontend
    volumes:
      - frontend-dist:/app/dist
    networks:
      - contextos-net

  # ============================================
  # PostgreSQL Database
  # ============================================
  postgres:
    image: postgres:16-alpine
    container_name: contextos-postgres
    environment:
      - POSTGRES_DB=contextos
      - POSTGRES_USER=${DB_USERNAME:-contextos}
      - POSTGRES_PASSWORD=${DB_PASSWORD:-contextos}
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./docker/postgres/init:/docker-entrypoint-initdb.d
    networks:
      - contextos-net
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 2G
        reservations:
          memory: 1G
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U contextos"]
      interval: 10s
      timeout: 5s
      retries: 5

  # ============================================
  # Redis Cache
  # ============================================
  redis:
    image: redis:7-alpine
    container_name: contextos-redis
    command: redis-server --appendonly yes --maxmemory 512mb --maxmemory-policy allkeys-lru
    volumes:
      - redis-data:/data
    networks:
      - contextos-net
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 256M
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 3

  # ============================================
  # RabbitMQ Message Broker
  # ============================================
  rabbitmq:
    image: rabbitmq:3-management-alpine
    container_name: contextos-rabbitmq
    environment:
      - RABBITMQ_DEFAULT_USER=${RABBITMQ_USER:-guest}
      - RABBITMQ_DEFAULT_PASS=${RABBITMQ_PASS:-guest}
      - RABBITMQ_DEFAULT_VHOST=/contextos
    volumes:
      - rabbitmq-data:/var/lib/rabbitmq
    networks:
      - contextos-net
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 512M
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "check_port_connectivity"]
      interval: 30s
      timeout: 10s
      retries: 5

  # ============================================
  # Ollama AI Service
  # ============================================
  ollama:
    image: ollama/ollama:latest
    container_name: contextos-ollama
    volumes:
      - ollama-data:/root/.ollama
      - ./docker/ollama/setup.sh:/setup.sh
    entrypoint: ["/bin/sh", "-c", "/setup.sh && ollama serve"]
    networks:
      - contextos-net
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 8G
        reservations:
          memory: 4G
    # Uncomment for GPU support:
    # deploy:
    #   resources:
    #     reservations:
    #       devices:
    #         - driver: nvidia
    #           count: 1
    #           capabilities: [gpu]

  # ============================================
  # Monitoring
  # ============================================
  prometheus:
    image: prom/prometheus:latest
    container_name: contextos-prometheus
    volumes:
      - ./docker/monitoring/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus-data:/prometheus
    networks:
      - contextos-net
    restart: unless-stopped
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--storage.tsdb.retention.time=30d'

  grafana:
    image: grafana/grafana:latest
    container_name: contextos-grafana
    environment:
      - GF_SECURITY_ADMIN_USER=${GRAFANA_USER:-admin}
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_PASS:-admin}
    volumes:
      - grafana-data:/var/lib/grafana
      - ./docker/monitoring/grafana/dashboards:/etc/grafana/provisioning/dashboards:ro
      - ./docker/monitoring/grafana/datasources:/etc/grafana/provisioning/datasources:ro
    networks:
      - contextos-net
    restart: unless-stopped

volumes:
  postgres-data:
  redis-data:
  rabbitmq-data:
  ollama-data:
  frontend-dist:
  prometheus-data:
  grafana-data:

networks:
  contextos-net:
    driver: bridge
```

## Dockerfile: Backend

```dockerfile
# backend/Dockerfile
# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -B
COPY src src
RUN ./mvnw package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine AS production
RUN addgroup -S contextos && adduser -S contextos -G contextos
USER contextos
WORKDIR /app
COPY --from=build /app/target/contextos-api-*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget -qO- http://localhost:8080/api/v1/health || exit 1
ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
```

## Dockerfile: Frontend

```dockerfile
# frontend/Dockerfile
# Build stage
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build

# Production stage
FROM nginx:1.25-alpine AS production
COPY --from=build /app/dist /usr/share/nginx/html
COPY docker/nginx/default.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost:80/health || exit 1
CMD ["nginx", "-g", "daemon off;"]
```

## .dockerignore

```dockerignore
# Backend
backend/.git
backend/target/
backend/.mvn/wrapper/
backend/*.md
backend/src/test/

# Frontend
frontend/node_modules/
frontend/dist/
frontend/.git
frontend/*.md
frontend/src/test/

# Common
.git
.gitignore
.env
*.md
docker-compose*.yml
```

## Makefile

```makefile
.PHONY: up down build logs clean

# Default environment
ENV ?= dev
VERSION ?= latest

# Start all services
up:
	docker compose -f docker-compose.yml -f docker-compose.${ENV}.yml up -d

# Stop all services
down:
	docker compose down

# Build images
build:
	docker compose build

# Rebuild and start
rebuild: build up

# View logs
logs:
	docker compose logs -f

# Database reset
db-reset:
	docker compose stop postgres
	docker compose rm -f postgres
	docker compose up -d postgres

# Run database migration
migrate:
	docker compose exec api ./mvnw flyway:migrate

# Clean everything
clean:
	docker compose down -v
	docker system prune -f

# Pull latest images
pull:
	docker compose pull

# Start with production profile
prod: ENV=prod
prod: up

# Backup database
backup:
	docker compose exec postgres pg_dump -U contextos contextos > backup_$(shell date +%Y%m%d_%H%M%S).sql

# Restore database
restore:
	cat $(file) | docker compose exec -T postgres psql -U contextos contextos
```
