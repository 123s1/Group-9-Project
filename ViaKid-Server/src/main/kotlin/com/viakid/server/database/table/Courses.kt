package com.viakid.server.database.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Courses : Table("courses") {
    val id = varchar("id", 50)
    val title = varchar("title", 200)
    val description = text("description").nullable()
    val coverUrl = varchar("cover_url", 500).nullable()
    val contentType = varchar("content_type", 20)  // video/article/livestream
    val contentUrl = varchar("content_url", 500).nullable()
    val videoUrl = varchar("video_url", 500).nullable()
    val duration = varchar("duration", 50).nullable()
    val courseType = varchar("course_type", 50).nullable()  // safety/service/emergency/psychology/regulation
    val type = varchar("type", 50).default("required")  // required/elective
    val isRequired = bool("is_required").default(false)
    val passScore = integer("pass_score").default(60)
    val sortOrder = integer("sort_order").default(0)
    val status = varchar("status", 20).default("active")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}
