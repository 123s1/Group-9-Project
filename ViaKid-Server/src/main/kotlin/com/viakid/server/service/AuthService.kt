package com.viakid.server.service

import com.viakid.server.config.AppConfig
import com.viakid.server.database.table.Certifications
import com.viakid.server.database.table.Drivers
import com.viakid.server.database.table.RefreshTokens
import com.viakid.server.database.table.Schedules
import com.viakid.server.exception.BusinessException
import com.viakid.server.exception.ErrorCodes
import com.viakid.server.model.*
import com.viakid.server.util.HashUtil
import com.viakid.server.util.JwtUtil
import com.viakid.server.util.SmsUtil
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

class AuthService(private val appConfig: AppConfig) {

    fun sendSmsCode(phone: String, type: String) {
        val code = SmsUtil.generateCode()
        SmsUtil.sendCode(phone, code, type)
    }

    fun register(request: RegisterRequest): AuthData = transaction {
        if (!SmsUtil.verifyCode(request.phone, request.code, "register")) {
            throw BusinessException(ErrorCodes.SMS_CODE_ERROR, "验证码错误或已过期")
        }

        val existing = Drivers.selectAll().where { Drivers.phone eq request.phone }.firstOrNull()
        if (existing != null) {
            throw BusinessException(ErrorCodes.PHONE_ALREADY_REGISTERED, "手机号已注册")
        }

        val driverId = createDriver(request.phone, request.password, request.deviceId)
        createTokens(driverId)
    }

    fun login(request: LoginRequest): AuthData = transaction {
        val driver = Drivers.selectAll().where { Drivers.phone eq request.phone }.firstOrNull()
            ?: throw BusinessException(ErrorCodes.USER_NOT_FOUND, "用户不存在，请先注册")

        if (!HashUtil.verify(request.password, driver[Drivers.passwordHash])) {
            throw BusinessException(ErrorCodes.PASSWORD_ERROR, "密码错误")
        }

        val driverId = driver[Drivers.id].value

        Drivers.update({ Drivers.id eq driverId }) {
            it[deviceId] = request.deviceId
            it[updatedAt] = LocalDateTime.now()
        }

        createTokens(driverId)
    }

    fun smsLogin(request: SmsLoginRequest): AuthData = transaction {
        if (!SmsUtil.verifyCode(request.phone, request.code, "login")) {
            throw BusinessException(ErrorCodes.SMS_CODE_ERROR, "验证码错误或已过期")
        }

        val driver = Drivers.selectAll().where { Drivers.phone eq request.phone }.firstOrNull()
            ?: throw BusinessException(ErrorCodes.USER_NOT_FOUND, "用户不存在")

        Drivers.update({ Drivers.id eq driver[Drivers.id] }) {
            it[deviceId] = request.deviceId
            it[updatedAt] = LocalDateTime.now()
        }

        createTokens(driver[Drivers.id].value)
    }

    fun refresh(refreshToken: String): AuthData = transaction {
        val tokenRecord = RefreshTokens.selectAll().where {
            (RefreshTokens.token eq refreshToken) and (RefreshTokens.expiredAt greater LocalDateTime.now())
        }.firstOrNull() ?: throw BusinessException(ErrorCodes.INVALID_TOKEN, "Refresh Token无效或已过期")
    
        RefreshTokens.deleteWhere { (RefreshTokens.id eq tokenRecord[RefreshTokens.id].value) }
    
        createTokens(tokenRecord[RefreshTokens.driverId].value)
    }
    
    fun logout(driverId: UUID) = transaction {
        RefreshTokens.deleteWhere { (RefreshTokens.driverId eq driverId) }
    }

    fun changePassword(driverId: UUID, request: ChangePasswordRequest) = transaction {
        val driver = Drivers.selectAll().where { Drivers.id eq driverId }.firstOrNull()
            ?: throw BusinessException(ErrorCodes.USER_NOT_FOUND, "用户不存在")

        if (!HashUtil.verify(request.oldPassword, driver[Drivers.passwordHash])) {
            throw BusinessException(ErrorCodes.PASSWORD_ERROR, "旧密码错误")
        }

        Drivers.update({ Drivers.id eq driverId }) {
            it[passwordHash] = HashUtil.hash(request.newPassword)
            it[updatedAt] = LocalDateTime.now()
        }
    }

    private fun createDriver(phone: String, password: String, deviceId: String): UUID {
        val driverId = UUID.randomUUID()
        Drivers.insert {
            it[id] = driverId
            it[this.phone] = phone
            it[passwordHash] = HashUtil.hash(password)
            it[this.deviceId] = deviceId
            it[status] = "pending"
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }

        Schedules.insert {
            it[Schedules.driverId] = driverId
            it[updatedAt] = LocalDateTime.now()
        }

        Certifications.insert {
            it[Certifications.driverId] = driverId
            it[updatedAt] = LocalDateTime.now()
        }

        return driverId
    }

    private fun createTokens(driverId: UUID): AuthData {
        val accessToken = JwtUtil.generateAccessToken(driverId.toString(), appConfig.jwt)
        val refreshToken = JwtUtil.generateRefreshToken(driverId.toString(), appConfig.jwt)

        transaction {
            RefreshTokens.insert {
                it[this.driverId] = driverId
                it[token] = refreshToken
                it[expiredAt] = LocalDateTime.now().plus(appConfig.jwt.refreshTokenValidity, ChronoUnit.MILLIS)
            }
        }

        val driver = transaction {
            Drivers.selectAll().where { Drivers.id eq driverId }.first()
        }

        return AuthData(
            accessToken = accessToken,
            refreshToken = refreshToken,
            driver = DriverInfo(
                id = driverId.toString(),
                phone = driver[Drivers.phone],
                name = driver[Drivers.name] ?: "",
                avatar = driver[Drivers.avatarUrl],
                status = driver[Drivers.status]
            )
        )
    }
}
