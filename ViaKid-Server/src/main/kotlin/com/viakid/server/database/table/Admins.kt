package com.viakid.server.database.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime

object Admins : UUIDTable("admins") {
    val phone = varchar("phone", 20).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val username = varchar("username", 50).uniqueIndex()
    val nickname = varchar("nickname", 100).nullable()
    val email = varchar("email", 100).nullable()
    val department = varchar("department", 100).nullable()
    val role = varchar("role", 50).default("staff")  // super_admin/operator/finance
    val status = varchar("status", 20).default("active")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}
