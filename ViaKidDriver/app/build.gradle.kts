buildscript {
    dependencies {
        classpath("com.squareup:javapoet:1.13.0")
    }
}


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.viakid.driver"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.viakid.driver"
        minSdk = 30
        targetSdk = 36
        // 版本号格式: major.minor.patch (例如: 0.3.1)
        val versionName = "0.3.1"
        val versionParts = versionName.split(".")
        val major = versionParts[0].toInt()
        val minor = versionParts[1].toInt()
        val patch = versionParts[2].toInt()

        // 计算公式: major * 10000 + minor * 100 + patch
        versionCode = major * 10000 + minor * 100 + patch  // 0*10000 + 3*100 + 1 = 301


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // 签名配置
    signingConfigs {
        create("release") {
            storeFile = file("D:\\Android_Projects\\time_announcer\\pwdunjx.nn.br.jks")
            storePassword = "unjx.nn.br"
            keyAlias = "unjx"
            keyPassword = "unjx.nn.br"
        }
        getByName("debug") {
            // debug 签名保持默认配置
        }
    }

    buildTypes {
        release {
            // Release 模式：开启所有优化
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")

            ndk {
                abiFilters.clear()
                abiFilters.addAll(listOf("arm64-v8a"))
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this as com.android.build.gradle.internal.api.ApkVariantOutputImpl
            output.outputFileName = "安心接送_专员版_v${variant.versionName}_${variant.buildType.name}.apk"
        }
    }
}

dependencies {
    // ========== Android 基础库 ==========
    implementation(libs.androidx.core.ktx)              // Kotlin 扩展函数
    implementation(libs.androidx.appcompat)             // 兼容性支持库
    implementation(libs.material)                       // Material Design 组件

    // ========== Jetpack Compose UI ==========
    implementation(platform(libs.androidx.compose.bom)) // Compose BOM (版本管理)
    implementation(libs.androidx.ui)                    // Compose 核心 UI
    implementation(libs.androidx.ui.graphics)           // 图形绘制支持
    implementation(libs.androidx.ui.tooling.preview)    // Preview 预览支持
    debugImplementation(libs.androidx.ui.tooling)       // UI 调试工具 (仅 debug)
    implementation(libs.androidx.material3)             // Material Design 3
    implementation(libs.androidx.material.icons.extended) // 扩展图标库

    // ========== Lifecycle & ViewModel ==========
    implementation(libs.androidx.lifecycle.runtime.ktx)        // Lifecycle KTX
    implementation(libs.androidx.lifecycle.viewmodel.compose)  // ViewModel for Compose
    implementation(libs.androidx.lifecycle.runtime.compose)    // Lifecycle for Compose

    // ========== Activity & Navigation ==========
    implementation(libs.androidx.activity.compose)      // Activity Compose 支持
    implementation(libs.androidx.navigation.compose)    // Compose 导航

    // ========== 网络请求 - Ktor ==========
    implementation(libs.ktor.client.core)               // Ktor 客户端核心
    implementation(libs.ktor.client.okhttp)             // OkHttp 引擎
    implementation(libs.ktor.client.content.negotiation) // 内容协商
    implementation(libs.ktor.serialization.kotlinx.json) // JSON 序列化
    implementation(libs.ktor.client.logging)            // 网络日志
    implementation(libs.kotlinx.serialization.json)     // Kotlinx JSON 序列化

    // ========== 异步编程 - Coroutines ==========
    implementation(libs.kotlinx.coroutines.android)     // Kotlin 协程 (Android)

    // ========== 依赖注入 - Hilt ==========
    implementation(libs.hilt.android)                   // Hilt 核心
    ksp(libs.hilt.compiler)                             // Hilt 编译器
    implementation(libs.hilt.navigation.compose)        // Hilt Navigation 集成

    // ========== 本地数据库 - Room ==========
    implementation(libs.androidx.room.runtime)          // Room 运行时
    implementation(libs.androidx.room.ktx)              // Room KTX 扩展
    ksp(libs.androidx.room.compiler)                    // Room 编译器

    // ========== 数据存储 - DataStore ==========
    implementation(libs.androidx.datastore.preferences) // Preferences DataStore

    // ========== 图片加载 - Coil ==========
    implementation(libs.coil.compose)                   // Coil 图片加载 (Compose)

    // ========== 媒体播放 - Media3 ==========
    implementation(libs.androidx.media3.common)         // Media3 通用库
    implementation(libs.androidx.media3.exoplayer)      // ExoPlayer 播放器
    implementation(libs.androidx.media3.ui)             // Media3 UI 组件

    // ========== 测试依赖 ==========
    testImplementation(libs.junit)                      // 单元测试
    androidTestImplementation(libs.androidx.junit)      // Android 单元测试
    androidTestImplementation(libs.androidx.espresso.core) // UI 测试
}
