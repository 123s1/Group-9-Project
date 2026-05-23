package com.viakid.driver.ui.screen.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * 登录页面 - 支持验证码登录和密码登录两种方式
 *
 * @param onLoginSuccess 登录成功回调，参数表示是否需要认证和培训
 * @param onNavigateToCertification 导航到认证页面的回调
 * @param onNavigateToTraining 导航到培训页面的回调
 * @param viewModel 认证视图模型，默认为 Hilt 注入的实例
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (needsCertification: Boolean, needsTraining: Boolean) -> Unit,
    onNavigateToCertification: () -> Unit,
    onNavigateToTraining: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // 监听登录成功
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            if (uiState.needsCertification) {
                onNavigateToCertification()
            } else if (uiState.needsTraining) {
                onNavigateToTraining()
            } else {
                onLoginSuccess(false, false)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Logo
        Icon(
            Icons.Default.DirectionsBus,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 标题
        Text(
            text = "安心接送",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "送娃送娃每一天",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Tab 切换栏
        val selectedTabIndex = when (uiState.loginMode) {
            LoginMode.SMS -> 0
            LoginMode.PASSWORD -> 1
        }

        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { viewModel.updateLoginMode(LoginMode.SMS) },
                text = { Text("验证码登录") }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { viewModel.updateLoginMode(LoginMode.PASSWORD) },
                text = { Text("密码登录") }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==================== 验证码登录 Tab ====================
        if (uiState.loginMode == LoginMode.SMS) {
            SmsLoginContent(
                uiState = uiState,
                onPhoneChange = viewModel::updatePhone,
                onCodeChange = viewModel::updateCode,
                onPasswordChange = viewModel::updatePassword,
                onConfirmPasswordChange = viewModel::updateConfirmPassword,
                onSendCode = viewModel::sendCode,
                onSubmit = viewModel::onSubmit,
                focusManager = focusManager
            )
        }

        // ==================== 密码登录 Tab ====================
        if (uiState.loginMode == LoginMode.PASSWORD) {
            PasswordLoginContent(
                uiState = uiState,
                onPhoneChange = viewModel::updatePhone,
                onPasswordChange = viewModel::updatePassword,
                onSubmit = viewModel::onSubmit,
                focusManager = focusManager
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 协议勾选
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = uiState.isAgreed,
                onCheckedChange = viewModel::updateAgreement
            )
            Text(
                text = "我已阅读并同意",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = { /* TODO: 打开服务协议 */ }) {
                Text("《服务协议》", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = "和",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = { /* TODO: 打开隐私政策 */ }) {
                Text("《隐私政策》", style = MaterialTheme.typography.bodySmall)
            }
        }

        // 错误提示
        uiState.errorMessage?.let {
            /**
             * @param error 错误信息文本，显示在界面上提醒用户
             */
                error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 主操作按钮
        Button(
            onClick = viewModel::onSubmit,
            enabled = !uiState.isLoading &&
                    uiState.isAgreed &&
                    when (uiState.loginMode) {
                        LoginMode.SMS -> uiState.phone.length == 11 &&
                                uiState.code.length == 6 &&
                                if (uiState.needsRegistration) {
                                    uiState.password.length >= 6 &&
                                            uiState.confirmPassword.length >= 6
                                } else true

                        LoginMode.PASSWORD -> uiState.phone.length == 11 &&
                                uiState.password.length >= 6
                    },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = when (uiState.loginMode) {
                        LoginMode.SMS -> if (uiState.needsRegistration) "注册" else "登录"
                        LoginMode.PASSWORD -> "登录"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 帮助链接
        TextButton(onClick = { /* TODO: 联系客服 */ }) {
            Icon(
                Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("遇到问题？联系客服")
        }

        // 开发者快捷入口
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = viewModel::loginAsDeveloper,
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.DeveloperMode,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("开发者快捷登录（跳过验证）")
        }
    }
}

/**
 * 验证码登录表单内容
 *
 * @param uiState 认证界面状态，包含所有输入字段和状态信息
 * @param onPhoneChange 手机号变化回调，用于更新手机号输入值
 * @param onCodeChange 验证码变化回调，用于更新验证码输入值
 * @param onPasswordChange 密码变化回调，用于更新密码输入值（注册时使用）
 * @param onConfirmPasswordChange 确认密码变化回调，用于更新确认密码输入值（注册时使用）
 * @param onSendCode 发送验证码回调，触发获取短信验证码操作
 * @param onSubmit 提交回调，触发登录或注册操作
 * @param focusManager 焦点管理器，用于控制输入框焦点移动和清除
 */
@Composable
private fun SmsLoginContent(
    uiState: AuthUiState,
    onPhoneChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSendCode: () -> Unit,
    onSubmit: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    // 手机号输入
    OutlinedTextField(
        value = uiState.phone,
        onValueChange = onPhoneChange,
        label = { Text("手机号") },
        placeholder = { Text("请输入手机号") },
        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    // 验证码输入
    OutlinedTextField(
        value = uiState.code,
        onValueChange = onCodeChange,
        label = { Text("验证码") },
        placeholder = { Text("请输入验证码") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
            if (uiState.countdownSeconds > 0) {
                Text(
                    text = "${uiState.countdownSeconds}s",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                TextButton(
                    onClick = onSendCode,
                    enabled = !uiState.isLoading && uiState.phone.length == 11
                ) {
                    Text("获取验证码")
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = if (uiState.needsRegistration) ImeAction.Next else ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
            onDone = {
                focusManager.clearFocus()
                onSubmit()
            }
        ),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    // 动态展开的注册字段
    AnimatedVisibility(
        visible = uiState.needsRegistration,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Column {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "该手机号尚未注册，请设置密码完成注册",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 密码输入
            var passwordVisible by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = { Text("密码") },
                placeholder = { Text("请设置密码（至少6位）") },
                leadingIcon = { Icon(Icons.Default.Password, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 确认密码输入
            var confirmPasswordVisible by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text("确认密码") },
                placeholder = { Text("请再次输入密码") },
                leadingIcon = { Icon(Icons.Default.Password, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onSubmit()
                    }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 密码登录表单内容
 *
 * @param uiState 认证界面状态，包含所有输入字段和状态信息
 * @param onPhoneChange 手机号变化回调，用于更新手机号输入值
 * @param onPasswordChange 密码变化回调，用于更新密码输入值
 * @param onSubmit 提交回调，触发登录操作
 * @param focusManager 焦点管理器，用于控制输入框焦点移动和清除
 */
@Composable
private fun PasswordLoginContent(
    uiState: AuthUiState,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    // 手机号输入
    OutlinedTextField(
        value = uiState.phone,
        onValueChange = onPhoneChange,
        label = { Text("手机号") },
        placeholder = { Text("请输入手机号") },
        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    // 密码输入
    var passwordVisible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = uiState.password,
        onValueChange = onPasswordChange,
        label = { Text("密码") },
        placeholder = { Text("请输入密码") },
        leadingIcon = { Icon(Icons.Default.Password, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null
                )
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                onSubmit()
            }
        ),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "未注册的手机号将自动创建账号",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
