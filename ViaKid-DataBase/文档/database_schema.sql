-- ============================================================================
-- 安心接送平台 — 数据库建表脚本（统一版）
-- 数据库：SQL Server 2019+
-- 版本：v2.0.0（统一版）
-- 日期：2026-04-22
-- 说明：三户独立（drivers / parents / admins），SQL Server T-SQL 语法
-- ============================================================================

-- ============================================================================
-- 数据库创建
-- ============================================================================
IF NOT EXISTS (SELECT name
               FROM sys.databases
               WHERE name = 'ViaKidDB')
    BEGIN
        CREATE DATABASE ViaKidDB;
    END
GO

USE ViaKidDB;
GO

-- ============================================================================
-- Part 1: 用户与认证模块 — 三户独立
-- ============================================================================

-- drivers — 接送员账户表
CREATE TABLE drivers
(
    id                 UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID() PRIMARY KEY,
    phone              NVARCHAR(20)     NOT NULL,
    password_hash      NVARCHAR(255)    NOT NULL,
    name               NVARCHAR(100)    NULL,
    gender             NVARCHAR(10)     NULL,
    birthday           DATE             NULL,
    avatar_url         NVARCHAR(500)    NULL,
    emergency_contact  NVARCHAR(100)    NULL,
    emergency_phone    NVARCHAR(20)     NULL,
    verification_level TINYINT          NOT NULL DEFAULT 0,          -- 0-未认证 1-基础 2-身份 3-驾照 4-车辆 5-完全
    avg_rating         DECIMAL(3, 2)    NOT NULL DEFAULT 0.00,
    total_orders       INT              NOT NULL DEFAULT 0,
    status             NVARCHAR(20)     NOT NULL DEFAULT N'pending', -- pending/approved/rejected/probation/formal/suspended
    online_status      TINYINT          NOT NULL DEFAULT 0,          -- 0-离线 1-空闲 2-接单中 3-服务中
    last_online_at     DATETIME2        NULL,
    device_id          NVARCHAR(100)    NULL,
    is_deleted         BIT              NOT NULL DEFAULT 0,
    created_at         DATETIME2        NOT NULL DEFAULT GETDATE(),
    updated_at         DATETIME2        NOT NULL DEFAULT GETDATE()
);

CREATE UNIQUE INDEX uk_drivers_phone ON drivers (phone);
CREATE INDEX idx_drivers_status ON drivers (status);
CREATE INDEX idx_drivers_online_status ON drivers (online_status);
GO

-- parents — 家长账户表（阶段一为空壳，仅供文档记录）
CREATE TABLE parents
(
    id            UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID() PRIMARY KEY,
    phone         NVARCHAR(20)     NOT NULL,
    password_hash NVARCHAR(255)    NOT NULL,
    name          NVARCHAR(100)    NULL,
    avatar_url    NVARCHAR(500)    NULL,
    gender        NVARCHAR(10)     NULL,
    birthday      DATE             NULL,
    status        NVARCHAR(20)     NOT NULL DEFAULT N'active',
    is_deleted    BIT              NOT NULL DEFAULT 0,
    created_at    DATETIME2        NOT NULL DEFAULT GETDATE(),
    updated_at    DATETIME2        NOT NULL DEFAULT GETDATE()
);

CREATE UNIQUE INDEX uk_parents_phone ON parents (phone);
CREATE INDEX idx_parents_status ON parents (status);
GO

-- admins — 管理员账户表
CREATE TABLE admins
(
    id            UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID() PRIMARY KEY,
    phone         NVARCHAR(20)     NOT NULL,
    password_hash NVARCHAR(255)    NOT NULL,
    username      NVARCHAR(50)     NOT NULL,
    nickname      NVARCHAR(100)    NULL,
    email         NVARCHAR(100)    NULL,
    department    NVARCHAR(100)    NULL,
    role          NVARCHAR(50)     NOT NULL DEFAULT N'staff', -- super_admin/operator/finance客服
    status        NVARCHAR(20)     NOT NULL DEFAULT N'active',
    created_at    DATETIME2        NOT NULL DEFAULT GETDATE(),
    updated_at    DATETIME2        NOT NULL DEFAULT GETDATE()
);

CREATE UNIQUE INDEX uk_admins_phone ON admins (phone);
CREATE UNIQUE INDEX uk_admins_username ON admins (username);
CREATE INDEX idx_admins_status ON admins (status);
GO

