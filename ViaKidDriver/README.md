# 安心接送 - 专员版

面向接送专员（司机）的儿童上下学接送服务 Android 应用。

## 项目简介

「安心接送」专员版是专为接送专员设计的移动端应用，帮助专员高效管理接送任务，包括抢单、订单管理、排班、培训考试、资质认证等功能。

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin 2.1.0 |
| UI | Jetpack Compose + Material3 |
| 架构 | 单 Activity + MVVM + Repository |
| 导航 | Compose Navigation |
| 网络 | Ktor Client (OkHttp 引擎) |
| 本地存储 | Room + DataStore Preferences |
| 依赖注入 | Hilt |
| 图片加载 | Coil |
| 媒体播放 | Media3 + ExoPlayer |
| 构建 | Gradle KTS + Version Catalog |
| 最低 API | Android 11 (API 30) |

## 快速开始

### 环境要求

- Android Studio Ladybug 或更高版本
- JDK 21
- Android SDK 36

### 构建运行

1. 克隆项目
2. 用 Android Studio 打开根目录
3. 等待 Gradle 同步完成
4. 选择 `app` 模块，点击 Run 即可

### Debug 与 Release

```bash
./gradlew assembleDebug    # Debug 构建
./gradlew assembleRelease  # Release 构建
```

构建产物位于 `app/build/outputs/apk/`。

## 核心功能

### 认证与准入流程

```
登录注册 → 资质认证 → 培训学习 → 在线考试 → 任务看板
```

- **登录/注册**：手机号密码登录、短信验证码登录
- **资质认证**：上传身份证、驾驶证、车辆证件等
- **培训中心**：视频课程学习（必修 + 选修）
- **在线考试**：系统自动判分，合格发证书

### 任务管理

- **任务看板**：今日任务概览（待办/进行中/已完成/收入统计）
- **抢单大厅**：主推订单 + 附近订单 + 倒计时抢单
- **订单管理**：订单详情查看、状态流转操作
- **排班管理**：设定可接单时间段和工作日

### 订单状态流转

```
待派单 → 已派单 → 已出发 → 已到达 → 已接孩子 → 途中 → 已送达 → 已完成
                            ↕
                         已取消（任何阶段）
```

### 个人中心

- 个人信息展示与编辑
- 头像上传
- 在线/离线状态切换

## 项目结构

```
app/src/main/java/com/viakid/driver/
├── MainActivity.kt              # 唯一入口 Activity
├── ViaKidApplication.kt         # Application 类 (Hilt)
├── di/                          # Hilt 依赖注入模块
├── data/
│   ├── local/                   # Room 数据库 + DataStore
│   ├── remote/                  # Ktor API 客户端
│   ├── model/                   # 数据模型
│   └── repository/              # 仓库层
└── ui/
    ├── navigation/              # 导航图 + 路由定义
    ├── theme/                   # Material3 主题
    └── screen/
        ├── auth/                # 登录/注册
        ├── certification/       # 资质认证
        ├── training/            # 培训考试
        ├── taskboard/           # 任务看板
        ├── order/grab/          # 抢单大厅
        ├── orderdetail/         # 订单详情
        ├── schedule/            # 排班管理
        └── profile/             # 个人中心
```

## 相关项目

- [ViaKid-Server](https://gitee.com/viakid/ViaKid-Server) — 后端 API 服务
- [viakid-admin](https://gitee.com/viakid/viakid-admin) — 后台管理系统（Web）
- [miniprogram-3](https://gitee.com/viakid/miniprogram-3) — 家长端微信小程序
