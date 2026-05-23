package com.viakid.server.service

import com.viakid.server.database.table.*
import com.viakid.server.model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class TrainingService {

    fun getCourses(driverId: UUID): List<CourseDto> = transaction {
        val progressMap = CourseProgress.selectAll()
            .where { CourseProgress.driverId eq driverId }
            .associate { it[CourseProgress.courseId] to it[CourseProgress.status] }

        Courses.selectAll().orderBy(Courses.sortOrder to SortOrder.ASC).map {
            val courseId = it[Courses.id]
            CourseDto(
                id = courseId,
                title = it[Courses.title],
                description = it[Courses.description] ?: "",
                duration = it[Courses.duration] ?: "",
                videoUrl = it[Courses.videoUrl],
                type = it[Courses.type] ?: "required",
                status = progressMap[courseId] ?: "not_started"
            )
        }
    }

    fun getCourseDetail(courseId: String): CourseDetailDto = transaction {
        val course = Courses.selectAll().where { Courses.id eq courseId }.firstOrNull()
            ?: throw IllegalArgumentException("课程不存在")

        val allCourses = Courses.selectAll().orderBy(Courses.sortOrder to SortOrder.ASC).map { it[Courses.id] }
        val idx = allCourses.indexOf(courseId)
        val prev = if (idx > 0) allCourses[idx - 1] else null
        val next = if (idx >= 0 && idx < allCourses.size - 1) allCourses[idx + 1] else null

        CourseDetailDto(
            id = courseId,
            title = course[Courses.title],
            description = course[Courses.description] ?: "",
            videoUrl = course[Courses.videoUrl],
            videoDuration = 0,
            lectureNotes = "",
            prevCourseId = prev,
            nextCourseId = next
        )
    }

    fun markCourseComplete(driverId: UUID, courseId: String) = transaction {
        CourseProgress.insertIgnore {
            it[this.driverId] = driverId
            it[this.courseId] = courseId
            it[status] = "completed"
            it[lastPosition] = 0
            it[updatedAt] = LocalDateTime.now()
        }
        CourseProgress.update({ (CourseProgress.driverId eq driverId) and (CourseProgress.courseId eq courseId) }) {
            it[status] = "completed"
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun getExamInfo(driverId: UUID): ExamInfoDto = transaction {
        val total = ExamQuestions.selectAll().count().toInt()
        val last = ExamResults.selectAll()
            .where { ExamResults.driverId eq driverId }
            .orderBy(ExamResults.takenAt, SortOrder.DESC)
            .firstOrNull()

        val completed = CourseProgress.selectAll()
            .where { (CourseProgress.driverId eq driverId) and (CourseProgress.status eq "completed") }
            .count().toInt()
        val allCourses = Courses.selectAll().count().toInt()
        val canTake = completed >= allCourses

        ExamInfoDto(
            totalQuestions = total,
            passingScore = 80,
            timeLimit = 1800,
            canTake = canTake,
            lastAttempt = last?.let {
                LastAttemptDto(
                    score = it[ExamResults.score],
                    passed = it[ExamResults.passed],
                    takenAt = it[ExamResults.takenAt]?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) ?: ""
                )
            }
        )
    }

    fun getExamQuestions(): List<QuestionDto> = transaction {
        val questions = ExamQuestions.selectAll().orderBy(ExamQuestions.sortOrder to SortOrder.ASC).toList()
        val options = ExamOptions.selectAll().toList().groupBy { it[ExamOptions.questionId] }

        questions.map { q ->
            val qid = q[ExamQuestions.id]
            QuestionDto(
                id = qid,
                type = q[ExamQuestions.type],
                content = q[ExamQuestions.content],
                options = options[qid]?.map {
                    OptionDto(key = it[ExamOptions.optionKey], content = it[ExamOptions.content])
                } ?: emptyList()
            )
        }
    }

    fun submitExam(driverId: UUID, answers: List<AnswerDto>): ExamResultDto = transaction {
        val questions = ExamQuestions.selectAll().associate { it[ExamQuestions.id] to it[ExamQuestions.type] }
        val correctOptions = ExamOptions.selectAll().where { ExamOptions.isCorrect eq true }
            .associate { it[ExamOptions.questionId] to it[ExamOptions.optionKey] }

        var score = 0
        val total = questions.size
        val perQuestion = if (total > 0) 100 / total else 0

        answers.forEach { ans ->
            val correct = correctOptions[ans.questionId]
            if (correct != null && ans.answer.trim().uppercase() == correct.uppercase()) {
                score += perQuestion
            }
        }

        val passed = score >= 80
        val certNo = if (passed) "CERT-${UUID.randomUUID().toString().take(8).uppercase()}" else null
        val validUntil = if (passed) LocalDateTime.now().plusYears(1) else null

        ExamResults.insert {
            it[this.driverId] = driverId
            it[courseId] = "GENERAL"
            it[totalQuestions] = total
            it[correctCount] = if (perQuestion > 0) score / perQuestion else 0
            it[this.score] = score
            it[this.passed] = passed
            it[certificateNo] = certNo
            it[this.validUntil] = validUntil?.toLocalDate()
            it[takenAt] = LocalDateTime.now()
        }

        ExamResultDto(
            score = score,
            passed = passed,
            certificateNo = certNo,
            validUntil = validUntil?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
    }

    fun getCertificate(driverId: UUID): CertificateDto = transaction {
        val result = ExamResults.selectAll()
            .where { (ExamResults.driverId eq driverId) and (ExamResults.passed eq true) }
            .orderBy(ExamResults.takenAt, SortOrder.DESC)
            .firstOrNull()

        val driver = Drivers.selectAll().where { Drivers.id eq driverId }.firstOrNull()

        if (result == null) {
            CertificateDto(status = "none")
        } else {
            val valid = result[ExamResults.validUntil]
            val expired = valid?.isBefore(java.time.LocalDate.now()) ?: false
            CertificateDto(
                certificateNo = result[ExamResults.certificateNo] ?: "",
                driverName = driver?.get(Drivers.name) ?: "",
                issueDate = result[ExamResults.takenAt]?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) ?: "",
                validUntil = valid?.toString() ?: "",
                status = if (expired) "expired" else "valid"
            )
        }
    }
}
