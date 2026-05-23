-- ============================================================
-- V9: 为 drivers 表补充 device_id 和 is_online 列
-- 同时更新 status 列注释，对齐 API 契约定义
-- ============================================================

-- 添加 device_id 列（nullable，已有数据不受影响）
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE Name = 'device_id'
      AND Object_ID = Object_ID(N'drivers')
)
BEGIN
    ALTER TABLE drivers ADD device_id NVARCHAR(100) NULL;
END
GO

-- 添加 is_online 列（nullable，已有数据不受影响）
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE Name = 'is_online'
      AND Object_ID = Object_ID(N'drivers')
)
BEGIN
    ALTER TABLE drivers ADD is_online BIT NULL DEFAULT 0;
END
GO

-- 更新 status 列注释（API 契约定义：pending/approved/rejected/probation/formal/suspended）
EXEC sp_addextendedproperty
    @name = N'MS_Description',
    @value = N'pending-待审核/approved-审核通过/rejected-审核驳回/probation-试用期/formal-正式/suspended-暂停',
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE',  @level1name = N'drivers',
    @level2type = N'COLUMN', @level2name = N'status';
GO
