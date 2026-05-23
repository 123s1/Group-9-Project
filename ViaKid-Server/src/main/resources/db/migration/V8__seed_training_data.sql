-- ============================================================
-- V8: 培训课程和考题种子数据
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM courses WHERE id = 'C001')
BEGIN
    INSERT INTO courses (id, title, description, cover_url, content_type, content_url, duration, course_type, is_required, pass_score, sort_order, status)
    VALUES ('C001', N'安全驾驶基础', N'接送服务中的安全驾驶规范与注意事项，包括儿童安全座椅使用规范等', NULL, 'video', 'https://example.com/video1.mp4', N'15分钟', 'safety', 1, 60, 1, 'active'),
           ('C002', N'儿童心理学入门', N'了解不同年龄段儿童的心理特征，建立良好互动', NULL, 'video', 'https://example.com/video2.mp4', N'20分钟', 'psychology', 0, 60, 2, 'active'),
           ('C003', N'应急处理流程', N'突发状况（急病/事故/迷路等）的标准化处理流程', NULL, 'video', 'https://example.com/video3.mp4', N'18分钟', 'emergency', 1, 60, 3, 'active');
END
GO

IF NOT EXISTS (SELECT 1 FROM exam_questions WHERE id = 'Q001')
BEGIN
    INSERT INTO exam_questions (id, type, content, sort_order)
    VALUES ('Q001', 'single_choice', N'行车中遇到儿童突然横穿道路时，应当：', 1),
           ('Q002', 'single_choice', N'接送学生时，车辆应在距离学校门口多少米处停车？', 2),
           ('Q003', 'true_false', N'驾驶员可以在接送途中使用手机通话。', 3);
END
GO

IF NOT EXISTS (SELECT 1 FROM exam_options WHERE id = 'O001')
BEGIN
    INSERT INTO exam_options (id, question_id, option_key, content, is_correct)
    VALUES ('O001', 'Q001', 'A', N'减速慢行，必要时停车让行', 1),
           ('O002', 'Q001', 'B', N'鸣笛示意，快速通过', 0),
           ('O003', 'Q001', 'C', N'保持原速通过', 0),
           ('O004', 'Q002', 'A', N'10米', 0),
           ('O005', 'Q002', 'B', N'30米', 1),
           ('O006', 'Q002', 'C', N'50米', 0),
           ('O007', 'Q003', 'A', N'正确', 0),
           ('O008', 'Q003', 'B', N'错误', 1);
END
GO
