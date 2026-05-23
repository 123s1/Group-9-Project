package com.viakid.server.database.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime

object RefreshTokens : UUIDTable("refresh_tokens") {
    val driverId = reference("driver_id", Drivers)
    val token = varchar("token", 500).uniqueIndex()
    val expiredAt = datetime("expired_at")
}
