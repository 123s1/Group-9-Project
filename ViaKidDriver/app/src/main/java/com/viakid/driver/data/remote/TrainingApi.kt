package com.viakid.driver.data.remote

import io.ktor.client.call.*
import io.ktor.client.request.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 培训模块 API
 * 专员端专用
 *
 * @property apiClient API客户端实例，用于执行网络请求
 */
@Singleton
class TrainingApi @Inject constructor(
    private val apiClient: ApiClient
) {

    /**
     * 获取所有培训课程列表
     *
     * @return ApiResponse<List<CourseDto>> 包含课程列表的响应
     */
    suspend fun getCourses(): ApiResponse<List<CourseDto>> {
        return apiClient.client.get("training/courses").body()
    }

    /**
     * 获取指定课程的详细信息
     *
     * @param courseId 课程ID
     * @return ApiResponse<CourseDetailDto> 包含课程详细信息的响应
     */
    suspend fun getCourseDetail(courseId: String): ApiResponse<CourseDetailDto> {
        return apiClient.client.get("training/courses/$courseId").body()
    }

    /**
     * 标记指定课程为已完成状态
     *
     * @param courseId 课程ID
     * @return ApiResponse<Unit> API响应，成功时data为null
     */
    suspend fun markCourseComplete(courseId: String): ApiResponse<Unit> {
        return apiClient.client.post("training/courses/$courseId/complete").body()
    }

    /**
     * 获取考试相关信息，包括题目数量、及格分数等
     *
     * @return ApiResponse<ExamInfoDto> 包含考试信息的响应
     */
    suspend fun getExamInfo(): ApiResponse<ExamInfoDto> {
        return apiClient.client.get("training/exam").body()
    }

    /**
     * 获取考试题目列表
     *
     * @return ApiResponse<List<QuestionDto>> 包含考试题目的响应
     */
    suspend fun getExamQuestions(): ApiResponse<List<QuestionDto>> {
        return apiClient.client.get("training/exam/questions").body()
    }

    /**
     * 提交考试答案并获取成绩
     *
     * @param answers 答案列表，每个元素包含题目ID和选择的答案
     * @return ApiResponse<ExamResultDto> 包含考试成绩和结果的响应
     */
    suspend fun submitExam(answers: List<AnswerDto>): ApiResponse<ExamResultDto> {
        return apiClient.client.post("training/exam/submit") {
            setBody(ExamSubmitRequest(answers))
        }.body()
    }

    /**
     * 获取司机培训证书信息
     *
     * @return ApiResponse<CertificateDto> 包含证书信息的响应
     */
    suspend fun getCertificate(): ApiResponse<CertificateDto> {
        return apiClient.client.get("training/certificate").body()
    }
}

/**
 * 课程简要信息数据类，用于列表展示
 *
 * @property id 课程ID
 * @property title 课程标题
 * @property description 课程描述
 * @property duration 课程时长
 * @property videoUrl 视频URL，可为空
 * @property type 课程类型：required-必修，optional-选修
 * @property status 学习状态：not_started-未开始，in_progress-学习中，completed-已完成
 */
@kotlinx.serialization.Serializable
data class CourseDto(
    val id: String,
    val title: String,
    val description: String = "",
    val duration: String = "",
    val videoUrl: String? = null,
    val type: String = "required", // required, optional
    val status: String = "not_started" // not_started, in_progress, completed
)

/**
 * 课程详细信息数据类
 *
 * @property id 课程ID
 * @property title 课程标题
 * @property description 课程描述
 * @property videoUrl 视频URL，可为空
 * @property videoDuration 视频时长（秒）
 * @property lectureNotes 讲义内容
 * @property prevCourseId 上一节课程ID，可为空表示第一节
 * @property nextCourseId 下一节课程ID，可为空表示最后一节
 */
@kotlinx.serialization.Serializable
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

/**
 * 考试信息数据类
 *
 * @property totalQuestions 总题目数量
 * @property passingScore 及格分数
 * @property timeLimit 考试时间限制（秒）
 * @property canTake 是否可以参加考试
 * @property lastAttempt 上次考试记录，可为空表示未考过
 */
@kotlinx.serialization.Serializable
data class ExamInfoDto(
    val totalQuestions: Int = 20,
    val passingScore: Int = 80,
    val timeLimit: Int = 1800, // 秒
    val canTake: Boolean = false,
    val lastAttempt: LastAttemptDto? = null
)

/**
 * 上次考试记录数据类
 *
 * @property score 考试分数
 * @property passed 是否通过考试
 * @property takenAt 考试时间
 */
@kotlinx.serialization.Serializable
data class LastAttemptDto(
    val score: Int,
    val passed: Boolean,
    val takenAt: String
)

/**
 * 考试题目数据类
 *
 * @property id 题目ID
 * @property type 题目类型：single-单选题，multiple-多选题
 * @property content 题目内容
 * @property options 选项列表
 */
@kotlinx.serialization.Serializable
data class QuestionDto(
    val id: String,
    val type: String = "single", // single, multiple
    val content: String,
    val options: List<OptionDto> = emptyList()
)

/**
 * 题目选项数据类
 *
 * @property key 选项标识（A, B, C, D等）
 * @property content 选项内容
 */
@kotlinx.serialization.Serializable
data class OptionDto(
    val key: String,
    val content: String
)

/**
 * 考试答案数据类
 *
 * @property questionId 题目ID
 * @property answer 答案内容：单选题为单个字母如"A"，多选题为JSON数组如"[\"A\",\"C\"]"
 */
@kotlinx.serialization.Serializable
data class AnswerDto(
    val questionId: String,
    val answer: String // single: "A", multiple: "[\"A\",\"C\"]"
)

/**
 * 考试提交请求数据类
 *
 * @property answers 答案列表
 */
@kotlinx.serialization.Serializable
data class ExamSubmitRequest(
    val answers: List<AnswerDto>
)

/**
 * 考试结果数据类
 *
 * @property score 考试分数
 * @property passed 是否通过考试
 * @property certificateNo 证书编号，通过后颁发，可为空
 * @property validUntil 证书有效期，可为空
 */
@kotlinx.serialization.Serializable
data class ExamResultDto(
    val score: Int,
    val passed: Boolean,
    val certificateNo: String? = null,
    val validUntil: String? = null
)

/**
 * 培训证书数据类
 *
 * @property certificateNo 证书编号
 * @property driverName 司机姓名
 * @property issueDate 发证日期
 * @property validUntil 有效期至
 * @property status 证书状态：valid-有效，expired-已过期
 */
@kotlinx.serialization.Serializable
data class CertificateDto(
    val certificateNo: String = "",
    val driverName: String = "",
    val issueDate: String = "",
    val validUntil: String = "",
    val status: String = "valid" // valid, expired
)
