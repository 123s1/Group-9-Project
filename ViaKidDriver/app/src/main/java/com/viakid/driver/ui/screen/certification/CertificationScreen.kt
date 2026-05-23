package com.viakid.driver.ui.screen.certification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * 资质认证页面
 *
 * @param onBack 返回按钮点击回调
 * @param onComplete 认证完成回调
 * @param onSkip 跳过认证回调
 * @param viewModel 认证ViewModel实例
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificationScreen(
    onBack: () -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    viewModel: CertificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 监听完成状态
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("资质认证") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(onClick = onSkip) {
                        Text("跳过", color = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { /* TODO: 帮助 */ }) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "帮助")
                    }
                }
            )
        }
    ) {
        /** @param paddingValues Scaffold的内边距参数 */
            paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // 进度指示器
                item {
                    CertificationProgressIndicator(currentStep = uiState.currentStep)
                }

                // 基础信息卡片
                item {
                    BasicInfoCard(
                        state = uiState.basicInfo,
                        onUpdate = viewModel::updateBasicInfo,
                        onSave = viewModel::saveBasicInfo
                    )
                }

                // 证件上传卡片
                item {
                    CertificateUploadCard(
                        state = uiState.certificates,
                        onUpload = viewModel::uploadCertificate
                    )
                }

                // 背景调查卡片
                item {
                    BackgroundCheckCard(state = uiState.backgroundCheck)
                }

                // 温馨提示
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "温馨提醒：审核通过前您可以先学习培训课程",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }

        // 错误提示
        uiState.errorMessage?.let {
            /** @param error 错误提示信息 */
                error ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = viewModel::clearError) {
                        Text("关闭")
                    }
                }
            ) {
                Text(error)
            }
        }
    }
}

/**
 * 认证进度指示器组件
 *
 * @param currentStep 当前认证步骤索引
 */
@Composable
private fun CertificationProgressIndicator(currentStep: Int) {
    val steps = listOf("基础信息", "证件上传", "背景调查", "完成审核")
    val progress = currentStep.toFloat() / steps.size

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "认证进度",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                steps.forEachIndexed {
                    /**
                     *  @param index 步骤索引
                     * @param step 步骤名称
                     */
                        index, step ->
                    Text(
                        text = step,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (index < currentStep)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 基础信息卡片组件
 *
 * @param state 基础信息状态
 * @param onUpdate 更新基础信息回调
 * @param onSave 保存基础信息回调
 */
@Composable
private fun BasicInfoCard(
    state: BasicInfoState,
    onUpdate: (String, String, String, String, String) -> Unit,
    onSave: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "基础信息",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (state.isCompleted) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isCompleted) {
                Text(
                    text = "姓名、性别、年龄、紧急联系人",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { onUpdate(it, state.gender, state.birthday, state.emergencyContact, state.emergencyPhone) },
                    label = { Text("姓名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 性别选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("male" to "男", "female" to "女").forEach { (value, label) ->
                        FilterChip(
                            selected = state.gender == value,
                            onClick = { onUpdate(state.name, value, state.birthday, state.emergencyContact, state.emergencyPhone) },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.birthday,
                    onValueChange = { onUpdate(state.name, state.gender, it, state.emergencyContact, state.emergencyPhone) },
                    label = { Text("出生日期") },
                    placeholder = { Text("格式：1990-01-01") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.emergencyContact,
                    onValueChange = { onUpdate(state.name, state.gender, state.birthday, it, state.emergencyPhone) },
                    label = { Text("紧急联系人") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.emergencyPhone,
                    onValueChange = { onUpdate(state.name, state.gender, state.birthday, state.emergencyContact, it) },
                    label = { Text("紧急联系电话") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onSave,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("保存")
                    }
                }
            }
        }
    }
}

/**
 * 证件上传卡片组件
 *
 * @param state 证件状态
 * @param onUpload 上传证件回调
 */
@Composable
private fun CertificateUploadCard(
    state: CertificatesState,
    onUpload: (String, String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Badge,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "证件上传",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "${state.completedCount}/${state.totalCount} 已完成",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (state.completedCount == state.totalCount)
                        Color(0xFF4CAF50)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 证件网格
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CertificateItem(
                        title = "身份证正面",
                        icon = Icons.Default.CreditCard,
                        state = state.idCardFront,
                        type = "idCardFront",
                        onUpload = onUpload,
                        modifier = Modifier.weight(1f)
                    )
                    CertificateItem(
                        title = "身份证反面",
                        icon = Icons.Default.CreditCard,
                        state = state.idCardBack,
                        type = "idCardBack",
                        onUpload = onUpload,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CertificateItem(
                        title = "驾驶证",
                        icon = Icons.Default.DirectionsCar,
                        state = state.driverLicense,
                        type = "driverLicense",
                        onUpload = onUpload,
                        modifier = Modifier.weight(1f)
                    )
                    CertificateItem(
                        title = "无犯罪记录",
                        icon = Icons.Default.VerifiedUser,
                        state = state.criminalRecord,
                        type = "criminalRecord",
                        onUpload = onUpload,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CertificateItem(
                        title = "健康证明",
                        icon = Icons.Default.HealthAndSafety,
                        state = state.healthCert,
                        type = "healthCert",
                        onUpload = onUpload,
                        modifier = Modifier.weight(1f)
                    )
                    CertificateItem(
                        title = "行驶证",
                        icon = Icons.Default.CarRental,
                        state = state.vehicleLicense,
                        type = "vehicleLicense",
                        onUpload = onUpload,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * 单个证件项组件
 *
 * @param title 证件标题
 * @param icon 证件图标
 * @param state 证件状态
 * @param type 证件类型标识
 * @param onUpload 上传回调
 * @param modifier 修饰符
 */
@Suppress("unused")
@Composable
private fun CertificateItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    state: CertificateItemState,
    type: String,
    onUpload: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUploading = state.localFilePath != null && type == state.localFilePath

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = when (state.status) {
                    "approved" -> Color(0xFF4CAF50)
                    "rejected" -> Color(0xFFF44336)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            when (state.status) {
                "approved" -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "已上传",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }

                "rejected" -> {
                    Text(
                        text = "已拒绝",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFF44336)
                    )
                }

                else -> {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        TextButton(
                            onClick = { /* TODO: 打开相册或相机 */ },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("上传", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 背景调查卡片组件
 *
 * @param state 背景调查状态
 */
@Composable
private fun BackgroundCheckCard(state: BackgroundCheckState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Policy,
                        contentDescription = null,
                        tint = when (state.status) {
                            "approved" -> Color(0xFF4CAF50)
                            "rejected" -> Color(0xFFF44336)
                            "processing" -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "背景调查",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = when (state.status) {
                        "approved" -> "已通过"
                        "rejected" -> "未通过"
                        "processing" -> "进行中..."
                        else -> "待验证"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = when (state.status) {
                        "approved" -> Color(0xFF4CAF50)
                        "rejected" -> Color(0xFFF44336)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            if (state.status == "processing" || state.status == "pending") {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "系统正在验证您的身份信息，预计${state.estimatedTime ?: "1-3个工作日"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (state.status == "processing") {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { state.progress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "${state.progress}%",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
