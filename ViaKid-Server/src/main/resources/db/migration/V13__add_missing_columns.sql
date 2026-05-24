-- ============================================================
-- V13: 补齐 ORM 层定义但数据库缺失的列
-- ============================================================

IF COL_LENGTH('orders', 'pickup_date') IS NULL
    ALTER TABLE orders ADD pickup_date DATE NULL;
IF COL_LENGTH('orders', 'platform_fee') IS NULL
    ALTER TABLE orders ADD platform_fee DECIMAL(10,2) NULL;
IF COL_LENGTH('orders', 'driver_income') IS NULL
    ALTER TABLE orders ADD driver_income DECIMAL(10,2) NULL;
IF COL_LENGTH('orders', 'distance') IS NULL
    ALTER TABLE orders ADD distance DECIMAL(8,2) NULL;
IF COL_LENGTH('orders', 'school_name') IS NULL
    ALTER TABLE orders ADD school_name NVARCHAR(200) NULL;
IF COL_LENGTH('orders', 'special_requirements') IS NULL
    ALTER TABLE orders ADD special_requirements NVARCHAR(MAX) NULL;
IF COL_LENGTH('orders', 'type') IS NULL
    ALTER TABLE orders ADD type NVARCHAR(50) NULL;
GO

IF COL_LENGTH('order_children', 'allergies') IS NULL
    ALTER TABLE order_children ADD allergies NVARCHAR(MAX) NULL;
IF COL_LENGTH('order_children', 'special_notes') IS NULL
    ALTER TABLE order_children ADD special_notes NVARCHAR(MAX) NULL;
GO
