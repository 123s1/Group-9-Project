package com.viakid.driver.data.remote

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 接送员模块 API
 * 专员端专用
 *
 * @property apiClient API客户端实例，用于执行网络请求
 */
@Singleton
class DriverApi @Inject constructor(
    private val apiClient: ApiClient
) {

    /**
     * 获取当前用户详细信息
     *
     * @return ApiResponse<DriverDetail> 包含司机详细信息的响应
     */
    suspend fun getMe(): ApiResponse<DriverDetail> {
        return apiClient.client.get("driver/me").body()
    }

    /**
     * 更新司机基础资料信息
     *
     * @param name 姓名
     * @param gender 性别
     * @param birthday 生日（格式：yyyy-MM-dd）
     * @param emergencyContact 紧急联系人姓名
     * @param emergencyPhone 紧急联系人电话
     * @return ApiResponse<Unit> API响应，成功时data为null
     */
    suspend fun updateProfile(
        name: String,
        gender: String,
        birthday: String,
        emergencyContact: String,
        emergencyPhone: String
    ): ApiResponse<Unit> {
        return apiClient.client.put("driver/profile") {
            setBody(ProfileRequest(name, gender, birthday, emergencyContact, emergencyPhone))
        }.body()
    }

    /**
     * 上传司机头像图片
     *
     * @param fileBytes 图片文件的字节数组
     * @param fileName 文件名
     * @return ApiResponse<AvatarUploadResult> 包含头像URL的响应
     */
    suspend fun uploadAvatar(fileBytes: ByteArray, fileName: String): ApiResponse<AvatarUploadResult> {
        return apiClient.client.post("driver/avatar") {
            setBody(fileBytes)
            contentType(ContentType.MultiPart.FormData.withParameter("filename", fileName))
        }.body()
    }

    /**
     * 获取资质认证状态和进度
     *
     * @return ApiResponse<CertificationStatus> 包含认证状态的响应
     */
    suspend fun getCertification(): ApiResponse<CertificationStatus> {
        return apiClient.client.get("driver/certification").body()
    }

    /**
     * 上传资质证件图片
     *
     * @param type 证件类型（id_card_front, id_card_back, driver_license等）
     * @param fileBytes 证件图片的字节数组
     * @param fileName 文件名
     * @return ApiResponse<CertificateUploadResult> 包含证件URL和审核状态的响应
     */
    suspend fun uploadCertificate(
        type: String,
        fileBytes: ByteArray,
        fileName: String
    ): ApiResponse<CertificateUploadResult> {
        return apiClient.client.post("driver/certification/certificate") {
            setBody(fileBytes)
            contentType(ContentType.MultiPart.FormData.withParameter("filename", fileName))
            parameter("type", type)
        }.body()
    }

    /**
     * 获取司机的排班信息
     *
     * @return ApiResponse<ScheduleInfo> 包含排班信息的响应
     */
    suspend fun getSchedule(): ApiResponse<ScheduleInfo> {
        return apiClient.client.get("driver/schedule").body()
    }

    /**
     * 更新司机的排班信息
     *
     * @param schedule 新的排班信息
     * @return ApiResponse<Unit> API响应，成功时data为null
     */
    suspend fun updateSchedule(schedule: ScheduleInfo): ApiResponse<Unit> {
        return apiClient.client.put("driver/schedule") {
            setBody(schedule)
        }.body()
    }

    /**
     * 切换司机在线/离线状态
     *
     * @param online true-上线接单，false-下线休息
     * @return ApiResponse<Unit> API响应，成功时data为null
     */
    suspend fun updateOnlineStatus(online: Boolean): ApiResponse<Unit> {
        return apiClient.client.post("driver/status") {
            setBody(mapOf("online" to online))
        }.body()
    }
}

/**
 * 更新个人资料请求数据类
 *
 * @property name 姓名
 * @property gender 性别
 * @property birthday 生日（格式：yyyy-MM-dd）
 * @property emergencyContact 紧急联系人姓名
 * @property emergencyPhone 紧急联系人电话
 */
@kotlinx.serialization.Serializable
data class ProfileRequest(
    val name: String,
    val gender: String,
    val birthday: String,
    val emergencyContact: String,
    val emergencyPhone: String
)

/**
 * 头像上传结果数据类
 *
 * @property avatarUrl 上传后的头像URL地址
 */
@kotlinx.serialization.Serializable
data class AvatarUploadResult(
    val avatarUrl: String
)

/**
 * 证件上传结果数据类
 *
 * @property url 上传后的证件URL地址
 * @property status 审核状态：pending-待审核，approved-已通过，rejected-已拒绝
 */
@kotlinx.serialization.Serializable
data class CertificateUploadResult(
    val url: String,
    val status: String
)

/**
 * 司机详细信息数据类
 *
 * @property id 司机ID
 * @property phone 手机号
 * @property name 姓名
 * @property avatar 头像URL，可为空
 * @property status 司机状态：pending-待审核，approved-已通过等
 * @property certificationStatus 资质认证状态，可为空
 * @property trainingStatus 培训状态，可为空
 * @property schedule 排班信息，可为空
 */
