package com.viakid.driver.ui.screen.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

/**
 * 排班设置页面
 *
 * 允许司机设置可接单时段、每周工作日期、不可用日期以及每日最大接单数，
 * 以控制接单时间和工作量。
 *
 * @param onBack 返回上一页面的回调函数
 * @param viewModel 排班 ViewModel，负责数据加载和保存逻辑
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onBack: () -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("保存成功")
            viewModel.clearSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("排班设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        /** @param paddingValues 填充 */
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

                // 可接单时段设置
                item {
                    TimeSlotsCard(
                        timeSlots = uiState.timeSlots,
                        onToggle = viewModel::toggleTimeSlot,
                        onUpdate = viewModel::updateTimeSlot
                    )
                }

                // 每周可用日期
                item {
                    WorkDaysCard(
                        selectedDays = uiState.workDays,
                        onToggle = viewModel::toggleWorkDay
                    )
                }

                // 不可用日期设置
                item {
                    UnavailableDatesCard(
                        dates = uiState.unavailableDates,
                        onAdd = viewModel::addUnavailableDate,
                        onRemove = viewModel::removeUnavailableDate
                    )
                }

                // 每日最大接单数
                item {
                    MaxOrdersCard(
                        value = uiState.maxOrdersPerDay,
                        onValueChange = viewModel::updateMaxOrders
                    )
                }

                // 保存按钮
                item {
                    Button(
                        onClick = viewModel::saveSchedule,
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("保存设置")
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

/**
 * 可接单时段设置卡片
 *
 * 显示多个时间段（如早班、中班、晚班），允许司机启用/禁用每个时段。
 *
 * @param timeSlots 时间段列表，包含标签、开始时间、结束时间和启用状态
 * @param onToggle 切换时间段启用状态的回调函数，参数为时间段索引
 * @param onUpdate 更新时间段详情的回调函数，参数为索引、开始时间、结束时间
 */
@Suppress("unused")
@Composable
private fun TimeSlotsCard(
    timeSlots: List<TimeSlotState>,
    onToggle: (Int) -> Unit,
    onUpdate: (Int, String, String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "可接单时段设置",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            timeSlots.forEachIndexed {
                /**
                 * @param index 时间段索引
                 * @param slot 时间段
                 */
                    index, slot ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = slot.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${slot.start} - ${slot.end}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = slot.enabled,
                        onCheckedChange = { onToggle(index) }
                    )
                }

                if (index < timeSlots.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                }
            }
        }
    }
}

/**
 * 每周可用日期选择卡片
 *
 * 显示周一到周日的选择按钮，允许司机选择每周的工作日期。
 *
 * @param selectedDays 已选中的工作日列表，使用 1-7 表示周一到周日
 * @param onToggle 切换某天是否工作的回调函数，参数为星期几（1-7）
 */
@Composable
private fun WorkDaysCard(
    selectedDays: List<Int>,
    onToggle: (Int) -> Unit
) {
    val days = listOf(
        1 to "一", 2 to "二", 3 to "三", 4 to "四", 5 to "五", 6 to "六", 7 to "日"
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "每周可用日期",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                days.forEach { (day, label) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FilterChip(
                            selected = day in selectedDays,
                            onClick = { onToggle(day) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 不可用日期设置卡片
 *
 * 显示司机设置的不可用日期范围（如节假日、请假等），
 * 允许添加和删除不可用日期。
 *
 * @param dates 不可用日期列表，包含开始日期、结束日期和原因
 * @param onAdd 添加不可用日期的回调函数
 * @param onRemove 删除不可用日期的回调函数，参数为日期索引
 */
@Suppress("unused")
@Composable
private fun UnavailableDatesCard(
    dates: List<com.viakid.driver.data.remote.UnavailableDate>,
    onAdd: (com.viakid.driver.data.remote.UnavailableDate) -> Unit,
    onRemove: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "不可用日期设置",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (dates.isEmpty()) {
                Text(
                    text = "暂无设置不可用日期",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                dates.forEachIndexed {
                    /**
                     * @param index 日期索引
                     * @param date 日期
                     */
                        index, date ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = date.reason.ifEmpty { "节假日" },
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${date.start} ~ ${date.end}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { onRemove(index) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    if (index < dates.size - 1) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { /* TODO: 添加节假日选择 */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("添加节假日")
            }
        }
    }
}

/**
 * 每日最大接单数设置卡片
 *
 * 通过滑块设置司机每天最多接受的订单数量。
 *
 * @param value 当前设置的每日最大接单数
 * @param onValueChange 更新最大接单数的回调函数
 */
@Composable
private fun MaxOrdersCard(
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "每日最大接单数",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "1",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Slider(
                    value = value.toFloat(),
                    onValueChange = { onValueChange(it.toInt()) },
                    valueRange = 1f..20f,
                    steps = 18,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "20",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "当前设置：$value 单",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
