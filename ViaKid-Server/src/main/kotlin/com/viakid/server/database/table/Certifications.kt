package com.viakid.server.database.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Certifications : Table("certifications") {
    val driverId = reference("driver_id", Drivers)
    val step = integer("step").default(1)
    val basicInfoCompleted = bool("basic_info_completed").default(false)
    val idCardFrontUrl = varchar("id_card_front_url", 500).nullable()
    val idCardFrontStatus = varchar("id_card_front_status", 20).default("pending")
    val idCardBackUrl = varchar("id_card_back_url", 500).nullable()
    val idCardBackStatus = varchar("id_card_back_status", 20).default("pending")
    val driverLicenseUrl = varchar("driver_license_url", 500).nullable()
    val driverLicenseStatus = varchar("driver_license_status", 20).default("pending")
    val criminalRecordUrl = varchar("criminal_record_url", 500).nullable()
    val criminalRecordStatus = varchar("criminal_record_status", 20).default("pending")
    val healthCertUrl = varchar("health_cert_url", 500).nullable()
    val healthCertStatus = varchar("health_cert_status", 20).default("pending")
    val vehicleLicenseUrl = varchar("vehicle_license_url", 500).nullable()
    val vehicleLicenseStatus = varchar("vehicle_license_status", 20).default("pending")
    val backgroundCheckStatus = varchar("background_check_status", 20).default("pending")
    val backgroundCheckProgress = integer("background_check_progress").default(0)
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(driverId)
}
