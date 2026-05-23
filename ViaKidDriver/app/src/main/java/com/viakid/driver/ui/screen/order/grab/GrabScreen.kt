package com.viakid.driver.ui.screen.order.grab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import com.viakid.driver.data.model.Order
import java.util.Locale

/**
 * 抢单界面
 *
 * 显示新订单列表，支持排序、倒计时抢单、拒绝订单等功能。
 * 包含主订单卡片和附近其他订单列表，提供抢单确认和拒绝确认弹窗。
 *
 * @param onNavigateToOrderDetail 跳转到订单详情界面的回调函数，传递订单 ID
 * @param viewModel 抢单视图模型，管理抢单状态和业务逻辑，默认通过 Hilt 注入
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrabScreen(
    onNavigateToOrderDetail: (String) -> Unit,
    viewModel: GrabViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 抢单确认弹窗
    if (uiState.showConfirmDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissConfirmDialog,
            title = { Text("确认抢单") },
            text = {
                uiState.mainOrder?.let {
                    /** @param order 当前主订单对象 */
                        order ->
                    Text("确认抢此订单？\n${order.schoolName}\n预计收入 ¥${"%.2f".format(order.estimatedIncome)}")
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmGrab) { Text("确认抢单") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissConfirmDialog) { Text("取消") }
            }
        )
    }

    // 拒绝确认弹窗
    if (uiState.showRejectDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissRejectDialog,
            title = { Text("拒绝订单") },
            text = { Text("确定拒绝此订单吗？") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmReject) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissRejectDialog) { Text("取消") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新订单") },
                actions = {
                    IconButton(onClick = { /* TODO: 刷新 */ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                    IconButton(onClick = { /* TODO: 筛选 */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "筛选")
                    }
                }
            )
        }
    ) {
        /**
         * @param paddingValues Scaffold 的内边距值，用于适配系统栏和顶部栏
         */
            paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.grabState) {
                GrabState.SUCCESS -> GrabSuccessView()
                GrabState.TIMEOUT -> GrabTimeoutView(onNext = viewModel::onTimeoutNext)
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 排序选择
                        item {
                            SortChips(
                                currentMode = uiState.sortMode,
                                onModeChanged = viewModel::onSortModeChanged
                            )
                        }

                        // 智能排序标签
                        item {
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "\u26A1 距离最近 \u00B7 收入最高",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        // 倒计时
                        item {
                            CountdownCard(seconds = uiState.countdownSeconds)
                        }

                        // 主订单卡片
                        uiState.mainOrder?.let {
                            /**
                             * @param order 主订单对象，包含订单详细信息如学校名称、距离、收入等
                             */
                                order ->
                            item {
                                MainOrderCard(
                                    order = order,
                                    countdown = uiState.countdownSeconds,
                                    onGrabClick = viewModel::onGrabClick,
                                    onRejectClick = viewModel::onRejectClick
                                )
                            }
                        }

                        // 附近其他订单
                        if (uiState.nearbyOrders.isNotEmpty()) {
                            item {
                                Text(
                                    text = "附近其他订单 (${uiState.nearbyOrders.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            items(uiState.nearbyOrders) {
                                /**
                                 * @param order 附近订单对象，用于在列表中展示简要信息
                                 */
                                    order ->
                                NearbyOrderCard(
                                    order = order,
                                    onGrab = { viewModel.grabNearbyOrder(order) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 排序选择组件
 *
 * 显示多个排序选项的筛选芯片，用户可选择按距离、收入或时间排序。
 *
 * @param currentMode 当前选中的排序模式
 * @param onModeChanged 排序模式改变时的回调函数，传递新的排序模式
 */
@Composable
private fun SortChips(
    currentMode: GrabSortMode,
    onModeChanged: (GrabSortMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GrabSortMode.entries.forEach {
            /** @param mode 排序模式对象，包含排序模式名称和图标 */
                mode ->
            FilterChip(
                selected = currentMode == mode,
                onClick = { onModeChanged(mode) },
                label = { Text(mode.label, style = MaterialTheme.typography.labelMedium) },
                leadingIcon = {
                    when (mode) {
                        GrabSortMode.DISTANCE -> Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        GrabSortMode.INCOME -> Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(16.dp))
                        GrabSortMode.TIME -> Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            )
        }
    }
}

/**
 * 倒计时卡片组件
 *
 * 显示剩余抢单时间的倒计时，时间紧急时使用红色警示样式。
 *
 * @param seconds 剩余时间（秒），用于计算和显示倒计时
 */
@Composable
private fun CountdownCard(seconds: Int) {
    val isUrgent = seconds <= 10
    val color = if (isUrgent) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.secondary

    val text = String.format(Locale.US, "\u23F1 剩余抢单时间: %02d:%02d", seconds / 60, seconds % 60)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (isUrgent) Color(0xFFFFEBEE) else Color(0xFFFFF3E0))
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 主订单卡片组件
 *
 * 显示主要订单的详细信息，包括学校、距离、孩子信息、接送点、时间和预计收入。
 * 提供抢单和拒绝操作按钮。
 *
 * @param order 订单对象，包含所有订单详细信息
 * @param countdown 剩余倒计时秒数，用于控制抢单按钮的启用状态
 * @param onGrabClick 点击抢单按钮时的回调函数
 * @param onRejectClick 点击拒绝按钮时的回调函数
 */
@Composable
private fun MainOrderCard(
    order: Order,
    countdown: Int,
    onGrabClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 学校和距离
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = order.schoolName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "距离您约 ${"%.1f".format(order.distance)}km",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // 孩子信息
            order.children.forEach {
                /**
                 * @param child 孩子信息对象，包含姓名、年龄和年级等信息
                 */
                    child ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${child.name} (${child.age}岁,${child.grade})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 接送点
            InfoRow(icon = Icons.Default.LocationOn, text = "接取点：${order.pickupLocation.address}")
            Spacer(modifier = Modifier.height(4.dp))
            InfoRow(icon = Icons.Default.Place, text = "送达点：${order.dropOffLocation.address}")

            Spacer(modifier = Modifier.height(12.dp))

            // 时间和类型
            InfoRow(icon = Icons.Default.Schedule, text = "接取时间：${order.pickupTime}")
            Spacer(modifier = Modifier.height(4.dp))
            InfoRow(
                icon = Icons.Default.LocalOffer,
                text = "订单类型：${order.type.label}"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // 预计收入（高亮）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "预计收入",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "\u00A5${"%.2f".format(order.estimatedIncome)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 操作按钮组
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onRejectClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("拒绝")
                }

                Button(
                    onClick = onGrabClick,
                    modifier = Modifier.weight(2f),
                    enabled = countdown > 0
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("抢单")
                }
            }
        }
    }
}

/**
 * 附近订单卡片组件
 *
 * 显示附近其他订单的简要信息，包括学校名称、距离、收入和接取时间。
 * 提供快速抢单按钮。
 *
 * @param order 订单对象，包含订单基本信息
 * @param onGrab 点击抢单按钮时的回调函数
 */
@Composable
private fun NearbyOrderCard(
    order: Order,
    onGrab: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = order.schoolName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${"%.1f".format(order.distance)}km \u00B7 \u00A5${"%.2f".format(order.estimatedIncome)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${order.pickupTime}接取",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledTonalButton(onClick = onGrab) {
                Text("抢单")
            }
        }
    }
}

/**
 * 信息行组件
 *
 * 显示带图标的文本信息行，用于展示订单的各项属性。
 *
 * @param icon 左侧显示的图标
 * @param text 右侧显示的文本内容
 */
@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 抢单成功视图
 *
 * 显示抢单成功的提示信息和跳转提示。
 */
@Composable
private fun GrabSuccessView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "抢单成功！",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "正在跳转订单详情...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 抢单超时视图
 *
 * 显示订单已被抢走的提示信息，并提供查看下一个订单的按钮。
 *
 * @param onNext 点击查看下一个订单按钮时的回调函数
 */
@Composable
private fun GrabTimeoutView(onNext: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "订单已被抢走",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "手慢了一步，看看下一个吧",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onNext) {
                Text("查看下一个订单")
            }
        }
    }
}
