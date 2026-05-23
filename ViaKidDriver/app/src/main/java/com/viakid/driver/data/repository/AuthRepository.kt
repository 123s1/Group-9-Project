package com.viakid.driver.data.repository

import com.viakid.driver.data.local.TokenManager
import com.viakid.driver.data.local.database.UserDao
import com.viakid.driver.data.local.database.UserEntity
import com.viakid.driver.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 认证数据仓库，负责处理用户认证相关的业务逻辑
 *
 * 协调本地数据库、Token管理和远程API调用，提供统一的认证接口
 *
 * @property userDao 用户数据访问对象，用于操作用户信息
 * @property tokenManager Token管理器，用于管理认证令牌
 * @property authApi 认证API接口，用于执行网络请求
 * @property apiClient API客户端，用于管理HTTP连接和令牌
 */
@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val tokenManager: TokenManager,
    private val authApi: AuthApi,
    private val apiClient: ApiClient
) {
    /**
     * 发送短信验证码
     *
     * @param phone 手机号
     * @param type 验证码类型：register-注册，login-登录
     * @return Result<Unit> 操作结果，成功时返回Unit，失败时返回异常
     */
    suspend fun sendSmsCode(phone: String, type: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.sendSmsCode(phone, type)
            if (response.code == ErrorCodes.SUCCESS) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 用户注册
     *
     * @param phone 手机号
     * @param code 短信验证码
     * @param password 用户设置的密码
     * @param deviceId 设备唯一标识
     * @return Result<DriverInfo> 注册成功后返回司机信息，失败时返回异常
     */
    suspend fun register(phone: String, code: String, password: String, deviceId: String): Result<DriverInfo> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.register(phone, code, password, deviceId)
            if (response.code == ErrorCodes.SUCCESS && response.data != null) {
                val authData = response.data
                // 保存 Token
                tokenManager.saveTokens(authData.accessToken, authData.refreshToken, authData.driver.id)
                apiClient.setTokens(authData.accessToken, authData.refreshToken)
                // 保存用户信息到本地数据库
                userDao.insertUser(authData.driver.toEntity(authData.accessToken, authData.refreshToken))
                Result.success(authData.driver)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 密码登录
     *
     * @param phone 手机号
     * @param password 用户密码
     * @param deviceId 设备唯一标识
     * @return Result<DriverInfo> 登录成功后返回司机信息，失败时返回异常
     */
    suspend fun login(phone: String, password: String, deviceId: String): Result<DriverInfo> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.login(phone, password, deviceId)
            if (response.code == ErrorCodes.SUCCESS && response.data != null) {
                val authData = response.data
                tokenManager.saveTokens(authData.accessToken, authData.refreshToken, authData.driver.id)
                apiClient.setTokens(authData.accessToken, authData.refreshToken)
                userDao.insertUser(authData.driver.toEntity(authData.accessToken, authData.refreshToken))
                Result.success(authData.driver)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 验证码登录
     *
     * @param phone 手机号
     * @param code 短信验证码
     * @param deviceId 设备唯一标识
     * @return Result<DriverInfo> 登录成功后返回司机信息，用户不存在时抛出UserNotFoundException
     */
    suspend fun smsLogin(phone: String, code: String, deviceId: String): Result<DriverInfo> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.smsLogin(phone, code, deviceId)
            if (response.code == ErrorCodes.SUCCESS && response.data != null) {
                val authData = response.data
                tokenManager.saveTokens(authData.accessToken, authData.refreshToken, authData.driver.id)
                apiClient.setTokens(authData.accessToken, authData.refreshToken)
                userDao.insertUser(authData.driver.toEntity(authData.accessToken, authData.refreshToken))
                Result.success(authData.driver)
            } else if (response.code == ErrorCodes.USER_NOT_FOUND) {
                Result.failure(UserNotFoundException(response.message))
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 退出登录，清除所有本地数据和远程会话
     *
     * @return Result<Unit> 操作结果，即使API调用失败也会清除本地数据并返回成功
     */
    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            authApi.logout()
            tokenManager.clearAll()
            apiClient.clearTokens()
            userDao.deleteAllUsers()
            Result.success(Unit)
        } catch (e: Exception) {
            // 即使 API 调用失败，也要清除本地数据
            tokenManager.clearAll()
            apiClient.clearTokens()
            userDao.deleteAllUsers()
            Result.success(Unit)
        }
    }

    /**
     * 刷新访问令牌，使用refreshToken获取新的access token
     *
     * @return Result<Unit> 刷新成功返回Unit，失败时返回异常
     */
    suspend fun refreshToken(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.refreshToken.first()
            if (token != null) {
                val response = authApi.refresh(token)
                if (response.code == ErrorCodes.SUCCESS && response.data != null) {
                    val authData = response.data
                    tokenManager.saveTokens(authData.accessToken, authData.refreshToken, authData.driver.id)
                    apiClient.setTokens(authData.accessToken, authData.refreshToken)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(response.message))
                }
            } else {
                Result.failure(Exception("No refresh token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 检查用户是否已登录
     *
     * @return Boolean true表示已登录，false表示未登录
     */
    suspend fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn.first()
    }

    /**
     * 获取当前登录用户的详细信息
     *
     * @return UserEntity? 当前用户实体对象，如果未登录则返回null
     */
    suspend fun getCurrentUser(): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getCurrentUserSync()
    }
}

/**
 * 将DriverInfo转换为UserEntity的扩展函数
 *
 * @param accessToken 访问令牌
 * @param refreshToken 刷新令牌
 * @return UserEntity 转换后的用户实体对象
 */
internal fun DriverInfo.toEntity(accessToken: String, refreshToken: String) = UserEntity(
    id = id,
    phone = phone,
    name = name,
    avatar = avatar,
    status = status,
    accessToken = accessToken,
    refreshToken = refreshToken
)
