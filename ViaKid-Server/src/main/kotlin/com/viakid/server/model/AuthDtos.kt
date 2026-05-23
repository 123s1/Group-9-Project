package com.viakid.server.model

import kotlinx.serialization.Serializable

@Serializable
data class SendCodeRequest(val phone: String, val type: String)

@Serializable
data class RegisterRequest(
    val phone: String,
    val code: String,
    val password: String,
    val deviceId: String
)

@Serializable
data class LoginRequest(
    val phone: String,
    val password: String,
    val deviceId: String
)

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

@Serializable
data class SmsLoginRequest(
    val phone: String,
    val code: String,
    val deviceId: String
)

@Serializable
data class AuthData(
    val accessToken: String,
    val refreshToken: String,
    val driver: DriverInfo
)

@Serializable
data class DriverInfo(
    val id: String,
    val phone: String,
    val name: String,
    val avatar: String?,
    val status: String
)
