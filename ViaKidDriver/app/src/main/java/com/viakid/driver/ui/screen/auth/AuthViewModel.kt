package com.viakid.driver.ui.screen.auth

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viakid.driver.data.local.TokenManager
import com.viakid.driver.data.remote.ApiClient
import com.viakid.driver.data.remote.UserNotFoundException
import com.viakid.driver.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 登录模式枚举
 *
 * 定义用户选择的登录方式。
 */
enum class LoginMode {
    /** 验证码登录模式 */
    SMS,

    /** 密码登录模式 */
    PASSWORD
}

/**
 * 认证界面的 UI 状态数据类
 *
 * 持有登录/注册页面的所有状态信息，包括用户输入、加载状态、错误信息等。
 * 通过 [AuthViewModel] 管理，以 [StateFlow] 的形式暴露给 UI 层。
 *
 * @property loginMode 当前选择的登录模式（验证码或密码）
 * @property phone 用户输入的手机号
 * @property code 用户输入的验证码
 * @property password 用户输入的密码
 * @property confirmPassword 用户输入的确认密码（注册时使用）
 * @property isAgreed 用户是否同意服务协议和隐私政策
 * @property isCodeSent 验证码是否已发送
 * @property countdownSeconds 验证码倒计时剩余秒数
 * @property isLoading 是否正在执行网络请求
 * @property errorMessage 错误提示信息，null 表示无错误
 * @property isLoggedIn 用户是否已成功登录
 * @property needsCertification 登录后是否需要完成资质认证
 * @property needsTraining 登录后是否需要完成培训
 * @property needsRegistration 验证码登录时发现手机号未注册，需要展示注册表单
 */
data class AuthUiState(
    val loginMode: LoginMode = LoginMode.SMS,
    val phone: String = "",
    val code: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isAgreed: Boolean = false,
    val isCodeSent: Boolean = false,
    val countdownSeconds: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val needsCertification: Boolean = false,
    val needsTraining: Boolean = false,
    /** 验证码登录时发现手机号未注册，需要展示注册表单 */
    val needsRegistration: Boolean = false
)

@HiltViewModel
/**
 * 认证界面 ViewModel
 *
 * 负责管理登录、注册相关的业务逻辑和 UI 状态。
 * 支持两种登录方式：验证码登录和密码登录。
 *
 * 主要功能：
 * - **验证码登录**：发送短信验证码，验证后登录；若用户不存在则自动切换到注册流程
 * - **密码登录**：使用手机号+密码登录；后端自动处理新用户注册
 * - **注册流程**：验证码+密码完成新用户注册
 * - **开发者快捷登录**：开发阶段使用的免验证登录入口
 *
 * 登录成功后的状态流转：
 * ```
 * 登录成功 → 检查司机状态
 *   ├─ pending/approved → needsCertification = true（需资质认证）
 *   └─ approved → needsTraining = true（需完成培训）
 * ```
 *
 * @property application 应用上下文，用于获取设备 ID
 * @property authRepository 认证数据仓库，处理登录/注册的网络请求
 * @property tokenManager Token 管理器，保存和管理认证令牌
 * @property apiClient API 客户端，用于设置认证头
 *
 * @see AuthUiState
 * @see LoginMode
 */
