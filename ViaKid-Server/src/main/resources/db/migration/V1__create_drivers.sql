-- ============================================================
-- V1: 接送员账户 + 短信验证码 + 刷新令牌
-- ============================================================
IF OBJECT_ID(N'drivers', N'U') IS NULL
BEGIN
    CREATE TABLE drivers (
        id                  UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID() PRIMARY KEY,
        phone               NVARCHAR(20)      NOT NULL,
        password_hash       NVARCHAR(255)     NOT NULL,
        name                NVARCHAR(100)     NULL,
        gender              NVARCHAR(10)      NULL,
        birthday            DATE              NULL,
        avatar_url          NVARCHAR(500)     NULL,
        emergency_contact   NVARCHAR(100)     NULL,
        emergency_phone     NVARCHAR(20)      NULL,
        verification_level  TINYINT           NOT NULL DEFAULT 0,
        avg_rating          DECIMAL(3,2)      NOT NULL DEFAULT 0.00,
        total_orders        INT               NOT NULL DEFAULT 0,
        status              NVARCHAR(20)      NOT NULL DEFAULT N'pending',
        online_status       TINYINT           NOT NULL DEFAULT 0,
        last_online_at      DATETIME2         NULL,
        is_deleted          BIT               NOT NULL DEFAULT 0,
        created_at          DATETIME2         NOT NULL DEFAULT GETDATE(),
        updated_at          DATETIME2         NOT NULL DEFAULT GETDATE()
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'uk_drivers_phone' AND object_id = OBJECT_ID(N'drivers'))
    CREATE UNIQUE INDEX uk_drivers_phone ON drivers(phone);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_drivers_status' AND object_id = OBJECT_ID(N'drivers'))
    CREATE INDEX idx_drivers_status ON drivers(status);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_drivers_online_status' AND object_id = OBJECT_ID(N'drivers'))
    CREATE INDEX idx_drivers_online_status ON drivers(online_status);
GO

IF OBJECT_ID(N'sms_codes', N'U') IS NULL
BEGIN
    CREATE TABLE sms_codes (
        id          UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID() PRIMARY KEY,
        phone       NVARCHAR(20)      NOT NULL,
        code        NVARCHAR(10)      NOT NULL,
        type        NVARCHAR(20)      NULL,
        expired_at  DATETIME2         NOT NULL,
        used        BIT               NOT NULL DEFAULT 0,
        created_at  DATETIME2         NOT NULL DEFAULT GETDATE()
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_sms_codes_phone_expired' AND object_id = OBJECT_ID(N'sms_codes'))
    CREATE INDEX idx_sms_codes_phone_expired ON sms_codes(phone, expired_at);
GO

IF OBJECT_ID(N'refresh_tokens', N'U') IS NULL
BEGIN
    CREATE TABLE refresh_tokens (
        id          UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID() PRIMARY KEY,
        driver_id   UNIQUEIDENTIFIER  NOT NULL,
        token       NVARCHAR(500)     NOT NULL,
        expired_at  DATETIME2         NOT NULL,
        created_at  DATETIME2         NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_refresh_tokens_driver FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'uk_refresh_tokens_token' AND object_id = OBJECT_ID(N'refresh_tokens'))
    CREATE UNIQUE INDEX uk_refresh_tokens_token ON refresh_tokens(token);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_refresh_tokens_driver' AND object_id = OBJECT_ID(N'refresh_tokens'))
    CREATE INDEX idx_refresh_tokens_driver ON refresh_tokens(driver_id);
GO
