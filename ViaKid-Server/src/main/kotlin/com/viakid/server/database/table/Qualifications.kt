package com.viakid.server.database.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime

object Qualifications : UUIDTable("qualifications") {
    val driverId = reference("driver_id", Drivers)
    val qualType = integer("qual_type")  // 1-身份证 2-驾驶证 3-行驶证 4-无犯罪记录 5-健康证明
    val qualName = varchar("qual_name", 50)
    val status = integer("status").default(0)  // 0-待提交 1-审核中 2-通过 3-驳回
    val rejectReason = varchar("reject_reason", 255).nullable()
    val expireDate = date("expire_date").nullable()
    val verifyTime = datetime("verify_time").nullable()
    val verifyAdminId = uuid("verify_admin_id").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}
