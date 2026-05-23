# AnXin JieSong - Driver Edition

An Android application for child school pickup/dropoff service drivers.

## About

"AnXin JieSong" Driver Edition is designed for service drivers to efficiently manage pickup/dropoff tasks, including order grabbing, order management, scheduling, training, exams, and certification.

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin 2.1.0 |
| UI | Jetpack Compose + Material3 |
| Architecture | Single Activity + MVVM + Repository |
| Navigation | Compose Navigation |
| Networking | Ktor Client (OkHttp engine) |
| Local Storage | Room + DataStore Preferences |
| DI | Hilt |
| Image Loading | Coil |
| Media | Media3 + ExoPlayer |
| Build | Gradle KTS + Version Catalog |
| Min SDK | Android 11 (API 30) |

## Quick Start

### Prerequisites

- Android Studio Ladybug or later
- JDK 21
- Android SDK 36

### Build & Run

1. Clone the repository
2. Open the root directory with Android Studio
3. Wait for Gradle sync to complete
4. Select the `app` module and click Run

### Build Variants

```bash
./gradlew assembleDebug    # Debug build
./gradlew assembleRelease  # Release build
```

APK outputs are located in `app/build/outputs/apk/`.

## Core Features

### Authentication & Onboarding

```
Login → Certification → Training → Exam → Dashboard
```

- **Login/Register**: Password login, SMS verification code login
- **Certification**: Upload ID card, driver's license, vehicle documents
- **Training Center**: Video course learning (required + elective)
- **Online Exam**: Auto-grading, certificate upon passing

### Task Management

- **Task Board**: Today's task overview (pending/active/completed/earnings)
- **Order Grabbing**: Featured orders + nearby orders with countdown
- **Order Management**: Order details view, status transitions
- **Schedule Management**: Set available time slots and work days

### Order Status Flow

```
Pending → Assigned → Departed → Arrived → Picked Up → En Route → Delivered → Completed
                                    ↕
                              Cancelled (any stage)
```

### Profile

- Personal information display and editing
- Avatar upload
- Online/offline status toggle

## Project Structure

```
app/src/main/java/com/viakid/driver/
├── MainActivity.kt              # Single entry Activity
├── ViaKidApplication.kt         # Application class (Hilt)
├── di/                          # Hilt DI modules
├── data/
│   ├── local/                   # Room database + DataStore
│   ├── remote/                  # Ktor API client
│   ├── model/                   # Data models
│   └── repository/              # Repository layer
└── ui/
    ├── navigation/              # Navigation graph + route definitions
    ├── theme/                   # Material3 theming
    └── screen/
        ├── auth/                # Login / Register
        ├── certification/       # Driver certification
        ├── training/            # Training & exams
        ├── taskboard/           # Task dashboard
        ├── order/grab/          # Order grabbing hall
        ├── orderdetail/         # Order details
        ├── schedule/            # Schedule management
        └── profile/             # User profile
```

## Related Projects

- [ViaKid-Server](https://gitee.com/viakid/ViaKid-Server) — Backend API service
- [viakid-admin](https://gitee.com/viakid/viakid-admin) — Web admin panel
- [miniprogram-3](https://gitee.com/viakid/miniprogram-3) — Parent WeChat Mini Program
