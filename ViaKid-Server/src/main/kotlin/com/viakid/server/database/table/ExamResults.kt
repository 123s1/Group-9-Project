package com.viakid.server.database.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime

object ExamResults : UUIDTable("exam_results") {
    val driverId = reference("driver_id", Drivers)
    val courseId = varchar("course_id", 50)
    val totalQuestions = integer("total_questions")
    val correctCount = integer("correct_count")
    val score = integer("score").default(0)
    val passed = bool("passed").default(false)
    val examDuration = integer("exam_duration").nullable()
    val answersJson = text("answers_json").nullable()
    val certificateNo = varchar("certificate_no", 100).nullable()
    val validUntil = date("valid_until").nullable()
    val takenAt = datetime("taken_at").nullable()
    val submittedAt = datetime("submitted_at")
}
