package com.viakid.server.plugins

import com.viakid.server.config.AppConfig
import com.viakid.server.util.JwtUtil
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {
    val jwtConfig = AppConfig.from(environment.config).jwt

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtConfig.realm
            verifier(JwtUtil.makeVerifier(jwtConfig))
            validate { credential ->
                val userId = credential.payload.getClaim("userId")?.asString()
                if (!userId.isNullOrBlank()) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }
}
