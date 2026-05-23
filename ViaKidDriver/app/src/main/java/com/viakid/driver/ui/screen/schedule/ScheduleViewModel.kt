package com.viakid.driver.ui.screen.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viakid.driver.data.remote.ScheduleInfo
import com.viakid.driver.data.remote.TimeSlot
import com.viakid.driver.data.remote.UnavailableDate
import com.viakid.driver.data.repository.DriverRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 排班设置页面 UI 状态数据类
 *
 * @param timeSlots 可接单时段列表，包含上午、下午、晚上等时段
 * @param workDays 每周工作日期列表，使用 1-7 表示周一到周日
 * @param unavailableDates 不可用日期列表，如节假日、请假等
 * @param maxOrdersPerDay 每日最大接单数
 * @param isLoading 是否正在加载排班信息
 * @param isSaving 是否正在保存排班设置
 * @param errorMessage 错误信息，null 表示无错误
 * @param saveSuccess 保存是否成功
 */
data class ScheduleUiState(
    val timeSlots: List<TimeSlotState> = listOf(
        TimeSlotState("morning", "上午", "07:00", "09:00", true),
        TimeSlotState("afternoon", "下午", "14:00", "17:00", true),
        TimeSlotState("evening", "晚上", "18:00", "20:00", false)
    ),
    val workDays: List<Int> = listOf(1, 2, 3, 4, 5),
    val unavailableDates: List<UnavailableDate> = emptyList(),
    val maxOrdersPerDay: Int = 8,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
)

/**
 * 时间段状态数据类
 *
 * @param type 时段类型标识，如 "morning"、"afternoon"、"evening"
 * @param label 时段显示标签，如 "上午"、"下午"、"晚上"
 * @param start 开始时间，格式为 "HH:mm"
 * @param end 结束时间，格式为 "HH:mm"
 * @param enabled 该时段是否启用
 */
data class TimeSlotState(
    val type: String,
    val label: String,
    val start: String,
    val end: String,
    val enabled: Boolean
)

/**
 * 排班设置 ViewModel
 *
 * 负责管理排班设置页面的业务逻辑和数据状态，包括加载排班信息、
 * 修改时段设置、工作日期、不可用日期、最大接单数以及保存排班设置。
 *
 * @param driverRepository 司机数据仓库，提供排班相关的 API 调用
 */
@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val driverRepository: DriverRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())


    /**
     * UI 状态流，供界面观察和响应式更新
     */
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        loadSchedule()
    }

    /**
     * 加载排班信息
     *
     * 从服务器获取当前司机的排班设置并更新 UI 状态。
     * 加载成功后会将服务端数据转换为 UI 状态，失败则记录错误信息。
     */
    fun loadSchedule() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            driverRepository.getSchedule().onSuccess {
                /** @param schedule 排班信息 */
                    schedule ->
                _uiState.value = _uiState.value.copy(
                    timeSlots = schedule.timeSlots.map { it.toTimeSlotState() },
                    workDays = schedule.workDays,
                    unavailableDates = schedule.unavailableDates,
                    maxOrdersPerDay = schedule.maxOrdersPerDay,
                    isLoading = false
                )
            }.onFailure {
                /** @param e 异常信息 */
                    e ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    /**
     * 将 TimeSlot 转换为 TimeSlotState
     *
     * 根据时段类型设置对应的中文标签。
     */
    private fun TimeSlot.toTimeSlotState() = TimeSlotState(
        type = type,
        label = when (type) {
            "morning" -> "上午"
            "afternoon" -> "下午"
            "evening" -> "晚上"
            else -> type
        },
        start = start,
        end = end,
        enabled = enabled
    )

    /**
     * 切换时间段启用状态
     *
     * 根据索引切换指定时间段的启用/禁用状态。
     *
     * @param index 时间段在列表中的索引
     */
    fun toggleTimeSlot(index: Int) {
        val updated = _uiState.value.timeSlots.toMutableList()
        updated[index] = updated[index].copy(enabled = !updated[index].enabled)
        _uiState.value = _uiState.value.copy(timeSlots = updated)
    }

    /**
     * 更新时间段的时间范围
     *
     * 修改指定时间段的开始时间和结束时间。
     *
     * @param index 时间段在列表中的索引
     * @param start 新的开始时间，格式为 "HH:mm"
     * @param end 新的结束时间，格式为 "HH:mm"
     */
    fun updateTimeSlot(index: Int, start: String, end: String) {
        val updated = _uiState.value.timeSlots.toMutableList()
        updated[index] = updated[index].copy(start = start, end = end)
        _uiState.value = _uiState.value.copy(timeSlots = updated)
    }

    /**
     * 切换工作日状态
     *
     * 根据星期几切换该天是否为工作日。如果已选中则取消，未选中则添加。
     *
     * @param day 星期几，使用 1-7 表示周一到周日
     */
    fun toggleWorkDay(day: Int) {
        val current = _uiState.value.workDays.toMutableList()
        if (day in current) current.remove(day) else {
            current.add(day); current.sort()
        }
        _uiState.value = _uiState.value.copy(workDays = current)
    }

    /**
     * 更新每日最大接单数
     *
     * 设置司机每天最多接受的订单数量，范围限制在 1-20 之间。
     *
     * @param value 新的每日最大接单数
     */
    fun updateMaxOrders(value: Int) {
        _uiState.value = _uiState.value.copy(maxOrdersPerDay = value.coerceIn(1, 20))
    }

    /**
     * 添加不可用日期
     *
     * 将指定的不可用日期（如节假日、请假）添加到列表中。
     *
     * @param date 不可用日期对象，包含开始日期、结束日期和原因
     */
    fun addUnavailableDate(date: UnavailableDate) {
        val updated = _uiState.value.unavailableDates.toMutableList()
        updated.add(date)
        _uiState.value = _uiState.value.copy(unavailableDates = updated)
    }

    /**
     * 删除不可用日期
     *
     * 根据索引从列表中移除指定的不可用日期。
     *
     * @param index 不可用日期在列表中的索引
     */
    fun removeUnavailableDate(index: Int) {
        val updated = _uiState.value.unavailableDates.toMutableList()
        if (index in updated.indices) {
            updated.removeAt(index)
            _uiState.value = _uiState.value.copy(unavailableDates = updated)
        }
    }

    /**
     * 保存排班设置
     *
     * 将当前的排班设置（时段、工作日、不可用日期、最大接单数）
     * 保存到服务器。保存成功后会标记 saveSuccess 为 true。
     */
    fun saveSchedule() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            val schedule = ScheduleInfo(
                timeSlots = _uiState.value.timeSlots.map { TimeSlot(it.type, it.start, it.end, it.enabled) },
                workDays = _uiState.value.workDays,
                unavailableDates = _uiState.value.unavailableDates,
                maxOrdersPerDay = _uiState.value.maxOrdersPerDay
            )
            driverRepository.updateSchedule(schedule).onSuccess {
                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            }.onFailure {
                /** @param e 错误信息 */
                    e ->
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = e.message)
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

    /**
     * 清除保存成功标志
     *
     * 将 saveSuccess 重置为 false，通常在显示成功提示后调用。
     */
    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }
}
