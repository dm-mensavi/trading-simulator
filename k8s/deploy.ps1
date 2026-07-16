# ── Trading Simulator — Kubernetes Deployment Guide ───────────────
# Prerequisites: kubectl + Docker Desktop (Kubernetes enabled) OR minikube

# ── Option A: Docker Desktop (simplest — images already built locally)
# Enable Kubernetes in Docker Desktop → Settings → Kubernetes → Enable Kubernetes

# ── Option B: minikube
# minikube start --memory=4096 --cpus=2
# minikube docker-env | Invoke-Expression        # Point shell to minikube Docker
# Then rebuild images inside minikube's Docker daemon:
# docker compose build

# ─────────────────────────────────────────────────────────────────
# STEP 1: Apply namespace and shared config first
# ─────────────────────────────────────────────────────────────────
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/configmap.yaml

# ─────────────────────────────────────────────────────────────────
# STEP 2: Deploy infrastructure (Postgres, RabbitMQ, Redis)
# ─────────────────────────────────────────────────────────────────
kubectl apply -f k8s/postgres/
kubectl apply -f k8s/rabbitmq/
kubectl apply -f k8s/redis/

# Wait for infrastructure to be ready before starting services
Write-Host "Waiting for infrastructure pods to be ready..."
kubectl wait --for=condition=ready pod -l app=postgres -n trading-sim --timeout=120s
kubectl wait --for=condition=ready pod -l app=rabbitmq -n trading-sim --timeout=120s
kubectl wait --for=condition=ready pod -l app=redis    -n trading-sim --timeout=60s

# ─────────────────────────────────────────────────────────────────
# STEP 3: Deploy microservices
# ─────────────────────────────────────────────────────────────────
kubectl apply -f k8s/user-service/
kubectl apply -f k8s/market-service/
kubectl apply -f k8s/order-service/

Write-Host "Waiting for microservices to be ready (JVM cold start ~30-60s)..."
kubectl wait --for=condition=ready pod -l app=user-service   -n trading-sim --timeout=120s
kubectl wait --for=condition=ready pod -l app=market-service -n trading-sim --timeout=120s
kubectl wait --for=condition=ready pod -l app=order-service  -n trading-sim --timeout=120s

# ─────────────────────────────────────────────────────────────────
# STEP 4: Deploy API Gateway + Frontend
# ─────────────────────────────────────────────────────────────────
kubectl apply -f k8s/api-gateway/
kubectl apply -f k8s/frontend/

Write-Host "Waiting for gateway and frontend..."
kubectl wait --for=condition=ready pod -l app=api-gateway -n trading-sim --timeout=120s
kubectl wait --for=condition=ready pod -l app=frontend    -n trading-sim --timeout=60s

# ─────────────────────────────────────────────────────────────────
# STEP 5 (optional): Ingress — only if using a cloud provider or minikube addons enable ingress
# ─────────────────────────────────────────────────────────────────
# kubectl apply -f k8s/ingress.yaml

# ─────────────────────────────────────────────────────────────────
# Access URLs
# ─────────────────────────────────────────────────────────────────
Write-Host ""
Write-Host "========================================================"
Write-Host "  Frontend:   http://localhost:30080"
Write-Host "  API:        http://localhost:30000/api/..."
Write-Host "  RabbitMQ:   kubectl port-forward svc/rabbitmq 15672:15672 -n trading-sim"
Write-Host "========================================================"
