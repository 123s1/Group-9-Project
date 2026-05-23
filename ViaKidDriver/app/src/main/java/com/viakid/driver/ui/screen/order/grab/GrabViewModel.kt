package com.viakid.driver.ui.screen.order.grab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viakid.driver.data.model.*
import com.viakid.driver.data.remote.*
import com.viakid.driver.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 抢单排序模式
 *
 * @property label 排序模式的显示文本
 */
enum class GrabSortMode(val label: String) {
    /** 距离优先排序 */
    DISTANCE("距离优先"),

    /** 收入优先排序 */
    INCOME("收入优先"),

    /** 时间优先排序 */
    TIME("时间优先")
}

/**
 * 抢单状态枚举
 */
enum class GrabState {
    /** 加载中 */
    LOADING,

    /** 倒计时中 */
    COUNTING_DOWN,

    /** 抢单成功 */
    SUCCESS,

    /** 超时未抢 */
    TIMEOUT,

    /** 被他人抢走 */
    GRABBED_BY_OTHERS,

    /** 抢单失败 */
    FAILED
}

/**
 * 抢单界面状态数据类
 *
 * @property countdownSeconds 倒计时剩余秒数，默认60秒
 * @property mainOrder 当前主推订单，可为空
 * @property nearbyOrders 附近订单列表
 * @property sortMode 当前排序模式，默认为距离优先
 * @property grabState 当前抢单状态，默认为加载中
 * @property showConfirmDialog 是否显示确认抢单对话框，默认为false
 * @property showRejectDialog 是否显示拒绝订单对话框，默认为false
 * @property errorMessage 错误信息，可为空
 */
data class GrabUiState(
    val countdownSeconds: Int = 60,
    val mainOrder: Order? = null,
    val nearbyOrders: List<Order> = emptyList(),
    val sortMode: GrabSortMode = GrabSortMode.DISTANCE,
    val grabState: GrabState = GrabState.LOADING,
    val showConfirmDialog: Boolean = false,
    val showRejectDialog: Boolean = false,
    val errorMessage: String? = null
)

/**
 * 抢单页面 ViewModel
 *
 * 负责管理抢单页面的业务逻辑和状态，包括：
 * - 加载可抢订单列表（主订单和附近订单）
 * - 管理抢单倒计时
 * - 处理抢单、拒绝订单等操作
 * - 支持按距离、收入、时间排序
 *
 * @property orderRepository 订单仓库，用于获取和操作订单数据
 */
