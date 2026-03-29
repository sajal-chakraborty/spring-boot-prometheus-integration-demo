# 🚀 End-to-End Observability: Spring Boot + Prometheus + Grafana

This guide walks through a **complete, production-style setup** of monitoring for a Spring Boot application using:

* **Spring Boot + Micrometer** → Metrics generation
* **Prometheus** → Metrics collection & storage
* **Grafana** → Visualization

---

# 🧠 Architecture Overview

```text
Spring Boot (Micrometer)
        ↓
/actuator/prometheus
        ↓
Prometheus (Pull Model)
        ↓
Grafana (Dashboards)
```

---

# ⚙️ Prerequisites

* macOS
* Java 17+
* Spring Boot project
* Homebrew installed

---

# 🟢 PHASE 1: Prometheus Setup

---

## Step 1: Install Prometheus

```bash
brew install prometheus
```

---

## Step 2: Configure Prometheus

Edit config:

```bash
nano /opt/homebrew/etc/prometheus.yml
```

### Configuration

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: "prometheus"
    static_configs:
      - targets: ["localhost:9090"]

  - job_name: "spring-boot"
    metrics_path: "/actuator/prometheus"
    static_configs:
      - targets: ["localhost:8080"]
```

---

## Step 3: Start Prometheus

```bash
prometheus --config.file=/opt/homebrew/etc/prometheus.yml
```

---

## Step 4: Verify

```
http://localhost:9090/targets
```

👉 Ensure:

* `prometheus` → UP
* `spring-boot` → UP

---

# 🌱 PHASE 2: Spring Boot Integration

---

## Step 1: Add Dependencies

### Gradle

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'io.micrometer:micrometer-registry-prometheus'
}
```

---

## Step 2: Configure Actuator

```yaml
management:
  endpoints:
    web:
      exposure:
        include: prometheus, health, info
  endpoint:
    prometheus:
      enabled: true
```

---

## Step 3: Verify Metrics Endpoint

```
http://localhost:8080/actuator/prometheus
```

---

## Step 4: Add Custom Metric (Best Practice)

```java
@RestController
public class OrderController {

    private final Counter orderCounter;

    public OrderController(MeterRegistry registry) {
        this.orderCounter = Counter.builder("orders_total")
                .description("Total orders processed")
                .tag("region", "india")
                .tag("service", "payment")
                .register(registry);
    }

    @GetMapping("/test")
    public String test() {
        orderCounter.increment();
        return "Order processed";
    }
}
```

---

## Step 5: Generate Traffic

Call API multiple times:

```
http://localhost:8080/test
```

---

## Step 6: Validate in Prometheus

Go to:

```
http://localhost:9090
```

Run query:

```sql
rate(http_server_requests_seconds_count[1m])
```

---

# 📊 PHASE 3: Grafana Setup

---

## Step 1: Install Grafana

```bash
brew install grafana
```

---

## Step 2: Start Grafana

```bash
brew services start grafana
```

---

## Step 3: Access UI

```
http://localhost:3000
```

Default:

```
admin / admin
```

---

## Step 4: Add Prometheus Data Source

* Go to **Settings → Data Sources**
* Select **Prometheus**

```
URL: http://localhost:9090
```

Click **Save & Test**

---

## Step 5: Create Dashboard

---

### Panel 1: Requests per second

```sql
rate(http_server_requests_seconds_count[1m])
```

---

### Panel 2: API Latency

```sql
rate(http_server_requests_seconds_sum[1m]) 
/
rate(http_server_requests_seconds_count[1m])
```

---

### Panel 3: Custom Metric

```sql
rate(orders_total[1m])
```

---

### Panel 4: JVM Memory

```sql
jvm_memory_used_bytes
```

---

# ⚠️ Common Issues & Fixes

| Issue              | Cause           | Fix               |
| ------------------ | --------------- | ----------------- |
| Target DOWN        | App not running | Start Spring Boot |
| No data in Grafana | No traffic      | Hit APIs          |
| YAML error         | Syntax issue    | Fix indentation   |
| 404 actuator       | Not exposed     | Check config      |

---

# 🧠 Key Concepts

* **Micrometer** → abstraction layer
* **MeterRegistry** → in-app metric store
* **Prometheus** → pull-based monitoring
* **Grafana** → visualization only

---

# 🎯 Final Flow

```text
API Call → Counter Increment
        ↓
MeterRegistry updated
        ↓
Prometheus scrapes
        ↓
Grafana visualizes
```

---

# 🚀 Next Steps

* Add Alertmanager
* Create SLA dashboards
* Add tracing (OpenTelemetry)
* Integrate with Datadog / Dynatrace

---

## 👨‍💻 Author

Sajal Chakraborty
