package com.viakid.driver.ui.screen.training

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.viakid.driver.data.remote.CourseDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
        /**
         * 培训中心主页面，展示学习进度、课程列表和考试入口。
         *
         * @param onNavigateToCourse 导航到课程详情页面的回调，传入课程ID。
         * @param onNavigateToExam 导航到结业考试页面的回调。
         * @param onNavigateToCertificate 导航到我的证书页面的回调。
         * @param onBack 返回按钮点击回调，用于返回上一级页面。
         * @param viewModel 培训页面的ViewModel，提供数据和业务逻辑。
         */
fun TrainingScreen(
    onNavigateToCourse: (String) -> Unit,
    onNavigateToExam: () -> Unit,
    onNavigateToCertificate: () -> Unit,
    onBack: () -> Unit,
    viewModel: TrainingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("培训中心") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(onClick = onNavigateToCertificate) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("我的证书")
                    }
                }
            )
        }
    ) {
        /** @param paddingValues 填充的Padding */
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // 学习进度卡片
                item {
                    TrainingProgressCard(
                        completedCount = uiState.completedCount,
                        totalCount = uiState.totalCount,
                        examInfo = uiState.examInfo
                    )
                }

                // 必修课程标题
                item {
                    Text(
                        text = "必修课程",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // 课程列表
                items(uiState.courses) {
                    /** @param course 课程数据对象，包含课程的详细信息。 */
                        course ->
                    CourseItem(
                        course = course,
                        onClick = { onNavigateToCourse(course.id) }
                    )
                }

                // 参加考试按钮
                item {
                    val canTakeExam = uiState.canTakeExam && (uiState.examInfo?.canTake != false)
                    val lastAttempt = uiState.examInfo?.lastAttempt

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (canTakeExam)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (lastAttempt != null) {
                                Text(
                                    text = "上次考试成绩：${lastAttempt.score}分",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (lastAttempt.passed)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Button(
                                onClick = onNavigateToExam,
                                enabled = canTakeExam,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Quiz,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (uiState.isTrainingCompleted)
                                        "参加结业考试 (≥${uiState.examInfo?.passingScore ?: 80}分通过)"
                                    else
                                        "完成全部课程后可参加考试"
                                )
                            }

                            if (!uiState.isTrainingCompleted) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "还需完成 ${uiState.totalCount - uiState.completedCount} 门课程",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
/**
 * 培训进度卡片，显示必修课程的完成进度和考试资格信息。
 *
 * @param completedCount 已完成的课程数量。
 * @param totalCount 必修课程的总数量。
 * @param examInfo 考试信息，包括是否可以参加考试、上次考试成绩等。
 */
private fun TrainingProgressCard(
    /**
     * @param completedCount 已完成的课程数量。
     */
    completedCount: Int,
    /**
     * @param totalCount 必修课程的总数量。
     */
    totalCount: Int,
    /**
     * @param examInfo 考试信息，包括是否可以参加考试、上次考试成绩等。
     */
    examInfo: com.viakid.driver.data.remote.ExamInfoDto?
) {
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "必修课程学习进度",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "完成 $completedCount/$totalCount 课程",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            if (!(examInfo?.canTake ?: return@Column)) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "完成全部必修课程后方可参加结业考试",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
/**
 * 课程列表项，展示课程标题、时长、状态等信息。
 *
 * @param course 课程数据对象，包含课程的详细信息。
 * @param onClick 课程项点击回调，用于导航到课程详情页面。
 */
private fun CourseItem(
    /**
     * @param course 课程数据对象，包含课程的详细信息。
     */
    course: CourseDto,
    /**
     * @param onClick 课程项点击回调，用于导航到课程详情页面。
     */
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 状态图标
            Icon(
                imageVector = when (course.status) {
                    "completed" -> Icons.Default.CheckCircle
                    "in_progress" -> Icons.Default.PlayCircle
                    else -> Icons.Default.PlayCircleOutline
                },
                contentDescription = null,
                tint = when (course.status) {
                    "completed" -> MaterialTheme.colorScheme.primary
                    "in_progress" -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.outline
                },
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = course.duration,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 状态标签
            Surface(
                shape = MaterialTheme.shapes.small,
                color = when (course.status) {
                    "completed" -> MaterialTheme.colorScheme.primaryContainer
                    "in_progress" -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Text(
                    text = when (course.status) {
                        "completed" -> "已完成"
                        "in_progress" -> "进行中"
                        else -> "未开始"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when (course.status) {
                        "completed" -> MaterialTheme.colorScheme.onPrimaryContainer
                        "in_progress" -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
