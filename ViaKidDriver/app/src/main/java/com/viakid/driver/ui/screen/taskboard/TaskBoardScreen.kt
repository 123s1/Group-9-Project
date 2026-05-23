package com.viakid.driver.ui.screen.taskboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.viakid.driver.data.model.Order
import com.viakid.driver.data.model.OrderStatus
import com.viakid.driver.data.model.TaskOverview
import java.text.SimpleDateFormat
import java.util.*

/**
 * 任务看板页面
 *
 * 显示司机的在线状态、今日任务概览、收入信息，以及按日期和状态筛选的订单列表。
 * 支持切换在线/离线状态，查看不同日期的订单，并按状态筛选订单。
 *
 * @param onNavigateToOrderDetail 导航到订单详情的回调函数，参数为订单 ID
 * @param onNavigateToCalendar 导航到日历页面的回调函数
 * @param viewModel 任务看板 ViewModel，负责数据加载和业务逻辑
 */
@Suppress("AssignedValueIsNeverRead")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskBoardScreen(
    onNavigateToOrderDetail: (String) -> Unit,
    onNavigateToCalendar: () -> Unit,
    viewModel: TaskBoardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(if (isOnline) "确定切换为离线状态？" else "确定切换为在线状态？") },
            text = { Text(if (isOnline) "离线后将不再接收订单推送" else "上线后开始接收订单") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.toggleOnlineStatus()
                    showConfirmDialog = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("取消") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("安心接送") },
                actions = {
                    IconButton(onClick = { /* TODO: 打开通知 */ }) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "通知")
                    }
                    IconButton(onClick = onNavigateToCalendar) {
                        Icon(Icons.Default.DateRange, contentDescription = "日历")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 在线状态切换
            item {
                OnlineStatusSwitch(
                    isOnline = isOnline,
                    onToggle = { showConfirmDialog = true }
                )
            }

            // 今日任务概览
            item {
                TaskOverviewCard(overview = uiState.taskOverview)
            }

            // 今日收入
            item {
                TodayIncomeCard(income = uiState.taskOverview.todayIncome)
            }

            // 日期选择
            item {
                DateSelector(
                    selectedDate = uiState.selectedDate,
                    onDateSelected = viewModel::selectDate
                )
            }

            // 状态筛选
            item {
                StatusFilter(
                    selectedStatus = uiState.selectedStatus,
                    orders = uiState.orders,
                    onStatusSelected = viewModel::selectStatus
                )
            }

            if (uiState.isLoading && uiState.orders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.filteredOrders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.AutoMirrored.Filled.Assignment,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outlineVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "暂无待处理订单",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // 按状态分组
                val groupedOrders = uiState.filteredOrders.groupBy { it.status }

                groupedOrders.forEach { (status, orders) ->
                    item {
                        Text(
                            text = "${status.label} (${orders.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(orders) { order ->
                        TaskOrderCard(
                            order = order,
                            onClick = { onNavigateToOrderDetail(order.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 在线状态切换卡片
 *
 * 显示当前在线/离线状态，点击可切换状态。
 *
 * @param isOnline 是否在线
 * @param onToggle 切换在线状态的回调函数
 */
@Composable
private fun OnlineStatusSwitch(
    isOnline: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isOnline) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isOnline) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isOnline) "在线 - 接收订单中" else "离线 - 不接收订单",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 今日任务概览卡片
 *
 * 显示今日待出发、进行中、已完成的订单数量统计。
 *
 * @param overview 任务概览数据，包含各状态订单数量和今日收入
 */
@Composable
private fun TaskOverviewCard(overview: TaskOverview) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "今日任务概览",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TaskCountItem("待出发", overview.pendingCount, MaterialTheme.colorScheme.secondary)
                TaskCountItem("进行中", overview.inProgressCount, MaterialTheme.colorScheme.primary)
                TaskCountItem("已完成", overview.completedCount, MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

/**
 * 任务计数项组件
 *
 * 显示单个状态的任务数量和标签。
 *
 * @param label 状态标签，如“待出发”、“进行中”等
 * @param count 该状态的任务数量
 * @param color 数字显示颜色
 */
@Composable
private fun TaskCountItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * 今日收入卡片
 *
 * 显示今日的总收入金额。
 *
 * @param income 今日收入金额
 */
@Composable
private fun TodayIncomeCard(income: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Wallet, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.width(8.dp))
                Text("今日收入", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Text(
                "¥%.2f".format(income),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * 日期选择器组件
 *
 * 显示当前选中的日期，并提供前后切换日期的按钮。
 *
 * @param selectedDate 当前选中的日期，格式为 "yyyy-MM-dd"
 * @param onDateSelected 选择新日期的回调函数
 */
@Composable
private fun DateSelector(
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
    val today = dateFormat.format(Date())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            val date = dateFormat.parse(selectedDate)
            val calendar = Calendar.getInstance().apply { time = date ?: Date() }
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            onDateSelected(dateFormat.format(calendar.time))
        }) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "前一天")
        }

        Text(
            text = if (selectedDate == today) "今天 ${displayFormat.format(dateFormat.parse(selectedDate) ?: Date())}"
            else displayFormat.format(dateFormat.parse(selectedDate) ?: Date()),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        IconButton(onClick = {
            val date = dateFormat.parse(selectedDate)
            val calendar = Calendar.getInstance().apply { time = date ?: Date() }
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            onDateSelected(dateFormat.format(calendar.time))
        }) {
            Icon(Icons.Default.ChevronRight, contentDescription = "后一天")
        }
    }
}

/**
 * 状态筛选器组件
 *
 * 显示所有订单状态的筛选按钮，包括“全部”和有订单的状态。
 *
 * @param selectedStatus 当前选中的状态，null 表示全部
 * @param orders 所有订单列表，用于统计各状态数量
 * @param onStatusSelected 选择状态的回调函数
 */
@Composable
private fun StatusFilter(
    selectedStatus: OrderStatus?,
    orders: List<Order>,
    onStatusSelected: (OrderStatus?) -> Unit
) {
    val statusCounts = orders.groupBy { it.status }.mapValues { it.value.size }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatus == null,
                    onClick = { onStatusSelected(null) },
                    label = { Text("全部 (${orders.size})") }
                )

                OrderStatus.entries.take(4).forEach { status ->
                    val count = statusCounts[status] ?: 0
                    if (count > 0) {
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { onStatusSelected(status) },
                            label = { Text("${status.label} ($count)") }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 任务订单卡片组件
 *
 * 显示订单的简要信息，包括学校、孩子信息、接送地点和时间。
 *
 * @param order 订单对象
 * @param onClick 点击卡片的回调函数，通常用于导航到订单详情
 */
@Composable
private fun TaskOrderCard(
    order: Order,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = order.schoolName.ifEmpty { "学校" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                StatusChip(status = order.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 孩子信息
            order.children.forEach { child ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${child.name} (${child.age}岁, ${child.grade})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (child.allergies.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⚠️${child.allergies}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 接送信息
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = order.pickupLocation.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = order.pickupTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onClick) {
                    Text("查看详情")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * 状态标签芯片组件
 *
 * 根据不同订单状态显示不同颜色的状态标签。
 *
 * @param status 订单状态
 */
@Composable
private fun StatusChip(status: OrderStatus) {
    val (bgColor, textColor) = when (status) {
        OrderStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        OrderStatus.ASSIGNED -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        OrderStatus.DEPARTED, OrderStatus.ARRIVED, OrderStatus.PICKED_UP, OrderStatus.IN_TRANSIT ->
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer

        OrderStatus.DELIVERED -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        OrderStatus.COMPLETED -> Color(0xFFC8E6C9) to Color(0xFF2E7D32)
        OrderStatus.CANCELLED -> Color(0xFFE0E0E0) to Color(0xFF757575)
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = bgColor
    ) {
        Text(
            text = status.label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}
