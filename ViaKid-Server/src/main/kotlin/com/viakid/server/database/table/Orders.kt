package com.viakid.server.database.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import java.math.BigDecimal

object Orders : UUIDTable("orders") {
    val orderNo = varchar("order_no", 50).uniqueIndex()
    val parentId = uuid("parent_id").nullable()
    val driverId = reference("driver_id", Drivers).nullable()
    val orderType = integer("order_type").default(1)  // 1-单次 2-包月 3-固定线路
    val serviceType = integer("service_type").default(1)  // 1-上学 2-放学 3-往返
    val status = varchar("status", 20).default("pending")  // pending/assigned/departed/arrived/picked_up/delivered/completed/cancelled
    val paymentStatus = integer("payment_status").default(0)  // 0-未支付 1-已支付 2-已退款
    val pickupAddress = varchar("pickup_address", 500)
    val pickupLat = decimal("pickup_lat", 10, 6)
    val pickupLng = decimal("pickup_lng", 10, 6)
    val pickupLocationName = varchar("pickup_location_name", 200).nullable()
    val dropoffAddress = varchar("dropoff_address", 500)
    val dropoffLat = decimal("dropoff_lat", 10, 6)
    val dropoffLng = decimal("dropoff_lng", 10, 6)
    val dropoffLocationName = varchar("dropoff_location_name", 200).nullable()
    val pickupDate = date("pickup_date").nullable()
    val pickupTime = datetime("pickup_time")
    val actualPickupTime = datetime("actual_pickup_time").nullable()
    val actualDropoffTime = datetime("actual_dropoff_time").nullable()
    val childCount = integer("child_count").default(1)
    val totalAmount = decimal("total_amount", 10, 2)
    val discountAmount = decimal("discount_amount", 10, 2).default(BigDecimal("0.00"))
    val payAmount = decimal("pay_amount", 10, 2)
    val platformFee = decimal("platform_fee", 10, 2).nullable()
    val driverIncome = decimal("driver_income", 10, 2).nullable()
    val distance = decimal("distance", 8, 2).nullable()
    val schoolName = varchar("school_name", 200).nullable()
    val type = varchar("type", 50).nullable()
    val specialRequirements = text("special_requirements").nullable()
    val paymentMethod = integer("payment_method").nullable()  // 1-钱包 2-微信 3-支付宝
    val paymentTime = datetime("payment_time").nullable()
    val paymentNo = varchar("payment_no", 64).nullable()
    val cancelReason = varchar("cancel_reason", 255).nullable()
    val cancelledBy = integer("cancelled_by").nullable()  // 1-家长 2-接送员 3-系统
    val remark = varchar("remark", 500).nullable()
    val isDeleted = bool("is_deleted").default(false)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}
