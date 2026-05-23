package com.viakid.server.plugins

import com.viakid.server.config.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cors.routing.*
import org.slf4j.event.Level

fun Application.configureHTTP() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.local.uri.startsWith("/") }
    }
    install(Compression)

    val allowedHosts = AppConfig.from(environment.config).corsAllowedHosts

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)

        if (allowedHosts.isEmpty()) {
            anyHost()
        } else {
            allowedHosts.forEach { host -> allowHost(host, schemes = listOf("https", "http")) }
        }
    }
}
