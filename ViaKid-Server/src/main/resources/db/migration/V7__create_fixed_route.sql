-- ============================================================
-- V7: 固定线路表
-- ============================================================
IF OBJECT_ID(N'fixed_route', N'U') IS NULL
BEGIN
    CREATE TABLE fixed_route (
        id                  UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID() PRIMARY KEY,
        driver_id           UNIQUEIDENTIFIER  NOT NULL,
        route_name          NVARCHAR(100)     NOT NULL,
        route_type          TINYINT           NOT NULL DEFAULT 1,
        start_point         NVARCHAR(255)     NOT NULL,
        start_lat           DECIMAL(10,6)     NOT NULL,
        start_lng           DECIMAL(10,6)     NOT NULL,
        end_point           NVARCHAR(255)     NOT NULL,
        end_lat             DECIMAL(10,6)     NOT NULL,
        end_lng             DECIMAL(10,6)     NOT NULL,
        route_points        NVARCHAR(MAX)     NULL,
        total_distance      DECIMAL(8,1)      NULL,
        estimated_duration  INT               NULL,
        week_days           NVARCHAR(20)      NULL,
        departure_time      NVARCHAR(20)      NULL,
        status              TINYINT           NOT NULL DEFAULT 1,
        created_at          DATETIME2         NOT NULL DEFAULT GETDATE(),
        updated_at          DATETIME2         NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_fixed_route_driver FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_fixed_route_driver' AND object_id = OBJECT_ID(N'fixed_route'))
    CREATE INDEX idx_fixed_route_driver ON fixed_route(driver_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_fixed_route_status' AND object_id = OBJECT_ID(N'fixed_route'))
    CREATE INDEX idx_fixed_route_status ON fixed_route(status);
GO
