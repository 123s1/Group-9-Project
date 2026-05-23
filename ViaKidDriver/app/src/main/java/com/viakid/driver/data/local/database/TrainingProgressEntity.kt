package com.viakid.driver.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 培训进度实体类，用于在本地数据库中存储培训课程的学习进度信息
 *
 * @property courseId 课程ID，作为主键唯一标识每个培训课程
 * @property title 课程标题
 * @property description 课程描述信息
 * @property duration 课程时长（格式如：15:30 表示15分30秒）
 * @property videoUrl 课程视频URL地址，可为空
 * @property type 课程类型：required-必修课程，optional-选修课程
 * @property status 课程学习状态：not_started-未开始，in_progress-学习中，completed-已完成
 * @property lastPosition 视频最后播放位置（毫秒），用于记录学习进度
 * @property updatedAt 最后更新时间戳（毫秒）
 */
@Entity(tableName = "training_progress")
data class TrainingProgressEntity(
    @PrimaryKey
    val courseId: String,
    val title: String,
    val description: String,
    val duration: String,
    val videoUrl: String?,
    val type: String, // required, optional
    val status: String, // not_started, in_progress, completed
    val lastPosition: Long = 0, // 视频播放位置
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 考试结果实体类，用于在本地数据库中存储司机的考试成绩和证书信息
 *
 * @property id 记录ID，固定为 'latest' 以确保只保留最新的一次考试结果
 * @property score 考试得分，可为空表示尚未参加考试
 * @property passed 是否通过考试，可为空表示尚未参加考试
 * @property certificateNo 证书编号，通过考试后颁发，可为空
 * @property validUntil 证书有效期截止日期（格式：yyyy-MM-dd），可为空
 * @property takenAt 参加考试的时间戳（毫秒）
 */
@Entity(tableName = "exam_results")
data class ExamResultEntity(
    @PrimaryKey
    val id: String = "latest",
    val score: Int?,
    val passed: Boolean?,
    val certificateNo: String?,
    val validUntil: String?,
    val takenAt: Long = System.currentTimeMillis()
)