# spring-boot-prometheus-integration-demo
This is a demo of important integration points of prometheus, Grafana, Spring boot along with Acturator and Micrometer


# 🚀 Prometheus Setup with Spring Boot (Local Development)

This guide explains how to install, configure, and integrate **Prometheus** with a **Spring Boot application** for metrics monitoring.

---

## 📌 What is Prometheus?

Prometheus is an open-source monitoring and alerting system that collects metrics from applications and stores them as time-series data.

---

## 🏗️ Architecture Overview

```
Spring Boot App  --->  Prometheus Server  --->  Grafana (optional)
        |                      |
   /actuator/prometheus   Scrapes metrics
```

---

## ⚙️ Prerequisites

* macOS
* Homebrew installed
* Java + Spring Boot application
* (Optional) Docker

---

# 🍏 Install Prometheus on macOS

## Step 1: Install via Homebrew

```bash
brew install prometheus
```

---

## Step 2: Verify Installation

```bash
prometheus --version
```

---

## Step 3: Locate Config File

```bash
brew info prometheus
```

Typical locations:

* Apple Silicon → `/opt/homebrew/etc/prometheus.yml`
* Intel → `/usr/local/etc/prometheus.yml`

---

# 🛠️ Configure Prometheus

Edit the config file:

```bash
nano /opt/homebrew/etc/prometheus.yml
```

### Sample Configuration

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  # Prometheus self-monitoring
  - job_name: "prometheus"
    static_configs:
      - targets: ["localhost:9090"]

  # Spring Boot App
  - job_name: "spring-boot"
    metrics_path: "/actuator/prometheus"
    static_configs:
      - targets: ["localhost:8080"]
```

---

# ▶️ Start Prometheus

```bash
prometheus --config.file=/opt/homebrew/etc/prometheus.yml
```

---

# 🌐 Access Prometheus UI

Open in browser:

```
http://localhost:9090
```

Check targets:

```
http://localhost:9090/targets
```

---

# 🌱 Spring Boot Integration

## 📦 Dependencies (Maven)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

---

## ⚙️ application.yml

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

## 🔍 Verify Metrics Endpoint

```
http://localhost:8080/actuator/prometheus
```

---

# 📊 Custom Metrics Example

## Counter Example

```java
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final Counter orderCounter;

    public OrderService(MeterRegistry registry) {
        this.orderCounter = Counter.builder("orders_created_total")
                .description("Total number of orders created")
                .register(registry);
    }

    public void createOrder() {
        orderCounter.increment();
    }
}
```

---

## ⏱️ Timer Example

```java
Timer timer = Timer.builder("order_processing_time")
        .description("Time taken to process orders")
        .register(registry);

timer.record(() -> {
    // business logic
});
```

---

# 🐳 Run Prometheus using Docker (Alternative)

## Step 1: Create Config File

```bash
mkdir prometheus-demo
cd prometheus-demo
nano prometheus.yml
```

Paste config:

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: "spring-boot"
    metrics_path: "/actuator/prometheus"
    static_configs:
      - targets: ["host.docker.internal:8080"]
```

---

## Step 2: Run Container

```bash
docker run -d \
  -p 9090:9090 \
  -v $(pwd)/prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus
```

---

# 📈 PromQL Examples

```sql
# Requests per second
rate(http_server_requests_seconds_count[1m])

# Average latency
rate(http_server_requests_seconds_sum[1m]) 
/
rate(http_server_requests_seconds_count[1m])
```

---

# 📊 (Optional) Setup Grafana

```bash
brew install grafana
brew services start grafana
```

Open:

```
http://localhost:3000
```

Add Prometheus as data source:

```
http://localhost:9090
```

---

# ⚠️ Common Issues

| Issue                       | Cause                | Fix                   |
| --------------------------- | -------------------- | --------------------- |
| Prometheus not starting     | YAML syntax error    | Validate indentation  |
| Target DOWN                 | App not running      | Start Spring Boot app |
| 404 on /actuator/prometheus | Actuator not enabled | Check config          |
| Duplicate scrape_configs    | Config mistake       | Merge into one block  |

---

# 🧠 Key Concepts

* Prometheus uses **pull model**
* Metrics exposed via `/actuator/prometheus`
* Uses **Micrometer** in Spring Boot
* Requires separate server/container

---

# 🎯 Next Steps

* Add alerts using Alertmanager
* Create dashboards in Grafana
* Integrate with Datadog / Dynatrace

---


# 🌱 Spring Boot Integration with Prometheus

This section explains how to expose application metrics from a Spring Boot application using **Micrometer** and make them available for **Prometheus scraping**.

---

## 🧠 How It Works

```text
Spring Boot App → Micrometer → MeterRegistry → /actuator/prometheus → Prometheus
```

* **Micrometer** → Metrics abstraction layer
* **MeterRegistry** → Stores metrics inside the app
* **Actuator** → Exposes metrics endpoint
* **Prometheus** → Scrapes metrics

---

## 📦 Step 1: Add Dependencies

### Maven

```xml
<dependencies>
    <!-- Actuator for exposing endpoints -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- Prometheus registry -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>
</dependencies>
```

### Gradle

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'io.micrometer:micrometer-registry-prometheus'
}
```

---

## ⚙️ Step 2: Configure Actuator

### `application.yml`

```yaml
management:
  endpoints:
    web:
      exposure:
        include: prometheus, health, info
  endpoint:
    prometheus:
      enabled: true
  metrics:
    tags:
      application: my-spring-app
```

---

## 🌐 Step 3: Verify Metrics Endpoint

Start your application and open:

```
http://localhost:8080/actuator/prometheus
```

Expected output:

```
# HELP jvm_memory_used_bytes The amount of used memory
jvm_memory_used_bytes{area="heap"} 1.23456E7
```

---

## 📊 Step 4: Create Custom Metrics

### ✅ Counter Example

```java
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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

## 📈 Step 5: Built-in Metrics (Auto Available)

Spring Boot automatically provides:

* `http_server_requests_seconds` → API latency
* `jvm_memory_used_bytes` → JVM memory
* `system_cpu_usage` → CPU usage
* `jvm_gc_pause_seconds` → GC pauses

---

## 🧪 Step 6: Validate in Prometheus

After configuring Prometheus:

* Open: `http://localhost:9090`
* Go to **Graph**
* Try query:

```sql
rate(http_server_requests_seconds_count[1m])
```

---

## ⚠️ Best Practices

### ✅ Reuse Metrics (Important)

```java
private final Counter counter;
```

❌ Avoid:

```java
Counter.builder(...).register(...); // inside method
```

---

### ⚠️ Avoid High Cardinality

❌ Bad:

```java
.tag("userId", userId)
```

👉 Causes memory and performance issues

---

### 🔐 Secure Actuator

```yaml
management:
  server:
    port: 8081
```

---

## 🧠 Key Concepts

* **Micrometer** → abstraction layer
* **MeterRegistry** → metric storage inside app
* **Prometheus** → pulls metrics
* **Tags** → dimensions for filtering

---

## 🎯 Summary

* Add dependencies
* Enable `/actuator/prometheus`
* Create custom metrics using `MeterRegistry`
* Configure Prometheus to scrape metrics

---


## 📚 References

* https://prometheus.io/docs/
* https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html

---

## ✅ Author

Sajal Chakraborty

