package com.viakid.server.plugins

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
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost() // TODO: 生产环境限制为具体域名
    }
}
