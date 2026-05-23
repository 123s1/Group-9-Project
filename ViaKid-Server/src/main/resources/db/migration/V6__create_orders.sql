-- ============================================================
-- V6: 订单 + 订单孩子 + 订单状态日志 + 订单评价 + 订单异常
-- ============================================================
IF OBJECT_ID(N'orders', N'U') IS NULL
BEGIN
    CREATE TABLE orders (
        id                     UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID() PRIMARY KEY,
        order_no               NVARCHAR(32)      NOT NULL,
        parent_id              UNIQUEIDENTIFIER  NULL,
        driver_id              UNIQUEIDENTIFIER  NULL,
        order_type             TINYINT           NOT NULL DEFAULT 1,
        service_type           TINYINT           NOT NULL DEFAULT 1,
        status                 TINYINT           NOT NULL DEFAULT 0,
        payment_status         TINYINT           NOT NULL DEFAULT 0,
        pickup_address         NVARCHAR(255)     NOT NULL,
        pickup_lat             DECIMAL(10,6)     NOT NULL,
        pickup_lng             DECIMAL(10,6)     NOT NULL,
        pickup_location_name   NVARCHAR(200)     NULL,
        dropoff_address        NVARCHAR(255)     NOT NULL,
        dropoff_lat            DECIMAL(10,6)     NOT NULL,
        dropoff_lng            DECIMAL(10,6)     NOT NULL,
        dropoff_location_name NVARCHAR(200)      NULL,
        pickup_time            DATETIME2         NOT NULL,
        actual_pickup_time     DATETIME2         NULL,
        actual_dropoff_time    DATETIME2         NULL,
        child_count            TINYINT           NOT NULL DEFAULT 1,
        total_amount           DECIMAL(10,2)     NOT NULL,
        discount_amount        DECIMAL(10,2)     NOT NULL DEFAULT 0.00,
        pay_amount             DECIMAL(10,2)     NOT NULL,
        payment_method         TINYINT           NULL,
        payment_time           DATETIME2         NULL,
        payment_no             NVARCHAR(64)      NULL,
        cancel_reason          NVARCHAR(255)     NULL,
        cancelled_by           TINYINT           NULL,
        remark                 NVARCHAR(500)     NULL,
        is_deleted             BIT               NOT NULL DEFAULT 0,
        created_at             DATETIME2         NOT NULL DEFAULT GETDATE(),
        updated_at             DATETIME2         NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_orders_driver FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE SET NULL
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'uk_orders_order_no' AND object_id = OBJECT_ID(N'orders'))
    CREATE UNIQUE INDEX uk_orders_order_no ON orders(order_no);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_orders_parent' AND object_id = OBJECT_ID(N'orders'))
    CREATE INDEX idx_orders_parent ON orders(parent_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_orders_driver' AND object_id = OBJECT_ID(N'orders'))
    CREATE INDEX idx_orders_driver ON orders(driver_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_orders_status' AND object_id = OBJECT_ID(N'orders'))
    CREATE INDEX idx_orders_status ON orders(status);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_orders_pickup_time' AND object_id = OBJECT_ID(N'orders'))
    CREATE INDEX idx_orders_pickup_time ON orders(pickup_time);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_orders_driver_status_date' AND object_id = OBJECT_ID(N'orders'))
    CREATE INDEX idx_orders_driver_status_date ON orders(driver_id, status, pickup_time);
GO

IF OBJECT_ID(N'order_children', N'U') IS NULL
BEGIN
    CREATE TABLE order_children (
        id              UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID() PRIMARY KEY,
        order_id        UNIQUEIDENTIFIER  NOT NULL,
        child_name      NVARCHAR(100)     NOT NULL,
        gender          NVARCHAR(10)      NULL,
        age             INT               NULL,
        grade           NVARCHAR(50)      NULL,
        class_info      NVARCHAR(50)      NULL,
        pickup_status   TINYINT           NOT NULL DEFAULT 0,
        pickup_photo    NVARCHAR(500)     NULL,
        dropoff_photo   NVARCHAR(500)     NULL,
        pickup_time     DATETIME2         NULL,
        dropoff_time    DATETIME2         NULL,
        verify_code     NVARCHAR(6)       NULL,
        verified        BIT               NOT NULL DEFAULT 0,
        health_notes    NVARCHAR(MAX)     NULL,
        created_at      DATETIME2         NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_order_children_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_order_children_order' AND object_id = OBJECT_ID(N'order_children'))
    CREATE INDEX idx_order_children_order ON order_children(order_id);
GO

IF OBJECT_ID(N'order_status_log', N'U') IS NULL
BEGIN
    CREATE TABLE order_status_log (
        id             UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID() PRIMARY KEY,
        order_id       UNIQUEIDENTIFIER  NOT NULL,
        from_status    TINYINT           NULL,
        to_status      TINYINT           NOT NULL,
        operator_type  TINYINT           NOT NULL,
        operator_id    UNIQUEIDENTIFIER  NULL,
        remark         NVARCHAR(255)     NULL,
        created_at     DATETIME2         NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_order_status_log_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_order_status_log_order' AND object_id = OBJECT_ID(N'order_status_log'))
    CREATE INDEX idx_order_status_log_order ON order_status_log(order_id);
GO

IF OBJECT_ID(N'order_review', N'U') IS NULL
BEGIN
    CREATE TABLE order_review (
        id             UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID() PRIMARY KEY,
        order_id       UNIQUEIDENTIFIER  NOT NULL UNIQUE,
        parent_id      UNIQUEIDENTIFIER  NULL,
        driver_id      UNIQUEIDENTIFIER  NOT NULL,
        rating         TINYINT           NOT NULL,
        content        NVARCHAR(500)     NULL,
        tags           NVARCHAR(255)     NULL,
        images         NVARCHAR(1000)    NULL,
        is_anonymous   BIT               NOT NULL DEFAULT 0,
        reply_content  NVARCHAR(500)     NULL,
        reply_time     DATETIME2         NULL,
        created_at     DATETIME2         NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_order_review_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
        CONSTRAINT fk_order_review_driver FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_order_review_driver' AND object_id = OBJECT_ID(N'order_review'))
    CREATE INDEX idx_order_review_driver ON order_review(driver_id);
GO

IF OBJECT_ID(N'order_exception', N'U') IS NULL
BEGIN
    CREATE TABLE order_exception (
        id               UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID() PRIMARY KEY,
        order_id         UNIQUEIDENTIFIER  NOT NULL,
        exception_type   TINYINT           NOT NULL,
        severity         TINYINT           NOT NULL,
        description      NVARCHAR(500)     NOT NULL,
        photo_urls       NVARCHAR(1000)    NULL,
        handler_type     TINYINT           NULL,
        handler_id       UNIQUEIDENTIFIER  NULL,
        handle_result    NVARCHAR(500)     NULL,
        handle_time      DATETIME2         NULL,
        status           TINYINT           NOT NULL DEFAULT 0,
        created_at       DATETIME2         NOT NULL DEFAULT GETDATE(),
        updated_at       DATETIME2         NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_order_exception_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_order_exception_order' AND object_id = OBJECT_ID(N'order_exception'))
    CREATE INDEX idx_order_exception_order ON order_exception(order_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_order_exception_status' AND object_id = OBJECT_ID(N'order_exception'))
    CREATE INDEX idx_order_exception_status ON order_exception(status);
GO
