package com.viakid.server.util

import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.application.*
import java.util.*

fun extractUserId(call: ApplicationCall): UUID {
    val principal = call.principal<JWTPrincipal>()
    val userId = principal?.payload?.getClaim("userId")?.asString()
        ?: throw IllegalArgumentException("无效的Token")
    return UUID.fromString(userId)
}
