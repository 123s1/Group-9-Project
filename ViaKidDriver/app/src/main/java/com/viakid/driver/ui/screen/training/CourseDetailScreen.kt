package com.viakid.driver.ui.screen.training

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Suppress("unused")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
        /**
         * 课程详情页面，展示视频播放、课程讲义和导航控制。
         *
         * @param courseId 课程ID，用于加载对应的课程内容。
         * @param onBack 返回按钮点击回调，用于返回上一级页面。
         * @param onPrevious 上一节按钮点击回调，用于跳转到上一节课。
         * @param onNext 下一节按钮点击回调，用于跳转到下一节课。
         * @param onComplete 完成按钮点击回调，用于标记当前课程为已完成状态。
         */
fun CourseDetailScreen(
    courseId: String,
    onBack: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onComplete: () -> Unit
) {
    // TODO: 加载课程详情
    val courseTitle = remember { "儿童安全保护规范" } // 临时

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(courseTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) {
        /** @param paddingValues 滚动区域 */
            paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 视频播放区域
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // TODO: 集成 ExoPlayer 视频播放器
                    Icon(
                        Icons.Default.PlayCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 课程讲义
            Text(
                text = "课程讲义",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. 儿童乘车安全须知",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• 上车前检查车辆安全性",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• 确认儿童安全座椅状态",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• 系好安全带教程",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 上一节/下一节按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onPrevious,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("上一节")
                }

                OutlinedButton(
                    onClick = onNext,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("下一节")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 标记完成按钮
            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("标记为已完成")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
