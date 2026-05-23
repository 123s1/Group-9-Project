package com.viakid.driver.ui.screen.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 个人中心页面
 *
 * 显示用户基本信息、资质认证状态、培训进度等功能入口，
 * 并提供设置、帮助、关于等选项以及退出登录功能。
 *
 * @param onLogout 退出登录的回调函数
 */
@Suppress("AssignedValueIsNeverRead")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = {
                showLogoutDialog = false
            },
            title = { Text("退出登录") },
            text = { Text("确定要退出当前账号吗？") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) { Text("退出") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("取消") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("我的") })
        }
    ) {
        /** @param paddingValues  */
            paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 用户信息卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "接送员",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "资质认证中",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 功能列表
            ProfileMenuItem(
                icon = Icons.Default.VerifiedUser,
                title = "资质认证",
                subtitle = "查看认证状态",
                onClick = { /* TODO: 导航到资质认证 */ }
            )

            ProfileMenuItem(
                icon = Icons.Default.School,
                title = "培训状态",
                subtitle = "查看培训进度",
                onClick = { /* TODO: 导航到培训 */ }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            ProfileMenuItem(
                icon = Icons.Default.Settings,
                title = "设置",
                subtitle = "应用设置与偏好",
                onClick = { /* TODO: 导航到设置 */ }
            )

            ProfileMenuItem(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                title = "帮助与反馈",
                subtitle = "常见问题与意见反馈",
                onClick = { /* TODO: 导航到帮助 */ }
            )

            ProfileMenuItem(
                icon = Icons.Default.Info,
                title = "关于",
                subtitle = "版本信息",
                onClick = { /* TODO: 导航到关于 */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 退出登录按钮
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("退出登录")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 个人中心菜单项组件
 *
 * 显示带有图标、标题、副标题和右箭头的可点击菜单项。
 *
 * @param icon 菜单项图标
 * @param title 菜单项标题
 * @param subtitle 菜单项副标题，显示补充说明信息
 * @param onClick 点击菜单项的回调函数
 */
@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
