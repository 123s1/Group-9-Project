package com.viakid.server.service

import io.ktor.server.application.*
import java.io.File
import java.util.*

class FileService(private val uploadDir: String) {
    init {
        File(uploadDir).mkdirs()
    }

    fun saveFile(bytes: ByteArray, originalName: String): String {
        val ext = originalName.substringAfterLast(".", "bin")
        val fileName = "${UUID.randomUUID()}.$ext"
        val file = File(uploadDir, fileName)
        file.writeBytes(bytes)
        return "/uploads/$fileName"
    }
}
