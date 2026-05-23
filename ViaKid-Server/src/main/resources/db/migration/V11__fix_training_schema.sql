IF COL_LENGTH(N'courses', N'video_url') IS NULL
BEGIN
    ALTER TABLE courses ADD video_url NVARCHAR(500) NULL;
END
GO

IF COL_LENGTH(N'courses', N'type') IS NULL
BEGIN
    ALTER TABLE courses ADD type NVARCHAR(50) NOT NULL CONSTRAINT DF_courses_type DEFAULT N'required';
END
GO

UPDATE courses
SET video_url = content_url
WHERE video_url IS NULL AND content_url IS NOT NULL;
GO

UPDATE courses
SET type = CASE WHEN is_required = 1 THEN N'required' ELSE N'elective' END
WHERE type IS NULL OR type = N'';
GO

IF COL_LENGTH(N'exam_results', N'certificate_no') IS NULL
BEGIN
    ALTER TABLE exam_results ADD certificate_no NVARCHAR(100) NULL;
END
GO

IF COL_LENGTH(N'exam_results', N'valid_until') IS NULL
BEGIN
    ALTER TABLE exam_results ADD valid_until DATE NULL;
END
GO

IF COL_LENGTH(N'exam_results', N'taken_at') IS NULL
BEGIN
    ALTER TABLE exam_results ADD taken_at DATETIME2 NULL;
END
GO
