# 📈 Trading Simulator

A full-stack, cloud-native **paper-trading platform** built with a microservices architecture. Users can register, browse live-like market data, place buy/sell orders, and track their portfolio — all without risking real money.

![CI Pipeline](https://github.com/dm-mensavi/trading-simulator/actions/workflows/ci.yml/badge.svg)

---

## ✨ Features

- 🔐 **JWT Authentication** — Secure register/login flow with Spring Security
- 📊 **Market Data** — Real-time-like stock quotes fetched and cached via Redis
- 🛒 **Order Matching** — Buy/sell orders processed through RabbitMQ message queues
- 💼 **Portfolio Tracking** — Per-user holdings and transaction history
- 🌐 **API Gateway** — Single entry point routing to all backend services
- 🐳 **Docker Compose** — One-command local setup for the full stack
- ☸️ **Kubernetes** — Production-ready manifests for cluster deployment

---

## 🏗️ Architecture

```
                         ┌──────────────────────────────┐
                         │        Angular Frontend       │
                         │         (port 4200)           │
                         └──────────────┬───────────────┘
                                        │ HTTP
                         ┌──────────────▼───────────────┐
                         │         API Gateway           │
                         │    Spring Cloud Gateway       │
                         │         (port 8000)           │
                         └──────┬─────────┬─────────────┘
                                │         │         │
               ┌────────────────▼─┐   ┌───▼──────┐  ┌▼──────────────┐
               │   User Service   │   │  Market  │  │ Order Service │
               │   (port 8080)    │   │  Service │  │  (port 8082)  │
               │  Spring Boot +   │   │ (port    │  │ Spring Boot + │
               │  PostgreSQL +    │   │  8081)   │  │ PostgreSQL +  │
               │  Spring Security │   │ Redis    │  │ RabbitMQ      │
               └──────────────────┘   └──────────┘  └───────────────┘
                                            │
                    ┌───────────────────────┼──────────────────┐
                    │                       │                  │
               ┌────▼──────┐        ┌───────▼───┐     ┌───────▼───┐
               │ PostgreSQL│        │   Redis   │     │ RabbitMQ  │
               │  (5433)   │        │  (6379)   │     │  (5672)   │
               └───────────┘        └───────────┘     └───────────┘
```

---

## 🧩 Services

| Service | Tech Stack | Port | Responsibility |
|---|---|---|---|
| **user-service** | Spring Boot 3, JPA, PostgreSQL, Spring Security, JWT, RabbitMQ | 8080 | Registration, login, JWT issuance, portfolio management |
| **market-service** | Spring Boot 3, Redis Cache, Jackson | 8081 | Fetches & caches market/stock price data |
| **order-service** | Spring Boot 3, JPA, PostgreSQL, RabbitMQ | 8082 | Order placement, matching, and event publishing |
| **api-gateway** | Spring Cloud Gateway 2023.0.2 | 8000 | Reverse proxy & routing for all microservices |
| **frontend** | Angular 21, TypeScript, Nginx | 4200 / 80 | SPA with dashboard, trading, portfolio & profile views |

---

## 🛠️ Tech Stack

### Backend
- **Java 21** + **Spring Boot 3.3**
- **Spring Cloud Gateway** (API routing)
- **Spring Security** + **JJWT 0.12** (authentication)
- **Spring Data JPA** + **PostgreSQL** (persistence)
- **Spring Data Redis** (caching)
- **Spring AMQP** + **RabbitMQ** (async messaging)
- **Lombok** + **Jackson** (utilities)

### Frontend
- **Angular 21** (standalone components)
- **TypeScript**
- **Nginx** (production serving)

### Infrastructure
- **Docker** + **Docker Compose** (local dev)
- **Kubernetes** (production)
- **GitHub Actions** (CI/CD)

---

## 🚀 Quick Start (Docker Compose)

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
| RabbitMQ UI | http://localhost:15672 (guest / guest) |
| PostgreSQL | localhost:5433 (admin / password) |

---

## 🖥️ Frontend Pages

| Route | Access | Description |
|---|---|---|
| `/login` | Public | Sign in with credentials |
| `/register` | Public | Create a new account |
| `/dashboard` | Protected | Market overview & quick stats |
| `/trade` | Protected | Place buy/sell orders |
| `/portfolio` | Protected | View holdings & P&L |
| `/profile` | Protected | Account settings |

---

## ☸️ Kubernetes Deployment

Full Kubernetes manifests are available in the [`k8s/`](./k8s/) directory.

```powershell
# From the project root (Docker Desktop Kubernetes or minikube):
.\k8s\deploy.ps1
```

See [`k8s/README.md`](./k8s/README.md) for the complete guide including prerequisites, access URLs, and teardown steps.

---

## 🔄 CI/CD

GitHub Actions pipeline (`.github/workflows/ci.yml`) runs on every push/PR to `main`:

1. ✅ Unit tests for all 4 backend services (Java 21 + Maven)
2. ✅ Angular frontend tests (Node 20, headless Chrome)
3. 🐳 Docker Compose build validation (only after all tests pass)

---

## 📁 Project Structure

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
├── k8s/                        # Kubernetes manifests
│   ├── deploy.ps1              # Ordered deployment script
│   └── README.md               # K8s setup guide
└── docker-compose.yml          # Full local stack
```

---

## 🔐 Environment Variables

All sensitive values are set via Docker Compose environment blocks or Kubernetes Secrets. For local dev, the defaults in `docker-compose.yml` work out of the box.

| Variable | Service | Description |
|---|---|---|
| `JWT_SECRET` | user-service | HS256 signing key (min 32 chars) |
| `JWT_EXPIRATION_MS` | user-service | Token lifetime in ms (default: 86400000 = 24 h) |
| `SPRING_DATASOURCE_URL` | user/order-service | JDBC connection URL |
| `SPRING_DATA_REDIS_HOST` | user/market-service | Redis hostname |
| `SPRING_RABBITMQ_HOST` | user/order-service | RabbitMQ hostname |

> ⚠️ **Never commit real secrets.** Rotate all credentials before any production deployment.

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -m 'feat: add my feature'`
4. Push to the branch: `git push origin feature/my-feature`
5. Open a Pull Request

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

*Built with ☕ Java & ⚡ Angular*
