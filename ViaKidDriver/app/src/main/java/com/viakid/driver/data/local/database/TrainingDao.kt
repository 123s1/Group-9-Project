package com.viakid.driver.data.local.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 培训进度数据访问对象，提供对本地数据库中培训课程进度的增删改查操作
 */
@Dao
interface TrainingDao {
    /**
     * 获取所有培训课程列表，按标题升序排列，以Flow形式返回支持响应式更新
     *
     * @return Flow<List<TrainingProgressEntity>> 培训进度实体列表的Flow流
     */
    @Query("SELECT * FROM training_progress ORDER BY title ASC")
    fun getAllCourses(): Flow<List<TrainingProgressEntity>>

    /**
     * 同步根据课程ID获取单个课程的培训进度，适用于协程作用域内的单次查询
     *
     * @param courseId 课程ID
     * @return TrainingProgressEntity? 培训进度实体，如果不存在则返回null
     */
    @Query("SELECT * FROM training_progress WHERE courseId = :courseId")
    suspend fun getCourse(courseId: String): TrainingProgressEntity?

    /**
     * 获取所有必修课程列表，以Flow形式返回支持响应式更新
     *
     * @return Flow<List<TrainingProgressEntity>> 必修课程进度实体列表的Flow流
     */
    @Query("SELECT * FROM training_progress WHERE type = 'required'")
    fun getRequiredCourses(): Flow<List<TrainingProgressEntity>>

    /**
     * 获取已完成课程的数量，以Flow形式返回支持响应式更新
     *
     * @return Flow<Int> 已完成课程数量的Flow流
     */
    @Query("SELECT COUNT(*) FROM training_progress WHERE status = 'completed'")
    fun getCompletedCourseCount(): Flow<Int>

    /**
     * 获取必修课程的总数量，以Flow形式返回支持响应式更新
     *
     * @return Flow<Int> 必修课程数量的Flow流
     */
    @Query("SELECT COUNT(*) FROM training_progress WHERE type = 'required'")
    fun getRequiredCourseCount(): Flow<Int>

    /**
     * 插入或替换单个培训课程进度，如果已存在相同主键的记录则进行替换
     *
     * @param course 要插入或更新的培训进度实体对象
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: TrainingProgressEntity)

    /**
     * 批量插入或替换培训课程进度列表，如果已存在相同主键的记录则进行替换
     *
     * @param courses 要插入或更新的培训进度实体列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<TrainingProgressEntity>)

    /**
     * 更新已有的培训课程进度信息，要求培训进度实体必须存在于数据库中
     *
     * @param course 包含更新数据的培训进度实体对象
     */
    @Update
    suspend fun updateCourse(course: TrainingProgressEntity)

    /**
     * 更新指定课程的培训进度，包括状态、最后观看位置和更新时间
     *
     * @param courseId 课程ID
     * @param status 新的课程状态（如：in_progress, completed等）
     * @param position 最后观看位置（毫秒），用于记录视频播放进度
     * @param updatedAt 更新时间戳（毫秒），默认为当前时间
     */
    @Query("UPDATE training_progress SET status = :status, lastPosition = :position, updatedAt = :updatedAt WHERE courseId = :courseId")
    suspend fun updateCourseProgress(courseId: String, status: String, position: Long, updatedAt: Long = System.currentTimeMillis())

    /**
     * 删除所有培训课程进度数据，慎用此方法
     */
    @Query("DELETE FROM training_progress")
    suspend fun deleteAllCourses()
}

/**
 * 考试结果数据访问对象，提供对本地数据库中考试结果的增删改查操作
 */
@Dao
interface ExamResultDao {
    /**
     * 获取最新的考试结果，以Flow形式返回支持响应式更新
     *
     * @return Flow<ExamResultEntity?> 最新考试结果实体的Flow流
     */
    @Query("SELECT * FROM exam_results WHERE id = 'latest'")
    fun getLatestExamResult(): Flow<ExamResultEntity?>

    /**
     * 同步获取最新的考试结果，适用于协程作用域内的单次查询
     *
     * @return ExamResultEntity? 最新考试结果实体，如果不存在则返回null
     */
    @Query("SELECT * FROM exam_results WHERE id = 'latest'")
    suspend fun getLatestExamResultSync(): ExamResultEntity?

    /**
     * 插入或替换考试结果，使用固定ID 'latest' 确保只保留最新的一次考试结果
     *
     * @param result 要插入或更新的考试结果实体对象
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamResult(result: ExamResultEntity)

    /**
     * 删除所有考试结果数据
     */
    @Query("DELETE FROM exam_results")
    suspend fun deleteAllExamResults()
}
