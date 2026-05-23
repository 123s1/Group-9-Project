package com.viakid.driver.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 认证信息实体类，用于在本地数据库中存储司机的认证进度和相关资料
 *
 * @property userId 用户ID，作为主键唯一标识每个用户的认证信息
 * @property step 当前认证步骤：1-基础信息，2-证件上传，3-背景调查，4-审核完成
 * @property basicInfoCompleted 基础信息是否已完成填写
 * @property idCardFrontUrl 身份证正面图片的URL地址
 * @property idCardFrontStatus 身份证正面审核状态
 * @property idCardBackUrl 身份证反面图片的URL地址
 * @property idCardBackStatus 身份证反面审核状态
 * @property driverLicenseUrl 驾驶证图片的URL地址
 * @property driverLicenseStatus 驾驶证审核状态
 * @property criminalRecordUrl 无犯罪记录证明图片的URL地址
 * @property criminalRecordStatus 无犯罪记录证明审核状态
 * @property healthCertUrl 健康证图片的URL地址
 * @property healthCertStatus 健康证审核状态
 * @property vehicleLicenseUrl 行驶证图片的URL地址
 * @property vehicleLicenseStatus 行驶证审核状态
 * @property backgroundCheckStatus 背景调查整体状态
 * @property backgroundCheckProgress 背景调查进度百分比（0-100）
 * @property updatedAt 最后更新时间戳（毫秒）
 */
@Entity(tableName = "certifications")
data class CertificationEntity(
    @PrimaryKey
    val userId: String,
    val step: Int, // 1:基础信息 2:证件上传 3:背景调查 4:审核完成
    val basicInfoCompleted: Boolean = false,
    val idCardFrontUrl: String?,
    val idCardFrontStatus: String?,
    val idCardBackUrl: String?,
    val idCardBackStatus: String?,
    val driverLicenseUrl: String?,
    val driverLicenseStatus: String?,
    val criminalRecordUrl: String?,
    val criminalRecordStatus: String?,
    val healthCertUrl: String?,
    val healthCertStatus: String?,
    val vehicleLicenseUrl: String?,
    val vehicleLicenseStatus: String?,
    val backgroundCheckStatus: String?,
    val backgroundCheckProgress: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)