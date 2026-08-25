# CartFlow API — E-Commerce Order Management

A production-grade REST API for e-commerce built with **Spring Boot 3**, featuring a Redis-backed cart, Kafka-driven order event pipeline, inventory management, coupon discounts, and product reviews.

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.3.4 |
| Language | Java 21 |
| Database | PostgreSQL 16 |
| Cache / Cart | Redis 7 |
| Messaging | Apache Kafka 3.7 |
| Security | Spring Security + JWT (JJWT 0.12) |
| Migrations | Flyway |
| Mapping | MapStruct |
| Build | Gradle 8 (Kotlin DSL) |
| Docs | SpringDoc OpenAPI / Swagger UI |
| Tests | JUnit 5 + MockMvc + Testcontainers |

## Features

- **Auth:** JWT access + refresh token rotation, ROLE_USER / ROLE_ADMIN
- **Catalog:** Category-scoped products with stock tracking
- **Cart:** Redis-backed ephemeral cart (no DB writes per interaction)
- **Orders:** Full lifecycle with state machine — `PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED`
- **Kafka Events:** `order.placed` and `order.status.changed` topics on every order transition
- **Coupons:** Percentage and fixed-amount discount codes with usage limits and expiry
- **Reviews:** One review per product per user with 1–5 star rating and average aggregation
- **Admin:** Dedicated admin endpoints for order management and product CRUD

## Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 21+

### Run Locally

```bash
# 1. Start all infrastructure (Postgres, Redis, Kafka)
docker-compose up -d

# 2. Run the application
./gradlew bootRun
```

The app starts on **http://localhost:8080**

### Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### API Overview

| Resource | Method | Endpoint |
|---|---|---|
| Auth | POST | `/api/v1/auth/register` |
| Auth | POST | `/api/v1/auth/login` |
| Products | GET | `/api/v1/products` |
| Products | POST | `/api/v1/admin/products` |
| Cart | GET/PUT/DELETE | `/api/v1/cart` |
| Orders | POST | `/api/v1/orders/checkout` |
| Orders | GET | `/api/v1/orders/{id}` |
| Admin Orders | PATCH | `/api/v1/admin/orders/{id}/status` |
| Coupons | POST | `/api/v1/admin/coupons` |
| Reviews | POST | `/api/v1/products/{id}/reviews` |

## Running Tests

```bash
# Requires Docker running for Testcontainers
./gradlew test
```

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/cartflow` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `cartflow_user` | DB user |
| `DB_PASSWORD` | `cartflow_pass` | DB password |
| `REDIS_HOST` | `localhost` | Redis hostname |
| `REDIS_PORT` | `6379` | Redis port |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker address |
| `JWT_SECRET` | *(dev default)* | Base64 secret — **always override in production** |
