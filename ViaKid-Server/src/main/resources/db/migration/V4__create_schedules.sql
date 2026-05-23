-- ============================================================
-- V4: 排班表
-- ============================================================
IF OBJECT_ID(N'schedules', N'U') IS NULL
BEGIN
    CREATE TABLE schedules (
        driver_id          UNIQUEIDENTIFIER  NOT NULL PRIMARY KEY,
        time_slots         NVARCHAR(MAX)     NULL,
        work_days          NVARCHAR(MAX)     NULL,
        unavailable_dates  NVARCHAR(MAX)     NULL,
        max_orders_per_day INT               NOT NULL DEFAULT 5,
        updated_at         DATETIME2         NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_schedules_driver FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE
    );
END
GO
