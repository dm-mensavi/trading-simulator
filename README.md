# Trading Simulator

A full-stack, cloud-native paper-trading platform built with a microservices architecture. Users can register, browse live-like market data, place buy/sell orders, and track their portfolio — all without risking real money.

![CI Pipeline](https://github.com/dm-mensavi/trading-simulator/actions/workflows/ci.yml/badge.svg)

---

## Features

- **JWT Authentication** — Secure register/login flow with Spring Security
- **Market Data** — Real-time-like stock quotes fetched and cached via Redis
- **Order Matching** — Buy/sell orders processed through RabbitMQ message queues
- **Portfolio Tracking** — Per-user holdings and transaction history
- **API Gateway** — Single entry point routing to all backend services
- **Docker Compose** — One-command local setup for the full stack
- **Kubernetes** — Production-ready manifests for cluster deployment

---

## Architecture

```
                         +------------------------------+
                         |       Angular Frontend        |
                         |         (port 4200)           |
                         +---------------+--------------+
                                         | HTTP
                         +---------------v--------------+
                         |          API Gateway          |
                         |     Spring Cloud Gateway      |
                         |          (port 8000)          |
                         +------+----------+-------------+
                                |          |          |
               +----------------v--+   +---v------+  +v--------------+
               |   User Service    |   |  Market  |  | Order Service |
               |   (port 8080)     |   |  Service |  |  (port 8082)  |
               |  Spring Boot +    |   | (port    |  | Spring Boot + |
               |  PostgreSQL +     |   |  8081)   |  | PostgreSQL +  |
               |  Spring Security  |   | Redis    |  | RabbitMQ      |
               +-------------------+   +----------+  +---------------+
                                              |
                    +--------------------------+------------------+
                    |                          |                  |
               +----v------+          +--------v--+     +--------v--+
               | PostgreSQL|          |   Redis   |     | RabbitMQ  |
               |  (5433)   |          |  (6379)   |     |  (5672)   |
               +-----------+          +-----------+     +-----------+
```

---

## Services

| Service | Tech Stack | Port | Responsibility |
|---|---|---|---|
| user-service | Spring Boot 3, JPA, PostgreSQL, Spring Security, JWT, RabbitMQ | 8080 | Registration, login, JWT issuance, portfolio management |
| market-service | Spring Boot 3, Redis Cache, Jackson | 8081 | Fetches and caches market/stock price data |
| order-service | Spring Boot 3, JPA, PostgreSQL, RabbitMQ | 8082 | Order placement, matching, and event publishing |
| api-gateway | Spring Cloud Gateway 2023.0.2 | 8000 | Reverse proxy and routing for all microservices |
| frontend | Angular 21, TypeScript, Nginx | 4200 / 80 | SPA with dashboard, trading, portfolio, and profile views |

---

## Tech Stack

### Backend
- Java 21 + Spring Boot 3.3
- Spring Cloud Gateway (API routing)
- Spring Security + JJWT 0.12 (authentication)
- Spring Data JPA + PostgreSQL (persistence)
- Spring Data Redis (caching)
- Spring AMQP + RabbitMQ (async messaging)
- Lombok + Jackson (utilities)

### Frontend
- Angular 21 (standalone components)
- TypeScript
- Nginx (production serving)

### Infrastructure
- Docker + Docker Compose (local development)
- Kubernetes (production deployment)
- GitHub Actions (CI/CD)

---

## Quick Start (Docker Compose)

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) with Docker Compose v2+

### Run

```bash
git clone https://github.com/dm-mensavi/trading-simulator.git
cd trading-simulator

docker compose up --build
```

> First build takes a few minutes. Subsequent starts are much faster.

### Access

| Service | URL |
|---|---|
| Frontend | http://localhost:4200 |
| API Gateway | http://localhost:8000 |
| User Service | http://localhost:8080 |
| Market Service | http://localhost:8081 |
| Order Service | http://localhost:8082 |
| RabbitMQ Management UI | http://localhost:15672 (guest / guest) |
| PostgreSQL | localhost:5433 (admin / password) |
| Grafana | http://localhost:3000 (admin / admin) |
| Prometheus | http://localhost:9090 |
| Loki | http://localhost:3100 |

---

## Frontend Pages

| Route | Access | Description |
|---|---|---|
| `/login` | Public | Sign in with credentials |
| `/register` | Public | Create a new account |
| `/dashboard` | Protected | Market overview and quick stats |
| `/trade` | Protected | Place buy/sell orders |
| `/portfolio` | Protected | View holdings and P&L |
| `/profile` | Protected | Account settings |

