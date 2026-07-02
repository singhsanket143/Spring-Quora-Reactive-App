# Kubernetes (K8s) Study Notes — Spring Quora Platform

> **Based on commit:** `eeed42a` — "Added k8s"  
> **Project:** SpringQuoraDeploy — a Spring Boot Quora-like app deployed to Kubernetes with MongoDB, Kafka, Elasticsearch, Redis, HPA, and Azure Container Registry (ACR).

---

## Table of Contents

1. [What We Built](#1-what-we-built)
2. [Prerequisites & Tool Setup](#2-prerequisites--tool-setup)
3. [K8s Core Concepts (Quick Reference)](#3-k8s-core-concepts-quick-reference)
4. [Manifest Folder Structure](#4-manifest-folder-structure)
5. [Hands-On Lab: Step-by-Step](#5-hands-on-lab-step-by-step)
6. [Azure Container Registry (ACR) Setup](#6-azure-container-registry-acr-setup)
7. [Deploy the Full Quora Stack](#7-deploy-the-full-quora-stack)
8. [Horizontal Pod Autoscaler (HPA)](#8-horizontal-pod-autoscaler-hpa)
9. [Exploring with k9s](#9-exploring-with-k9s)
10. [Useful kubectl Commands Cheat Sheet](#10-useful-kubectl-commands-cheat-sheet)
11. [Troubleshooting](#11-troubleshooting)
12. [Quick Revision Checklist](#12-quick-revision-checklist)

---

## 1. What We Built

In the last commit we added:

| Component | Purpose |
|-----------|---------|
| `Dockerfile` | Multi-stage build for the Spring Boot app (Java 21) |
| `k8s/00-namespace` | Isolated `quora` namespace for our platform |
| `k8s/01-pods` | Raw Pod — smallest deployable unit (learning) |
| `k8s/02-services` | Service + LoadBalancer — stable network access |
| `k8s/03-replicaset` | ReplicaSet — keeps N identical pods running |
| `k8s/04-deployments` | Deployment — manages ReplicaSets + rolling updates |
| `k8s/05-redis` | Redis cache deployment + service |
| `k8s/06-kafka` | Kafka broker (KRaft mode) deployment + service |
| `k8s/07-mongo` | MongoDB with PersistentVolumeClaim + service |
| `k8s/08-elasticsearch` | Elasticsearch single-node deployment + service |
| `k8s/09-app` | Spring Boot app (ConfigMap, Deployment, LoadBalancer Service, HPA) |
| `application.yml` | Env-var driven config for K8s (Mongo, Kafka, ES, Actuator probes) |
| `build.gradle` | Added Spring Actuator for health/readiness/liveness probes |

**Architecture overview:**

```
                    ┌─────────────────────────────────────┐
                    │         quora namespace             │
                    │                                     │
  Internet ────────►│  quora-app-service (LoadBalancer)   │
                    │         │                           │
                    │    quora-app (2–6 pods via HPA)     │
                    │    /    |      \                    │
                    │ Mongo  Kafka  Elasticsearch         │
                    │         Redis                       │
                    └─────────────────────────────────────┘
```

---

## 2. Prerequisites & Tool Setup

### Install these tools

```bash
# macOS (Homebrew)
brew install kubectl
brew install azure-cli
brew install docker
brew install k9s

# Verify
kubectl version --client
az version
docker --version
k9s version
```

### Choose a Kubernetes cluster

**Option A — Azure Kubernetes Service (AKS) [recommended for this project]**

```bash
# Login to Azure
az login

# Set variables (change to your values)
export RESOURCE_GROUP="quora-rg"
export LOCATION="uksouth"
export AKS_NAME="quora-aks"
export ACR_NAME="demoquoraacr"   # must be globally unique, lowercase alphanumeric only

# Create resource group
az group create --name $RESOURCE_GROUP --location $LOCATION

# Create AKS cluster (takes ~5–10 min)
az aks create \
  --resource-group $RESOURCE_GROUP \
  --name $AKS_NAME \
  --node-count 2 \
  --node-vm-size Standard_B2s \
  --generate-ssh-keys

# Get kubeconfig
az aks get-credentials --resource-group $RESOURCE_GROUP --name $AKS_NAME

# Verify cluster
kubectl get nodes
```

**Option B — Local cluster (Minikube / Docker Desktop)**

```bash
# Minikube
brew install minikube
minikube start --cpus=4 --memory=8192

# Or enable Kubernetes in Docker Desktop → Settings → Kubernetes

kubectl cluster-info
kubectl get nodes
```

> **Note:** Elasticsearch and Kafka are memory-heavy. Use at least 8 GB RAM for local clusters.

---

## 3. K8s Core Concepts (Quick Reference)

| Concept | What it is | Analogy |
|---------|-----------|---------|
| **Cluster** | Set of machines running K8s | A data center |
| **Node** | A worker machine (VM or physical) | One server in the data center |
| **Pod** | Smallest unit; 1+ containers sharing network/storage | A running process wrapper |
| **ReplicaSet** | Keeps N copies of a pod running | "Always keep 3 of these running" |
| **Deployment** | Manages ReplicaSets; supports rolling updates & rollbacks | App release manager |
| **Service** | Stable DNS + IP to reach pods (ClusterIP / LoadBalancer) | Internal load balancer / front door |
| **Namespace** | Logical isolation inside a cluster | A folder/project boundary |
| **ConfigMap** | Key-value config injected as env vars or files | `.env` file for pods |
| **PersistentVolumeClaim (PVC)** | Request for persistent disk storage | "Give me 5 GB disk" |
| **HPA** | Auto-scales pod count based on CPU/memory/custom metrics | Auto-scaling group for pods |
| **Probe** | Health check (readiness / liveness / startup) | "Is this container alive and ready?" |

**Key relationships:**

```
Deployment
  └── manages → ReplicaSet
                  └── manages → Pod(s)
                                    └── contains → Container(s)

Service → routes traffic to Pods (via label selector)

HPA → watches Deployment → scales ReplicaSet → changes Pod count
```

---

## 4. Manifest Folder Structure

```
k8s/
├── 00-namespace/namespace.yaml       # quora namespace
├── 01-pods/pods.yaml                 # single sample pod (learning)
├── 02-services/service.yaml          # LoadBalancer service (learning)
├── 03-replicaset/replicaset.yaml     # 3 replicas of hello-kubernetes
├── 04-deployments/deployment.yaml    # deployment + rolling update demo
├── 05-redis/redis.yaml               # Redis deployment + service
├── 06-kafka/kafka.yaml               # Kafka deployment + service
├── 07-mongo/mongo.yaml               # MongoDB + PVC + service
├── 08-elasticsearch/elasticsearch.yaml
└── 09-app/
    ├── app.yaml                      # ConfigMap + quora-app Deployment + Service
    └── hpa.yaml                      # HorizontalPodAutoscaler
```

Apply manifests in order when learning. For the full stack, skip `01–04` (those are learning demos in the default namespace).

---

## 5. Hands-On Lab: Step-by-Step

### Lab 0 — Verify your cluster

```bash
kubectl get nodes
kubectl cluster-info
```

Expected output: at least 1 node in `Ready` state.

---

### Lab 1 — Namespace (`k8s/00-namespace`)

**Concept:** Namespaces isolate resources. Our app lives in `quora`.

```bash
kubectl apply -f k8s/00-namespace/namespace.yaml

# Verify
kubectl get namespaces
kubectl get ns quora
```

**Explore:**

```bash
# Set quora as default namespace for this shell session
kubectl config set-context --current --namespace=quora

# Or prefix every command with -n quora
kubectl get all -n quora
```

---

### Lab 2 — Pod (`k8s/01-pods`)

**Concept:** A Pod is the smallest K8s object. Usually you don't create Pods directly — Deployments do it for you. This lab shows the raw unit.

```bash
kubectl apply -f k8s/01-pods/pods.yaml

kubectl get pods
kubectl describe pod samplepod
kubectl logs samplepod

# Port-forward to access locally (no Service yet)
kubectl port-forward pod/samplepod 8080:8080
# Open http://localhost:8080 in browser

# Clean up when done
kubectl delete -f k8s/01-pods/pods.yaml
```

**What to notice:**

- Pod name: `samplepod`
- Label: `app: sampleapp`
- Image: `paulbouwer/hello-kubernetes:1.10`
- Container port: `8080`

---

### Lab 3 — Service (`k8s/02-services`)

**Concept:** Pods get new IPs when they restart. A Service gives a stable DNS name and load-balances across matching pods.

```bash
# Re-create the pod first (if deleted)
kubectl apply -f k8s/01-pods/pods.yaml
kubectl apply -f k8s/02-services/service.yaml

kubectl get svc sampleservice
kubectl describe svc sampleservice

# On AKS: EXTERNAL-IP will appear after ~1–2 min
# On Minikube: run `minikube tunnel` in another terminal
```

**Service types:**

| Type | Use case |
|------|----------|
| `ClusterIP` | Internal only (default) |
| `NodePort` | Expose on each node's IP |
| `LoadBalancer` | Cloud LB (AKS assigns public IP) |

Our sampleservice maps port `3001` → pod port `8080`.

```bash
# Get external IP
kubectl get svc sampleservice -w

# Test (replace EXTERNAL-IP)
curl http://<EXTERNAL-IP>:3001

# Clean up
kubectl delete -f k8s/02-services/service.yaml
kubectl delete -f k8s/01-pods/pods.yaml
```

---

### Lab 4 — ReplicaSet (`k8s/03-replicaset`)

**Concept:** ReplicaSet ensures a specified number of pod replicas are always running.

```bash
kubectl apply -f k8s/03-replicaset/replicaset.yaml

kubectl get rs
kubectl get pods -l app=hello

# Kill a pod — ReplicaSet recreates it automatically
kubectl delete pod <one-of-the-hello-pods>
kubectl get pods -l app=hello -w

# Scale manually (ReplicaSet supports this, but Deployment is preferred)
kubectl scale rs hello-rs --replicas=5
kubectl get pods -l app=hello

# Clean up
kubectl delete -f k8s/03-replicaset/replicaset.yaml
```

---

### Lab 5 — Deployment (`k8s/04-deployments`)

**Concept:** Deployment wraps ReplicaSet and adds **rolling updates** and **rollouts**.

```bash
kubectl apply -f k8s/04-deployments/deployment.yaml

kubectl get deployment hello-deployment
kubectl get rs
kubectl get pods -l app=hello

# Rolling update: change image from 1.10 → 1.10.1 (already in manifest)
kubectl set image deployment/hello-deployment hello-container=paulbouwer/hello-kubernetes:1.10.1

# Watch rollout
kubectl rollout status deployment/hello-deployment
kubectl rollout history deployment/hello-deployment

# Rollback if needed
kubectl rollout undo deployment/hello-deployment

# Clean up
kubectl delete -f k8s/04-deployments/deployment.yaml
```

**Deployment vs ReplicaSet:**

| Feature | ReplicaSet | Deployment |
|---------|-----------|------------|
| Keep N pods running | Yes | Yes |
| Rolling updates | No | Yes |
| Rollback | No | Yes |
| Pause/resume rollout | No | Yes |

---

## 6. Azure Container Registry (ACR) Setup

Before deploying the Spring Boot app, build and push the Docker image to ACR.

### Step 1 — Create ACR

```bash
export RESOURCE_GROUP="quora-rg"
export ACR_NAME="demoquoraacr"   # change if taken; must be globally unique

az acr create \
  --resource-group $RESOURCE_GROUP \
  --name $ACR_NAME \
  --sku Basic \
  --admin-enabled true
```

### Step 2 — Login to ACR

```bash
# Login via Azure CLI (recommended)
az acr login --name $ACR_NAME

# Or get admin credentials
az acr credential show --name $ACR_NAME
# docker login demoquoraacr.azurecr.io -u <username> -p <password>
```

### Step 3 — Build and push the Spring Boot image

From the project root (where `Dockerfile` lives):

```bash
export IMAGE_TAG="v2"
export FULL_IMAGE="${ACR_NAME}.azurecr.io/quora-app:${IMAGE_TAG}"

# Build locally and push
docker build -t $FULL_IMAGE .
docker push $FULL_IMAGE

# OR build directly in ACR (no local Docker needed)
az acr build \
  --registry $ACR_NAME \
  --image quora-app:${IMAGE_TAG} \
  .
```

### Step 4 — Attach ACR to AKS (so nodes can pull images)

```bash
export AKS_NAME="quora-aks"

az aks update \
  --resource-group $RESOURCE_GROUP \
  --name $AKS_NAME \
  --attach-acr $ACR_NAME

# Verify attachment
az aks check-acr \
  --resource-group $RESOURCE_GROUP \
  --name $AKS_NAME \
  --acr $ACR_NAME
```

### Step 5 — Update image in manifest (if your ACR name differs)

In `k8s/09-app/app.yaml`, the image is:

```yaml
image: demoquoraacr.azurecr.io/quora-app:v2
```

Change `demoquoraacr` to your ACR name and tag as needed.

### Dockerfile recap (multi-stage build)

```dockerfile
# Stage 1: Build JAR with Gradle
FROM eclipse-temurin:21-jdk-alpine AS builder
# ... runs ./gradlew bootJar

# Stage 2: Run JAR
FROM eclipse-temurin:21-jre-alpine
# ... runs java -jar app.jar on port 8080
```

Build locally to test:

```bash
docker build -t quora-app:local .
docker run -p 8080:8080 \
  -e MONGODB_HOST=host.docker.internal \
  -e ELASTICSEARCH_URIS=http://host.docker.internal:9200 \
  -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
  quora-app:local
```

---

## 7. Deploy the Full Quora Stack

Apply infrastructure first, then the app.

```bash
# 1. Namespace
kubectl apply -f k8s/00-namespace/namespace.yaml

# 2. Infrastructure (order matters — app depends on these)
kubectl apply -f k8s/05-redis/redis.yaml
kubectl apply -f k8s/06-kafka/kafka.yaml
kubectl apply -f k8s/07-mongo/mongo.yaml
kubectl apply -f k8s/08-elasticsearch/elasticsearch.yaml

# Wait for infra to be ready
kubectl get pods -n quora -w

# 3. App (after image is pushed to ACR)
kubectl apply -f k8s/09-app/app.yaml

# 4. HPA (requires metrics-server — see HPA section)
kubectl apply -f k8s/09-app/hpa.yaml
```

### Watch everything come up

```bash
kubectl get all -n quora
kubectl get pvc -n quora
kubectl get configmap -n quora
```

### Access the app

```bash
kubectl get svc quora-app-service -n quora -w
# Wait for EXTERNAL-IP

curl http://<EXTERNAL-IP>/actuator/health
curl http://<EXTERNAL-IP>/actuator/health/readiness
```

### What each infra component does

| Service DNS (inside cluster) | Port | Used by app for |
|------------------------------|------|-----------------|
| `mongodb-service` | 27017 | MongoDB data store |
| `kafka-service` | 9092 | View-count events |
| `elasticsearch-service` | 9200 | Question search index |
| `redis-service` | 6379 | Caching (deployed, wire up in app later) |

ConfigMap `quora-app-config` injects these as environment variables (see `k8s/09-app/app.yaml`).

### Health probes (Spring Actuator)

The app uses three probes:

| Probe | Path | Purpose |
|-------|------|---------|
| `startupProbe` | `/actuator/health` | Wait for slow startup (up to ~5 min) |
| `readinessProbe` | `/actuator/health/readiness` | Pod receives traffic only when ready |
| `livenessProbe` | `/actuator/health/liveness` | Restart pod if app is dead |

---

## 8. Horizontal Pod Autoscaler (HPA)

**File:** `k8s/09-app/hpa.yaml`

```yaml
minReplicas: 2
maxReplicas: 6
target CPU utilization: 40%
```

HPA watches the `quora-app` Deployment and adds/removes pods to keep average CPU near 40%.

### Prerequisites — install metrics-server

HPA needs CPU metrics. On AKS, metrics-server is usually pre-installed. Verify:

```bash
kubectl get deployment metrics-server -n kube-system

# If missing (Minikube/local):
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

# Minikube also ships with metrics-server:
minikube addons enable metrics-server
```

### Verify HPA

```bash
kubectl get hpa -n quora
kubectl describe hpa quora-app-hpa -n quora

# See live CPU per pod
kubectl top pods -n quora
kubectl top nodes
```

### Trigger a scale-up (load test)

```bash
EXTERNAL_IP=$(kubectl get svc quora-app-service -n quora -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

# Install hey (HTTP load generator)
brew install hey

# Generate load for 2 minutes
hey -z 120s -c 50 http://$EXTERNAL_IP/api/questions

# Watch HPA scale in another terminal
watch kubectl get hpa,pods -n quora
```

Expected behavior:

1. CPU rises above 40%
2. HPA increases replicas (up to 6)
3. Load stops → after stabilization window (60s), scales back down (min 2)

```bash
kubectl describe hpa quora-app-hpa -n quora
# Look at Events: "New size: 4; reason: cpu resource utilization above target"
```

---

## 9. Exploring with k9s

**k9s** is a terminal UI for Kubernetes — great for revision and live exploration.

### Launch k9s

```bash
# Default namespace view
k9s

# Jump directly into quora namespace
k9s -n quora
```

### Essential k9s keyboard shortcuts

| Key | Action |
|-----|--------|
| `:` | Command mode (type resource name) |
| `/` | Filter/search |
| `Enter` | Describe selected resource |
| `d` | Describe (detailed view) |
| `l` | Logs |
| `s` | Shell into container |
| `e` | Edit resource (YAML) |
| `Ctrl+D` | Delete resource |
| `0` | Show all namespaces |
| `1`–`9` | Switch to numbered namespace |
| `?` | Help |
| `Esc` | Go back |
| `Ctrl+C` | Quit |

### k9s exploration exercises

**Exercise 1 — Browse Pods**

```
:pods          → list all pods
/pending       → filter pending pods
Enter          → see pod details, events, container status
l              → stream logs
```

**Exercise 2 — Watch a Deployment rollout**

```
:deployments
Select quora-app → Enter
Watch READY column change during rollout
```

**Exercise 3 — Inspect HPA**

```
:hpa
Select quora-app-hpa → Enter
See current/target replicas and CPU metrics
```

**Exercise 4 — Explore Services**

```
:svc
Select quora-app-service
Note ClusterIP, external IP, ports
```

**Exercise 5 — Check nodes**

```
:nodes
Select a node → Enter
See capacity, allocatable CPU/memory, running pods
```

**Exercise 6 — View ReplicaSets (created by Deployment)**

```
:rs
Notice hello-deployment or quora-app has RS with hash suffix
Old RS kept for rollback history
```

**Exercise 7 — ConfigMaps & Secrets**

```
:configmaps
Select quora-app-config → Enter → see env vars
```

**Exercise 8 — PersistentVolumeClaims**

```
:pvc
Select mongodb-pvc → see bound volume and storage class
```

**Exercise 9 — Events (great for debugging)**

```
:events
Sort by LAST SEEN — shows OOMKills, pull errors, probe failures
```

**Exercise 10 — Port-forward from k9s**

```
:svc
Select quora-app-service
Shift+F          → port-forward to localhost
```

### k9s + load test combo

Terminal 1:

```bash
k9s -n quora
# Navigate to :hpa, watch replicas change
```

Terminal 2:

```bash
hey -z 120s -c 50 http://<EXTERNAL-IP>/api/questions
```

---

## 10. Useful kubectl Commands Cheat Sheet

### Cluster & context

```bash
kubectl config current-context
kubectl config get-contexts
kubectl config use-context <context-name>
kubectl cluster-info
kubectl get componentstatuses
```

### Nodes

```bash
kubectl get nodes
kubectl get nodes -o wide
kubectl describe node <node-name>
kubectl top nodes
kubectl cordon <node-name>
kubectl drain <node-name> --ignore-daemonsets
```

### Namespaces

```bash
kubectl get ns
kubectl create ns my-ns
kubectl delete ns quora
```

### Pods

```bash
kubectl get pods -n quora
kubectl get pods -n quora -o wide
kubectl get pods -n quora -w
kubectl describe pod <pod-name> -n quora
kubectl logs <pod-name> -n quora
kubectl logs <pod-name> -n quora -f
kubectl logs <pod-name> -n quora --previous
kubectl exec -it <pod-name> -n quora -- /bin/sh
kubectl port-forward pod/<pod-name> 8080:8080 -n quora
kubectl delete pod <pod-name> -n quora
```

### Deployments

```bash
kubectl get deploy -n quora
kubectl describe deploy quora-app -n quora
kubectl rollout status deploy/quora-app -n quora
kubectl rollout history deploy/quora-app -n quora
kubectl rollout undo deploy/quora-app -n quora
kubectl set image deploy/quora-app quora-app=demoquoraacr.azurecr.io/quora-app:v3 -n quora
kubectl scale deploy quora-app --replicas=3 -n quora
```

### ReplicaSets

```bash
kubectl get rs -n quora
kubectl describe rs <rs-name> -n quora
```

### Services

```bash
kubectl get svc -n quora
kubectl describe svc quora-app-service -n quora
kubectl get endpoints -n quora
```

### ConfigMaps & Secrets

```bash
kubectl get configmap -n quora
kubectl describe configmap quora-app-config -n quora
kubectl get configmap quora-app-config -n quora -o yaml
```

### HPA

```bash
kubectl get hpa -n quora
kubectl describe hpa quora-app-hpa -n quora
kubectl top pods -n quora
```

### Storage

```bash
kubectl get pvc -n quora
kubectl get pv
kubectl describe pvc mongodb-pvc -n quora
```

### Apply & delete

```bash
kubectl apply -f k8s/09-app/
kubectl delete -f k8s/09-app/
kubectl apply -f k8s/ --recursive
```

### Debugging

```bash
kubectl get events -n quora --sort-by='.lastTimestamp'
kubectl api-resources
kubectl explain deployment.spec
kubectl auth can-i create pods -n quora
```

---

## 11. Troubleshooting

### Pod stuck in `Pending`

```bash
kubectl describe pod <pod-name> -n quora
kubectl get pvc -n quora
kubectl describe pvc mongodb-pvc -n quora
```

Common causes: insufficient CPU/memory, PVC not bound, no nodes available.

### Pod in `CrashLoopBackOff`

```bash
kubectl logs <pod-name> -n quora
kubectl logs <pod-name> -n quora --previous
kubectl describe pod <pod-name> -n quora
```

Common causes: wrong env vars, can't connect to Mongo/Kafka/ES, OOM.

### Pod in `ImagePullBackOff`

```bash
kubectl describe pod <pod-name> -n quora
az aks check-acr --resource-group $RESOURCE_GROUP --name $AKS_NAME --acr $ACR_NAME
```

Common causes: wrong image name, ACR not attached to AKS, missing pull secret.

### Readiness probe failing

```bash
kubectl logs <pod-name> -n quora
kubectl port-forward pod/<pod-name> 8080:8080 -n quora
curl http://localhost:8080/actuator/health/readiness
```

App may still be starting — check startupProbe failureThreshold.

### HPA shows `<unknown>` for CPU

```bash
kubectl top pods -n quora
```

If error: metrics-server not installed or pods missing resource requests. Our `app.yaml` sets `resources.requests.cpu: 250m` — required for HPA CPU %.

### LoadBalancer stuck on `<pending>`

```bash
# AKS: wait 2–5 min, check Azure portal → Load Balancer resource
# Minikube: run `minikube tunnel` in separate terminal
# Docker Desktop: usually gets localhost
```

### Elasticsearch OOMKilled

```bash
kubectl describe pod -l app=elasticsearch -n quora
```

Needs ~768Mi–1.5Gi; increase node size or reduce `ES_JAVA_OPTS`.

### Kafka not ready

```bash
kubectl logs -l app=kafka -n quora
```

Takes 30–60s to start; readiness probe has `initialDelaySeconds: 30`.

---

## 12. Quick Revision Checklist

Before an exam or interview, make sure you can explain and demo:

- [ ] Difference between Pod, ReplicaSet, Deployment
- [ ] What a Service does and types (ClusterIP vs LoadBalancer)
- [ ] How label selectors connect Service → Pods
- [ ] Rolling update + rollback with Deployment
- [ ] What a ConfigMap is and how `envFrom` works
- [ ] Readiness vs Liveness vs Startup probes
- [ ] What HPA does and why metrics-server is needed
- [ ] Build → push to ACR → deploy to AKS flow
- [ ] Navigate pods, deployments, services, HPA in k9s
- [ ] Debug with `kubectl describe`, `kubectl logs`, `kubectl get events`

---

## One-Command Full Deploy Script (Reference)

Save this as a personal script after completing the labs:

```bash
#!/bin/bash
set -e

NS=quora
RG=quora-rg
AKS=quora-aks
ACR=demoquoraacr
TAG=v2

echo "=== Building & pushing image ==="
az acr build --registry $ACR --image quora-app:$TAG .

echo "=== Deploying infrastructure ==="
kubectl apply -f k8s/00-namespace/namespace.yaml
kubectl apply -f k8s/05-redis/redis.yaml
kubectl apply -f k8s/06-kafka/kafka.yaml
kubectl apply -f k8s/07-mongo/mongo.yaml
kubectl apply -f k8s/08-elasticsearch/elasticsearch.yaml

echo "=== Waiting for infra pods ==="
kubectl wait --for=condition=ready pod -l app=mongodb -n $NS --timeout=300s
kubectl wait --for=condition=ready pod -l app=kafka -n $NS --timeout=300s
kubectl wait --for=condition=ready pod -l app=elasticsearch -n $NS --timeout=300s

echo "=== Deploying app + HPA ==="
kubectl apply -f k8s/09-app/app.yaml
kubectl apply -f k8s/09-app/hpa.yaml

echo "=== Status ==="
kubectl get all -n $NS
kubectl get hpa -n $NS
echo "App URL:"
kubectl get svc quora-app-service -n $NS
```

---

*Notes generated from SpringQuoraDeploy commit `eeed42a` — Dockerfile, 12 K8s manifests, Spring Actuator health probes, and env-driven `application.yml`.*