-- sms_codes — 短信验证码表
CREATE TABLE sms_codes
(
    id         UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID() PRIMARY KEY,
    phone      NVARCHAR(20)     NOT NULL,
    code       NVARCHAR(10)     NOT NULL,
    type       NVARCHAR(20)     NULL, -- login/register/reset_password
    expired_at DATETIME2        NOT NULL,
    used       BIT              NOT NULL DEFAULT 0,
    created_at DATETIME2        NOT NULL DEFAULT GETDATE()
);

CREATE INDEX idx_sms_codes_phone_expired ON sms_codes (phone, expired_at);
GO

-- refresh_tokens — 刷新令牌表
CREATE TABLE refresh_tokens
(
    id         UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID() PRIMARY KEY,
    driver_id  UNIQUEIDENTIFIER NOT NULL,
    token      NVARCHAR(500)    NOT NULL,
    expired_at DATETIME2        NOT NULL,
    created_at DATETIME2        NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_refresh_tokens_driver FOREIGN KEY (driver_id) REFERENCES drivers (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX uk_refresh_tokens_token ON refresh_tokens (token);
CREATE INDEX idx_refresh_tokens_driver ON refresh_tokens (driver_id);
GO

-- ============================================================================
-- Part 2: 接送员端模块
-- ============================================================================

-- qualifications — 五重资质认证表（由原 certifications 重构）
CREATE TABLE qualifications
(
    id              UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID() PRIMARY KEY,
    driver_id       UNIQUEIDENTIFIER NOT NULL,
    qual_type       TINYINT          NOT NULL,           -- 1-身份证 2-驾驶证 3-行驶证 4-无犯罪记录 5-健康证明
    qual_name       NVARCHAR(50)     NOT NULL,
    status          TINYINT          NOT NULL DEFAULT 0, -- 0-待提交 1-审核中 2-通过 3-驳回
    reject_reason   NVARCHAR(255)    NULL,
    expire_date     DATE             NULL,
    verify_time     DATETIME2        NULL,
    verify_admin_id UNIQUEIDENTIFIER NULL,
    created_at      DATETIME2        NOT NULL DEFAULT GETDATE(),
    updated_at      DATETIME2        NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_qualifications_driver FOREIGN KEY (driver_id) REFERENCES drivers (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX uk_qualifications_driver_type ON qualifications (driver_id, qual_type);
CREATE INDEX idx_qualifications_driver ON qualifications (driver_id);
GO

-- qualification_documents — 资质材料表
CREATE TABLE qualification_documents
(
    id               UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID() PRIMARY KEY,
    qualification_id UNIQUEIDENTIFIER NOT NULL,
    doc_name         NVARCHAR(100)    NOT NULL,
    doc_url          NVARCHAR(500)    NOT NULL,
    doc_type         NVARCHAR(20)     NOT NULL, -- IMAGE/PDF/VIDEO
    file_size        INT              NULL,
    sort             INT              NOT NULL DEFAULT 0,
    created_at       DATETIME2        NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_qualification_documents_qual FOREIGN KEY (qualification_id) REFERENCES qualifications (id) ON DELETE CASCADE
);

CREATE INDEX idx_qual_documents_qual_id ON qualification_documents (qualification_id);
GO

-- schedules — 排班表
CREATE TABLE schedules
(
    driver_id          UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    time_slots         NVARCHAR(MAX)    NULL, -- JSON: [{week_day:1, start_time:"08:00", end_time:"18:00", is_available:1, max_order_count:10}]
    work_days          NVARCHAR(MAX)    NULL, -- JSON: [1,2,3,4,5] 表示工作日
    unavailable_dates  NVARCHAR(MAX)    NULL, -- JSON: ["2026-05-01","2026-05-02"]
    max_orders_per_day INT              NOT NULL DEFAULT 5,
    updated_at         DATETIME2        NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_schedules_driver FOREIGN KEY (driver_id) REFERENCES drivers (id) ON DELETE CASCADE
);
GO

-- ============================================================================
-- Part 3: 培训模块
-- ============================================================================

-- courses — 培训课程表
CREATE TABLE courses
(
    id           NVARCHAR(50)  NOT NULL PRIMARY KEY,
    title        NVARCHAR(200) NOT NULL,
    description  NVARCHAR(MAX) NULL,
    cover_url    NVARCHAR(500) NULL,
    content_type NVARCHAR(20)  NOT NULL,           -- video/article/livestream
    content_url  NVARCHAR(500) NULL,
    duration     NVARCHAR(50)  NULL,               -- 课程时长描述
    course_type  NVARCHAR(50)  NULL,               -- safety/service/emergency/psychology/regulation
    is_required  BIT           NOT NULL DEFAULT 0, -- 1-必修
    pass_score   TINYINT       NOT NULL DEFAULT 60,
    sort_order   INT           NOT NULL DEFAULT 0,
    status       NVARCHAR(20)  NOT NULL DEFAULT N'active',
    created_at   DATETIME2     NOT NULL DEFAULT GETDATE(),
    updated_at   DATETIME2     NOT NULL DEFAULT GETDATE()
);

CREATE INDEX idx_courses_type ON courses (course_type);
CREATE INDEX idx_courses_status ON courses (status);
GO

-- course_progress — 培训进度表
CREATE TABLE course_progress
(
    driver_id     UNIQUEIDENTIFIER NOT NULL,
    course_id     NVARCHAR(50)     NOT NULL,
    status        NVARCHAR(20)     NOT NULL DEFAULT N'not_started', -- not_started/in_progress/completed/failed
    progress      TINYINT          NOT NULL DEFAULT 0,              -- 0-100
    last_position BIGINT           NOT NULL DEFAULT 0,              -- 视频播放位置（秒）
    enrolled_at   DATETIME2        NOT NULL DEFAULT GETDATE(),
    completed_at  DATETIME2        NULL,
    updated_at    DATETIME2        NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_cp_driver FOREIGN KEY (driver_id) REFERENCES drivers (id) ON DELETE CASCADE,
    CONSTRAINT fk_cp_course FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE,
    PRIMARY KEY (driver_id, course_id)
);
GO

-- exam_questions — 考题表
CREATE TABLE exam_questions
(
    id          NVARCHAR(50)  NOT NULL PRIMARY KEY,
    course_id   NVARCHAR(50)  NULL,     -- 关联课程，可为空（通用题库）
    type        NVARCHAR(20)  NOT NULL, -- single_choice/multiple_choice/true_false
    content     NVARCHAR(MAX) NOT NULL,
    explanation NVARCHAR(MAX) NULL,     -- 答案解析
    sort_order  INT           NOT NULL DEFAULT 0,
    created_at  DATETIME2     NOT NULL DEFAULT GETDATE()
);

CREATE INDEX idx_exam_questions_course ON exam_questions (course_id);
GO

-- exam_options — 考题选项表
CREATE TABLE exam_options
(
    id          NVARCHAR(50)  NOT NULL PRIMARY KEY,
    question_id NVARCHAR(50)  NOT NULL,
    option_key  NVARCHAR(10)  NOT NULL, -- A/B/C/D
    content     NVARCHAR(MAX) NOT NULL,
    is_correct  BIT           NOT NULL DEFAULT 0,
    sort        INT           NOT NULL DEFAULT 0,
    CONSTRAINT fk_options_question FOREIGN KEY (question_id) REFERENCES exam_questions (id) ON DELETE CASCADE
);

CREATE INDEX idx_exam_options_question ON exam_options (question_id);
GO

-- exam_results — 考试成绩表
CREATE TABLE exam_results
(
    id              UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID() PRIMARY KEY,
    driver_id       UNIQUEIDENTIFIER NOT NULL,
    course_id       NVARCHAR(50)     NOT NULL,
    total_questions INT              NOT NULL,
    correct_count   INT              NOT NULL,
    score           INT              NOT NULL,
    passed          BIT              NOT NULL DEFAULT 0,
    exam_duration   INT              NULL, -- 答题用时（秒）
    answers_json    NVARCHAR(MAX)    NULL, -- 答题详情 JSON
    submitted_at    DATETIME2        NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_exam_results_driver FOREIGN KEY (driver_id) REFERENCES drivers (id) ON DELETE CASCADE
);

CREATE INDEX idx_exam_results_driver ON exam_results (driver_id);
CREATE INDEX idx_exam_results_course ON exam_results (course_id);
GO

-- ============================================================================
-- Part 4: 订单核心模块
-- ============================================================================

-- orders — 订单主表
CREATE TABLE orders
(
    id                    UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID() PRIMARY KEY,
    order_no              NVARCHAR(32)     NOT NULL,
    parent_id             UNIQUEIDENTIFIER NULL,               -- 阶段一为模拟 UUID
    driver_id             UNIQUEIDENTIFIER NULL,
    order_type            TINYINT          NOT NULL DEFAULT 1, -- 1-单次 2-包月 3-固定线路
    service_type          TINYINT          NOT NULL DEFAULT 1, -- 1-上学 2-放学 3-往返
    status                TINYINT          NOT NULL DEFAULT 0, -- 0-待支付 1-待接单 2-待出发 3-接送中 4-已送达 5-待评价 6-已完成 7-已取消 8-异常
    payment_status        TINYINT          NOT NULL DEFAULT 0, -- 0-未支付 1-已支付 2-已退款
    pickup_address        NVARCHAR(255)    NOT NULL,
    pickup_lat            DECIMAL(10, 6)   NOT NULL,
    pickup_lng            DECIMAL(10, 6)   NOT NULL,
    pickup_location_name  NVARCHAR(200)    NULL,
    dropoff_address       NVARCHAR(255)    NOT NULL,
    dropoff_lat           DECIMAL(10, 6)   NOT NULL,
    dropoff_lng           DECIMAL(10, 6)   NOT NULL,
    dropoff_location_name NVARCHAR(200)    NULL,
    pickup_time           DATETIME2        NOT NULL,
    actual_pickup_time    DATETIME2        NULL,
    actual_dropoff_time   DATETIME2        NULL,
    child_count           TINYINT          NOT NULL DEFAULT 1,
    total_amount          DECIMAL(10, 2)   NOT NULL,
    discount_amount       DECIMAL(10, 2)   NOT NULL DEFAULT 0.00,
    pay_amount            DECIMAL(10, 2)   NOT NULL,
    payment_method        TINYINT          NULL,               -- 1-钱包 2-微信 3-支付宝
    payment_time          DATETIME2        NULL,
    payment_no            NVARCHAR(64)     NULL,               -- 第三方支付流水号
    cancel_reason         NVARCHAR(255)    NULL,
    cancelled_by          TINYINT          NULL,               -- 1-家长 2-接送员 3-系统
    remark                NVARCHAR(500)    NULL,
    is_deleted            BIT              NOT NULL DEFAULT 0,
    created_at            DATETIME2        NOT NULL DEFAULT GETDATE(),
    updated_at            DATETIME2        NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_orders_driver FOREIGN KEY (driver_id) REFERENCES drivers (id) ON DELETE SET NULL
);

CREATE UNIQUE INDEX uk_orders_order_no ON orders (order_no);
CREATE INDEX idx_orders_parent ON orders (parent_id);
CREATE INDEX idx_orders_driver ON orders (driver_id);
CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_pickup_time ON orders (pickup_time);
CREATE INDEX idx_orders_driver_status_date ON orders (driver_id, status, pickup_time);
GO

-- order_children — 订单孩子关联表
CREATE TABLE order_children
(
    id            UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID() PRIMARY KEY,
    order_id      UNIQUEIDENTIFIER NOT NULL,
    child_name    NVARCHAR(100)    NOT NULL,
    gender        NVARCHAR(10)     NULL,
    age           INT              NULL,
    grade         NVARCHAR(50)     NULL,
    class_info    NVARCHAR(50)     NULL,
    pickup_status TINYINT          NOT NULL DEFAULT 0, -- 0-待上车 1-已上车 2-已送达 3-未上车
    pickup_photo  NVARCHAR(500)    NULL,
    dropoff_photo NVARCHAR(500)    NULL,
    pickup_time   DATETIME2        NULL,
    dropoff_time  DATETIME2        NULL,
    verify_code   NVARCHAR(6)      NULL,
    verified      BIT              NOT NULL DEFAULT 0,
    health_notes  NVARCHAR(MAX)    NULL,               -- 过敏/疾病等
    created_at    DATETIME2        NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_order_children_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

CREATE INDEX idx_order_children_order ON order_children (order_id);
GO

-- order_status_log — 订单状态流转记录表
CREATE TABLE order_status_log
(
    id            UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID() PRIMARY KEY,
    order_id      UNIQUEIDENTIFIER NOT NULL,
    from_status   TINYINT          NULL,
    to_status     TINYINT          NOT NULL,
    operator_type TINYINT          NOT NULL, -- 1-家长 2-接送员 3-系统
    operator_id   UNIQUEIDENTIFIER NULL,
    remark        NVARCHAR(255)    NULL,
    created_at    DATETIME2        NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_order_status_log_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

CREATE INDEX idx_order_status_log_order ON order_status_log (order_id);
GO

-- order_review — 订单评价表
CREATE TABLE order_review
(
    id            UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID() PRIMARY KEY,
    order_id      UNIQUEIDENTIFIER NOT NULL UNIQUE,
    parent_id     UNIQUEIDENTIFIER NULL,     -- 阶段一为模拟 UUID
    driver_id     UNIQUEIDENTIFIER NOT NULL,
    rating        TINYINT          NOT NULL, -- 1-5
    content       NVARCHAR(500)    NULL,
    tags          NVARCHAR(255)    NULL,     -- 准时/安全/耐心/整洁 等
    images        NVARCHAR(1000)   NULL,
    is_anonymous  BIT              NOT NULL DEFAULT 0,
    reply_content NVARCHAR(500)    NULL,
    reply_time    DATETIME2        NULL,
    created_at    DATETIME2        NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_order_review_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

CREATE INDEX idx_order_review_driver ON order_review (driver_id);
GO

-- order_exception — 订单异常记录表
CREATE TABLE order_exception
(
    id             UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID() PRIMARY KEY,
    order_id       UNIQUEIDENTIFIER NOT NULL,
    exception_type TINYINT          NOT NULL,           -- 1-迟到 2-孩子未出现 3-交通拥堵 4-车辆故障 5-天气异常 6-家长未到 7-其他
    severity       TINYINT          NOT NULL,           -- 1-轻微 2-一般 3-严重
    description    NVARCHAR(500)    NOT NULL,
    photo_urls     NVARCHAR(1000)   NULL,
    handler_type   TINYINT          NULL,               -- 1-系统自动 2-客服
    handler_id     UNIQUEIDENTIFIER NULL,
    handle_result  NVARCHAR(500)    NULL,
    handle_time    DATETIME2        NULL,
    status         TINYINT          NOT NULL DEFAULT 0, -- 0-待处理 1-处理中 2-已解决
    created_at     DATETIME2        NOT NULL DEFAULT GETDATE(),
    updated_at     DATETIME2        NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_order_exception_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

CREATE INDEX idx_order_exception_order ON order_exception (order_id);
CREATE INDEX idx_order_exception_status ON order_exception (status);
GO

-- ============================================================================
-- Part 5: 固定线路模块（暂为模拟数据）
-- ============================================================================

-- fixed_route — 固定线路表
CREATE TABLE fixed_route
(
    id                 UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID() PRIMARY KEY,
    driver_id          UNIQUEIDENTIFIER NOT NULL,
    route_name         NVARCHAR(100)    NOT NULL,
    route_type         TINYINT          NOT NULL DEFAULT 1, -- 1-上学 2-放学 3-往返
    start_point        NVARCHAR(255)    NOT NULL,
    start_lat          DECIMAL(10, 6)   NOT NULL,
    start_lng          DECIMAL(10, 6)   NOT NULL,
    end_point          NVARCHAR(255)    NOT NULL,
    end_lat            DECIMAL(10, 6)   NOT NULL,
    end_lng            DECIMAL(10, 6)   NOT NULL,
    route_points       NVARCHAR(MAX)    NULL,               -- JSON 途经点
    total_distance     DECIMAL(8, 1)    NULL,               -- km
    estimated_duration INT              NULL,               -- 分钟
    week_days          NVARCHAR(20)     NULL,               -- 1,2,3,4,5
    departure_time     NVARCHAR(20)     NULL,               -- HH:mm:ss
    status             TINYINT          NOT NULL DEFAULT 1, -- 1-启用 2-停用
    created_at         DATETIME2        NOT NULL DEFAULT GETDATE(),
    updated_at         DATETIME2        NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_fixed_route_driver FOREIGN KEY (driver_id) REFERENCES drivers (id) ON DELETE CASCADE
);

CREATE INDEX idx_fixed_route_driver ON fixed_route (driver_id);
CREATE INDEX idx_fixed_route_status ON fixed_route (status);
GO

-- ============================================================================
-- 建表脚本结束 — 共 19 张表
-- ViaKidDB 统一版 v2.0.0
-- ============================================================================
