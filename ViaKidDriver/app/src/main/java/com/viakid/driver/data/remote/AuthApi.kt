package com.viakid.driver.data.remote

import io.ktor.client.call.*
import io.ktor.client.request.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 认证模块 API
 * 三端共用
 *
 * @property apiClient API客户端实例，用于执行网络请求
 */
@Singleton
class AuthApi @Inject constructor(
    private val apiClient: ApiClient
) {

    /**
     * 发送验证码
     *
     * @param phone 手机号
     * @param type 验证码类型：register-注册，login-登录
     * @return ApiResponse<Unit> API响应，成功时data为null
     */
    suspend fun sendSmsCode(phone: String, type: String): ApiResponse<Unit> {
        return apiClient.client.post("auth/sms/send") {
            setBody(mapOf("phone" to phone, "type" to type))
        }.body()
    }

    /**
     * 用户注册
     *
     * @param phone 手机号
     * @param code 短信验证码
     * @param password 用户设置的密码
     * @param deviceId 设备唯一标识
     * @return ApiResponse<AuthData> 包含认证令牌和司机信息的响应
     */
    suspend fun register(phone: String, code: String, password: String, deviceId: String): ApiResponse<AuthData> {
        return apiClient.client.post("auth/register") {
            setBody(RegisterRequest(phone, code, password, deviceId))
        }.body()
    }

    /**
     * 密码登录
     * 后端逻辑：手机号已存在→验证密码登录；手机号不存在→自动注册并登录
     *
     * @param phone 手机号
     * @param password 用户密码
     * @param deviceId 设备唯一标识
     * @return ApiResponse<AuthData> 包含认证令牌和司机信息的响应
     */
    suspend fun login(phone: String, password: String, deviceId: String): ApiResponse<AuthData> {
        return apiClient.client.post("auth/login") {
            setBody(LoginRequest(phone, password, deviceId))
        }.body()
    }

    /**
     * 验证码登录
     * 手机号已存在→登录成功；手机号不存在→返回 USER_NOT_FOUND(2001)
     *
     * @param phone 手机号
     * @param code 短信验证码
     * @param deviceId 设备唯一标识
     * @return ApiResponse<AuthData> 包含认证令牌和司机信息的响应
     */
    suspend fun smsLogin(phone: String, code: String, deviceId: String): ApiResponse<AuthData> {
        return apiClient.client.post("auth/login/sms") {
            setBody(SmsLoginRequest(phone, code, deviceId))
        }.body()
    }

    /**
     * 刷新Token，使用refreshToken获取新的access token
     *
     * @param refreshToken 刷新令牌
     * @return ApiResponse<AuthData> 包含新的认证令牌的响应
     */
    suspend fun refresh(refreshToken: String): ApiResponse<AuthData> {
        return apiClient.client.post("auth/refresh") {
            setBody(mapOf("refreshToken" to refreshToken))
        }.body()
    }

    /**
     * 退出登录，清除服务端会话
     *
     * @return ApiResponse<Unit> API响应，成功时data为null
     */
    suspend fun logout(): ApiResponse<Unit> {
        return apiClient.client.post("auth/logout").body()
    }

    /**
     * 修改密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return ApiResponse<Unit> API响应，成功时data为null
     */
    suspend fun changePassword(oldPassword: String, newPassword: String): ApiResponse<Unit> {
        return apiClient.client.post("auth/password/change") {
            setBody(mapOf("oldPassword" to oldPassword, "newPassword" to newPassword))
        }.body()
    }
}

/**
 * 注册请求数据类
 *
 * @property phone 手机号
 * @property code 短信验证码
 * @property password 用户设置的密码
 * @property deviceId 设备唯一标识
 */
@kotlinx.serialization.Serializable
data class RegisterRequest(
    val phone: String,
    val code: String,
    val password: String,
    val deviceId: String
)

/**
 * 密码登录请求数据类
 *
 * @property phone 手机号
 * @property password 用户密码
 * @property deviceId 设备唯一标识
 */
@kotlinx.serialization.Serializable
data class LoginRequest(
    val phone: String,
    val password: String,
    val deviceId: String
)

/**
 * 验证码登录请求数据类
 *
 * @property phone 手机号
 * @property code 短信验证码
 * @property deviceId 设备唯一标识
 */
@kotlinx.serialization.Serializable
data class SmsLoginRequest(
    val phone: String,
    val code: String,
    val deviceId: String
)

/**
 * 认证响应数据类，包含登录成功后返回的令牌和用户信息
 *
 * @property accessToken 访问令牌，用于API认证
 * @property refreshToken 刷新令牌，用于获取新的access token
 * @property driver 司机基本信息
 */
@kotlinx.serialization.Serializable
data class AuthData(
    val accessToken: String,
    val refreshToken: String,
    val driver: DriverInfo
)

/**
 * 司机基本信息数据类
 *
 * @property id 司机ID
 * @property phone 手机号
 * @property name 司机姓名
 * @property avatar 头像URL，可为空
 * @property status 司机状态：pending-待审核，approved-已通过等
 */
@kotlinx.serialization.Serializable
data class DriverInfo(
    val id: String,
    val phone: String,
    val name: String = "",
    val avatar: String? = null,
    val status: String = "pending"
)
