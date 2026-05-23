# ViaKid-Server

Backend API service for the AnXin JieSong child pickup/dropoff platform.

## About

A RESTful API service built with Ktor framework, providing core business interfaces including user authentication, order management, driver management, training and exams for the AnXin JieSong platform.

## Tech Stack

| Category | Technology |
|----------|------------|
| Framework | Ktor 3.4 (Netty) |
| Language | Kotlin + JVM 21 |
| Serialization | kotlinx.serialization |
| Auth | JWT (Dual tokens: Access + Refresh) |
| DI | Koin |
| Database | SQL Server + Exposed ORM |
| Connection Pool | HikariCP |
| Migration | Flyway |
| Cache | Redis (Lettuce) |
| Password | BCrypt |
| Logging | Logback |

## Quick Start

### Prerequisites

- JDK 21
- SQL Server (local or remote)
- Redis 7 (optional, pre-configured in docker-compose)

### Start the Service

```bash
# Start Redis (optional)
docker-compose up -d

# Start the server
./gradlew run
```

Verify the service is running at `http://localhost:8900/health`.

Expected output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8900
```

### Configuration

Main configuration file is `src/main/resources/application.yaml`:

| Config | Default | Description |
|--------|---------|-------------|
| Port | 8900 | HTTP listen port |
| Database | localhost:1433/ViaKidDB | SQL Server connection |
| JWT Secret | viakid-server-secret-key-...(32 chars) | Access token signing key |
| Access TTL | 1 hour | Short-lived token |
| Refresh TTL | 7 days | Long-lived token |
| Upload Dir | ./uploads | Local file storage |

## API Overview

All business endpoints are prefixed with `/api/v1`.

### Auth (`/api/v1/auth`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/auth/sms/send` | Send SMS verification code |
| POST | `/auth/register` | Register with phone + code |
| POST | `/auth/login` | Password login |
| POST | `/auth/login/sms` | SMS code login |
| POST | `/auth/refresh` | Refresh Access Token |
| POST | `/auth/logout` | Logout |
| POST | `/auth/password/change` | Change password |

### Driver (`/api/v1/driver`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/driver/me` | Get personal info |
| PUT | `/driver/profile` | Update profile |
| POST | `/driver/avatar` | Upload avatar |
| GET | `/driver/certification` | Get certification progress |
| POST | `/driver/certification/certificate` | Upload documents |
| GET | `/driver/schedule` | Get schedule |
| PUT | `/driver/schedule` | Update schedule |
| POST | `/driver/status` | Toggle online/offline |

### Training (`/api/v1/training`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/training/courses` | Course list (with progress) |
| GET | `/training/courses/{id}` | Course detail |
| POST | `/training/courses/{id}/complete` | Mark course complete |
| GET | `/training/exam` | Exam info |
| GET | `/training/exam/questions` | Get exam questions |
| POST | `/training/exam/submit` | Submit exam (auto-grading) |
| GET | `/training/certificate` | Get certificate |

### Orders (`/api/v1/orders`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/orders/overview` | Today's task overview |
| GET | `/orders/grab` | Available orders for grabbing |
| GET | `/orders` | My order list |
| GET | `/orders/{id}` | Order detail |
| POST | `/orders/{id}/accept` | Accept order |
| POST | `/orders/{id}/reject` | Reject order |
| POST | `/orders/{id}/status` | Update order status |
| POST | `/orders/{id}/grab` | Grab order (optimistic lock) |

### Routes (`/api/v1/routes`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/routes` | Fixed route list |
| POST | `/routes/bind` | Bind driver to route |

## Project Structure

```
src/main/kotlin/com/viakid/server/
├── Application.kt           # Entry point
├── config/                  # Configuration data classes
├── di/                      # Koin DI module
├── plugins/                 # Ktor plugins (CORS, Auth, Serialization, etc.)
├── route/                   # Route definitions (5 modules)
├── service/                 # Business service layer (6 services)
├── model/                   # DTO data classes
├── database/
│   ├── DatabaseFactory.kt   # Database connection factory
│   └── table/               # 20 table definitions
├── exception/               # Global exception handling
└── util/                    # Utilities (JWT, hash, SMS, etc.)
```

## Order State Machine

```
pending → assigned → departed → arrived → picked_up → delivered → completed
pending → cancelled
```

## Related Projects

- [ViaKid-Driver](https://gitee.com/viakid/ViaKid-Driver) — Driver Android app
- [viakid-admin](https://gitee.com/viakid/viakid-admin) — Web admin panel
- [miniprogram-3](https://gitee.com/viakid/miniprogram-3) — Parent WeChat Mini Program
