package com.viakid.server.service

import com.viakid.server.database.table.*
import com.viakid.server.model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class OrderService {

    fun getOverview(driverId: UUID): TaskOverviewDto = transaction {
        val today = LocalDate.now()

        val pendingCount = Orders.selectAll().where {
            (Orders.driverId eq driverId) and (Orders.status eq "pending") and (Orders.pickupDate eq today)
        }.count().toInt()

        val inProgressCount = Orders.selectAll().where {
            (Orders.driverId eq driverId) and
            (Orders.status inList listOf("assigned", "departed", "arrived", "picked_up"))
        }.count().toInt()

        val completedCount = Orders.selectAll().where {
            (Orders.driverId eq driverId) and (Orders.status eq "completed") and (Orders.pickupDate eq today)
        }.count().toInt()

        val todayIncome = Orders.selectAll().where {
            (Orders.driverId eq driverId) and (Orders.status eq "completed") and (Orders.pickupDate eq today)
        }.sumOf { it[Orders.driverIncome]?.toDouble() ?: 0.0 }

        val isOnline = Drivers.selectAll().where { Drivers.id eq driverId }
            .firstOrNull()?.get(Drivers.isOnline) ?: false

        TaskOverviewDto(
            pendingCount = pendingCount,
            inProgressCount = inProgressCount,
            completedCount = completedCount,
            todayIncome = todayIncome,
            onlineStatus = isOnline
        )
    }

    fun getOrders(
        driverId: UUID,
        status: String?,
        date: String?,
        page: Int,
        size: Int
    ): OrderListData = transaction {
        var query = Orders.selectAll().where { Orders.driverId eq driverId }

        status?.let {
            query = query.andWhere { Orders.status eq it }
        }

        date?.let {
            val localDate = LocalDate.parse(it)
            query = query.andWhere { Orders.pickupDate eq localDate }
        }

        val totalCount = query.count().toInt()

        val items = query
            .orderBy(Orders.pickupDate, SortOrder.DESC)
            .orderBy(Orders.pickupTime, SortOrder.ASC)
            .limit(size).offset(((page - 1) * size).toLong())
            .map { rowToOrderDto(it) }

        OrderListData(
            items = items,
            total = totalCount,
            page = page,
            size = size
        )
    }

    fun getOrderDetail(orderId: UUID): OrderDetail = transaction {
        val row = Orders.selectAll().where { Orders.id eq orderId }.firstOrNull()
            ?: throw IllegalArgumentException("订单不存在")

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
        val currentStatus = Orders.selectAll().where { Orders.id eq orderId }
            .firstOrNull()?.get(Orders.status)
            ?: throw IllegalArgumentException("订单不存在")

        if (currentStatus != "pending") {
            throw IllegalArgumentException("订单状态不允许接单，当前状态：$currentStatus")
        }

        Orders.update({ Orders.id eq orderId }) {
            it[status] = "assigned"
            it[Orders.driverId] = driverId
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun rejectOrder(orderId: UUID, driverId: UUID, req: RejectRequest) = transaction {
        val currentStatus = Orders.selectAll().where { Orders.id eq orderId }
            .firstOrNull()?.get(Orders.status)
            ?: throw IllegalArgumentException("订单不存在")

        if (currentStatus != "pending") {
            throw IllegalArgumentException("订单状态不允许拒单，当前状态：$currentStatus")
        }

        Orders.update({ Orders.id eq orderId }) {
            it[status] = "cancelled"
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun updateOrderStatus(orderId: UUID, driverId: UUID, newStatus: String) = transaction {
        val row = Orders.selectAll().where { Orders.id eq orderId }.firstOrNull()
            ?: throw IllegalArgumentException("订单不存在")

        val currentDriverId = row[Orders.driverId]
        if (currentDriverId != driverId) {
            throw IllegalArgumentException("无权操作此订单")
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
            throw IllegalArgumentException("不允许的状态流转：$currentStatus -> $newStatus")
        }

        Orders.update({ Orders.id eq orderId }) {
            it[status] = newStatus
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun getGrabOrders(driverId: UUID, sort: String, page: Int, size: Int): GrabOrderList = transaction {
        val grabableOrders = Orders.selectAll().where {
            (Orders.status eq "pending") and (Orders.driverId.isNull())
        }

        val sorted = when (sort) {
            "income" -> grabableOrders.orderBy(Orders.driverIncome, SortOrder.DESC)
            "time" -> grabableOrders.orderBy(Orders.pickupDate, SortOrder.ASC)
            else -> grabableOrders.orderBy(Orders.distance, SortOrder.ASC)
        }

        val items = sorted.limit(size).offset(((page - 1) * size).toLong())
            .map { rowToOrderDto(it) }

        val mainOrder = items.firstOrNull()

        GrabOrderList(
            mainOrder = mainOrder,
            nearbyOrders = if (items.size > 1) items.drop(1) else emptyList(),
            countdownSeconds = 60
        )
    }

    fun grabOrder(orderId: UUID, driverId: UUID): GrabResult = transaction {
        val row = Orders.selectAll().where { Orders.id eq orderId }.firstOrNull()
            ?: throw IllegalArgumentException("订单不存在")

        if (row[Orders.status] != "pending" || row[Orders.driverId] != null) {
            return@transaction GrabResult(success = false)
        }

        // 使用乐观锁：检查状态后再更新
        val updated = Orders.update({
            (Orders.id eq orderId) and (Orders.status eq "pending") and (Orders.driverId.isNull())
        }) {
            it[status] = "assigned"
            it[Orders.driverId] = driverId
            it[updatedAt] = LocalDateTime.now()
        }

        GrabResult(success = updated > 0)
    }

    private fun rowToOrderDto(row: ResultRow): OrderDto {
        val orderId = row[Orders.id]
        val children = transaction {
            OrderChildren.selectAll().where { OrderChildren.orderId eq orderId }
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
        }

        val parentInfo = loadParentInfo(row[Orders.parentId])

        return OrderDto(
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

    private fun loadParentInfo(parentId: UUID?): ParentDto {
        if (parentId == null) return ParentDto(id = "", name = "", phone = "", rating = 0.0)
        // 阶段1：家长表不在当前范围，返回模拟数据
        return ParentDto(
            id = parentId.toString(),
            name = "家长用户",
            phone = "138****0001",
            rating = 4.8
        )
    }
}