@kotlinx.serialization.Serializable
data class DriverDetail(
    val id: String,
    val phone: String,
    val name: String = "",
    val avatar: String? = null,
    val status: String = "pending",
    val certificationStatus: CertificationStatus? = null,
    val trainingStatus: TrainingStatus? = null,
    val schedule: ScheduleInfo? = null
)

/**
 * 资质认证状态数据类
 *
 * @property step 当前认证步骤：1-基础信息，2-证件上传，3-背景调查
 * @property basicInfo 基础信息完成状态，可为空
 * @property certificates 各类证件的上传和审核状态，可为空
 * @property backgroundCheck 背景调查状态，可为空
 */
@kotlinx.serialization.Serializable
data class CertificationStatus(
    val step: Int = 1,
    val basicInfo: BasicInfoStatus? = null,
    val certificates: CertificatesStatus? = null,
    val backgroundCheck: BackgroundCheckStatus? = null
)

/**
 * 基础信息完成状态数据类
 *
 * @property completed 是否已完成基础信息填写
 */
@kotlinx.serialization.Serializable
data class BasicInfoStatus(
    val completed: Boolean = false
)

/**
 * 各类证件状态数据类
 *
 * @property idCardFront 身份证正面信息，可为空
 * @property idCardBack 身份证反面信息，可为空
 * @property driverLicense 驾驶证信息，可为空
 * @property criminalRecord 无犯罪记录证明信息，可为空
 * @property healthCert 健康证信息，可为空
 * @property vehicleLicense 行驶证信息，可为空
 */
@kotlinx.serialization.Serializable
data class CertificatesStatus(
    val idCardFront: CertificateDetail? = null,
    val idCardBack: CertificateDetail? = null,
    val driverLicense: CertificateDetail? = null,
    val criminalRecord: CertificateDetail? = null,
    val healthCert: CertificateDetail? = null,
    val vehicleLicense: CertificateDetail? = null
)

/**
 * 单个证件详细信息数据类
 *
 * @property url 证件图片URL，可为空
 * @property status 审核状态：pending-待审核，approved-已通过，rejected-已拒绝
 */
@kotlinx.serialization.Serializable
data class CertificateDetail(
    val url: String? = null,
    val status: String = "pending" // approved, pending, rejected
)

/**
 * 背景调查状态数据类
 *
 * @property status 背景调查状态：pending-待处理，processing-处理中，approved-已通过，rejected-已拒绝
 * @property progress 背景调查进度百分比（0-100）
 * @property estimatedTime 预计完成时间，可为空
 */
@kotlinx.serialization.Serializable
data class BackgroundCheckStatus(
    val status: String = "pending", // pending, processing, approved, rejected
    val progress: Int = 0,
    val estimatedTime: String? = null
)

/**
 * 培训状态数据类
 *
 * @property totalCourses 总课程数量
 * @property completedCourses 已完成课程数量
 * @property examScore 考试分数，可为空表示未考试
 * @property passed 是否通过考试
 * @property certificateNo 证书编号，通过考试后颁发，可为空
 */
@kotlinx.serialization.Serializable
data class TrainingStatus(
    val totalCourses: Int = 6,
    val completedCourses: Int = 0,
    val examScore: Int? = null,
    val passed: Boolean = false,
    val certificateNo: String? = null
)

/**
 * 排班信息数据类
 *
 * @property timeSlots 可用时间段列表
 * @property workDays 工作日期列表（1-7，代表周一到周日）
 * @property unavailableDates 不可用日期列表
 * @property maxOrdersPerDay 每日最大订单数
 */
@kotlinx.serialization.Serializable
data class ScheduleInfo(
    val timeSlots: List<TimeSlot> = emptyList(),
    val workDays: List<Int> = emptyList(), // 1-7, 周一到周日
    val unavailableDates: List<UnavailableDate> = emptyList(),
    val maxOrdersPerDay: Int = 8
)

/**
 * 时间段数据类
 *
 * @property type 时间段类型：morning-上午，afternoon-下午，evening-晚上
 * @property start 开始时间（格式：HH:mm）
 * @property end 结束时间（格式：HH:mm）
 * @property enabled 是否启用该时间段
 */
@kotlinx.serialization.Serializable
data class TimeSlot(
    val type: String = "morning", // morning, afternoon, evening
    val start: String = "07:00",
    val end: String = "09:00",
    val enabled: Boolean = true
)

/**
 * 不可用日期数据类
 *
 * @property start 不可用开始日期（格式：yyyy-MM-dd）
 * @property end 不可用结束日期（格式：yyyy-MM-dd）
 * @property reason 不可用原因说明
 */
@kotlinx.serialization.Serializable
data class UnavailableDate(
    val start: String,
    val end: String,
    val reason: String = ""
)
