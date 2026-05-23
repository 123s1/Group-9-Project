package com.viakid.server.database.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime

object QualificationDocuments : UUIDTable("qualification_documents") {
    val qualificationId = reference("qualification_id", Qualifications)
    val docName = varchar("doc_name", 100)
    val docUrl = varchar("doc_url", 500)
    val docType = varchar("doc_type", 20)  // IMAGE/PDF/VIDEO
    val fileSize = integer("file_size").nullable()
    val sort = integer("sort").default(0)
    val createdAt = datetime("created_at")
}
