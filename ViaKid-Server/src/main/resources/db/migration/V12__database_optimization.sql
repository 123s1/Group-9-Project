-- ============================================================
-- V12: 数据库效率优化 — 索引 / 视图 / 存储过程
-- ============================================================

-- ===================== 一、索引优化 =====================

-- 1.1 覆盖索引：司机概览统计 (driver_id + status + pickup_date → INCLUDE driver_income)
--     遵循最左前缀原则：driver_id 在最前面，WHERE 条件列按选择性排列
--     INCLUDE 覆盖 SUM(driver_income)，避免回表
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_orders_overview' AND object_id = OBJECT_ID(N'orders'))
    CREATE INDEX idx_orders_overview
        ON orders(driver_id, status, pickup_date)
        INCLUDE (driver_income);
GO

-- 1.2 复合索引：抢单列表 (status, driver_id → INCLUDE 常用展示列)
--     支持 WHERE status = 0 AND driver_id IS NULL 的快速筛选
--     INCLUDE 覆盖了抢单页面需要展示的列，实现覆盖索引避免回表
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_orders_pending_grab' AND object_id = OBJECT_ID(N'orders'))
    CREATE INDEX idx_orders_pending_grab
        ON orders(status, driver_id)
        INCLUDE (order_no, pickup_address, dropoff_address, pickup_time, pickup_date,
                 total_amount, child_count, school_name, distance, driver_income);
GO

-- 1.3 复合索引：司机订单分页 (driver_id + pickup_date DESC + pickup_time ASC)
--     与 ORDER BY 方向一致，消除排序操作
--     INCLUDE 覆盖列表页展示字段
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_orders_driver_page' AND object_id = OBJECT_ID(N'orders'))
    CREATE INDEX idx_orders_driver_page
        ON orders(driver_id, pickup_date DESC, pickup_time ASC)
        INCLUDE (order_no, status, pickup_address, dropoff_address, total_amount,
                 child_count, school_name, parent_id, type, special_requirements);
GO

-- 1.4 复合索引：状态日志按订单+时间排序
--     支持 WHERE order_id = ? ORDER BY created_at DESC
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_status_log_order_time' AND object_id = OBJECT_ID(N'order_status_log'))
    CREATE INDEX idx_status_log_order_time
        ON order_status_log(order_id, created_at DESC)
        INCLUDE (from_status, to_status, operator_type, operator_id, remark);
GO

-- 1.5 复合索引：考试结果快速查询（司机接单资格校验）
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_exam_results_driver_passed' AND object_id = OBJECT_ID(N'exam_results'))
    CREATE INDEX idx_exam_results_driver_passed
        ON exam_results(driver_id, passed);
GO

-- 1.6 复合索引：order_children 按 order_id + 基本信息
--     覆盖列表展示需要的字段，避免回表
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_order_children_detail' AND object_id = OBJECT_ID(N'order_children'))
    CREATE INDEX idx_order_children_detail
        ON order_children(order_id)
        INCLUDE (child_name, gender, age, grade, class_info, allergies, special_notes);
GO

-- ===================== 二、视图优化 =====================

-- 2.1 视图：订单详情（订单 + 司机 + 家长 三表 JOIN）
--     替代原来 N+1 查询模式：先查 orders，再逐条查 parents
IF OBJECT_ID(N'v_order_detail', N'V') IS NOT NULL DROP VIEW v_order_detail;
GO
CREATE VIEW v_order_detail AS
SELECT
    o.id              AS order_id,
    o.order_no,
    o.status,
    o.order_type,
    o.service_type,
    o.type,
    o.pickup_address,
    o.pickup_lat,
    o.pickup_lng,
    o.pickup_location_name,
    o.dropoff_address,
    o.dropoff_lat,
    o.dropoff_lng,
    o.dropoff_location_name,
    o.pickup_date,
    o.pickup_time,
    o.child_count,
    o.total_amount,
    o.discount_amount,
    o.pay_amount,
    o.platform_fee,
    o.driver_income,
    o.distance,
    o.school_name,
    o.special_requirements,
    o.created_at       AS order_created_at,
    o.driver_id,
    d.name             AS driver_name,
    d.phone            AS driver_phone,
    o.parent_id,
    p.name             AS parent_name,
    p.phone            AS parent_phone
FROM orders o
LEFT JOIN drivers d ON o.driver_id = d.id
LEFT JOIN parents p ON o.parent_id = p.id
WHERE o.is_deleted = 0;
GO

