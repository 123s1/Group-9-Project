package com.viakid.server.config

import io.ktor.server.config.*

data class AppConfig(
    val database: DatabaseConfig,
    val jwt: JwtConfig
) {
    companion object {
        fun from(config: ApplicationConfig): AppConfig {
            return AppConfig(
                database = DatabaseConfig.from(config.config("database")),
                jwt = JwtConfig.from(config.config("jwt"))
            )
        }
    }
}

data class DatabaseConfig(
    val driver: String,
    val url: String,
    val user: String,
    val password: String,
    val poolSize: Int
) {
    companion object {
        fun from(config: ApplicationConfig): DatabaseConfig {
            return DatabaseConfig(
                driver = config.property("driver").getString(),
                url = config.property("url").getString(),
                user = config.property("user").getString(),
                password = config.property("password").getString(),
                poolSize = config.propertyOrNull("poolSize")?.getString()?.toInt() ?: 10
            )
        }
    }
}

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val accessTokenValidity: Long,
    val refreshTokenValidity: Long
) {
    companion object {
        fun from(config: ApplicationConfig): JwtConfig {
            return JwtConfig(
                secret = config.property("secret").getString(),
                issuer = config.property("issuer").getString(),
                audience = config.property("audience").getString(),
                realm = config.property("realm").getString(),
                accessTokenValidity = config.property("accessTokenValidity").getString().toLong(),
                refreshTokenValidity = config.property("refreshTokenValidity").getString().toLong()
            )
        }
    }
}
