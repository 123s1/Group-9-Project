package com.viakid.driver.data.remote

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API客户端，负责配置和管理HTTP网络请求
 *
 * 使用Ktor客户端库，配置了JSON序列化、日志记录、超时处理和认证令牌管理
 */
@Singleton
class ApiClient @Inject constructor() {
    companion object {
        private const val TAG = "ApiClient"
        private const val BASE_URL = "http://10.164.205.150:8900/api/v1/"
    }

    private var accessToken: String? = null
    private var refreshToken: String? = null

    /**
     * JSON序列化配置，用于处理API请求和响应的数据序列化
     */
    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    /**
     * HTTP客户端实例，配置了网络引擎、内容协商、日志记录和超时处理
     */
    val client: HttpClient = HttpClient(OkHttp) {
        engine {
            config {
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
                writeTimeout(30, TimeUnit.SECONDS)
            }
        }

        install(ContentNegotiation) {
            json(json)
        }

        install(Logging) {
            logger = object : Logger {
                /**
                 * 记录HTTP请求和响应日志
                 *
                 * @param message 日志消息内容
                 */
                override fun log(message: String) {
                    Log.d(TAG, message)
                }
            }
            level = LogLevel.BODY
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 30_000
        }

        defaultRequest {
            url(BASE_URL)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)

            accessToken?.let {
                /**
                 * @param token 访问令牌，用于Bearer认证
                 */
                    token ->
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        HttpResponseValidator {
            handleResponseExceptionWithRequest {
                /**
                 * 处理HTTP响应异常
                 *
                 * @param exception 发生的异常对象
                 * @param _ 请求对象（未使用）
                 */
                    exception, _ ->
                Log.e(TAG, "HTTP Error: ${exception.message}")
            }
        }
    }

    /**
     * 设置认证令牌，用于后续的API请求认证
     *
     * @param access 访问令牌（AccessToken）
     * @param refresh 刷新令牌（RefreshToken）
     */
    fun setTokens(access: String?, refresh: String?) {
        accessToken = access
        refreshToken = refresh
    }

    /**
     * 清除所有认证令牌，通常在用户登出时调用
     */
    fun clearTokens() {
        accessToken = null
        refreshToken = null
    }

    /**
     * 获取当前的访问令牌
     *
     * @return String? 当前的访问令牌，如果未设置则返回null
     */
    fun getAccessToken(): String? = accessToken

    /**
     * 获取当前的刷新令牌
     *
     * @return String? 当前的刷新令牌，如果未设置则返回null
     */
    fun getRefreshToken(): String? = refreshToken
}
