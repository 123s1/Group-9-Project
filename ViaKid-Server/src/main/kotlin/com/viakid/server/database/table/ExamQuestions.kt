package com.viakid.server.database.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object ExamQuestions : Table("exam_questions") {
    val id = varchar("id", 50)
    val courseId = varchar("course_id", 50).nullable()
    val type = varchar("type", 20)  // single_choice/multiple_choice/true_false
    val content = text("content")
    val explanation = text("explanation").nullable()
    val sortOrder = integer("sort_order").default(0)
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}
