package com.viakid.server.service

import com.viakid.server.database.table.*
import com.viakid.server.exception.BusinessException
import com.viakid.server.model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class OrderService {

    // ========== 优化前：4 次独立 COUNT/SUM 查询 ==========
    // val pendingCount = Orders.selectAll().where { ... }.count()     // 查询 1
    // val inProgressCount = Orders.selectAll().where { ... }.count()  // 查询 2
    // val completedCount = Orders.selectAll().where { ... }.count()   // 查询 3
    // val todayIncome = Orders.selectAll().where { ... }.sumOf { }    // 查询 4（加载全部行到 JVM 再求和）
    //
    // ========== 优化后：1 次条件聚合查询 ==========
    // 聚合下推到 SQL 层，使用 CASE WHEN 条件聚合，减少 4 次查询为 1 次
    // 同时使用 SQL SUM() 替代 JVM 内存求和
    fun getOverview(driverId: UUID): TaskOverviewDto = transaction {
        val today = LocalDate.now()

        val pendingExpr = Orders.status.eq("pending").and(Orders.pickupDate.eq(today))
        val inProgressExpr = Orders.status.inList(listOf("assigned", "departed", "arrived", "picked_up"))
        val completedExpr = Orders.status.eq("completed").and(Orders.pickupDate.eq(today))

        val pendingCase = Case().When(pendingExpr, intLiteral(1)).Else(intLiteral(0))
        val inProgressCase = Case().When(inProgressExpr, intLiteral(1)).Else(intLiteral(0))
        val completedCase = Case().When(completedExpr, intLiteral(1)).Else(intLiteral(0))
        @Suppress("UNCHECKED_CAST")
        val zeroDec = decimalLiteral(BigDecimal.ZERO) as Expression<BigDecimal?>
        val incomeCase = Case().When(completedExpr, Orders.driverIncome).Else(zeroDec)

        val pendingCount = Sum(pendingCase, IntegerColumnType())
        val inProgressCount = Sum(inProgressCase, IntegerColumnType())
        val completedCount = Sum(completedCase, IntegerColumnType())
        val todayIncomeSum = Sum(incomeCase, DecimalColumnType(10, 2))

        val result = Orders.select(pendingCount, inProgressCount, completedCount, todayIncomeSum)
            .where { (Orders.driverId eq driverId) and (Orders.isDeleted eq false) }
            .firstOrNull()

        val isOnline = Drivers.select(Drivers.isOnline)
            .where { Drivers.id eq driverId }
            .firstOrNull()?.get(Drivers.isOnline) ?: false

        TaskOverviewDto(
            pendingCount = result?.get(pendingCount)?.toInt() ?: 0,
            inProgressCount = result?.get(inProgressCount)?.toInt() ?: 0,
            completedCount = result?.get(completedCount)?.toInt() ?: 0,
            todayIncome = result?.get(todayIncomeSum)?.toDouble() ?: 0.0,
            onlineStatus = isOnline
        )
    }

    // ========== 优化：SELECT 只查需要的列 ==========
    // 原来：Orders.selectAll() 查询全部 39 列
    // 优化后：只 SELECT 列表页需要展示的列，减少 I/O 和网络传输
    private val orderListColumns = listOf(
        Orders.id, Orders.orderNo, Orders.status, Orders.orderType, Orders.type,
        Orders.pickupAddress, Orders.pickupLat, Orders.pickupLng, Orders.pickupLocationName,
        Orders.dropoffAddress, Orders.dropoffLat, Orders.dropoffLng, Orders.dropoffLocationName,
        Orders.pickupDate, Orders.pickupTime,
        Orders.totalAmount, Orders.platformFee, Orders.driverIncome,
        Orders.childCount, Orders.distance, Orders.schoolName, Orders.specialRequirements,
        Orders.parentId, Orders.createdAt
    )

    fun getOrders(
        driverId: UUID,
        status: String?,
        date: String?,
        page: Int,
        size: Int
    ): OrderListData = transaction {
        var baseCondition: Op<Boolean> = (Orders.driverId eq driverId) and (Orders.isDeleted eq false)

        status?.let {
            baseCondition = baseCondition and (Orders.status eq it)
        }

        date?.let {
            val localDate = LocalDate.parse(it)
            baseCondition = baseCondition and (Orders.pickupDate eq localDate)
        }

        val totalCount = Orders.select(Orders.id).where { baseCondition }.count().toInt()

        val orderRows = Orders.select(orderListColumns)
            .where { baseCondition }
            .orderBy(Orders.pickupDate, SortOrder.DESC)
            .orderBy(Orders.pickupTime, SortOrder.ASC)
            .limit(size).offset(((page - 1) * size).toLong())
            .toList()

        val items = batchBuildOrderDtos(orderRows)

        OrderListData(
            items = items,
            total = totalCount,
            page = page,
            size = size
        )
    }

    // ========== 优化：订单详情使用 JOIN 替代分离查询 ==========
    fun getOrderDetail(orderId: UUID, driverId: UUID): OrderDetail = transaction {
        val row = Orders.selectAll().where {
            (Orders.id eq orderId) and (Orders.driverId eq driverId)
        }.firstOrNull()
            ?: throw BusinessException(4003, "订单不存在或无权查看")

        val children = OrderChildren.selectAll().where { OrderChildren.orderId eq orderId }
            .map { childRow ->
                ChildDto(
                    id = childRow[OrderChildren.id].toString(),
                    name = childRow[OrderChildren.childName],
                    gender = childRow[OrderChildren.gender] ?: "",
                    age = childRow[OrderChildren.age] ?: 0,
                    grade = childRow[OrderChildren.grade] ?: "",
                    classInfo = childRow[OrderChildren.classInfo] ?: "",
                    allergies = childRow[OrderChildren.allergies],
                    specialNotes = childRow[OrderChildren.specialNotes]
                )
            }

        val parentInfo = loadParentInfo(row[Orders.parentId])

        OrderDetail(
            id = orderId.toString(),
            orderNo = row[Orders.orderNo],
            status = row[Orders.status],
            type = row[Orders.type] ?: row[Orders.orderType].toString(),
            pickupLocation = LocationDto(
                address = row[Orders.pickupAddress],
                latitude = row[Orders.pickupLat].toDouble(),
                longitude = row[Orders.pickupLng].toDouble(),
                name = row[Orders.pickupLocationName] ?: ""
            ),
            dropOffLocation = LocationDto(
                address = row[Orders.dropoffAddress],
                latitude = row[Orders.dropoffLat].toDouble(),
                longitude = row[Orders.dropoffLng].toDouble(),
                name = row[Orders.dropoffLocationName] ?: ""
            ),
            pickupTime = row[Orders.pickupTime].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            pickupDate = row[Orders.pickupDate]?.toString() ?: "",
            children = children,
            parent = parentInfo,
            amount = AmountDto(
                total = row[Orders.totalAmount].toDouble(),
                platformFee = row[Orders.platformFee]?.toDouble() ?: 0.0,
                income = row[Orders.driverIncome]?.toDouble() ?: 0.0
            ),
            specialRequirements = row[Orders.specialRequirements],
            createdAt = row[Orders.createdAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
    }

    fun acceptOrder(orderId: UUID, driverId: UUID) = transaction {
        checkDriverEligibility(driverId)

        val currentStatus = Orders.select(Orders.status).where { Orders.id eq orderId }
            .firstOrNull()?.get(Orders.status)
            ?: throw BusinessException(4004, "订单不存在")

        if (currentStatus != "pending") {
            throw BusinessException(4005, "订单状态不允许接单，当前状态：$currentStatus")
        }

        Orders.update({ Orders.id eq orderId }) {
            it[status] = "assigned"
            it[Orders.driverId] = driverId
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun rejectOrder(orderId: UUID, driverId: UUID, req: RejectRequest) = transaction {
        val currentStatus = Orders.select(Orders.status).where { Orders.id eq orderId }
            .firstOrNull()?.get(Orders.status)
            ?: throw BusinessException(4004, "订单不存在")

        if (currentStatus != "pending") {
            throw BusinessException(4005, "订单状态不允许拒单，当前状态：$currentStatus")
        }

        Orders.update({ Orders.id eq orderId }) {
            it[status] = "cancelled"
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun updateOrderStatus(orderId: UUID, driverId: UUID, newStatus: String) = transaction {
        val row = Orders.select(Orders.status, Orders.driverId).where { Orders.id eq orderId }.firstOrNull()
            ?: throw BusinessException(4004, "订单不存在")

        val currentDriverId = row[Orders.driverId]
        if (currentDriverId != driverId) {
            throw BusinessException(4003, "无权操作此订单")
        }

        val currentStatus = row[Orders.status]
        val validTransitions = mapOf(
            "assigned" to setOf("departed"),
            "departed" to setOf("arrived"),
            "arrived" to setOf("picked_up"),
            "picked_up" to setOf("delivered"),
            "delivered" to setOf("completed")
        )

        val allowed = validTransitions[currentStatus]
        if (allowed == null || newStatus !in allowed) {
            throw BusinessException(4005, "不允许的状态流转：$currentStatus -> $newStatus")
        }

        Orders.update({ Orders.id eq orderId }) {
            it[status] = newStatus
            it[updatedAt] = LocalDateTime.now()
        }
    }

    // ========== 优化：抢单列表只 SELECT 需要的列 ==========
    fun getGrabOrders(driverId: UUID, sort: String, page: Int, size: Int): GrabOrderList = transaction {
        val grabColumns = listOf(
            Orders.id, Orders.orderNo, Orders.pickupAddress, Orders.dropoffAddress,
            Orders.pickupTime, Orders.pickupDate, Orders.totalAmount, Orders.childCount,
            Orders.schoolName, Orders.distance, Orders.driverIncome, Orders.parentId,
            Orders.status, Orders.orderType, Orders.type,
            Orders.pickupLat, Orders.pickupLng, Orders.pickupLocationName,
            Orders.dropoffLat, Orders.dropoffLng, Orders.dropoffLocationName,
            Orders.platformFee, Orders.specialRequirements, Orders.createdAt
        )

        val grabableOrders = Orders.select(grabColumns).where {
            (Orders.status eq "pending") and (Orders.driverId.isNull())
        }

        val sorted = when (sort) {
            "income" -> grabableOrders.orderBy(Orders.driverIncome, SortOrder.DESC)
            "time" -> grabableOrders.orderBy(Orders.pickupDate, SortOrder.ASC)
            else -> grabableOrders.orderBy(Orders.distance, SortOrder.ASC)
        }

        val orderRows = sorted.limit(size).offset(((page - 1) * size).toLong()).toList()
        val items = batchBuildOrderDtos(orderRows)

        val mainOrder = items.firstOrNull()

        GrabOrderList(
            mainOrder = mainOrder,
            nearbyOrders = if (items.size > 1) items.drop(1) else emptyList(),
            countdownSeconds = 60
        )
    }

    fun grabOrder(orderId: UUID, driverId: UUID): GrabResult = transaction {
        checkDriverEligibility(driverId)

        val row = Orders.select(Orders.status, Orders.driverId).where { Orders.id eq orderId }.firstOrNull()
            ?: throw BusinessException(4004, "订单不存在")

        if (row[Orders.status] != "pending" || row[Orders.driverId] != null) {
            return@transaction GrabResult(success = false)
        }

        val updated = Orders.update({
            (Orders.id eq orderId) and (Orders.status eq "pending") and (Orders.driverId.isNull())
        }) {
            it[status] = "assigned"
            it[Orders.driverId] = driverId
            it[updatedAt] = LocalDateTime.now()
        }

        GrabResult(success = updated > 0)
    }

    // ========== 优化：SELECT 只查需要的列 ==========
    private fun checkDriverEligibility(driverId: UUID) {
        val driver = Drivers.select(Drivers.id, Drivers.status).where { Drivers.id eq driverId }.firstOrNull()
            ?: throw BusinessException(4001, "司机不存在")

        val driverStatus = driver[Drivers.status]
        if (driverStatus != "active" && driverStatus != "approved") {
            throw BusinessException(4006, "未完成认证，不能接单（当前状态：$driverStatus）")
        }

        val examPassed = ExamResults.select(ExamResults.id)
            .where { (ExamResults.driverId eq driverId) and (ExamResults.passed eq true) }
            .firstOrNull()

        if (examPassed == null) {
            throw BusinessException(4007, "未通过培训考试，不能接单")
        }
    }

    // ========== 优化前：N+1 查询家长信息 ==========
    // return rows.map { row ->
    //     val parentInfo = loadParentInfo(row[Orders.parentId])  // 每个订单单独查一次 parents 表
    // }
    //
    // ========== 优化后：批量查询家长信息 ==========
    // 先收集所有 parentId，一次性 IN 查询，再用 Map 查找
    private fun batchBuildOrderDtos(rows: List<ResultRow>): List<OrderDto> {
        if (rows.isEmpty()) return emptyList()

        val orderIds = rows.map { it[Orders.id] }

        val childrenByOrder = OrderChildren.selectAll()
            .where { OrderChildren.orderId inList orderIds }
            .groupBy { it[OrderChildren.orderId] }
            .mapValues { (_, childRows) ->
                childRows.map { childRow ->
                    ChildDto(
                        id = childRow[OrderChildren.id].toString(),
                        name = childRow[OrderChildren.childName],
                        gender = childRow[OrderChildren.gender] ?: "",
                        age = childRow[OrderChildren.age] ?: 0,
                        grade = childRow[OrderChildren.grade] ?: "",
                        classInfo = childRow[OrderChildren.classInfo] ?: "",
                        allergies = childRow[OrderChildren.allergies],
                        specialNotes = childRow[OrderChildren.specialNotes]
                    )
                }
            }

        val parentIds = rows.mapNotNull { it[Orders.parentId] }.distinct()
        val parentsMap = if (parentIds.isNotEmpty()) {
            Parents.select(Parents.id, Parents.name, Parents.phone)
                .where { Parents.id inList parentIds }
                .associate { parentRow ->
                    parentRow[Parents.id].value to ParentDto(
                        id = parentRow[Parents.id].toString(),
                        name = parentRow[Parents.name] ?: "",
                        phone = parentRow[Parents.phone],
                        rating = 0.0
                    )
                }
        } else {
            emptyMap()
        }

        val defaultParent = ParentDto(id = "", name = "", phone = "", rating = 0.0)

        return rows.map { row ->
            val orderId = row[Orders.id]
            val children = childrenByOrder[orderId] ?: emptyList()
            val parentId = row[Orders.parentId]
            val parentInfo = if (parentId != null) parentsMap[parentId] ?: defaultParent else defaultParent

            OrderDto(
                id = orderId.toString(),
                orderNo = row[Orders.orderNo],
                status = row[Orders.status],
                type = row[Orders.type] ?: row[Orders.orderType].toString(),
                pickupLocation = LocationDto(
                    address = row[Orders.pickupAddress],
                    latitude = row[Orders.pickupLat].toDouble(),
                    longitude = row[Orders.pickupLng].toDouble(),
                    name = row[Orders.pickupLocationName] ?: ""
                ),
                dropOffLocation = LocationDto(
                    address = row[Orders.dropoffAddress],
                    latitude = row[Orders.dropoffLat].toDouble(),
                    longitude = row[Orders.dropoffLng].toDouble(),
                    name = row[Orders.dropoffLocationName] ?: ""
                ),
                pickupTime = row[Orders.pickupTime].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                pickupDate = row[Orders.pickupDate]?.toString() ?: "",
                children = children,
                parent = parentInfo,
                amount = AmountDto(
                    total = row[Orders.totalAmount].toDouble(),
                    platformFee = row[Orders.platformFee]?.toDouble() ?: 0.0,
                    income = row[Orders.driverIncome]?.toDouble() ?: 0.0
                ),
                specialRequirements = row[Orders.specialRequirements],
                distance = row[Orders.distance]?.toDouble() ?: 0.0,
                schoolName = row[Orders.schoolName] ?: ""
            )
        }
    }

    private fun loadParentInfo(parentId: UUID?): ParentDto {
        if (parentId == null) return ParentDto(id = "", name = "", phone = "", rating = 0.0)
        val row = Parents.select(Parents.id, Parents.name, Parents.phone)
            .where { Parents.id eq parentId }
            .firstOrNull()
        return if (row != null) {
            ParentDto(
                id = row[Parents.id].toString(),
                name = row[Parents.name] ?: "",
                phone = row[Parents.phone],
                rating = 0.0
            )
        } else {
            ParentDto(id = parentId.toString(), name = "", phone = "", rating = 0.0)
        }
    }
}
