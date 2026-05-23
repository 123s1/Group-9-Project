package com.viakid.driver.data.repository

import com.viakid.driver.data.local.database.CertificationDao
import com.viakid.driver.data.local.database.CertificationEntity
import com.viakid.driver.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 认证数据仓库，负责处理司机相关的业务逻辑
 *
 * 协调本地数据库和远程API调用，提供统一的司机管理接口
 *
 * @property certificationDao 认证数据访问对象，用于操作认证信息
 * @property driverApi 司机API接口，用于执行网络请求
 */
@Singleton
class DriverRepository @Inject constructor(
    private val certificationDao: CertificationDao,
    private val driverApi: DriverApi
) {
    /**
     * 获取司机详细信息
     *
     * @return Result<DriverDetail> 获取成功返回司机详细信息，失败时返回异常
     */
    suspend fun getDriverDetail(): Result<DriverDetail> = withContext(Dispatchers.IO) {
        try {
            val response = driverApi.getMe()
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
     * 更新司机基础资料
     *
     * @param name 姓名
     * @param gender 性别
     * @param birthday 生日（格式：yyyy-MM-dd）
     * @param emergencyContact 紧急联系人姓名
     * @param emergencyPhone 紧急联系人电话
     * @return Result<Unit> 更新成功返回Unit，失败时返回异常
     */
    suspend fun updateProfile(
        name: String,
        gender: String,
        birthday: String,
        emergencyContact: String,
        emergencyPhone: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = driverApi.updateProfile(name, gender, birthday, emergencyContact, emergencyPhone)
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
     * 上传司机头像
     *
     * @param fileBytes 图片文件的字节数组
     * @param fileName 文件名
     * @return Result<String> 上传成功返回头像URL，失败时返回异常
     */
    suspend fun uploadAvatar(fileBytes: ByteArray, fileName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = driverApi.uploadAvatar(fileBytes, fileName)
            if (response.code == ErrorCodes.SUCCESS && response.data != null) {
                Result.success(response.data.avatarUrl)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取资质认证状态，并同步到本地数据库
     *
     * @return Result<CertificationStatus> 获取成功返回认证状态，失败时返回异常
     */
    suspend fun getCertificationStatus(): Result<CertificationStatus> = withContext(Dispatchers.IO) {
        try {
            val response = driverApi.getCertification()
            if (response.code == ErrorCodes.SUCCESS && response.data != null) {
                certificationDao.insertCertification(response.data.toEntity())
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 上传资质证件
     *
     * @param type 证件类型（id_card_front, id_card_back, driver_license等）
     * @param fileBytes 证件图片的字节数组
     * @param fileName 文件名
     * @return Result<String> 上传成功返回证件URL，失败时返回异常
     */
    suspend fun uploadCertificate(
        type: String,
        fileBytes: ByteArray,
        fileName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = driverApi.uploadCertificate(type, fileBytes, fileName)
            if (response.code == ErrorCodes.SUCCESS && response.data != null) {
                Result.success(response.data.url)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取司机排班信息
     *
     * @return Result<ScheduleInfo> 获取成功返回排班信息，失败时返回异常
     */
    suspend fun getSchedule(): Result<ScheduleInfo> = withContext(Dispatchers.IO) {
        try {
            val response = driverApi.getSchedule()
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
     * 更新司机排班信息
     *
     * @param schedule 新的排班信息
     * @return Result<Unit> 更新成功返回Unit，失败时返回异常
     */
    suspend fun updateSchedule(schedule: ScheduleInfo): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = driverApi.updateSchedule(schedule)
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
     * 更新司机在线/离线状态
     *
     * @param online true-上线接单，false-下线休息
     * @return Result<Unit> 更新成功返回Unit，失败时返回异常
     */
    suspend fun updateOnlineStatus(online: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = driverApi.updateOnlineStatus(online)
            if (response.code == ErrorCodes.SUCCESS) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * 将CertificationStatus转换为CertificationEntity的扩展函数
 *
 * @return CertificationEntity 转换后的认证实体对象
 */
internal fun CertificationStatus.toEntity() = CertificationEntity(
    userId = "",
    step = step,
    basicInfoCompleted = basicInfo?.completed ?: false,
    idCardFrontUrl = certificates?.idCardFront?.url,
    idCardFrontStatus = certificates?.idCardFront?.status,
    idCardBackUrl = certificates?.idCardBack?.url,
    idCardBackStatus = certificates?.idCardBack?.status,
    driverLicenseUrl = certificates?.driverLicense?.url,
    driverLicenseStatus = certificates?.driverLicense?.status,
    criminalRecordUrl = certificates?.criminalRecord?.url,
    criminalRecordStatus = certificates?.criminalRecord?.status,
    healthCertUrl = certificates?.healthCert?.url,
    healthCertStatus = certificates?.healthCert?.status,
    vehicleLicenseUrl = certificates?.vehicleLicense?.url,
    vehicleLicenseStatus = certificates?.vehicleLicense?.status,
    backgroundCheckStatus = backgroundCheck?.status,
    backgroundCheckProgress = backgroundCheck?.progress ?: 0
)