---

## Kubernetes Deployment

Full Kubernetes manifests are available in the [`k8s/`](./k8s/) directory.

```powershell
# From the project root (Docker Desktop Kubernetes or minikube):
.\k8s\deploy.ps1
```

See [`k8s/README.md`](./k8s/README.md) for the complete guide including prerequisites, access URLs, and teardown steps.

---

## CI/CD

GitHub Actions pipeline (`.github/workflows/ci.yml`) runs on every push and pull request to `main`:

1. Unit tests for all 4 backend services (Java 21 + Maven)
2. Angular frontend tests (Node 20, headless Chrome)
3. Docker Compose build validation (runs only after all tests pass)

---

## Observability (PLG Stack)

The stack ships with a fully provisioned observability setup based on **Prometheus**, **Loki**, and **Grafana**.

### Components

| Component | Role | Port |
|---|---|---|
| Prometheus | Scrapes `/actuator/prometheus` on all 4 backend services every 15 s | 9090 |
| Loki | Aggregates container logs collected by Promtail | 3100 |
| Promtail | Tails Docker container stdout/stderr and ships to Loki | — |
| Grafana | Unified UI for metrics and logs — dashboards auto-provisioned on first start | 3000 |

### Pre-built Grafana Dashboards

Two dashboards are automatically loaded when Grafana starts:

- **Spring Boot JVM Metrics** — HTTP throughput, p95 response times, JVM heap, CPU, thread count, HikariCP connection pool, and error rates, filterable per service.
- **Trading Simulator Logs** — Log volume chart and live log stream from all containers via Loki, filterable by service name.

### Adding custom metrics

Use Micrometer's `MeterRegistry` directly in any service:

```java
@Service
public class OrderService {

    private final Counter ordersPlaced;

    public OrderService(MeterRegistry registry) {
        this.ordersPlaced = Counter.builder("trading.orders.placed")
            .description("Total orders placed")
            .tag("type", "buy")
            .register(registry);
    }

    public void placeOrder(Order order) {
        ordersPlaced.increment();
        // ...
    }
}
```

The new metric appears in Prometheus and is immediately queryable in Grafana.

---

## Project Structure

```
trading-simulator/
├── .github/
│   └── workflows/
│       └── ci.yml              # GitHub Actions CI pipeline
├── backend/
│   ├── api-gateway/            # Spring Cloud Gateway
│   ├── market-service/         # Market data + Redis caching
│   ├── order-service/          # Order matching + RabbitMQ
│   └── user-service/           # Auth + portfolio (JWT, JPA)
├── frontend/                   # Angular 21 SPA
│   ├── src/app/
│   │   ├── components/         # dashboard, trade, portfolio, profile, login, register
│   │   ├── guards/             # auth route guard
│   │   ├── interceptors/       # HTTP auth token interceptor
│   │   ├── models/             # TypeScript interfaces
│   │   └── services/           # auth, market, order, user services
│   ├── Dockerfile
│   └── nginx.conf
├── observability/              # PLG stack configuration
│   ├── prometheus/
│   │   └── prometheus.yml      # Scrape targets for all services
│   ├── promtail/
│   │   └── promtail.yml        # Docker log collection config
│   └── grafana/
│       └── provisioning/
│           ├── datasources/    # Auto-registers Prometheus + Loki
│           └── dashboards/     # JVM metrics + log explorer dashboards
├── k8s/                        # Kubernetes manifests
│   ├── deploy.ps1              # Ordered deployment script
│   └── README.md               # K8s setup guide
└── docker-compose.yml          # Full local stack including PLG
```

---

## Environment Variables

All sensitive values are set via Docker Compose environment blocks or Kubernetes Secrets. For local development, the defaults in `docker-compose.yml` work out of the box.

| Variable | Service | Description |
|---|---|---|
| `JWT_SECRET` | user-service | HS256 signing key (min 32 chars) |
| `JWT_EXPIRATION_MS` | user-service | Token lifetime in ms (default: 86400000 = 24 h) |
| `SPRING_DATASOURCE_URL` | user-service, order-service | JDBC connection URL |
| `SPRING_DATA_REDIS_HOST` | user-service, market-service | Redis hostname |
| `SPRING_RABBITMQ_HOST` | user-service, order-service | RabbitMQ hostname |

> **Never commit real secrets.** Rotate all credentials before any production deployment.

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -m 'feat: add my feature'`
4. Push to the branch: `git push origin feature/my-feature`
5. Open a Pull Request

---

## License

This project is open source and available under the [MIT License](LICENSE).
