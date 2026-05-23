-- ============================================================
-- V5: 培训课程 + 进度 + 考题 + 选项 + 考试成绩
-- ============================================================
IF OBJECT_ID(N'courses', N'U') IS NULL
BEGIN
    CREATE TABLE courses (
        id           NVARCHAR(50)       NOT NULL PRIMARY KEY,
        title        NVARCHAR(200)      NOT NULL,
        description  NVARCHAR(MAX)      NULL,
        cover_url    NVARCHAR(500)      NULL,
        content_type NVARCHAR(20)       NOT NULL,
        content_url  NVARCHAR(500)      NULL,
        duration     NVARCHAR(50)       NULL,
        course_type  NVARCHAR(50)       NULL,
        is_required  BIT                NOT NULL DEFAULT 0,
        pass_score   TINYINT            NOT NULL DEFAULT 60,
        sort_order   INT                NOT NULL DEFAULT 0,
        status       NVARCHAR(20)       NOT NULL DEFAULT N'active',
        created_at   DATETIME2          NOT NULL DEFAULT GETDATE(),
        updated_at   DATETIME2          NOT NULL DEFAULT GETDATE()
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_courses_type' AND object_id = OBJECT_ID(N'courses'))
    CREATE INDEX idx_courses_type ON courses(course_type);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_courses_status' AND object_id = OBJECT_ID(N'courses'))
    CREATE INDEX idx_courses_status ON courses(status);
GO

IF OBJECT_ID(N'course_progress', N'U') IS NULL
BEGIN
    CREATE TABLE course_progress (
        driver_id     UNIQUEIDENTIFIER  NOT NULL,
        course_id     NVARCHAR(50)      NOT NULL,
        status        NVARCHAR(20)      NOT NULL DEFAULT N'not_started',
        progress      TINYINT           NOT NULL DEFAULT 0,
        last_position BIGINT            NOT NULL DEFAULT 0,
        enrolled_at   DATETIME2         NOT NULL DEFAULT GETDATE(),
        completed_at  DATETIME2         NULL,
        updated_at    DATETIME2         NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_cp_driver FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE,
        CONSTRAINT fk_cp_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
        PRIMARY KEY (driver_id, course_id)
    );
END
GO

IF OBJECT_ID(N'exam_questions', N'U') IS NULL
BEGIN
    CREATE TABLE exam_questions (
        id          NVARCHAR(50)  NOT NULL PRIMARY KEY,
        course_id   NVARCHAR(50)  NULL,
        type        NVARCHAR(20)  NOT NULL,
        content     NVARCHAR(MAX) NOT NULL,
        explanation NVARCHAR(MAX) NULL,
        sort_order  INT           NOT NULL DEFAULT 0,
        created_at  DATETIME2     NOT NULL DEFAULT GETDATE()
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_exam_questions_course' AND object_id = OBJECT_ID(N'exam_questions'))
    CREATE INDEX idx_exam_questions_course ON exam_questions(course_id);
GO

IF OBJECT_ID(N'exam_options', N'U') IS NULL
BEGIN
    CREATE TABLE exam_options (
        id          NVARCHAR(50)  NOT NULL PRIMARY KEY,
        question_id NVARCHAR(50)  NOT NULL,
        option_key  NVARCHAR(10)  NOT NULL,
        content     NVARCHAR(MAX) NOT NULL,
        is_correct  BIT           NOT NULL DEFAULT 0,
        sort        INT           NOT NULL DEFAULT 0,
        CONSTRAINT fk_options_question FOREIGN KEY (question_id) REFERENCES exam_questions(id) ON DELETE CASCADE
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_exam_options_question' AND object_id = OBJECT_ID(N'exam_options'))
    CREATE INDEX idx_exam_options_question ON exam_options(question_id);
GO

IF OBJECT_ID(N'exam_results', N'U') IS NULL
BEGIN
    CREATE TABLE exam_results (
        id              UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID() PRIMARY KEY,
        driver_id       UNIQUEIDENTIFIER  NOT NULL,
        course_id       NVARCHAR(50)      NOT NULL,
        total_questions INT               NOT NULL,
        correct_count   INT               NOT NULL,
        score           INT               NOT NULL,
        passed          BIT               NOT NULL DEFAULT 0,
        exam_duration   INT               NULL,
        answers_json    NVARCHAR(MAX)     NULL,
        submitted_at    DATETIME2         NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_exam_results_driver FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_exam_results_driver' AND object_id = OBJECT_ID(N'exam_results'))
    CREATE INDEX idx_exam_results_driver ON exam_results(driver_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_exam_results_course' AND object_id = OBJECT_ID(N'exam_results'))
    CREATE INDEX idx_exam_results_course ON exam_results(course_id);
GO
