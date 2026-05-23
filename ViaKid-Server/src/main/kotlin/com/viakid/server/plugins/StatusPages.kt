package com.viakid.server.plugins

import com.viakid.server.exception.BusinessException
import com.viakid.server.model.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
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
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(
                    code = 500,
                    message = cause.message ?: "Internal Server Error"
                )
            )
        }
    }
}
