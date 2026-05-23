package com.viakid.driver.data.repository

import com.viakid.driver.data.local.database.OrderDao
import com.viakid.driver.data.local.database.OrderEntity
import com.viakid.driver.data.model.*
import com.viakid.driver.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * JSON序列化实例，用于处理订单数据的序列化和反序列化
 */
private val json = Json { ignoreUnknownKeys = true }

/**
 * 订单数据仓库，负责处理订单相关的业务逻辑
 *
 * 协调本地数据库和远程API调用，提供统一的订单管理接口
 *
 * @property orderDao 订单数据访问对象，用于操作订单信息
 * @property orderApi 订单API接口，用于执行网络请求
 */
@Singleton
class OrderRepository @Inject constructor(
    private val orderDao: OrderDao,
    private val orderApi: OrderApi
) {

    /**
     * 获取今日任务概览统计信息
     *
     * @return Result<TaskOverview> 获取成功返回任务概览，失败时返回异常
     */
    suspend fun getOverview(): Result<com.viakid.driver.data.model.TaskOverview> = withContext(Dispatchers.IO) {
        try {
            val response = orderApi.getOverview()
            if (response.code == ErrorCodes.SUCCESS && response.data != null) {
                val data = response.data
                Result.success(
                    com.viakid.driver.data.model.TaskOverview(
                        pendingCount = data.pendingCount,
                        inProgressCount = data.inProgressCount,
                        completedCount = data.completedCount,
                        todayIncome = data.todayIncome,
                        isOnline = data.onlineStatus
                    )
                )
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取订单列表，并缓存到本地数据库
     *
     * @param status 订单状态过滤，可为空
     * @param date 日期过滤，格式 yyyy-MM-dd，可为空
     * @param page 页码，从1开始
     * @param size 每页数量，默认20
     * @return Result<List<Order>> 获取成功返回订单列表，失败时返回异常
     */
    suspend fun getOrders(
        status: String? = null,
        date: String? = null,
        page: Int = 1,
        size: Int = 20
    ): Result<List<Order>> = withContext(Dispatchers.IO) {
        try {
            val response = orderApi.getOrders(status, date, page, size)
            if (response.code == ErrorCodes.SUCCESS && response.data != null) {
                val orders = response.data.items.map { it.toOrderModel() }
                // 缓存订单到本地
                orderDao.insertOrders(response.data.items.map { it.toEntity() })
                Result.success(orders)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取订单详细信息
     *
     * @param orderId 订单ID
     * @return Result<Order> 获取成功返回订单详情，失败时返回异常
     */
    suspend fun getOrderDetail(orderId: String): Result<Order> = withContext(Dispatchers.IO) {
        try {
            val response = orderApi.getOrderDetail(orderId)
            if (response.code == ErrorCodes.SUCCESS && response.data != null) {
                Result.success(response.data.toOrderDetailModel())
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 司机接受订单（接单）
     *
     * @param orderId 订单ID
     * @return Result<Unit> 接单成功返回Unit，失败时返回异常
     */
    suspend fun acceptOrder(orderId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = orderApi.acceptOrder(orderId)
            if (response.code == ErrorCodes.SUCCESS) {
                orderDao.updateOrderStatus(orderId, "assigned")
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 司机拒绝订单（拒单）
     *
     * @param orderId 订单ID
     * @param reason 拒单原因
     * @param remark 备注说明，可为空
     * @return Result<Unit> 拒单成功返回Unit，失败时返回异常
     */
    suspend fun rejectOrder(orderId: String, reason: String, remark: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = orderApi.rejectOrder(orderId, reason, remark)
            if (response.code == ErrorCodes.SUCCESS) {
                orderDao.updateOrderStatus(orderId, "cancelled")
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 更新订单状态
     *
     * @param orderId 订单ID
     * @param status 新状态
     * @return Result<Unit> 更新成功返回Unit，失败时返回异常
     */
    suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = orderApi.updateOrderStatus(orderId, status)
            if (response.code == ErrorCodes.SUCCESS) {
                orderDao.updateOrderStatus(orderId, status)
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取可抢订单列表
     *
     * @param sort 排序方式：distance-距离优先，income-收入优先，time-时间优先
     * @param page 页码，从1开始
     * @param size 每页数量，默认10
     * @return Result<GrabOrderList> 获取成功返回可抢订单列表，失败时返回异常
     */
    suspend fun getGrabOrders(sort: String = "distance", page: Int = 1, size: Int = 10): Result<GrabOrderList> = withContext(Dispatchers.IO) {
        try {
            val response = orderApi.getGrabOrders(sort, page, size)
            if (response.code == ErrorCodes.SUCCESS && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 司机抢单
     *
     * @param orderId 订单ID
     * @return Result<Boolean> 抢单成功返回true/false，失败时返回异常
     */
    suspend fun grabOrder(orderId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = orderApi.grabOrder(orderId)
            if (response.code == ErrorCodes.SUCCESS && response.data != null) {
                Result.success(response.data.success)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 本地缓存查询
    /**
     * 从本地数据库获取指定日期的订单列表
     *
     * @param date 日期，格式 yyyy-MM-dd
     * @return Flow<List<Order>> 订单列表的数据流
     */
    fun getLocalOrders(date: String): Flow<List<Order>> {
        return orderDao.getOrdersByDate(date).map {
            /** @param entities 订单实体列表*/
                entities ->
            entities.map { it.toOrderModel() }
        }
    }

    /**
     * 从本地数据库获取指定订单的详细信息
     *
     * @param orderId 订单ID
     * @return Flow<Order?> 订单详情的数据流，如果不存在则返回null
     */
    fun getLocalOrder(orderId: String): Flow<Order?> {
        return orderDao.getOrderByIdFlow(orderId).map { it?.toOrderModel() }
    }

    /**
     * 从本地数据库获取指定日期的活跃订单数量
     *
     * @param date 日期，格式 yyyy-MM-dd
     * @return Flow<Int> 活跃订单数量的数据流
     */
    fun getActiveOrderCountByDate(date: String): Flow<Int> = orderDao.getActiveOrderCountByDate(date)

    /**
     * 从本地数据库获取指定日期的已完成订单数量
     *
     * @param date 日期，格式 yyyy-MM-dd
     * @return Flow<Int> 已完成订单数量的数据流
     */
    fun getCompletedOrderCountByDate(date: String): Flow<Int> = orderDao.getCompletedOrderCountByDate(date)
}

// Extension functions for conversion

/**
 * 将OrderDto转换为Order模型的扩展函数
 *
 * @return Order 转换后的订单模型对象
 */
internal fun OrderDto.toOrderModel() = Order(
    id = id,
    orderNo = orderNo,
    status = OrderStatus.valueOf(status.uppercase().replace("_", "_")),
    type = OrderType.valueOf(type.uppercase()),
    pickupLocation = Location(
        address = pickupLocation.address,
        latitude = pickupLocation.latitude,
        longitude = pickupLocation.longitude,
        name = pickupLocation.name
    ),
    dropOffLocation = Location(
        address = dropOffLocation.address,
        latitude = dropOffLocation.latitude,
        longitude = dropOffLocation.longitude,
        name = dropOffLocation.name
    ),
    pickupTime = pickupTime,
    estimatedArrivalTime = "",
    children = children.map { it.toChildModel() },
    parent = parent.toParentModel(),
    totalAmount = amount.total,
    platformFee = amount.platformFee,
    estimatedIncome = amount.income,
    specialRequirements = specialRequirements ?: "",
)

/**
 * 将OrderDto转换为OrderEntity的扩展函数
 *
 * @return OrderEntity 转换后的订单实体对象
 */
internal fun OrderDto.toEntity() = OrderEntity(
    id = id,
    orderNo = orderNo,
    status = status,
    type = type,
    pickupAddress = pickupLocation.address,
    pickupLatitude = pickupLocation.latitude,
    pickupLongitude = pickupLocation.longitude,
    pickupLocationName = pickupLocation.name,
    dropOffAddress = dropOffLocation.address,
    dropOffLatitude = dropOffLocation.latitude,
    dropOffLongitude = dropOffLocation.longitude,
    dropOffLocationName = dropOffLocation.name,
    pickupTime = pickupTime,
    pickupDate = pickupDate,
    childrenJson = json.encodeToString(children),
    parentId = parent.id,
    parentName = parent.name,
    parentPhone = parent.phone,
    parentRating = parent.rating,
    totalAmount = amount.total,
    platformFee = amount.platformFee,
    estimatedIncome = amount.income,
    specialRequirements = specialRequirements ?: "",
    distance = distance,
    schoolName = schoolName
)

/**
 * 将OrderDetail转换为Order模型的扩展函数
 *
 * @return Order 转换后的订单模型对象
 */
internal fun OrderDetail.toOrderDetailModel() = Order(
    id = id,
    orderNo = orderNo,
    status = OrderStatus.valueOf(status.uppercase().replace("_", "_")),
    type = OrderType.valueOf(type.uppercase()),
    pickupLocation = Location(
        address = pickupLocation.address,
        latitude = pickupLocation.latitude,
        longitude = pickupLocation.longitude,
        name = pickupLocation.name
    ),
    dropOffLocation = Location(
        address = dropOffLocation.address,
        latitude = dropOffLocation.latitude,
        longitude = dropOffLocation.longitude,
        name = dropOffLocation.name
    ),
    pickupTime = pickupTime,
    estimatedArrivalTime = "",
    children = children.map { it.toChildModel() },
    parent = parent.toParentModel(),
    totalAmount = amount.total,
    platformFee = amount.platformFee,
    estimatedIncome = amount.income,
    specialRequirements = specialRequirements ?: "",
    distance = 0.0,
    schoolName = ""
)

/**
 * 将ChildDto转换为Child模型的扩展函数
 *
 * @return Child 转换后的儿童信息模型对象
 */
internal fun ChildDto.toChildModel() = Child(
    id = id,
    name = name,
    gender = gender,
    age = age,
    grade = grade,
    classInfo = classInfo,
    allergies = allergies ?: "",
    specialNotes = specialNotes ?: ""
)

/**
 * 将ParentDto转换为Parent模型的扩展函数
 *
 * @return Parent 转换后的家长信息模型对象
 */
internal fun ParentDto.toParentModel() = Parent(
    id = id,
    name = name,
    phone = phone,
    rating = rating
)

/**
 * 将OrderEntity转换为Order模型的扩展函数
 *
 * @return Order 转换后的订单模型对象
 */
internal fun OrderEntity.toOrderModel(): Order {
    val childrenList: List<Child> = try {
        json.decodeFromString<List<ChildDto>>(childrenJson).map { it.toChildModel() }
    } catch (e: Exception) {
        emptyList()
    }

    return Order(
        id = id,
        orderNo = orderNo,
        status = try {
            OrderStatus.valueOf(status.uppercase())
        } catch (e: Exception) {
            OrderStatus.PENDING
        },
        type = try {
            OrderType.valueOf(type.uppercase())
        } catch (e: Exception) {
            OrderType.SINGLE
        },
        pickupLocation = Location(
            address = pickupAddress,
            latitude = pickupLatitude,
            longitude = pickupLongitude,
            name = pickupLocationName
        ),
        dropOffLocation = Location(
            address = dropOffAddress,
            latitude = dropOffLatitude,
            longitude = dropOffLongitude,
            name = dropOffLocationName
        ),
        pickupTime = pickupTime,
        estimatedArrivalTime = "",
        children = childrenList,
        parent = Parent(
            id = parentId,
            name = parentName,
            phone = parentPhone,
            rating = parentRating
        ),
        totalAmount = totalAmount,
        platformFee = platformFee,
        estimatedIncome = estimatedIncome,
        specialRequirements = specialRequirements ?: "",
        distance = distance,
        schoolName = schoolName
    )
}
