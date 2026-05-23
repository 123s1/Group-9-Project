package com.viakid.driver.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ========== 字体规范 - 文档定义 ==========
/**
 * ViaKid Driver 应用的排版样式
 *
 * 定义了应用中使用的文本样式，包括标题、正文、标签等不同层级的字体规范。
 * 遵循 Material Design 3 的排版指南，确保文本层次清晰、易读性强。
 *
 * 包含的样式层级：
 * - **Headline** (大/中/小标题) - 用于页面标题、重要信息展示
 * - **Body** (大/中正文) - 用于主要内容、描述文本
 * - **Label** (大/小标签) - 用于按钮、表单标签等短文本
 *
 * 使用示例：
 * ```kotlin
 * Text(
 *     text = "欢迎使用",
 *     style = MaterialTheme.typography.headlineLarge
 * )
 *
 * Text(
 *     text = "这是正文内容",
 *     style = MaterialTheme.typography.bodyMedium
 * )
 * ```
 *
 * @see androidx.compose.material3.Typography
 * @see androidx.compose.material3.MaterialTheme.typography
 */
val Typography: Typography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
)
