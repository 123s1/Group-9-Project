package com.viakid.server.service

import com.viakid.server.database.table.*
import com.viakid.server.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

class DriverService(private val fileService: FileService) {

    fun getMe(driverId: UUID): DriverDetail = transaction {
        val driver = Drivers.selectAll().where { Drivers.id eq driverId }.firstOrNull()
            ?: throw IllegalArgumentException("用户不存在")

        DriverDetail(
            id = driverId.toString(),
            phone = driver[Drivers.phone],
            name = driver[Drivers.name] ?: "",
            avatar = driver[Drivers.avatarUrl],
            status = driver[Drivers.status],
            certificationStatus = loadCertificationStatus(driverId),
            schedule = loadSchedule(driverId)
        )
    }

    fun updateProfile(driverId: UUID, req: ProfileRequest) = transaction {
        Drivers.update({ Drivers.id eq driverId }) {
            it[name] = req.name
            it[gender] = req.gender
            it[birthday] = java.sql.Date.valueOf(req.birthday).toLocalDate()
            it[emergencyContact] = req.emergencyContact
            it[emergencyPhone] = req.emergencyPhone
            it[updatedAt] = LocalDateTime.now()
        }

        Certifications.update({ Certifications.driverId eq driverId }) {
            it[basicInfoCompleted] = true
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun uploadAvatar(driverId: UUID, bytes: ByteArray, fileName: String): String {
        val url = fileService.saveFile(bytes, fileName)
        transaction {
            Drivers.update({ Drivers.id eq driverId }) {
                it[avatarUrl] = url
                it[updatedAt] = LocalDateTime.now()
            }
        }
        return url
    }

    fun getCertification(driverId: UUID): CertificationStatus = transaction {
        loadCertificationStatus(driverId)
    }

    fun uploadCertificate(driverId: UUID, type: String, bytes: ByteArray, fileName: String): CertificateUploadResult {
        val url = fileService.saveFile(bytes, fileName)
        transaction {
            val cert = Certifications.selectAll().where { Certifications.driverId eq driverId }.firstOrNull()
                ?: throw IllegalArgumentException("资质记录不存在")

            val step = maxOf(cert[Certifications.step], 2)
            when (type) {
                "idCardFront" -> Certifications.update({ Certifications.driverId eq driverId }) {
                    it[idCardFrontUrl] = url
                    it[idCardFrontStatus] = "pending"
                    it[this.step] = step
                    it[updatedAt] = LocalDateTime.now()
                }
                "idCardBack" -> Certifications.update({ Certifications.driverId eq driverId }) {
                    it[idCardBackUrl] = url
                    it[idCardBackStatus] = "pending"
                    it[this.step] = step
                    it[updatedAt] = LocalDateTime.now()
                }
                "driverLicense" -> Certifications.update({ Certifications.driverId eq driverId }) {
                    it[driverLicenseUrl] = url
                    it[driverLicenseStatus] = "pending"
                    it[this.step] = step
                    it[updatedAt] = LocalDateTime.now()
                }
                "criminalRecord" -> Certifications.update({ Certifications.driverId eq driverId }) {
                    it[criminalRecordUrl] = url
                    it[criminalRecordStatus] = "pending"
                    it[this.step] = step
                    it[updatedAt] = LocalDateTime.now()
                }
                "healthCert" -> Certifications.update({ Certifications.driverId eq driverId }) {
                    it[healthCertUrl] = url
                    it[healthCertStatus] = "pending"
                    it[this.step] = step
                    it[updatedAt] = LocalDateTime.now()
                }
                "vehicleLicense" -> Certifications.update({ Certifications.driverId eq driverId }) {
                    it[vehicleLicenseUrl] = url
                    it[vehicleLicenseStatus] = "pending"
                    it[this.step] = step
                    it[updatedAt] = LocalDateTime.now()
                }
                else -> throw IllegalArgumentException("不支持的证件类型")
            }
        }
        return CertificateUploadResult(url = url, status = "pending")
    }

    fun getSchedule(driverId: UUID): ScheduleInfo = transaction {
        loadSchedule(driverId)
    }

    fun updateSchedule(driverId: UUID, req: ScheduleInfo) = transaction {
        Schedules.update({ Schedules.driverId eq driverId }) {
            it[timeSlots] = Json.encodeToString(req.timeSlots)
            it[workDays] = Json.encodeToString(req.workDays)
            it[unavailableDates] = Json.encodeToString(req.unavailableDates)
            it[maxOrdersPerDay] = req.maxOrdersPerDay
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun updateOnlineStatus(driverId: UUID, online: Boolean) = transaction {
        Drivers.update({ Drivers.id eq driverId }) {
            it[onlineStatus] = if (online) 1 else 0
            it[updatedAt] = LocalDateTime.now()
        }
    }

    private fun loadCertificationStatus(driverId: UUID): CertificationStatus {
        val cert = Certifications.selectAll().where { Certifications.driverId eq driverId }.firstOrNull()
            ?: return CertificationStatus()

        return CertificationStatus(
            step = cert[Certifications.step],
            basicInfo = BasicInfoStatus(completed = cert[Certifications.basicInfoCompleted]),
            certificates = CertificatesStatus(
                idCardFront = CertificateDetail(url = cert[Certifications.idCardFrontUrl], status = cert[Certifications.idCardFrontStatus]),
                idCardBack = CertificateDetail(url = cert[Certifications.idCardBackUrl], status = cert[Certifications.idCardBackStatus]),
                driverLicense = CertificateDetail(url = cert[Certifications.driverLicenseUrl], status = cert[Certifications.driverLicenseStatus]),
                criminalRecord = CertificateDetail(url = cert[Certifications.criminalRecordUrl], status = cert[Certifications.criminalRecordStatus]),
                healthCert = CertificateDetail(url = cert[Certifications.healthCertUrl], status = cert[Certifications.healthCertStatus]),
                vehicleLicense = CertificateDetail(url = cert[Certifications.vehicleLicenseUrl], status = cert[Certifications.vehicleLicenseStatus])
            ),
            backgroundCheck = BackgroundCheckStatus(
                status = cert[Certifications.backgroundCheckStatus],
                progress = cert[Certifications.backgroundCheckProgress]
            )
        )
    }

    private fun loadSchedule(driverId: UUID): ScheduleInfo {
        val row = Schedules.selectAll().where { Schedules.driverId eq driverId }.firstOrNull()
            ?: return ScheduleInfo()

        return ScheduleInfo(
            timeSlots = row[Schedules.timeSlots]?.let { Json.decodeFromString<List<TimeSlot>>(it) } ?: emptyList(),
            workDays = row[Schedules.workDays]?.let { Json.decodeFromString<List<Int>>(it) } ?: emptyList(),
            unavailableDates = row[Schedules.unavailableDates]?.let { Json.decodeFromString<List<UnavailableDate>>(it) } ?: emptyList(),
            maxOrdersPerDay = row[Schedules.maxOrdersPerDay]
        )
    }
}
