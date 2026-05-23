-- ============================================================
-- V3: 五重资质认证 + 资质材料
-- ============================================================
IF OBJECT_ID(N'qualifications', N'U') IS NULL
BEGIN
    CREATE TABLE qualifications (
        id               UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID() PRIMARY KEY,
        driver_id        UNIQUEIDENTIFIER  NOT NULL,
        qual_type        TINYINT           NOT NULL,
        qual_name        NVARCHAR(50)      NOT NULL,
        status           TINYINT           NOT NULL DEFAULT 0,
        reject_reason    NVARCHAR(255)     NULL,
        expire_date      DATE              NULL,
        verify_time      DATETIME2         NULL,
        verify_admin_id  UNIQUEIDENTIFIER  NULL,
        created_at       DATETIME2         NOT NULL DEFAULT GETDATE(),
        updated_at       DATETIME2         NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_qualifications_driver FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'uk_qualifications_driver_type' AND object_id = OBJECT_ID(N'qualifications'))
    CREATE UNIQUE INDEX uk_qualifications_driver_type ON qualifications(driver_id, qual_type);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_qualifications_driver' AND object_id = OBJECT_ID(N'qualifications'))
    CREATE INDEX idx_qualifications_driver ON qualifications(driver_id);
GO

IF OBJECT_ID(N'qualification_documents', N'U') IS NULL
BEGIN
    CREATE TABLE qualification_documents (
        id                UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID() PRIMARY KEY,
        qualification_id  UNIQUEIDENTIFIER  NOT NULL,
        doc_name          NVARCHAR(100)     NOT NULL,
        doc_url           NVARCHAR(500)     NOT NULL,
        doc_type          NVARCHAR(20)      NOT NULL,
        file_size         INT               NULL,
        sort              INT               NOT NULL DEFAULT 0,
        created_at        DATETIME2         NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_qualification_documents_qual FOREIGN KEY (qualification_id) REFERENCES qualifications(id) ON DELETE CASCADE
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_qual_documents_qual_id' AND object_id = OBJECT_ID(N'qualification_documents'))
    CREATE INDEX idx_qual_documents_qual_id ON qualification_documents(qualification_id);
GO
