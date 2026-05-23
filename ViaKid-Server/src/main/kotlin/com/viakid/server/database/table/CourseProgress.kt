package com.viakid.server.database.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object CourseProgress : Table("course_progress") {
    val driverId = reference("driver_id", Drivers)
    val courseId = reference("course_id", Courses.id)
    val status = varchar("status", 20).default("not_started")
    val progress = integer("progress").default(0)  // 0-100
    val lastPosition = long("last_position").default(0)
    val enrolledAt = datetime("enrolled_at")
    val completedAt = datetime("completed_at").nullable()
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(driverId, courseId)
}
