package com.viakid.server.model

import kotlinx.serialization.Serializable

@Serializable
data class CourseDto(
    val id: String,
    val title: String,
    val description: String = "",
    val duration: String = "",
    val videoUrl: String? = null,
    val type: String = "required",
    val status: String = "not_started"
)

@Serializable
data class CourseDetailDto(
    val id: String,
    val title: String,
    val description: String = "",
    val videoUrl: String? = null,
    val videoDuration: Long = 0,
    val lectureNotes: String = "",
    val prevCourseId: String? = null,
    val nextCourseId: String? = null
)

@Serializable
data class ExamInfoDto(
    val totalQuestions: Int = 20,
    val passingScore: Int = 80,
    val timeLimit: Int = 1800,
    val canTake: Boolean = false,
    val lastAttempt: LastAttemptDto? = null
)

@Serializable
data class LastAttemptDto(val score: Int, val passed: Boolean, val takenAt: String)

@Serializable
data class QuestionDto(
    val id: String,
    val type: String = "single",
    val content: String,
    val options: List<OptionDto> = emptyList()
)

@Serializable
data class OptionDto(val key: String, val content: String)

@Serializable
data class AnswerDto(val questionId: String, val answer: String)

@Serializable
data class ExamSubmitRequest(val answers: List<AnswerDto>)

@Serializable
data class ExamResultDto(
    val score: Int,
    val passed: Boolean,
    val certificateNo: String? = null,
    val validUntil: String? = null
)

@Serializable
data class CertificateDto(
    val certificateNo: String = "",
    val driverName: String = "",
    val issueDate: String = "",
    val validUntil: String = "",
    val status: String = "valid"
)
