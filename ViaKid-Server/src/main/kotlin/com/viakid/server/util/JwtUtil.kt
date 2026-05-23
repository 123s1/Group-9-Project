package com.viakid.server.util

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.viakid.server.config.JwtConfig
import java.util.*

object JwtUtil {
    fun makeVerifier(config: JwtConfig): JWTVerifier {
        return JWT.require(Algorithm.HMAC256(config.secret))
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .build()
    }

    fun generateAccessToken(userId: String, config: JwtConfig): String {
        return JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + config.accessTokenValidity))
            .sign(Algorithm.HMAC256(config.secret))
    }

    fun generateRefreshToken(userId: String, config: JwtConfig): String {
        return JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withClaim("userId", userId)
            .withClaim("type", "refresh")
            .withExpiresAt(Date(System.currentTimeMillis() + config.refreshTokenValidity))
            .sign(Algorithm.HMAC256(config.secret))
    }
}