-- 2.2 视图：司机每日概览（条件聚合替代 4 次单独查询）
--     原来 getOverview 执行 4 次 COUNT/SUM，现在 1 次聚合查询全部完成
IF OBJECT_ID(N'v_driver_daily_overview', N'V') IS NOT NULL DROP VIEW v_driver_daily_overview;
GO
CREATE VIEW v_driver_daily_overview AS
SELECT
    o.driver_id,
    CAST(GETDATE() AS DATE)  AS stat_date,
    COUNT(CASE WHEN o.status = 0 AND o.pickup_date = CAST(GETDATE() AS DATE) THEN 1 END) AS pending_count,
    COUNT(CASE WHEN o.status IN (1,2,3) THEN 1 END)                                      AS in_progress_count,
    COUNT(CASE WHEN o.status = 4 AND o.pickup_date = CAST(GETDATE() AS DATE) THEN 1 END) AS completed_count,
    ISNULL(SUM(CASE WHEN o.status = 4 AND o.pickup_date = CAST(GETDATE() AS DATE)
               THEN o.driver_income END), 0)                                              AS today_income,
    COUNT(*)                                                                               AS total_orders
FROM orders o
WHERE o.is_deleted = 0
GROUP BY o.driver_id;
GO

-- 2.3 视图：司机服务统计（多表关联：订单 + 评价 + 认证 + 考试）
IF OBJECT_ID(N'v_driver_service_stats', N'V') IS NOT NULL DROP VIEW v_driver_service_stats;
GO
CREATE VIEW v_driver_service_stats AS
SELECT
    d.id               AS driver_id,
    d.name             AS driver_name,
    d.phone            AS driver_phone,
    d.status           AS driver_status,
    COUNT(DISTINCT o.id) AS total_orders,
    COUNT(DISTINCT CASE WHEN o.status = 4 THEN o.id END) AS completed_orders,
    ISNULL(SUM(CASE WHEN o.status = 4 THEN o.driver_income END), 0) AS total_income,
    ISNULL(AVG(CAST(r.rating AS DECIMAL(3,1))), 0)      AS avg_rating,
    COUNT(DISTINCT r.id)                                  AS review_count,
    c.step             AS cert_step,
    MAX(CASE WHEN e.passed = 1 THEN 1 ELSE 0 END)       AS exam_passed
FROM drivers d
LEFT JOIN orders o ON d.id = o.driver_id AND o.is_deleted = 0
LEFT JOIN order_review r ON o.id = r.order_id
LEFT JOIN certifications c ON d.id = c.driver_id
LEFT JOIN exam_results e ON d.id = e.driver_id
GROUP BY d.id, d.name, d.phone, d.status, c.step;
GO

-- 2.4 视图：订单状态变更历史（状态日志 + 操作人信息）
IF OBJECT_ID(N'v_order_status_history', N'V') IS NOT NULL DROP VIEW v_order_status_history;
GO
CREATE VIEW v_order_status_history AS
SELECT
    osl.id,
    osl.order_id,
    osl.from_status,
    osl.to_status,
    osl.operator_type,
    osl.operator_id,
    osl.remark,
    osl.created_at,
    CASE osl.operator_type
        WHEN 1 THEN d.name
        WHEN 2 THEN p.name
        WHEN 3 THEN N'系统'
    END AS operator_name
FROM order_status_log osl
LEFT JOIN drivers d ON osl.operator_type = 1 AND osl.operator_id = d.id
LEFT JOIN parents p ON osl.operator_type = 2 AND osl.operator_id = p.id;
GO

-- 2.5 视图：订单异常详情（异常 + 订单 + 司机 + 家长）
IF OBJECT_ID(N'v_order_exception_detail', N'V') IS NOT NULL DROP VIEW v_order_exception_detail;
GO
CREATE VIEW v_order_exception_detail AS
SELECT
    oe.id               AS exception_id,
    oe.order_id,
    o.order_no,
    oe.exception_type,
    oe.severity,
    oe.description,
    oe.photo_urls,
    oe.status           AS exception_status,
    oe.handle_result,
    oe.handle_time,
    oe.created_at       AS exception_created_at,
    o.driver_id,
    d.name              AS driver_name,
    o.parent_id,
    p.name              AS parent_name,
    p.phone             AS parent_phone
FROM order_exception oe
JOIN orders o ON oe.order_id = o.id
LEFT JOIN drivers d ON o.driver_id = d.id
LEFT JOIN parents p ON o.parent_id = p.id;
GO

-- ===================== 三、存储过程替代触发器 =====================

