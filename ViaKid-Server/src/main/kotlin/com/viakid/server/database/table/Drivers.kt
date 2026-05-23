package com.viakid.server.database.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import java.math.BigDecimal

object Drivers : UUIDTable("drivers") {
    val phone = varchar("phone", 20).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val name = varchar("name", 100).nullable()
    val gender = varchar("gender", 10).nullable()
    val birthday = date("birthday").nullable()
    val avatarUrl = varchar("avatar_url", 500).nullable()
    val emergencyContact = varchar("emergency_contact", 100).nullable()
    val emergencyPhone = varchar("emergency_phone", 20).nullable()
    val verificationLevel = integer("verification_level").default(0)  // 0-未认证 1-基础 2-身份 3-驾照 4-车辆 5-完全
    val avgRating = decimal("avg_rating", 3, 2).default(BigDecimal("0.00"))
    val totalOrders = integer("total_orders").default(0)
    val status = varchar("status", 20).default("pending")
    val onlineStatus = integer("online_status").default(0)  // 0-离线 1-空闲 2-接单中 3-服务中
    val isOnline = bool("is_online").default(false)
    val lastOnlineAt = datetime("last_online_at").nullable()
    val deviceId = varchar("device_id", 100).nullable()
    val isDeleted = bool("is_deleted").default(false)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}
