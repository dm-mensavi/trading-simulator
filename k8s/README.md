# Trading Simulator — Kubernetes Deployment

## Directory Structure

```
k8s/
├── namespace.yaml          # trading-sim namespace
├── configmap.yaml          # Non-sensitive environment variables
├── secrets.yaml            # Credentials (DB, RabbitMQ, JWT, API key)
├── ingress.yaml            # Production ingress (optional — needs ingress controller)
├── deploy.ps1              # Ordered deployment script (PowerShell)
│
├── postgres/
│   ├── pvc.yaml            # 2Gi PersistentVolumeClaim
│   ├── statefulset.yaml    # postgres:15-alpine StatefulSet
│   └── service.yaml        # Headless ClusterIP Service
│
├── rabbitmq/
│   ├── pvc.yaml            # 1Gi PersistentVolumeClaim
│   ├── statefulset.yaml    # rabbitmq:3.13-management-alpine StatefulSet
│   └── service.yaml        # Headless ClusterIP (AMQP + Management ports)
│
├── redis/
│   └── redis.yaml          # redis:7-alpine Deployment + ClusterIP Service
│
├── user-service/
│   └── user-service.yaml   # Deployment + ClusterIP (port 8080)
│
├── market-service/
│   └── market-service.yaml # Deployment + ClusterIP (port 8081)
│
├── order-service/
│   └── order-service.yaml  # Deployment + ClusterIP (port 8082)
│
├── api-gateway/
│   └── api-gateway.yaml    # Deployment + NodePort 30000 (port 8000)
│
└── frontend/
    └── frontend.yaml       # Deployment + NodePort 30080 (Nginx port 80)
```

---

## Prerequisites

### Option A — Docker Desktop (recommended for local dev)
1. Open Docker Desktop → Settings → Kubernetes → ✅ Enable Kubernetes → Apply
2. Confirm: `kubectl cluster-info` should show `Kubernetes control plane is running`

### Option B — minikube
```powershell
winget install Kubernetes.minikube
minikube start --memory=4096 --cpus=2
# Point your shell to minikube's Docker (so it can find local images):
minikube docker-env | Invoke-Expression
# Then rebuild all images inside minikube's Docker daemon:
docker compose build
```

---

## Deploy

```powershell
# From the project root:
.\k8s\deploy.ps1
```

Or manually, step by step:

```powershell
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/postgres/
kubectl apply -f k8s/rabbitmq/
kubectl apply -f k8s/redis/
kubectl apply -f k8s/user-service/
kubectl apply -f k8s/market-service/
kubectl apply -f k8s/order-service/
kubectl apply -f k8s/api-gateway/
kubectl apply -f k8s/frontend/
```

---

## Access

| Service       | URL                              |
|---------------|----------------------------------|
| Frontend      | http://localhost:30080           |
| API Gateway   | http://localhost:30000/api/...   |
| RabbitMQ UI   | `kubectl port-forward svc/rabbitmq 15672:15672 -n trading-sim` → http://localhost:15672 |
| Postgres      | `kubectl port-forward svc/postgres 5432:5432 -n trading-sim` |

---

## Useful Commands

```powershell
# Watch all pods come up
kubectl get pods -n trading-sim -w

# View logs for a service
kubectl logs -n trading-sim -l app=user-service --follow

# Describe a pod (for debugging crashloop / startup failures)
kubectl describe pod -n trading-sim -l app=order-service

# Restart a deployment (e.g. after updating a secret)
kubectl rollout restart deployment/user-service -n trading-sim

# Scale a service
kubectl scale deployment/market-service --replicas=2 -n trading-sim
```

---

## Tearing Down

```powershell
# Remove all resources (preserves PVC data)
kubectl delete namespace trading-sim

# Also delete persistent data
kubectl delete pvc postgres-pvc rabbitmq-pvc -n trading-sim
```

---

## Going to Production (Cloud)

1. **Push images to a registry** (GCR, ECR, ACR, Docker Hub)
   ```powershell
   docker tag trading-simulator-user-service:latest gcr.io/YOUR_PROJECT/user-service:v1
   docker push gcr.io/YOUR_PROJECT/user-service:v1
   ```
2. **Update `image:` fields** in each deployment YAML to point to the registry
3. **Change `imagePullPolicy: Always`** in each deployment
4. **Install an Ingress controller** and apply `k8s/ingress.yaml` instead of using NodePorts
5. **Rotate all secrets** — never use the demo credentials in production
