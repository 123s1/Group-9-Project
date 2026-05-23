package com.viakid.server.database.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime

object SmsCodes : UUIDTable("sms_codes") {
    val phone = varchar("phone", 20)
    val code = varchar("code", 10)
    val type = varchar("type", 20).nullable()
    val expiredAt = datetime("expired_at")
    val used = bool("used").default(false)
}
