package com.viakid.server.plugins

import com.viakid.server.exception.BusinessException
import com.viakid.server.model.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    val isDev = System.getenv("KTOR_ENV")?.lowercase() == "dev"
        || environment.config.propertyOrNull("ktor.development")?.getString()?.toBoolean() == true

    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Nothing>(
                    code = 400,
                    message = cause.reasons.joinToString("; ")
                )
            )
        }

        exception<BusinessException> { call, cause ->
            call.respond(
                HttpStatusCode.OK,
                ApiResponse<Nothing>(
                    code = cause.code,
                    message = cause.message ?: "Business Error"
                )
            )
        }

        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled exception", cause)
            val message = if (isDev) {
                cause.message ?: "Internal Server Error"
            } else {
                "服务器内部错误，请稍后重试"
            }
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(
                    code = 500,
                    message = message
                )
            )
        }
    }
}
