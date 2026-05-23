package com.viakid.driver.ui.screen.orderdetail

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.viakid.driver.data.model.Order
import com.viakid.driver.data.model.OrderStatus
import androidx.core.net.toUri

/**
 * 订单详情页面
 *
 * 显示订单的完整信息，包括订单状态、孩子信息、家长信息、接送路线、
 * 家长留言、收入信息，并提供相应的操作按钮。
 *
 * @param onBack 返回上一页面的回调函数
 * @param viewModel 订单详情 ViewModel，负责数据加载和业务逻辑
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    onBack: () -> Unit,
    viewModel: OrderDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.acceptSuccess) {
        if (uiState.acceptSuccess) {
            snackbarHostState.showSnackbar("订单接受成功")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("订单详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) {
        /** @param paddingValues 用于控制内容布局 */
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
            uiState.order?.let {
                /** @param order 订单对象 */
                    order ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 订单状态卡片
                    OrderStatusCard(order = order)

                    // 孩子信息
                    ChildInfoCard(children = order.children)

                    // 家长信息
                    ParentInfoCard(
                        parent = order.parent,
                        onCall = { /* 拨打家长电话 */ }
                    )

                    // 接送路线
                    RouteCard(
                        order = order,
                        onNavigate = {
                            // 打开地图导航
                            val uri =
                                ("geo:${order.pickupLocation.latitude}," +
                                        "${order.pickupLocation.longitude}?q=${order.pickupLocation.latitude}," +
                                        "${order.pickupLocation.longitude}(${order.pickupLocation.name})").toUri()
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        }
                    )

                    // 家长留言
                    if (order.specialRequirements.isNotBlank()) {
                        RemarksCard(remarks = order.specialRequirements)
                    }

                    // 收入信息
                    IncomeCard(order = order)

                    // 操作按钮
                    ActionButtons(
                        order = order,
                        onAccept = viewModel::acceptOrder,
                        onReject = { viewModel.rejectOrder("too_far") },
                        onUpdateStatus = viewModel::updateStatus
                    )
                }
            }
        }
    }
}

/**
 * 订单状态卡片
 *
 * 显示订单的基本状态信息，包括订单编号和订单类型。
 *
 * @param order 订单对象
 */
@Composable
private fun OrderStatusCard(order: Order) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "订单状态",
                    style = MaterialTheme.typography.titleSmall
                )
                StatusChip(status = order.status)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            InfoRow(label = "订单编号", value = order.orderNo)
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(label = "订单类型", value = order.type.label)
        }
    }
}

/**
 * 孩子信息卡片
 *
 * 显示订单中所有孩子的详细信息，包括姓名、年龄、学校、班级，
 * 以及过敏信息和特殊备注。
 *
 * @param children 孩子列表
 */
@Composable
private fun ChildInfoCard(children: List<com.viakid.driver.data.model.Child>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "孩子信息",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            children.forEach {
                /** @param child 孩子对象 */
                    child ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "姓名：${child.name}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "年龄：${child.age}岁",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "学校：${child.grade}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (child.classInfo.isNotEmpty()) {
                            Text(
                                text = "班级：${child.classInfo}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Column {
                        if (child.allergies.isNotEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "⚠️ 过敏：${child.allergies}",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(4.dp),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        if (child.specialNotes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "备注：${child.specialNotes}",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(4.dp),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                if (children.indexOf(child) < children.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                }
            }
        }
    }
}

/**
 * 家长信息卡片
 *
 * 显示家长的姓名、电话和评分信息，并提供拨打电话联系的功能。
 *
 * @param parent 家长对象
 * @param onCall 拨打电话的回调函数
 */
@Composable
private fun ParentInfoCard(
    parent: com.viakid.driver.data.model.Parent,
    onCall: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "家长信息",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "姓名：${parent.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "电话：${parent.phone}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "评分：",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        repeat(5) {
                            /** @param index 当前星星的索引 */
                                index ->
                            Icon(
                                if (index < parent.rating.toInt()) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = " ${parent.rating}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Row {
                    OutlinedButton(onClick = onCall) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("拨打")
                    }
                }
            }
        }
    }
}

