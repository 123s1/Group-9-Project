package com.viakid.server.database.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime

object OrderException : UUIDTable("order_exception") {
    val orderId = reference("order_id", Orders)
    val exceptionType = integer("exception_type")  // 1-迟到 2-孩子未出现 3-交通拥堵 4-车辆故障 5-天气异常 6-家长未到 7-其他
    val severity = integer("severity")  // 1-轻微 2-一般 3-严重
    val description = varchar("description", 500)
    val photoUrls = varchar("photo_urls", 1000).nullable()
    val handlerType = integer("handler_type").nullable()  // 1-系统 2-客服
    val handlerId = uuid("handler_id").nullable()
    val handleResult = varchar("handle_result", 500).nullable()
    val handleTime = datetime("handle_time").nullable()
    val status = integer("status").default(0)  // 0-待处理 1-处理中 2-已解决
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}
