package com.viakid.driver.data.repository

import com.viakid.driver.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 培训数据仓库，负责处理司机培训相关的业务逻辑
 *
 * 协调远程API调用，提供统一的培训课程和考试管理接口
 *
 * @property trainingApi 培训API接口，用于执行网络请求
 */
@Singleton
class TrainingRepository @Inject constructor(
    private val trainingApi: TrainingApi
) {
    /**
     * 获取所有培训课程列表
     *
     * @return Result<List<CourseDto>> 获取成功返回课程列表，失败时返回异常
     */
    suspend fun getCourses(): Result<List<CourseDto>> = withContext(Dispatchers.IO) {
        try {
            val response = trainingApi.getCourses()
            if (response.code == ErrorCodes.SUCCESS && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取指定课程的详细信息
     *
     * @param courseId 课程ID
     * @return Result<CourseDetailDto> 获取成功返回课程详情，失败时返回异常
     */
    suspend fun getCourseDetail(courseId: String): Result<CourseDetailDto> = withContext(Dispatchers.IO) {
        try {
            val response = trainingApi.getCourseDetail(courseId)
            if (response.code == ErrorCodes.SUCCESS && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 标记指定课程为已完成状态
     *
     * @param courseId 课程ID
     * @return Result<Unit> 标记成功返回Unit，失败时返回异常
     */
    suspend fun markCourseComplete(courseId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = trainingApi.markCourseComplete(courseId)
            if (response.code == ErrorCodes.SUCCESS) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取考试相关信息，包括题目数量、及格分数等
     *
     * @return Result<ExamInfoDto> 获取成功返回考试信息，失败时返回异常
     */
    suspend fun getExamInfo(): Result<ExamInfoDto> = withContext(Dispatchers.IO) {
        try {
            val response = trainingApi.getExamInfo()
            if (response.code == ErrorCodes.SUCCESS && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取考试题目列表
     *
     * @return Result<List<QuestionDto>> 获取成功返回题目列表，失败时返回异常
     */
    suspend fun getExamQuestions(): Result<List<QuestionDto>> = withContext(Dispatchers.IO) {
        try {
            val response = trainingApi.getExamQuestions()
            if (response.code == ErrorCodes.SUCCESS && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 提交考试答案并获取成绩
     *
     * @param answers 答案列表，每个元素包含题目ID和选择的答案
     * @return Result<ExamResultDto> 提交成功返回考试结果，失败时返回异常
     */
    suspend fun submitExam(answers: List<AnswerDto>): Result<ExamResultDto> = withContext(Dispatchers.IO) {
        try {
            val response = trainingApi.submitExam(answers)
            if (response.code == ErrorCodes.SUCCESS && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取司机培训证书信息
     *
     * @return Result<CertificateDto> 获取成功返回证书信息，失败时返回异常
     */
    suspend fun getCertificate(): Result<CertificateDto> = withContext(Dispatchers.IO) {
        try {
            val response = trainingApi.getCertificate()
            if (response.code == ErrorCodes.SUCCESS && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