/**
 * 接送路线卡片
 *
 * 显示接取点和送达点的详细信息，包括地址和接取时间，
 * 并提供一键导航功能。
 *
 * @param order 订单对象，包含位置信息
 * @param onNavigate 打开地图导航的回调函数
 */
@Composable
private fun RouteCard(
    order: Order,
    onNavigate: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "接送路线",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 接取点
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Default.TripOrigin,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "接取点",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = order.pickupLocation.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = order.pickupLocation.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.ArrowDownward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 送达点
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "送达点",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = order.dropOffLocation.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = order.dropOffLocation.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "接取时间",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = order.pickupTime,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(onClick = onNavigate) {
                    Icon(
                        Icons.Default.Navigation,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("一键导航")
                }
            }
        }
    }
}

/**
 * 家长留言卡片
 *
 * 显示家长的特殊要求或留言信息。
 *
 * @param remarks 家长留言内容
 */
@Composable
private fun RemarksCard(remarks: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "家长留言",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = remarks,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 收入信息卡片
 *
 * 显示订单的收入明细，包括基础接送费、平台服务费和预计收入。
 *
 * @param order 订单对象，包含金额信息
 */
@Composable
private fun IncomeCard(order: Order) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "收入信息",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "基础接送费",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "¥${"%.2f".format(order.totalAmount)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "平台服务费",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "-¥${"%.2f".format(order.platformFee)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "预计收入",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "¥${"%.2f".format(order.estimatedIncome)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 操作按钮区域
 *
 * 根据订单状态显示不同的操作按钮：
 * - PENDING: 显示接受/拒绝订单按钮
 * - ASSIGNED: 显示出发按钮
 * - DEPARTED: 显示到达接取点按钮
 * - ARRIVED: 显示已接到孩子按钮
 * - PICKED_UP: 显示已送达按钮
 * - DELIVERED: 显示确认完成按钮
 *
 * @param order 订单对象
 * @param onAccept 接受订单的回调函数
 * @param onReject 拒绝订单的回调函数
 * @param onUpdateStatus 更新订单状态的回调函数
 */
@Composable
private fun ActionButtons(
    order: Order,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onUpdateStatus: (OrderStatus) -> Unit
) {
    when (order.status) {
        OrderStatus.PENDING -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("拒单")
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(2f)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("接受订单")
                }
            }
        }

        OrderStatus.ASSIGNED -> {
            Button(
                onClick = { onUpdateStatus(OrderStatus.DEPARTED) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("出发")
            }
        }

        OrderStatus.DEPARTED -> {
            Button(
                onClick = { onUpdateStatus(OrderStatus.ARRIVED) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("到达接取点")
            }
        }

        OrderStatus.ARRIVED -> {
            Button(
                onClick = { onUpdateStatus(OrderStatus.PICKED_UP) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.ChildCare,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("已接到孩子")
            }
        }

        OrderStatus.PICKED_UP -> {
            Button(
                onClick = { onUpdateStatus(OrderStatus.DELIVERED) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Flag,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("已送达")
            }
        }

        OrderStatus.DELIVERED -> {
            Button(
                onClick = { onUpdateStatus(OrderStatus.COMPLETED) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Done,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("确认完成")
            }
        }

        else -> {
            /* 其他状态不显示按钮 */
        }
    }
}

/**
 * 信息行组件
 *
 * 显示标签和值的键值对信息，用于展示订单的基本信息。
 *
 * @param label 信息标签
 * @param value 信息值
 */
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * 状态标签芯片
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
        OrderStatus.COMPLETED -> androidx.compose.ui.graphics.Color(0xFFC8E6C9) to androidx.compose.ui.graphics.Color(0xFF2E7D32)
        OrderStatus.CANCELLED -> androidx.compose.ui.graphics.Color(0xFFE0E0E0) to androidx.compose.ui.graphics.Color(0xFF757575)
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
