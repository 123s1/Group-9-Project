package com.viakid.driver

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * ViaKidDriver 应用程序入口类
 *
 * 这是整个应用的全局上下文，负责：
 * - 初始化 Hilt 依赖注入框架
 * - 管理应用级别的生命周期
 * - 提供全局配置和资源访问
 *
 * 使用 [@HiltAndroidApp] 注解后，Hilt 会自动生成所需的依赖注入代码，
 * 并在应用启动时完成初始化。
 *
 * @constructor 创建 ViaKidApplication 实例
 * @see Application
 * @see <a href="https://dagger.dev/hilt/">Hilt 官方文档</a>
 */
@HiltAndroidApp
class ViaKidApplication : Application()
