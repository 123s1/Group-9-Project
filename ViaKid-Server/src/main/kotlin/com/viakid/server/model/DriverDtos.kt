package com.viakid.server.model

import kotlinx.serialization.Serializable

@Serializable
data class ProfileRequest(
    val name: String,
    val gender: String,
    val birthday: String,
    val emergencyContact: String,
    val emergencyPhone: String
)

@Serializable
data class AvatarUploadResult(val avatarUrl: String)

@Serializable
data class CertificateUploadResult(val url: String, val status: String)

@Serializable
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

@Serializable
data class CertificationStatus(
    val step: Int = 1,
    val basicInfo: BasicInfoStatus? = null,
    val certificates: CertificatesStatus? = null,
    val backgroundCheck: BackgroundCheckStatus? = null
)

@Serializable
data class BasicInfoStatus(val completed: Boolean = false)

@Serializable
data class CertificatesStatus(
    val idCardFront: CertificateDetail? = null,
    val idCardBack: CertificateDetail? = null,
    val driverLicense: CertificateDetail? = null,
    val criminalRecord: CertificateDetail? = null,
    val healthCert: CertificateDetail? = null,
    val vehicleLicense: CertificateDetail? = null
)

@Serializable
data class CertificateDetail(val url: String? = null, val status: String = "pending")

@Serializable
data class BackgroundCheckStatus(
    val status: String = "pending",
    val progress: Int = 0,
    val estimatedTime: String? = null
)

@Serializable
data class TrainingStatus(
    val totalCourses: Int = 6,
    val completedCourses: Int = 0,
    val examScore: Int? = null,
    val passed: Boolean = false,
    val certificateNo: String? = null
)

@Serializable
data class ScheduleInfo(
    val timeSlots: List<TimeSlot> = emptyList(),
    val workDays: List<Int> = emptyList(),
    val unavailableDates: List<UnavailableDate> = emptyList(),
    val maxOrdersPerDay: Int = 8
)

@Serializable
data class TimeSlot(
    val type: String = "morning",
    val start: String = "07:00",
    val end: String = "09:00",
    val enabled: Boolean = true
)

@Serializable
data class UnavailableDate(val start: String, val end: String, val reason: String = "")

@Serializable
data class OnlineStatusRequest(val online: Boolean)
