package com.viakid.server.database.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime

object OrderChildren : UUIDTable("order_children") {
    val orderId = reference("order_id", Orders)
    val childName = varchar("child_name", 100)
    val gender = varchar("gender", 10).nullable()
    val age = integer("age").nullable()
    val grade = varchar("grade", 50).nullable()
    val classInfo = varchar("class_info", 50).nullable()
    val pickupStatus = integer("pickup_status").default(0)  // 0-待上车 1-已上车 2-已送达 3-未上车
    val pickupPhoto = varchar("pickup_photo", 500).nullable()
    val dropoffPhoto = varchar("dropoff_photo", 500).nullable()
    val pickupTime = datetime("pickup_time").nullable()
    val dropoffTime = datetime("dropoff_time").nullable()
    val verifyCode = varchar("verify_code", 6).nullable()
    val verified = bool("verified").default(false)
    val healthNotes = text("health_notes").nullable()
    val allergies = text("allergies").nullable()
    val specialNotes = text("special_notes").nullable()
    val createdAt = datetime("created_at")
}
