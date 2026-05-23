package com.viakid.driver.data.remote

import io.ktor.client.call.*
import io.ktor.client.request.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 订单模块 API
 * 三端共用
 *
 * @property apiClient API客户端实例，用于执行网络请求
 */
@Singleton
class OrderApi @Inject constructor(
    private val apiClient: ApiClient
) {

    /**
     * 获取今日任务概览统计信息
     *
     * @return ApiResponse<TaskOverviewDto> 包含任务统计和收入信息的响应
     */
    suspend fun getOverview(): ApiResponse<TaskOverviewDto> {
        return apiClient.client.get("orders/overview").body()
    }

    /**
     * 获取订单列表，支持按状态、日期筛选和分页
     *
     * @param status 订单状态过滤：pending-待处理，in_progress-进行中，completed-已完成
     * @param date 日期过滤，格式 yyyy-MM-dd
     * @param page 页码，从1开始
     * @param size 每页数量，默认20
     * @return ApiResponse<OrderListData> 包含订单列表和分页信息的响应
     */
    suspend fun getOrders(
        status: String? = null,
        date: String? = null,
        page: Int = 1,
        size: Int = 20
    ): ApiResponse<OrderListData> {
        return apiClient.client.get("orders") {
            status?.let { parameter("status", it) }
            date?.let { parameter("date", it) }
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    /**
     * 获取订单详细信息
     *
     * @param orderId 订单ID
     * @return ApiResponse<OrderDetail> 包含订单详细信息的响应
     */
    suspend fun getOrderDetail(orderId: String): ApiResponse<OrderDetail> {
        return apiClient.client.get("orders/$orderId").body()
    }

    /**
     * 司机接受订单（接单）
     *
     * @param orderId 订单ID
     * @return ApiResponse<Unit> API响应，成功时data为null
     */
    suspend fun acceptOrder(orderId: String): ApiResponse<Unit> {
        return apiClient.client.post("orders/$orderId/accept").body()
    }

    /**
     * 司机拒绝订单（拒单）
     *
     * @param orderId 订单ID
     * @param reason 拒单原因：schedule_conflict-时间冲突，too_far-距离太远，other-其他
     * @param remark 备注说明，可为空
     * @return ApiResponse<Unit> API响应，成功时data为null
     */
    suspend fun rejectOrder(orderId: String, reason: String, remark: String? = null): ApiResponse<Unit> {
        return apiClient.client.post("orders/$orderId/reject") {
            setBody(RejectRequest(reason, remark))
        }.body()
    }

    /**
     * 更新订单状态，推进订单流程
     *
     * @param orderId 订单ID
     * @param status 新状态：departed-已出发，arrived-已到达，picked_up-已接孩子，delivered-已送达，completed-已完成
     * @return ApiResponse<Unit> API响应，成功时data为null
     */
    suspend fun updateOrderStatus(orderId: String, status: String): ApiResponse<Unit> {
        return apiClient.client.post("orders/$orderId/status") {
            setBody(mapOf("status" to status))
        }.body()
    }

    /**
     * 获取可抢订单列表，用于抢单大厅展示
     *
     * @param sort 排序方式：distance-距离优先，income-收入优先，time-时间优先
     * @param page 页码，从1开始
     * @param size 每页数量，默认10
     * @return ApiResponse<GrabOrderList> 包含可抢订单列表的响应
     */
    suspend fun getGrabOrders(sort: String = "distance", page: Int = 1, size: Int = 10): ApiResponse<GrabOrderList> {
        return apiClient.client.get("orders/grab") {
            parameter("sort", sort)
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    /**
     * 司机抢单
     *
     * @param orderId 订单ID
     * @return ApiResponse<GrabResult> 包含抢单结果的响应
     */
    suspend fun grabOrder(orderId: String): ApiResponse<GrabResult> {
        return apiClient.client.post("orders/$orderId/grab").body()
    }
}

/**
 * 订单列表响应数据类
 *
 * @property items 订单列表
 * @property total 总记录数
 * @property page 当前页码
 * @property size 每页数量
 */
@kotlinx.serialization.Serializable
data class OrderListData(
    val items: List<OrderDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val size: Int = 20
)

/**
 * 订单简要信息数据类，用于列表展示
 *
 * @property id 订单ID
 * @property orderNo 订单编号
 * @property status 订单状态
 * @property type 订单类型
 * @property pickupLocation 接车地点信息
 * @property dropOffLocation 送车地点信息
 * @property pickupTime 接车时间
 * @property pickupDate 接车日期
 * @property children 乘车儿童列表
 * @property parent 家长信息
 * @property amount 金额信息
 * @property specialRequirements 特殊要求，可为空
 * @property distance 订单距离（公里）
 * @property schoolName 学校名称
 */
@kotlinx.serialization.Serializable
data class OrderDto(
    val id: String,
    val orderNo: String,
    val status: String,
    val type: String,
    val pickupLocation: LocationDto,
    val dropOffLocation: LocationDto,
    val pickupTime: String,
    val pickupDate: String,
    val children: List<ChildDto> = emptyList(),
    val parent: ParentDto,
    val amount: AmountDto,
    val specialRequirements: String? = null,
    val distance: Double = 0.0,
    val schoolName: String = ""
)

/**
 * 位置信息数据类
 *
 * @property address 详细地址
 * @property latitude 纬度坐标
 * @property longitude 经度坐标
 * @property name 地点名称
 */
@kotlinx.serialization.Serializable
data class LocationDto(
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val name: String = ""
)

/**
 * 儿童信息数据类
 *
 * @property id 儿童ID
 * @property name 姓名
 * @property gender 性别
 * @property age 年龄
 * @property grade 年级
 * @property classInfo 班级信息
 * @property allergies 过敏史，可为空
 * @property specialNotes 特殊注意事项，可为空
 */
@kotlinx.serialization.Serializable
data class ChildDto(
    val id: String,
    val name: String,
    val gender: String = "",
    val age: Int = 0,
    val grade: String = "",
    val classInfo: String = "",
    val allergies: String? = null,
    val specialNotes: String? = null
)

/**
 * 家长信息数据类
 *
 * @property id 家长ID
 * @property name 姓名
 * @property phone 联系电话
 * @property rating 评分
 */
@kotlinx.serialization.Serializable
data class ParentDto(
    val id: String,
    val name: String,
    val phone: String,
    val rating: Double = 0.0
)

/**
 * 订单金额信息数据类
 *
 * @property total 订单总金额
 * @property platformFee 平台服务费
 * @property income 司机预估收入
 */
@kotlinx.serialization.Serializable
data class AmountDto(
    val total: Double = 0.0,
    val platformFee: Double = 0.0,
    val income: Double = 0.0
)

/**
 * 任务概览统计数据类
 *
 * @property pendingCount 待处理订单数量
 * @property inProgressCount 进行中订单数量
 * @property completedCount 已完成订单数量
 * @property todayIncome 今日收入金额
 * @property onlineStatus 在线状态
 */
@kotlinx.serialization.Serializable
data class TaskOverviewDto(
    val pendingCount: Int = 0,
    val inProgressCount: Int = 0,
    val completedCount: Int = 0,
    val todayIncome: Double = 0.0,
    val onlineStatus: Boolean = false
)

/**
 * 订单详细信息数据类
 *
 * @property id 订单ID
 * @property orderNo 订单编号
 * @property status 订单状态
 * @property type 订单类型
 * @property pickupLocation 接车地点信息
 * @property dropOffLocation 送车地点信息
 * @property pickupTime 接车时间
 * @property pickupDate 接车日期
 * @property children 乘车儿童列表
 * @property parent 家长信息
 * @property amount 金额信息
 * @property specialRequirements 特殊要求，可为空
 * @property createdAt 订单创建时间
 */
@kotlinx.serialization.Serializable
data class OrderDetail(
    val id: String,
    val orderNo: String,
    val status: String,
    val type: String,
    val pickupLocation: LocationDto,
    val dropOffLocation: LocationDto,
    val pickupTime: String,
    val pickupDate: String,
    val children: List<ChildDto>,
    val parent: ParentDto,
    val amount: AmountDto,
    val specialRequirements: String? = null,
    val createdAt: String = ""
)

/**
 * 拒单请求数据类
 *
 * @property reason 拒单原因
 * @property remark 备注说明，可为空
 */
@kotlinx.serialization.Serializable
data class RejectRequest(
    val reason: String,
    val remark: String? = null
)

/**
 * 可抢订单列表数据类
 *
 * @property mainOrder 主推订单，可为空
 * @property nearbyOrders 附近订单列表
 * @property countdownSeconds 抢单倒计时秒数
 */
@kotlinx.serialization.Serializable
data class GrabOrderList(
    val mainOrder: OrderDto? = null,
    val nearbyOrders: List<OrderDto> = emptyList(),
    val countdownSeconds: Int = 60
)

/**
 * 抢单结果数据类
 *
 * @property success 是否抢单成功
 */
@kotlinx.serialization.Serializable
data class GrabResult(
    val success: Boolean = true
)
