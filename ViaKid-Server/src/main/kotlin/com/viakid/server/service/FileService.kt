package com.viakid.server.service

import com.viakid.server.exception.BusinessException
import java.io.File
import java.util.*

class FileService(private val uploadDir: String) {

    companion object {
        private const val MAX_FILE_SIZE = 5 * 1024 * 1024L // 5MB
        private val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp")
        private val ALLOWED_MIME_PREFIXES = setOf("image/")
    }

    init {
        File(uploadDir).mkdirs()
    }

    fun saveFile(bytes: ByteArray, originalName: String): String {
        if (bytes.size > MAX_FILE_SIZE) {
            throw BusinessException(4010, "文件大小超过限制（最大 5MB）")
        }

        val ext = originalName.substringAfterLast(".", "").lowercase()
        if (ext !in ALLOWED_EXTENSIONS) {
            throw BusinessException(4011, "不支持的文件类型，仅允许：${ALLOWED_EXTENSIONS.joinToString()}")
        }

        val fileName = "${UUID.randomUUID()}.$ext"
        val file = File(uploadDir, fileName)
        file.writeBytes(bytes)
        return "/uploads/$fileName"
    }
}
