# Monitoring Strategy

## Architecture

```
┌───────────────────────────────────────────────────────────┐
│                    Monitoring Stack                        │
│                                                           │
│  ┌────────────┐   ┌────────────┐   ┌──────────────────┐  │
│  │ Prometheus  │   │  Grafana   │   │  Alertmanager    │  │
│  │ (Metrics)   │──▶│ (Dashboard)│──▶│  (Alerts)        │  │
│  └──────┬─────┘   └────────────┘   └────────┬─────────┘  │
│         │                                    │            │
│  ┌──────┴─────┐                    ┌─────────▼─────────┐ │
│  │ Node       │                    │  Slack / Email    │ │
│  │ Exporter   │                    │  / PagerDuty      │ │
│  └────────────┘                    └───────────────────┘ │
│                                                           │
│  ┌────────────┐   ┌────────────┐   ┌──────────────────┐  │
│  │ Spring     │   │  PostgreSQL│   │  RabbitMQ        │  │
│  │ Actuator   │   │  Exporter  │   │  Exporter        │  │
│  └────────────┘   └────────────┘   └──────────────────┘  │
└───────────────────────────────────────────────────────────┘
```

## Prometheus Configuration

```yaml
# docker/monitoring/prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  scrape_timeout: 10s

rule_files:
  - /etc/prometheus/rules/*.yml

alerting:
  alertmanagers:
    - static_configs:
        - targets: ['alertmanager:9093']

scrape_configs:
  - job_name: 'contextos-api'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s
    static_configs:
      - targets: ['api:8080']
        labels:
          service: 'contextos-api'
          environment: 'production'

  - job_name: 'postgres'
    static_configs:
      - targets: ['postgres-exporter:9187']
        labels:
          service: 'postgres'

  - job_name: 'redis'
    static_configs:
      - targets: ['redis-exporter:9121']
        labels:
          service: 'redis'

  - job_name: 'rabbitmq'
    static_configs:
      - targets: ['rabbitmq-exporter:9419']
        labels:
          service: 'rabbitmq'

  - job_name: 'node'
    static_configs:
      - targets: ['node-exporter:9100']
        labels:
          service: 'node'
```

## Alert Rules

```yaml
# docker/monitoring/rules/alerts.yml
groups:
  - name: contextos-alerts
    rules:
      # API Health
      - alert: APIDown
        expr: up{job="contextos-api"} == 0
        for: 1m
        annotations:
          summary: "API is down"
          description: "ContextOS API has been unreachable for 1 minute"

      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 5m
        annotations:
          summary: "High error rate ({{ $value | humanizePercentage }})"
          description: "API error rate exceeds 5% for 5 minutes"

      - alert: SlowResponses
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 1
        for: 5m
        annotations:
          summary: "Slow API responses (p95 > 1s)"
          description: "95th percentile response time exceeds 1 second"

      # Database
      - alert: PostgresDown
        expr: pg_up == 0
        for: 1m
        annotations:
          summary: "PostgreSQL is down"

      - alert: PostgresHighConnections
        expr: pg_stat_activity_count > 80
        for: 5m
        annotations:
          summary: "High database connections ({{ $value }})"

      - alert: PostgresSlowQueries
        expr: rate(pg_stat_activity_max_tx_duration[2m]) > 1
        for: 5m
        annotations:
          summary: "Slow database queries detected"

      # Redis
      - alert: RedisDown
        expr: redis_up == 0
        for: 1m
        annotations:
          summary: "Redis is down"

      - alert: RedisMemoryHigh
        expr: redis_memory_used_bytes / redis_memory_max_bytes > 0.8
        for: 5m
        annotations:
          summary: "Redis memory usage > 80%"

      # RabbitMQ
      - alert: RabbitMQDown
        expr: rabbitmq_up == 0
        for: 1m
        annotations:
          summary: "RabbitMQ is down"

      - alert: RabbitMQQueueDepth
        expr: rabbitmq_queue_messages > 1000
        for: 5m
        annotations:
          summary: "RabbitMQ queue depth > 1000"

      - alert: RabbitMQDeadLetter
        expr: rabbitmq_queue_messages{queue="dead.letter"} > 10
        for: 5m
        annotations:
          summary: "Dead letter queue has {{ $value }} messages"

      # AI / Ollama
      - alert: OllamaDown
        expr: ollama_up == 0
        for: 2m
        annotations:
          summary: "Ollama is unreachable"

      - alert: EnrichmentBacklog
        expr: ai_context_enrichment_pending_count > 100
        for: 30m
        annotations:
          summary: "AI enrichment backlog > 100"

      # System
      - alert: HighCPU
        expr: node_cpu_utilization > 0.8
        for: 10m
        annotations:
          summary: "CPU usage > 80%"

      - alert: HighMemory
        expr: node_memory_utilization > 0.85
        for: 10m
        annotations:
          summary: "Memory usage > 85%"

      - alert: DiskSpace
        expr: node_filesystem_avail_bytes{mountpoint="/"} / node_filesystem_size_bytes{mountpoint="/"} < 0.1
        for: 5m
        annotations:
          summary: "Disk space below 10%"
```

