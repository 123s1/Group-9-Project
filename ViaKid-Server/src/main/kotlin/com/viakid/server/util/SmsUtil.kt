package com.viakid.server.util

import com.viakid.server.database.table.SmsCodes
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

object SmsUtil {
    fun generateCode(): String = (100000..999999).random().toString()

    fun sendCode(phone: String, code: String, type: String) {
        transaction {
            SmsCodes.insert {
                it[SmsCodes.phone] = phone
                it[SmsCodes.code] = code
                it[SmsCodes.type] = type
                it[SmsCodes.expiredAt] = LocalDateTime.now().plusMinutes(5)
                it[SmsCodes.used] = false
            }
        }
        println("[SMS] To $phone: Code is $code (type=$type)")
    }

    fun verifyCode(phone: String, code: String, type: String): Boolean {
        return transaction {
            val record = SmsCodes.selectAll()
                .where {
                    (SmsCodes.phone eq phone) and
                    (SmsCodes.code eq code) and
                    (SmsCodes.type eq type) and
                    (SmsCodes.used eq false)
                }
                .firstOrNull()

            if (record != null && record[SmsCodes.expiredAt].isAfter(LocalDateTime.now())) {
                SmsCodes.update({ SmsCodes.id eq record[SmsCodes.id] }) {
                    it[SmsCodes.used] = true
                }
                true
            } else {
                false
            }
        }
    }
}
