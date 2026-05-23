# ViaKid-Server

安心接送平台后端 API 服务。

## 项目简介

基于 Ktor 框架构建的 RESTful API 服务，为「安心接送」平台提供用户认证、订单管理、司机管理、培训考试等核心业务接口。

## 技术栈

| 类别 | 技术 |
|------|------|
| 框架 | Ktor 3.4 (Netty) |
| 语言 | Kotlin + JVM 21 |
| 序列化 | kotlinx.serialization |
| 认证 | JWT (双令牌：Access + Refresh) |
| DI | Koin |
| 数据库 | SQL Server + Exposed ORM |
| 连接池 | HikariCP |
| 迁移 | Flyway |
| 缓存 | Redis (Lettuce) |
| 密码 | BCrypt |
| 日志 | Logback |

## 快速开始

### 环境要求

- JDK 21
- SQL Server（本地或远程）
- Redis 7（可选，docker-compose 中已配置）

### 启动服务

```bash
# 启动 Redis（可选）
docker-compose up -d

# 启动服务器
./gradlew run
```

服务启动后访问 `http://localhost:8900/health` 确认运行状态。

预期输出：

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8900
```

### 配置

配置文件位于 `src/main/resources/application.yaml`，主要配置项：

| 配置 | 默认值 | 说明 |
|------|--------|------|
| 服务端口 | 8900 | HTTP 监听端口 |
| 数据库 | localhost:1433/ViaKidDB | SQL Server 连接 |
| JWT 密钥 | viakid-server-secret-key-...(32位) | access token 签名 |
| Access 有效期 | 1 小时 | 短令牌 |
| Refresh 有效期 | 7 天 | 长令牌 |
| 文件上传目录 | ./uploads | 本地文件存储 |

## API 概览

所有业务接口统一前缀 `/api/v1`。

### 认证 (`/api/v1/auth`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/sms/send` | 发送短信验证码 |
| POST | `/auth/register` | 手机号+验证码注册 |
| POST | `/auth/login` | 密码登录 |
| POST | `/auth/login/sms` | 短信验证码登录 |
| POST | `/auth/refresh` | 刷新 Access Token |
| POST | `/auth/logout` | 登出 |
| POST | `/auth/password/change` | 修改密码 |

### 司机 (`/api/v1/driver`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/driver/me` | 获取个人信息 |
| PUT | `/driver/profile` | 更新个人资料 |
| POST | `/driver/avatar` | 上传头像 |
| GET | `/driver/certification` | 获取认证进度 |
| POST | `/driver/certification/certificate` | 上传证件 |
| GET | `/driver/schedule` | 获取排班 |
| PUT | `/driver/schedule` | 更新排班 |
| POST | `/driver/status` | 切换在线/离线 |

### 培训考试 (`/api/v1/training`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/training/courses` | 课程列表（含进度） |
| GET | `/training/courses/{id}` | 课程详情 |
| POST | `/training/courses/{id}/complete` | 标记课程完成 |
| GET | `/training/exam` | 考试信息 |
| GET | `/training/exam/questions` | 获取考题 |
| POST | `/training/exam/submit` | 提交答卷（自动判分） |
| GET | `/training/certificate` | 获取证书 |

### 订单 (`/api/v1/orders`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/orders/overview` | 今日任务概览 |
| GET | `/orders/grab` | 可抢订单列表 |
| GET | `/orders` | 我的订单列表 |
| GET | `/orders/{id}` | 订单详情 |
| POST | `/orders/{id}/accept` | 接单 |
| POST | `/orders/{id}/reject` | 拒单 |
| POST | `/orders/{id}/status` | 更新订单状态 |
| POST | `/orders/{id}/grab` | 抢单（乐观锁） |

### 固定线路 (`/api/v1/routes`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/routes` | 固定线路列表 |
| POST | `/routes/bind` | 绑定固定线路 |

## 项目结构

```
src/main/kotlin/com/viakid/server/
├── Application.kt           # 入口
├── config/                  # 配置数据类
├── di/                      # Koin DI 模块
├── plugins/                 # Ktor 插件 (CORS, 认证, 序列化等)
├── route/                   # 路由定义 (5个模块)
├── service/                 # 业务服务层 (6个服务)
├── model/                   # DTO 数据类
├── database/
│   ├── DatabaseFactory.kt   # 数据库连接工厂
│   └── table/               # 20张表定义
├── exception/               # 全局异常处理
└── util/                    # 工具类 (JWT, 哈希, 短信等)
```

## 订单状态机

```
pending(待接) → assigned(已指派) → departed(已出发) → arrived(已到达)
    → picked_up(已接到) → delivered(已送达) → completed(已完成)
pending → cancelled(已取消)
```

## 相关项目

- [ViaKid-Driver](https://gitee.com/viakid/ViaKid-Driver) — 专员端 Android 应用
- [viakid-admin](https://gitee.com/viakid/viakid-admin) — 后台管理系统（Web）
- [miniprogram-3](https://gitee.com/viakid/miniprogram-3) — 家长端微信小程序
