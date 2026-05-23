package com.viakid.server.database.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime

object OrderReview : UUIDTable("order_review") {
    val orderId = reference("order_id", Orders)
    val parentId = uuid("parent_id").nullable()  // 阶段一为模拟 UUID
    val driverId = reference("driver_id", Drivers)
    val rating = integer("rating")
    val content = varchar("content", 500).nullable()
    val tags = varchar("tags", 255).nullable()
    val images = varchar("images", 1000).nullable()
    val isAnonymous = bool("is_anonymous").default(false)
    val replyContent = varchar("reply_content", 500).nullable()
    val replyTime = datetime("reply_time").nullable()
    val createdAt = datetime("created_at")
}
