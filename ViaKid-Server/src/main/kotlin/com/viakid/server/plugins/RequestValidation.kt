package com.viakid.server.plugins

import com.viakid.server.model.*
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*

fun Application.configureRequestValidation() {
    install(RequestValidation) {
        validate<SendCodeRequest> { req ->
            when {
                !req.phone.matches(Regex("^1[3-9]\\d{9}$")) ->
                    ValidationResult.Invalid("手机号格式不正确")
                req.type !in listOf("register", "login", "reset") ->
                    ValidationResult.Invalid("无效的验证码类型")
                else -> ValidationResult.Valid
            }
        }

        validate<RegisterRequest> { req ->
            when {
                !req.phone.matches(Regex("^1[3-9]\\d{9}$")) ->
                    ValidationResult.Invalid("手机号格式不正确")
                req.code.length != 6 ->
                    ValidationResult.Invalid("验证码为6位")
                req.password.length < 6 || req.password.length > 32 ->
                    ValidationResult.Invalid("密码长度需为6-32位")
                req.deviceId.isBlank() ->
                    ValidationResult.Invalid("设备ID不能为空")
                else -> ValidationResult.Valid
            }
        }

        validate<LoginRequest> { req ->
            when {
                !req.phone.matches(Regex("^1[3-9]\\d{9}$")) ->
                    ValidationResult.Invalid("手机号格式不正确")
                req.password.length < 6 || req.password.length > 32 ->
                    ValidationResult.Invalid("密码长度需为6-32位")
                req.deviceId.isBlank() ->
                    ValidationResult.Invalid("设备ID不能为空")
                else -> ValidationResult.Valid
            }
        }

        validate<SmsLoginRequest> { req ->
            when {
                !req.phone.matches(Regex("^1[3-9]\\d{9}$")) ->
                    ValidationResult.Invalid("手机号格式不正确")
                req.code.length != 6 ->
                    ValidationResult.Invalid("验证码为6位")
                req.deviceId.isBlank() ->
                    ValidationResult.Invalid("设备ID不能为空")
                else -> ValidationResult.Valid
            }
        }

        validate<ChangePasswordRequest> { req ->
            when {
                req.oldPassword.length < 6 || req.oldPassword.length > 32 ->
                    ValidationResult.Invalid("旧密码长度需为6-32位")
                req.newPassword.length < 6 || req.newPassword.length > 32 ->
                    ValidationResult.Invalid("新密码长度需为6-32位")
                else -> ValidationResult.Valid
            }
        }

        validate<OrderStatusRequest> { req ->
            val validStatuses = setOf(
                "pending", "assigned", "departed", "arrived",
                "picked_up", "delivered", "completed", "cancelled"
            )
            if (req.status !in validStatuses) {
                ValidationResult.Invalid("无效的订单状态：${req.status}")
            } else {
                ValidationResult.Valid
            }
        }

        validate<RejectRequest> { req ->
            if (req.reason.isBlank()) {
                ValidationResult.Invalid("拒单原因不能为空")
            } else {
                ValidationResult.Valid
            }
        }

        validate<ProfileRequest> { req ->
            when {
                req.name.isBlank() || req.name.length > 50 ->
                    ValidationResult.Invalid("姓名长度需为1-50位")
                req.gender !in listOf("male", "female", "other") ->
                    ValidationResult.Invalid("性别值无效")
                else -> ValidationResult.Valid
            }
        }
    }
}