class AuthViewModel @Inject constructor(
    private val application: Application,
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
    private val apiClient: ApiClient
) : ViewModel() {

    /** UI 状态的内部可变流 */
    private val _uiState = MutableStateFlow(AuthUiState())

    /**
     * UI 状态流，供界面观察
     *
     * 界面通过收集此 Flow 来响应状态变化，更新 UI 显示。
     */
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * 切换登录模式
     *
     * 在验证码登录和密码登录之间切换，同时重置注册相关状态。
     *
     * @param mode 要切换到的登录模式
     */
    fun updateLoginMode(mode: LoginMode) {
        _uiState.value = _uiState.value.copy(
            loginMode = mode,
            needsRegistration = false,
            confirmPassword = "",
            errorMessage = null
        )
    }

    /**
     * 更新手机号输入
     *
     * 限制最大长度为 11 位（中国手机号格式），并清除之前的错误提示。
     *
     * @param phone 用户输入的手机号字符串
     */
    fun updatePhone(phone: String) {
        if (phone.length <= 11) {
            _uiState.value = _uiState.value.copy(phone = phone, errorMessage = null)
        }
    }

    /**
     * 更新验证码输入
     *
     * 限制最大长度为 6 位，并清除之前的错误提示。
     *
     * @param code 用户输入的验证码字符串
     */
    fun updateCode(code: String) {
        if (code.length <= 6) {
            _uiState.value = _uiState.value.copy(code = code, errorMessage = null)
        }
    }

    /**
     * 更新密码输入
     *
     * 限制最大长度为 20 位，并清除之前的错误提示。
     *
     * @param password 用户输入的密码字符串
     */
    fun updatePassword(password: String) {
        if (password.length <= 20) {
            _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
        }
    }

    /**
     * 更新确认密码输入
     *
     * 限制最大长度为 20 位，并清除之前的错误提示。
     *
     * @param confirmPassword 用户输入的确认密码字符串
     */
    fun updateConfirmPassword(confirmPassword: String) {
        if (confirmPassword.length <= 20) {
            _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword, errorMessage = null)
        }
    }

    /**
     * 更新用户协议勾选状态
     *
     * @param isAgreed 用户是否勾选同意服务协议和隐私政策
     */
    fun updateAgreement(isAgreed: Boolean) {
        _uiState.value = _uiState.value.copy(isAgreed = isAgreed)
    }

    /**
     * 发送短信验证码
     *
     * 验证手机号格式后，调用接口发送验证码，并启动 60 秒倒计时。
     * 根据当前状态决定验证码类型：
     * - 正常登录流程：type = "login"
     * - 注册流程：type = "register"
     */
    fun sendCode() {
        val phone = _uiState.value.phone
        if (phone.length != 11) {
            _uiState.value = _uiState.value.copy(errorMessage = "请输入正确的手机号")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // 根据当前模式决定验证码类型：验证码登录用 login，注册用 register
            val type = if (_uiState.value.needsRegistration) "register" else "login"
            val result = authRepository.sendSmsCode(phone, type)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, isCodeSent = true, countdownSeconds = 60)
                startCountdown()
            }.onFailure {
                /**
                 * @param e 登录失败，根据错误类型显示错误信息
                 */
                    e ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    /**
     * 启动验证码倒计时
     *
     * 从 60 秒开始每秒递减，直到归零。
     */
    private fun startCountdown() {
        viewModelScope.launch {
            while (_uiState.value.countdownSeconds > 0) {
                delay(1000)
                _uiState.value = _uiState.value.copy(countdownSeconds = _uiState.value.countdownSeconds - 1)
            }
        }
    }

    /**
     * 主按钮点击事件，根据当前模式分发
     */
    fun onSubmit() {
        when (_uiState.value.loginMode) {
            LoginMode.SMS -> onSmsSubmit()
            LoginMode.PASSWORD -> onPasswordSubmit()
        }
    }

    /**
     * 验证码登录模式提交
     * - 若 needsRegistration=true，走注册流程
     * - 否则走验证码登录流程
     */
    private fun onSmsSubmit() {
        val state = _uiState.value

        // 基础校验
        if (state.phone.length != 11) {
            _uiState.value = state.copy(errorMessage = "请输入正确的手机号")
            return
        }
        if (state.code.length != 6) {
            _uiState.value = state.copy(errorMessage = "请输入6位验证码")
            return
        }
        if (!state.isAgreed) {
            _uiState.value = state.copy(errorMessage = "请阅读并同意服务协议和隐私政策")
            return
        }

        if (state.needsRegistration) {
            // 注册流程校验
            if (state.password.length < 6) {
                _uiState.value = state.copy(errorMessage = "密码至少6位")
                return
            }
            if (state.password != state.confirmPassword) {
                _uiState.value = state.copy(errorMessage = "两次密码输入不一致")
                return
            }
            registerWithSms()
        } else {
            // 验证码登录流程
            smsLogin()
        }
    }

    /**
     * 密码登录模式提交
     */
    private fun onPasswordSubmit() {
        val state = _uiState.value

        // 基础校验
        if (state.phone.length != 11) {
            _uiState.value = state.copy(errorMessage = "请输入正确的手机号")
            return
        }
        if (state.password.length < 6) {
            _uiState.value = state.copy(errorMessage = "请输入密码（至少6位）")
            return
        }
        if (!state.isAgreed) {
            _uiState.value = state.copy(errorMessage = "请阅读并同意服务协议和隐私政策")
            return
        }

        passwordLogin()
    }

    /**
     * 验证码登录
     *
     * 使用手机号+验证码进行登录。成功则更新登录状态，失败则根据错误类型处理：
     * - USER_NOT_FOUND：手机号未注册，切换到注册流程（needsRegistration = true）
     * - 其他错误：显示错误提示
     *
     * @suppress HardwareIds 警告：此处使用 ANDROID_ID 仅用于防止多设备同时登录，不用于用户追踪
     */
    @SuppressLint("HardwareIds")
    private fun smsLogin() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)

            val deviceId = android.provider.Settings.Secure.getString(
                application.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )

            val result = authRepository.smsLogin(state.phone, state.code, deviceId)
            result.onSuccess {
                /**
                 * @param driver 登录成功后返回的司机信息，包含状态字段用于判断后续流程
                 */
                    driver ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    needsCertification = driver.status == "pending" || driver.status == "approved",
                    needsTraining = driver.status == "approved"
                )
            }.onFailure {
                /**
                 * @param e 登录失败的异常对象，用于判断错误类型
                 */
                    e ->
                if (e is UserNotFoundException) {
                    // 手机号未注册，展开注册表单
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        needsRegistration = true,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "登录失败"
                    )
                }
            }
        }
    }

    /**
     * 使用验证码+密码注册新用户
     *
     * 在验证码登录发现用户不存在时调用，完成新用户注册并自动登录。
     *
     * @suppress HardwareIds 警告：此处使用 ANDROID_ID 仅用于防止多设备同时登录，不用于用户追踪
     */
    @SuppressLint("HardwareIds")
    private fun registerWithSms() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)

            val deviceId = android.provider.Settings.Secure.getString(
                application.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )

            val result = authRepository.register(state.phone, state.code, state.password, deviceId)
            result.onSuccess {
                /**
                 * @param driver 注册成功后返回的司机信息
                 */
                    driver ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    needsCertification = driver.status == "pending" || driver.status == "approved",
                    needsTraining = driver.status == "approved"
                )
            }.onFailure {
                /**
                 * @param e 注册失败的异常对象
                 */
                    e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "注册失败"
                )
            }
        }
    }

    /**
     * 密码登录
     *
     * 使用手机号+密码进行登录。后端会自动处理：
     * - 已注册用户：直接登录
     * - 未注册用户：自动注册并登录
     *
     * @suppress HardwareIds 警告：此处使用 ANDROID_ID 仅用于防止多设备同时登录，不用于用户追踪
     */
    @SuppressLint("HardwareIds")
    private fun passwordLogin() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)

            val deviceId = android.provider.Settings.Secure.getString(
                application.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )

            val result = authRepository.login(state.phone, state.password, deviceId)
            result.onSuccess {
                /**
                 * @param driver 登录成功后返回的司机信息
                 */
                    driver ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    needsCertification = driver.status == "pending" || driver.status == "approved",
                    needsTraining = driver.status == "approved"
                )
            }.onFailure {
                /**
                 * @param e 登录失败的异常对象
                 */
                    e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "登录失败"
                )
            }
        }
    }

    /**
     * 清除错误提示
     *
     * 将 errorMessage 重置为 null，通常在用户重新输入时调用。
     */
    @Suppress("unused")
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * 开发者快捷登录 - 直接跳过验证码进入主页
     * 始终可用（开发者后门）
     */
    fun loginAsDeveloper() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // 保存模拟 Token 到 ApiClient（使 API 请求带上认证头）
            val mockAccessToken = "dev_access_token_${System.currentTimeMillis()}"
            val mockRefreshToken = "dev_refresh_token_${System.currentTimeMillis()}"
            apiClient.setTokens(mockAccessToken, mockRefreshToken)

            // 保存 Token 到本地（即使应用重启也能保持登录状态）
            tokenManager.saveTokens(
                accessToken = mockAccessToken,
                refreshToken = mockRefreshToken,
                userId = "dev_driver_001"
            )

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isLoggedIn = true,
                needsCertification = false,
                needsTraining = false
            )
        }
    }
}