@HiltViewModel
class GrabViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GrabUiState())

    /** 抢单界面状态流，供UI层观察 */
    val uiState: StateFlow<GrabUiState> = _uiState.asStateFlow()
    private var countdownJob: Job? = null

    init {
        loadGrabOrders()
    }

    /**
     * 加载可抢订单列表
     *
     * 根据当前排序模式从服务器获取主订单和附近订单列表，
     * 并在加载成功后启动倒计时。
     */
    fun loadGrabOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(grabState = GrabState.LOADING) }
            val sortParam = when (_uiState.value.sortMode) {
                GrabSortMode.DISTANCE -> "distance"
                GrabSortMode.INCOME -> "income"
                GrabSortMode.TIME -> "time"
            }
            orderRepository.getGrabOrders(sort = sortParam).onSuccess {
                /** @param grabData 抢单数据 */
                    grabData ->
                _uiState.update {
                    it.copy(
                        mainOrder = grabData.mainOrder?.toOrder(),
                        nearbyOrders = grabData.nearbyOrders.map {
                            /** @param dto 订单数据 */
                                dto ->
                            dto.toOrder()
                        },
                        countdownSeconds = grabData.countdownSeconds,
                        grabState = GrabState.COUNTING_DOWN, errorMessage = null
                    )
                }
                startCountdown()
            }.onFailure {
                /** @param e 错误信息 */
                    e ->
                _uiState.update { it.copy(grabState = GrabState.FAILED, errorMessage = e.message) }
            }
        }
    }

    /**
     * 启动抢单倒计时
     *
     * 每秒更新一次倒计时秒数，当倒计时归零时将状态设置为超时。
     */
    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update {
                    /** @param state 当前界面状态 */
                        state ->
                    val newSeconds = state.countdownSeconds - 1
                    when {
                        newSeconds <= 0 -> state.copy(countdownSeconds = 0, grabState = GrabState.TIMEOUT)
                        else -> state.copy(countdownSeconds = newSeconds)
                    }
                }
                if (_uiState.value.countdownSeconds <= 0) break
            }
        }
    }

    /**
     * 切换排序模式并重新加载订单列表
     *
     * @param mode 新的排序模式（距离优先、收入优先或时间优先）
     */
    fun onSortModeChanged(mode: GrabSortMode) {
        _uiState.update { it.copy(sortMode = mode) }; loadGrabOrders()
    }

    /**
     * 用户点击抢单按钮时的处理逻辑
     *
     * 显示确认对话框让用户二次确认抢单操作。
     */
    fun onGrabClick() {
        _uiState.update { it.copy(showConfirmDialog = true) }
    }

    /**
     * 确认抢单操作
     *
     * 向服务器发起抢单请求，根据抢单结果更新界面状态：
     * - 抢单成功：显示成功状态，2秒后重新加载订单列表
     * - 被他人抢走：显示相应提示
     * - 抢单失败：显示错误信息
     */
    fun confirmGrab() {
        val order = _uiState.value.mainOrder ?: return
        _uiState.update { it.copy(showConfirmDialog = false) }
        countdownJob?.cancel()
        viewModelScope.launch {
            orderRepository.grabOrder(order.id).onSuccess {
                /** @param success 抢单结果 */
                    success ->
                if (success) {
                    _uiState.update { it.copy(grabState = GrabState.SUCCESS) }
                    delay(2000); loadGrabOrders()
                } else {
                    _uiState.update { it.copy(grabState = GrabState.GRABBED_BY_OTHERS, errorMessage = "订单已被抢走") }
                }
            }.onFailure {
                /** @param e 错误信息 */
                    e ->
                _uiState.update { it.copy(grabState = GrabState.FAILED, errorMessage = e.message) }
            }
        }
    }

    /**
     * 关闭确认抢单对话框
     */
    fun dismissConfirmDialog() {
        _uiState.update { it.copy(showConfirmDialog = false) }
    }

    /**
     * 用户点击拒绝订单按钮时的处理逻辑
     *
     * 显示拒绝原因选择对话框。
     */
    fun onRejectClick() {
        _uiState.update { it.copy(showRejectDialog = true) }
    }

    /**
     * 确认拒绝订单操作
     *
     * 向服务器发送拒绝订单请求（原因为"too_far"），
     * 成功后重新加载订单列表，失败则显示错误信息。
     */
    fun confirmReject() {
        val order = _uiState.value.mainOrder ?: return
        _uiState.update { it.copy(showRejectDialog = false) }
        countdownJob?.cancel()
        viewModelScope.launch {
            orderRepository.rejectOrder(order.id, "too_far").onSuccess { loadGrabOrders() }
                .onFailure {
                    /** @param e 错误信息 */
                        e ->
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
        }
    }

    /**
     * 关闭拒绝订单对话框
     */
    fun dismissRejectDialog() {
        _uiState.update { it.copy(showRejectDialog = false) }
    }

    /**
     * 从附近订单列表中选择一个订单进行抢单
     *
     * 将选中的订单设置为主订单，并从附近订单列表中移除该订单，
     * 同时重置倒计时为45秒并启动倒计时。
     *
     * @param order 用户选择的附近订单
     */
    fun grabNearbyOrder(order: Order) {
        _uiState.update {
            it.copy(
                mainOrder = order, nearbyOrders = it.nearbyOrders.filter {
                    /** @param o 订单 */
                        o ->
                    o.id != order.id
                },
                countdownSeconds = 45, grabState = GrabState.COUNTING_DOWN
            )
        }
        startCountdown()
    }

    /**
     * 超时后点击"下一个"按钮的处理逻辑
     *
     * 重新加载可抢订单列表，为用户提供新的订单机会。
     */
    fun onTimeoutNext() {
        loadGrabOrders()
    }

    /**
     * 清除错误信息
     *
     * 将错误信息设置为null，用于在用户处理后隐藏错误提示。
     */
    @Suppress("unused")
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * ViewModel 销毁时的清理工作
     *
     * 取消正在进行的倒计时任务，防止内存泄漏。
     */
    override fun onCleared() {
        countdownJob?.cancel(); super.onCleared()
    }
}

/**
 * 将订单DTO转换为领域模型Order
 *
 * 此函数处理数据传输对象到领域模型的转换，包括：
 * - 订单基本信息（ID、订单号、状态、类型）
 * - 取送地点信息（地址、经纬度、名称）
 * - 儿童和家长信息
 * - 费用信息（总金额、平台费、预估收入）
 * - 其他信息（特殊要求、距离、学校名称）
 *
 * 对于枚举值解析失败的情况，提供默认值以保证数据完整性。
 *
 * @return 转换后的Order领域模型对象
 */
internal fun OrderDto.toOrder() = Order(
    id = id, orderNo = orderNo,
    status = try {
        OrderStatus.valueOf(status.uppercase())
    } catch (_: Exception) {
        OrderStatus.PENDING
    },
    type = try {
        OrderType.valueOf(type.uppercase())
    } catch (_: Exception) {
        OrderType.SINGLE
    },
    pickupLocation = Location(pickupLocation.address, pickupLocation.latitude, pickupLocation.longitude, pickupLocation.name),
    dropOffLocation = Location(dropOffLocation.address, dropOffLocation.latitude, dropOffLocation.longitude, dropOffLocation.name),
    pickupTime = pickupTime, estimatedArrivalTime = "",
    children = children.map { Child(it.id, it.name, it.gender, it.age, it.grade, it.classInfo, it.allergies ?: "", it.specialNotes ?: "") },
    parent = Parent(parent.id, parent.name, parent.phone, parent.rating),
    totalAmount = amount.total, platformFee = amount.platformFee, estimatedIncome = amount.income,
    specialRequirements = specialRequirements ?: "", distance = distance, schoolName = schoolName
)
