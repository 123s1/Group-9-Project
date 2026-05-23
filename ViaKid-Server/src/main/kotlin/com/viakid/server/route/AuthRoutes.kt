package com.viakid.server.route

import com.viakid.server.model.*
import com.viakid.server.service.AuthService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.*

fun Route.authRoutes() {
    val authService by inject<AuthService>()

    route("/auth") {
        post("/sms/send") {
            val req = call.receive<SendCodeRequest>()
            authService.sendSmsCode(req.phone, req.type)
            call.respond(ApiResponse<Unit>(code = 0, message = "success"))
        }

        post("/register") {
            val req = call.receive<RegisterRequest>()
            val data = authService.register(req)
            call.respond(ApiResponse(code = 0, message = "success", data = data))
        }

        post("/login") {
            val req = call.receive<LoginRequest>()
            val data = authService.login(req)
            call.respond(ApiResponse(code = 0, message = "success", data = data))
        }

        post("/login/sms") {
            val req = call.receive<SmsLoginRequest>()
            val data = authService.smsLogin(req)
            call.respond(ApiResponse(code = 0, message = "success", data = data))
        }

        post("/refresh") {
            val req = call.receive<RefreshRequest>()
            val data = authService.refresh(req.refreshToken)
            call.respond(ApiResponse(code = 0, message = "success", data = data))
        }

        authenticate("auth-jwt") {
            post("/logout") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                    ?: throw IllegalArgumentException("无效的Token")
                authService.logout(UUID.fromString(userId))
                call.respond(ApiResponse<Unit>(code = 0, message = "success"))
            }

            post("/password/change") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                    ?: throw IllegalArgumentException("无效的Token")
                val req = call.receive<ChangePasswordRequest>()
                authService.changePassword(UUID.fromString(userId), req)
                call.respond(ApiResponse<Unit>(code = 0, message = "success"))
            }
        }
    }
}
