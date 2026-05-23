package com.viakid.driver.ui.screen.taskboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viakid.driver.data.model.Order
import com.viakid.driver.data.model.OrderStatus
import com.viakid.driver.data.model.TaskOverview
import com.viakid.driver.data.repository.DriverRepository
import com.viakid.driver.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * 任务看板页面 UI 状态数据类
 *
 * @param selectedDate 当前选中的日期，格式为 "yyyy-MM-dd"
 * @param orders 指定日期的所有订单列表
 * @param filteredOrders 经过状态筛选后的订单列表
 * @param selectedStatus 当前选中的筛选状态，null 表示显示全部
 * @param isOnline 司机是否在线
 * @param taskOverview 今日任务概览数据，包含各状态数量和收入
 * @param isLoading 是否正在加载数据
 * @param errorMessage 错误信息，null 表示无错误
 */
data class TaskBoardUiState(
    val selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val orders: List<Order> = emptyList(),
    val filteredOrders: List<Order> = emptyList(),
    val selectedStatus: OrderStatus? = null,
    val isOnline: Boolean = true,
    val taskOverview: TaskOverview = TaskOverview(0, 0, 0, 0.0),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * 任务看板 ViewModel
 *
 * 负责管理任务看板页面的业务逻辑和数据状态，包括加载任务概览、
 * 订单列表、切换在线状态、日期选择和状态筛选等功能。
 *
 * @param orderRepository 订单数据仓库，提供订单相关的 API 调用
 * @param driverRepository 司机数据仓库，提供司机状态相关的 API 调用
 */
@HiltViewModel
class TaskBoardViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val driverRepository: DriverRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskBoardUiState())

    /**
     * UI 状态流，供界面观察和响应式更新
     */
    val uiState: StateFlow<TaskBoardUiState> = _uiState.asStateFlow()

    private val _isOnline = MutableStateFlow(true)

    /**
     * 在线状态流，独立于 UI 状态以便快速响应
     */
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    init {
        loadData()
    }

    /**
     * 加载所有数据
     *
     * 同时加载任务概览和订单列表。
     */
    fun loadData() {
        loadOverview()
        loadOrders()
    }

    private fun loadOverview() {
        viewModelScope.launch {
            orderRepository.getOverview().onSuccess {
                /** @param overview 任务概览数据 */
                    overview ->
                _isOnline.value = overview.isOnline
                _uiState.value = _uiState.value.copy(
                    taskOverview = overview,
                    isOnline = overview.isOnline
                )
            }
        }
    }

    /**
     * 加载订单列表
     *
     * 根据当前选中的日期从服务器获取订单列表，并应用当前的状态筛选。
     */
    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            orderRepository.getOrders(date = _uiState.value.selectedDate).onSuccess {
                /** @param orders 订单列表 */
                    orders ->
                _uiState.value = _uiState.value.copy(
                    orders = orders,
                    filteredOrders = filterOrders(orders, _uiState.value.selectedStatus),
                    isLoading = false
                )
            }.onFailure {
                /** @param e 错误信息 */
                    e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    /**
     * 选择日期
     *
     * 更新选中的日期并重新加载该日期的订单列表。
     *
     * @param date 选中的日期，格式为 "yyyy-MM-dd"
     */
    fun selectDate(date: String) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        loadOrders()
    }

    /**
     * 选择筛选状态
     *
     * 更新选中的订单状态并重新过滤订单列表。
     *
     * @param status 选中的订单状态，null 表示显示全部
     */
    fun selectStatus(status: OrderStatus?) {
        _uiState.value = _uiState.value.copy(
            selectedStatus = status,
            filteredOrders = filterOrders(_uiState.value.orders, status)
        )
    }

    /**
     * 过滤订单列表
     *
     * 根据指定的状态过滤订单，如果状态为 null 则返回所有订单。
     *
     * @param orders 原始订单列表
     * @param status 筛选状态，null 表示不过滤
     * @return 过滤后的订单列表
     */
    private fun filterOrders(orders: List<Order>, status: OrderStatus?): List<Order> {
        return if (status == null) {
            orders
        } else {
            orders.filter { it.status == status }
        }
    }

    /**
     * 切换在线状态
     *
     * 切换司机的在线/离线状态，并同步到服务器。
     * 如果服务器更新失败，会回滚到之前的状态。
     */
    fun toggleOnlineStatus() {
        val newStatus = !_isOnline.value
        _isOnline.value = newStatus
        _uiState.value = _uiState.value.copy(isOnline = newStatus)

        viewModelScope.launch {
            driverRepository.updateOnlineStatus(newStatus).onFailure {
                /** @param e 错误信息 */
                    e ->
                // 回滚状态
                _isOnline.value = !newStatus
                _uiState.value = _uiState.value.copy(
                    isOnline = !newStatus,
                    errorMessage = e.message
                )
            }
        }
    }

    /**
     * 清除错误信息
     *
     * 将错误信息重置为 null，通常在用户处理完错误后调用。
     */
    @Suppress("unused")
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
