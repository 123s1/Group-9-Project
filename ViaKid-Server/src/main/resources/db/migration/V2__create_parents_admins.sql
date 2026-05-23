-- ============================================================
-- V2: 家长账户 + 管理员账户（阶段一家长空壳）
-- ============================================================
IF OBJECT_ID(N'parents', N'U') IS NULL
BEGIN
    CREATE TABLE parents (
        id                  UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID() PRIMARY KEY,
        phone               NVARCHAR(20)      NOT NULL,
        password_hash       NVARCHAR(255)     NOT NULL,
        name                NVARCHAR(100)     NULL,
        avatar_url          NVARCHAR(500)     NULL,
        gender              NVARCHAR(10)      NULL,
        birthday            DATE              NULL,
        status              NVARCHAR(20)      NOT NULL DEFAULT N'active',
        is_deleted          BIT               NOT NULL DEFAULT 0,
        created_at          DATETIME2         NOT NULL DEFAULT GETDATE(),
        updated_at          DATETIME2         NOT NULL DEFAULT GETDATE()
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'uk_parents_phone' AND object_id = OBJECT_ID(N'parents'))
    CREATE UNIQUE INDEX uk_parents_phone ON parents(phone);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_parents_status' AND object_id = OBJECT_ID(N'parents'))
    CREATE INDEX idx_parents_status ON parents(status);
GO

IF OBJECT_ID(N'admins', N'U') IS NULL
BEGIN
    CREATE TABLE admins (
        id                  UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID() PRIMARY KEY,
        phone               NVARCHAR(20)      NOT NULL,
        password_hash       NVARCHAR(255)     NOT NULL,
        username            NVARCHAR(50)      NOT NULL,
        nickname            NVARCHAR(100)     NULL,
        email               NVARCHAR(100)     NULL,
        department          NVARCHAR(100)     NULL,
        role                NVARCHAR(50)      NOT NULL DEFAULT N'staff',
        status              NVARCHAR(20)      NOT NULL DEFAULT N'active',
        created_at          DATETIME2         NOT NULL DEFAULT GETDATE(),
        updated_at          DATETIME2         NOT NULL DEFAULT GETDATE()
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'uk_admins_phone' AND object_id = OBJECT_ID(N'admins'))
    CREATE UNIQUE INDEX uk_admins_phone ON admins(phone);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'uk_admins_username' AND object_id = OBJECT_ID(N'admins'))
    CREATE UNIQUE INDEX uk_admins_username ON admins(username);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_admins_status' AND object_id = OBJECT_ID(N'admins'))
    CREATE INDEX idx_admins_status ON admins(status);
GO