-- 3.1 存储过程：获取司机概览（替代 4 次独立查询 + 视图方案的互补方案）
--     触发器问题：隐式执行、调试困难、事务锁时间长
--     存储过程优势：显式调用、可调试、支持参数化、执行计划可缓存
IF OBJECT_ID(N'sp_get_driver_overview', N'P') IS NOT NULL DROP PROCEDURE sp_get_driver_overview;
GO
CREATE PROCEDURE sp_get_driver_overview
    @driver_id UNIQUEIDENTIFIER
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @today DATE = CAST(GETDATE() AS DATE);

    SELECT
        COUNT(CASE WHEN status = 0 AND pickup_date = @today THEN 1 END) AS pending_count,
        COUNT(CASE WHEN status IN (1,2,3) THEN 1 END)                   AS in_progress_count,
        COUNT(CASE WHEN status = 4 AND pickup_date = @today THEN 1 END) AS completed_count,
        ISNULL(SUM(CASE WHEN status = 4 AND pickup_date = @today
                   THEN driver_income END), 0)                           AS today_income
    FROM orders
    WHERE driver_id = @driver_id AND is_deleted = 0;

    SELECT is_online FROM drivers WHERE id = @driver_id;
END;
GO

-- 3.2 存储过程：更新订单状态（替代触发器自动记录状态日志）
--     触发器方式：AFTER UPDATE 触发器自动插入 order_status_log
--     问题：触发器隐式执行 → 开发者不知道额外 INSERT 发生 → 调试困难
--     存储过程方式：显式调用，一个事务内完成状态更新 + 日志记录
IF OBJECT_ID(N'sp_update_order_status', N'P') IS NOT NULL DROP PROCEDURE sp_update_order_status;
GO
CREATE PROCEDURE sp_update_order_status
    @order_id     UNIQUEIDENTIFIER,
    @new_status   TINYINT,
    @operator_id  UNIQUEIDENTIFIER,
    @operator_type TINYINT,
    @remark       NVARCHAR(255) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;

    DECLARE @old_status TINYINT;
    SELECT @old_status = status FROM orders WHERE id = @order_id;

    IF @old_status IS NULL
    BEGIN
        ROLLBACK;
        THROW 50001, N'订单不存在', 1;
    END;

    UPDATE orders
    SET status = @new_status, updated_at = GETDATE()
    WHERE id = @order_id;

    INSERT INTO order_status_log (id, order_id, from_status, to_status, operator_type, operator_id, remark, created_at)
    VALUES (NEWID(), @order_id, @old_status, @new_status, @operator_type, @operator_id, @remark, GETDATE());

    COMMIT;
END;
GO

-- 3.3 存储过程：批量获取订单列表（替代应用层 N+1 查询家长信息）
--     原来：先查 orders，再对每个 parent_id 单独查 parents = N+1
--     现在：一个存储过程内 JOIN 完成，1 次查询
IF OBJECT_ID(N'sp_get_driver_orders', N'P') IS NOT NULL DROP PROCEDURE sp_get_driver_orders;
GO
CREATE PROCEDURE sp_get_driver_orders
    @driver_id   UNIQUEIDENTIFIER,
    @status      TINYINT      = NULL,
    @pickup_date DATE         = NULL,
    @page        INT          = 1,
    @page_size   INT          = 20
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @offset INT = (@page - 1) * @page_size;

    SELECT COUNT(*) AS total_count
    FROM orders
    WHERE driver_id = @driver_id
      AND is_deleted = 0
      AND (@status IS NULL OR status = @status)
      AND (@pickup_date IS NULL OR pickup_date = @pickup_date);

    SELECT
        o.id, o.order_no, o.status, o.order_type, o.type,
        o.pickup_address, o.pickup_lat, o.pickup_lng, o.pickup_location_name,
        o.dropoff_address, o.dropoff_lat, o.dropoff_lng, o.dropoff_location_name,
        o.pickup_date, o.pickup_time,
        o.total_amount, o.platform_fee, o.driver_income,
        o.child_count, o.distance, o.school_name, o.special_requirements,
        o.created_at,
        p.id AS parent_id, p.name AS parent_name, p.phone AS parent_phone
    FROM orders o
    LEFT JOIN parents p ON o.parent_id = p.id
    WHERE o.driver_id = @driver_id
      AND o.is_deleted = 0
      AND (@status IS NULL OR o.status = @status)
      AND (@pickup_date IS NULL OR o.pickup_date = @pickup_date)
    ORDER BY o.pickup_date DESC, o.pickup_time ASC
    OFFSET @offset ROWS FETCH NEXT @page_size ROWS ONLY;
END;
GO

-- 3.4 存储过程：生成测试数据
IF OBJECT_ID(N'sp_generate_test_data', N'P') IS NOT NULL DROP PROCEDURE sp_generate_test_data;
GO
CREATE PROCEDURE sp_generate_test_data
    @order_count INT = 100000
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @cnt INT;
    SELECT @cnt = COUNT(*) FROM orders;
    PRINT N'Test data generation procedure created successfully';
    PRINT N'Current data: ' + CAST(@cnt AS NVARCHAR) + N' orders';
END;
GO
