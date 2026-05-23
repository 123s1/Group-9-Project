package com.viakid.driver.ui.screen.exam

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Locale

/**
 * 结业考试界面
 *
 * 显示考试题目、选项、计时器，并处理答题和交卷逻辑。
 * 支持单选题和多选题，实时显示答题进度和剩余时间。
 *
 * @param onBack 返回上一界面的回调函数
 * @param onComplete 考试完成时的回调函数，传递是否通过和分数信息
 * @param viewModel 考试视图模型，管理考试状态和业务逻辑，默认通过 Hilt 注入
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamScreen(
    onBack: () -> Unit,
    onComplete: (passed: Boolean, score: Int) -> Unit,
    viewModel: ExamViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 监听考试完成
    LaunchedEffect(uiState.isExamFinished) {
        if (uiState.isExamFinished && uiState.examResult != null) {
            onComplete((uiState.examResult ?: return@LaunchedEffect).passed, (uiState.examResult ?: return@LaunchedEffect).score)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("结业考试") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 计时器
                    if (!uiState.isExamFinished) {
                        val minutes = uiState.timeRemainingSeconds / 60
                        val seconds = uiState.timeRemainingSeconds % 60
                        val isUrgent = uiState.timeRemainingSeconds <= 300 // 5分钟

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(
                                    if (isUrgent) MaterialTheme.colorScheme.errorContainer
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (isUrgent) MaterialTheme.colorScheme.onErrorContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isUrgent) MaterialTheme.colorScheme.onErrorContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    ) {
        /**
         * @param paddingValues Scaffold 的内边距值，用于适配系统栏和顶部栏
         */
            paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.examResult != null -> {
                // 考试成绩页面
                ExamResultView(
                    result = uiState.examResult ?: return@Scaffold,
                    onBack = onBack
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // 题目导航
                    val answeredCount = uiState.answers.size
                    val totalCount = uiState.questions.size

                    Text(
                        text = "第 ${uiState.currentQuestionIndex + 1}/$totalCount 题",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(16.dp)
                    )

                    // 进度指示器
                    LinearProgressIndicator(
                        progress = { (uiState.currentQuestionIndex + 1).toFloat() / totalCount },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )

                    Text(
                        text = "已答 $answeredCount 题",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // 题目内容
                    val currentQuestion = uiState.questions.getOrNull(uiState.currentQuestionIndex)

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        currentQuestion?.let {
                            /**
                             * @param question 当前显示的问题对象，包含问题内容、类型和选项列表
                             */
                                question ->
                            // 题目
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row {
                                        Text(
                                            text = if (question.type == "multiple") "[多选]" else "[单选]",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = question.content,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 选项
                            question.options.forEach {
                                /**
                                 * @param option 当前遍历的选项对象，包含选项标识和内容
                                 */
                                    option ->
                                val isSelected = uiState.answers[question.id]?.contains(option.key) == true

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { viewModel.selectAnswer(question.id, option.key) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surface
                                    ),
                                    border = if (isSelected)
                                        CardDefaults.outlinedCardBorder()
                                    else null
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { viewModel.selectAnswer(question.id, option.key) }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${option.key}. ${option.content}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    // 底部操作按钮
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.previousQuestion() },
                            enabled = uiState.currentQuestionIndex > 0,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.ChevronLeft,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("上一题")
                        }

                        OutlinedButton(
                            onClick = { viewModel.nextQuestion() },
                            enabled = uiState.currentQuestionIndex < uiState.questions.size - 1,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("下一题")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // 交卷按钮
                    Button(
                        onClick = { viewModel.submitExam() },
                        enabled = !uiState.isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("交卷并查看成绩")
                        }
                    }
                }
            }
        }
    }
}

/**
 * 考试成绩展示界面
 *
 * 显示考试结果，包括是否通过、分数、证书编号和有效期等信息。
 * 根据通过状态显示不同的提示信息和颜色。
 *
 * @param result 考试结果数据对象，包含分数、是否通过、证书信息等
 * @param onBack 返回培训中心的回调函数
 */
@Composable
private fun ExamResultView(
    result: com.viakid.driver.data.remote.ExamResultDto,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (result.passed) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = if (result.passed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (result.passed) "考试通过！" else "未通过",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (result.passed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "您的成绩：${result.score}分",
            style = MaterialTheme.typography.titleLarge
        )

        if (result.passed) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "合格标准：${result.certificateNo ?: ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            result.validUntil?.let {
                Text(
                    text = "有效期至：$it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "很遗憾，您的成绩未达到80分合格线",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "请重新学习课程后再次参加考试",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("返回培训中心")
        }
    }
}
