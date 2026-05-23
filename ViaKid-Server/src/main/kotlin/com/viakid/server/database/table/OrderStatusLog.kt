package com.viakid.server.database.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime

object OrderStatusLog : UUIDTable("order_status_log") {
    val orderId = reference("order_id", Orders)
    val fromStatus = integer("from_status").nullable()
    val toStatus = integer("to_status")
    val operatorType = integer("operator_type")  // 1-家长 2-接送员 3-系统
    val operatorId = uuid("operator_id").nullable()
    val remark = varchar("remark", 255).nullable()
    val createdAt = datetime("created_at")
}