## Grafana Dashboards

### Dashboard 1: API Overview

```
Panels:
  - Request Rate (QPS)          - Time series
  - Error Rate (%)              - Time series  
  - Response Time (p50/p95/p99) - Time series
  - Active Users                - Gauge
  - Container Count by Type     - Bar chart
  - AI Enrichment Status        - Pie chart
  - Top Endpoints by Latency    - Table
  - HTTP Status Code Breakdown  - Time series
```

### Dashboard 2: Infrastructure

```
Panels:
  - CPU Usage (%)               - Time series per container
  - Memory Usage (MB)           - Time series per container
  - Disk I/O                    - Time series
  - Network I/O                 - Time series
  - PostgreSQL Connections      - Gauge
  - Redis Memory Usage          - Gauge
  - RabbitMQ Queue Depth        - Time series
  - Docker Container Status     - Table
```

### Dashboard 3: AI Performance

```
Panels:
  - Enrichment Processing Time  - Histogram
  - Embedding Generation Time   - Histogram
  - RAG Query Latency           - Time series
  - Enrichment Queue Depth      - Time series
  - Ollama Response Time        - Time series
  - Model Memory Usage          - Gauge
  - Enrichment Success Rate     - Time series
  - Embedding Quality Score     - Time series
```

## Metrics to Track

| Category | Metric | Type | Alert Threshold |
|---|---|---|---|
| API | Request rate (rps) | Counter | — |
| API | Error rate (%) | Gauge | > 5% |
| API | p95 latency (ms) | Histogram | > 1000ms |
| API | Active users | Gauge | — |
| DB | Active connections | Gauge | > 80 |
| DB | Query latency (ms) | Histogram | > 500ms |
| DB | Cache hit ratio | Gauge | < 90% |
| AI | Enrichment queue | Gauge | > 100 |
| AI | Embedding time (ms) | Histogram | > 2000ms |
| AI | RAG latency (ms) | Histogram | > 8000ms |
| Infra | CPU usage | Gauge | > 80% |
| Infra | Memory usage | Gauge | > 85% |
| Infra | Disk usage | Gauge | > 90% |
| Business | Container count | Gauge | — |
| Business | Active users | Gauge | — |
| Business | Search volume | Counter | — |

## Application Monitoring Configuration

```yaml
# application.yml - Actuator
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,env,loggers,threaddump
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true
  metrics:
    tags:
      application: ${spring.application.name}
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
      slo:
        http.server.requests: 10ms, 50ms, 100ms, 200ms, 500ms, 1s, 2s

info:
  app:
    name: ContextOS API
    version: @project.version@
    java-version: ${java.version}
```
