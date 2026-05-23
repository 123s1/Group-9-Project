package com.viakid.driver.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// TODO: 支持用户通过调色盘自定义主题色（动态替换 primary 色值）

// ========== 色彩系统 - 安心蓝主色调 ==========
private val Blue500 = Color(0xFF2196F3)
private val Blue700 = Color(0xFF1976D2)
private val Orange500 = Color(0xFFFF9800)
private val Green500 = Color(0xFF4CAF50)
private val Red500 = Color(0xFFF44336)
private val Grey50 = Color(0xFFFAFAFA)
private val Grey900 = Color(0xFF212121)
private val Grey600 = Color(0xFF757575)

private val LightColorScheme = lightColorScheme(
    primary = Blue500,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Grey900,
    secondary = Orange500,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE0B2),
    onSecondaryContainer = Grey900,
    tertiary = Green500,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC8E6C9),
    onTertiaryContainer = Grey900,
    error = Red500,
    onError = Color.White,
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = Grey900,
    background = Grey50,
    onBackground = Grey900,
    surface = Color.White,
    onSurface = Grey900,
    surfaceVariant = Color(0xFFE3F2FD),
    onSurfaceVariant = Grey600,
    outline = Color(0xFFBDBDBD),
    outlineVariant = Color(0xFFE0E0E0),
    inversePrimary = Blue700,
    inverseSurface = Grey900,
    inverseOnSurface = Grey50,
    scrim = Color.Black,
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Grey900,
    primaryContainer = Blue700,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFFFB74D),
    onSecondary = Grey900,
    secondaryContainer = Color(0xFFE65100),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF81C784),
    onTertiary = Grey900,
    tertiaryContainer = Color(0xFF2E7D32),
    onTertiaryContainer = Color.White,
    error = Color(0xFFEF9A9A),
    onError = Grey900,
    errorContainer = Color(0xFFC62828),
    onErrorContainer = Color.White,
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Grey600,
    outline = Color(0xFF424242),
    outlineVariant = Color(0xFF333333),
    inversePrimary = Blue500,
    inverseSurface = Color(0xFFE0E0E0),
    inverseOnSurface = Grey900,
    scrim = Color.Black,
)

/**
 * ViaKid Driver 应用主题
 *
 * 提供 Material Design 3 主题配置，支持浅色/深色模式切换和动态颜色。
 * 采用安心蓝作为主色调，橙色为辅助色，绿色为第三色，营造专业、可靠的司机端应用视觉风格。
 *
 * 主题特性：
 * - **浅色/深色模式**：根据系统设置或手动指定自动切换
 * - **动态颜色**：Android 12+ 支持基于壁纸自动生成配色方案
 * - **完整色彩系统**：包含 primary、secondary、tertiary、error 等全套色板
 *
 * @param darkTheme 是否使用深色主题。默认为 [isSystemInDarkTheme]，即跟随系统设置。
 *                  设置为 true 强制使用深色模式，false 强制使用浅色模式。
 * @param dynamicColor 是否启用动态颜色功能。默认为 true。
 *                     - true：在 Android 12+ 设备上使用系统动态颜色（基于壁纸生成）
 *                     - false：使用预定义的 LightColorScheme 或 DarkColorScheme
 *                     - 在 Android 11 及以下设备会自动降级为预定义配色
 * @param content 要应用此主题的 Composable 内容。通常是应用的根组件或导航容器。
 *
 * 使用示例：
 * ```kotlin
 * // 跟随系统主题
 * ViaKidDriverTheme {
 *     MyAppContent()
 * }
 *
 * // 强制深色模式
 * ViaKidDriverTheme(darkTheme = true) {
 *     MyAppContent()
 * }
 *
 * // 禁用动态颜色，使用固定配色
 * ViaKidDriverTheme(dynamicColor = false) {
 *     MyAppContent()
 * }
 * ```
 *
 * @see MaterialTheme
 * @see lightColorScheme
 * @see darkColorScheme
 * @see dynamicLightColorScheme
 * @see dynamicDarkColorScheme
 */
@Composable
fun ViaKidDriverTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
