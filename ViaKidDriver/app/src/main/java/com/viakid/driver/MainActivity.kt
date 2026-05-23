package com.viakid.driver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.viakid.driver.data.local.TokenManager
import com.viakid.driver.ui.navigation.ViaKidApp
import com.viakid.driver.ui.theme.ViaKidDriverTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 应用主 Activity
 *
 * 这是应用的唯一 Activity，采用单 Activity + 多 Screen 的架构模式。
 * 主要职责：
 * - 启用边到边（Edge-to-Edge）显示模式，让内容延伸到状态栏和导航栏下方
 * - 设置 Compose UI 内容视图
 * - 应用全局主题样式
 * - 作为 Hilt 依赖注入的入口点
 *
 * 使用 [@AndroidEntryPoint] 注解后，可以在该 Activity 及其 Fragment 中
 * 通过 [@Inject] 注入由 Hilt 管理的依赖项。
 *
 * UI 结构：
 * ```
 * MainActivity
 *   └─ ViaKidDriverTheme (主题)
 *       └─ Surface (背景容器)
 *           └─ ViaKidApp (导航组件)
 *               ├─ 登录/注册页面
 *               ├─ 首页
 *               ├─ 订单管理
 *               └─ 其他功能页面
 * ```
 *
 * @see ComponentActivity
 * @see ViaKidApp
 * @see <a href="https://dagger.dev/hilt/">Hilt 官方文档</a>
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * Token 管理器，用于管理用户登录状态和认证令牌
     *
     * 通过 Hilt 依赖注入自动提供，用于在应用启动时检查用户是否已登录，
     * 从而决定显示登录页面还是直接进入主界面。
     */
    @Inject
    lateinit var tokenManager: TokenManager

    /**
     * Activity 创建时的回调方法
     *
     * 这是 Activity 生命周期的第一个回调，在此方法中完成以下初始化工作：
     * - 调用父类的 onCreate 方法进行基础初始化
     * - 启用边到边（Edge-to-Edge）显示模式，让内容延伸到系统栏下方
     * - 设置 Compose UI 内容视图，包含主题和导航组件
     *
     * @param savedInstanceState 之前保存的状态数据 Bundle，如果是首次创建则为 null。
     *                           当 Activity 被系统销毁后重建时（如屏幕旋转），
     *                           会传入之前 onSaveInstanceState 保存的数据用于恢复状态。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ViaKidDriverTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ViaKidApp(tokenManager = tokenManager)
                }
            }
        }
    }
}
