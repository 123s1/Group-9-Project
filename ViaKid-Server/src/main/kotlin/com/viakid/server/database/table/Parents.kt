package com.viakid.server.database.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime

object Parents : UUIDTable("parents") {
    val phone = varchar("phone", 20).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val name = varchar("name", 100).nullable()
    val avatarUrl = varchar("avatar_url", 500).nullable()
    val gender = varchar("gender", 10).nullable()
    val birthday = date("birthday").nullable()
    val status = varchar("status", 20).default("active")
    val isDeleted = bool("is_deleted").default(false)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}
