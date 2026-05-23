package com.viakid.driver.ui.screen.orderdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viakid.driver.data.model.Order
import com.viakid.driver.data.model.OrderStatus
import com.viakid.driver.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 订单详情页面 UI 状态数据类
 *
 * @param order 当前订单对象，null 表示未加载
 * @param isLoading 是否正在加载中
 * @param errorMessage 错误信息，null 表示无错误
 * @param acceptSuccess 接受订单是否成功
 * @param rejectSuccess 拒绝订单是否成功
 */
data class OrderDetailUiState(
    val order: Order? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val acceptSuccess: Boolean = false,
    val rejectSuccess: Boolean = false
)

/**
 * 订单详情 ViewModel
 *
 * 负责管理订单详情页面的业务逻辑和数据状态，包括加载订单详情、
 * 接受/拒绝订单、更新订单状态等操作。
 *
 * @param savedStateHandle 保存的状态句柄，用于获取订单 ID
 * @param orderRepository 订单数据仓库，提供订单相关的 API 调用
 */
@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val orderId: String = savedStateHandle["orderId"] ?: ""

    private val _uiState = MutableStateFlow(OrderDetailUiState())

    /**
     * UI 状态流，供界面观察和响应式更新
     */
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    init {
        loadOrderDetail()
    }

    /**
     * 加载订单详情
     *
     * 从服务器获取指定订单的详细信息并更新 UI 状态。
     * 加载成功后会更新订单数据，失败则记录错误信息。
     */
    fun loadOrderDetail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            orderRepository.getOrderDetail(orderId).onSuccess {
                /** @param order 订单对象，包含订单的详细信息 */
                    order ->
                _uiState.value = _uiState.value.copy(order = order, isLoading = false)
            }.onFailure {
                /** @param e 错误对象 */
                    e ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    /**
     * 接受订单
     *
     * 向服务器发送接受订单请求，成功后刷新订单详情并标记接受成功状态。
     */
    fun acceptOrder() {
        viewModelScope.launch {
            orderRepository.acceptOrder(orderId).onSuccess {
                _uiState.value = _uiState.value.copy(acceptSuccess = true)
                loadOrderDetail()
            }.onFailure {
                /** @param e 错误对象 */
                    e ->
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    /**
     * 拒绝订单
     *
     * 向服务器发送拒绝订单请求，需要提供拒绝原因。
     *
     * @param reason 拒绝原因代码，如 "too_far"（太远）等
     * @param remark 拒绝备注说明，可选参数
     */
    fun rejectOrder(reason: String, remark: String? = null) {
        viewModelScope.launch {
            orderRepository.rejectOrder(orderId, reason, remark).onSuccess {
                _uiState.value = _uiState.value.copy(rejectSuccess = true)
            }.onFailure {
                /** @param e 错误对象 */
                    e ->
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    /**
     * 更新订单状态
     *
     * 将订单更新到指定的新状态，支持的状态包括：已出发、已到达、已接孩子、已送达、已完成。
     * 更新成功后会自动刷新订单详情。
     *
     * @param newStatus 新的订单状态
     */
    fun updateStatus(newStatus: OrderStatus) {
        val statusStr = when (newStatus) {
            OrderStatus.DEPARTED -> "departed"
            OrderStatus.ARRIVED -> "arrived"
            OrderStatus.PICKED_UP -> "picked_up"
            OrderStatus.DELIVERED -> "delivered"
            OrderStatus.COMPLETED -> "completed"
            else -> return
        }
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, statusStr).onSuccess {
                loadOrderDetail()
            }.onFailure {
                /** @param e 错误对象 */
                    e ->
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
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
