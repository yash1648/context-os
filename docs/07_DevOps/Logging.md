# Logging Strategy

## Architecture

```
┌────────────┐    ┌────────────┐    ┌────────────┐    ┌────────────┐
│ Application│    │  Filebeat  │    │Elasticsearch│    │  Kibana    │
│  Logs      │───▶│  (Agent)   │───▶│  (Storage)  │───▶│  (UI)      │
└────────────┘    └────────────┘    └────────────┘    └────────────┘
                         │
                         ▼
                    ┌────────────┐
                    │  Log files │
                    │  (backup)  │
                    └────────────┘
```

## Log Levels

| Level | Usage | Examples |
|---|---|---|
| ERROR | System is broken or data is corrupt | DB connection failed, enrichment failed |
| WARN | Something unexpected but system recovers | Rate limit hit, retry attempt |
| INFO | Normal operations tracking | Container created, user logged in |
| DEBUG | Development troubleshooting | SQL queries, request details |
| TRACE | Deep debugging (never in production) | Method entry/exit |

## Log Configuration

```yaml
# application.yml
logging:
  level:
    com.contextos: DEBUG
    org.springframework: INFO
    org.springframework.security: WARN
    org.hibernate.SQL: WARN
    org.hibernate.type.descriptor.sql: WARN
    
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    
  file:
    name: /var/log/contextos/api.log
    max-size: 100MB
    max-history: 30
    total-size-cap: 2GB
```

## Structured Logging (JSON)

```java
@Configuration
public class LoggingConfig {

    @Bean
    public LogstashEncoder logstashEncoder() {
        LogstashEncoder encoder = new LogstashEncoder();
        encoder.setIncludeContext(true);
        encoder.setIncludeCallerInfo(false);
        encoder.setCustomFields("{\"service\":\"contextos-api\",\"environment\":\"${ENV:dev}\"}");
        return encoder;
    }

    @Bean
    public LoggerContext loggerContext() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.setPackagingDataEnabled(false);
        return context;
    }
}
```

```xml
<!-- logback-spring.xml -->
<configuration>
    <springProperty name="ENV" source="spring.profiles.active" defaultValue="dev"/>
    
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeContext>false</includeContext>
            <customFields>{"service":"contextos-api","environment":"${ENV}"}</customFields>
            <fieldNames>
                <timestamp>@timestamp</timestamp>
                <message>message</message>
                <thread>thread</thread>
                <logger>logger</logger>
                <level>level</level>
            </fieldNames>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>/var/log/contextos/api.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>/var/log/contextos/api.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxHistory>30</maxHistory>
            <totalSizeCap>2GB</totalSizeCap>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>100MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
        </rollingPolicy>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeContext>false</includeContext>
            <customFields>{"service":"contextos-api","environment":"${ENV}"}</customFields>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
    
    <logger name="com.contextos" level="DEBUG"/>
    <logger name="org.springframework.security" level="WARN"/>
    <logger name="org.hibernate.SQL" level="WARN"/>
</configuration>
```

## Log Correlation

```java
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }
}
```

## Audit Logging

```java
@Aspect
@Component
public class AuditLogger {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    @AfterReturning("@annotation(auditable)")
    public void logAuditEvent(JoinPoint joinPoint, Auditable auditable) {
        String action = auditable.action();
        String resource = auditable.resource();
        Object[] args = joinPoint.getArgs();

        AuditEvent event = AuditEvent.builder()
            .action(action)
            .resource(resource)
            .userId(SecurityUtil.getCurrentUserId())
            .timestamp(Instant.now())
            .details(buildDetails(args))
            .build();

        auditLog.info("AUDIT: action={}, resource={}, user={}, details={}",
            event.action(), event.resource(), event.userId(), event.details());
    }
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String action();
    String resource();
}
```

## Log Retention

```yaml
Log Retention Policy:
  application_logs:
    retention: 30 days
    storage: Local + Elasticsearch
    compression: gzip after 7 days
    max_size: 2GB per service
    
  audit_logs:
    retention: 1 year
    storage: PostgreSQL (audit_logs table)
    access: Admin only
    
  access_logs:
    retention: 90 days
    storage: Nginx logs → Elasticsearch
    
  error_logs:
    retention: 1 year
    storage: Elasticsearch + Sentry
```

## Kibana Dashboards

### Dashboard 1: Error Analysis
```
Panels:
  - Error rate over time
  - Top 10 error messages
  - Errors by service
  - Errors by endpoint
  - Correlation IDs for recent errors
```

### Dashboard 2: User Activity
```
Panels:
  - Active users over time
  - Top 10 actions
  - User registration trend
  - Container creation rate
  - Search query frequency
```

### Dashboard 3: Performance
```
Panels:
  - Slowest endpoints (p95)
  - Database slow queries
  - Cache miss rates
  - API response time distribution
  - AI enrichment duration
```
