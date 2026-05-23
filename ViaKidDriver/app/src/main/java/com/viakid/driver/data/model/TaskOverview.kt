package com.viakid.driver.data.model

/**
 * 任务概览数据类，用于展示司机当前的工作任务统计和收入情况
 *
 * @property pendingCount 待处理订单数量
 * @property inProgressCount 进行中的订单数量
 * @property completedCount 已完成的订单数量
 * @property todayIncome 今日收入金额
 * @property isOnline 司机是否在线状态
 */
data class TaskOverview(
    val pendingCount: Int = 0,
    val inProgressCount: Int = 0,
    val completedCount: Int = 0,
    val todayIncome: Double = 0.0,
    val isOnline: Boolean = false
)
