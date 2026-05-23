package com.viakid.server

import com.viakid.server.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureKoin()
    configureHTTP()
    configureSerialization()
    configureSecurity()
    configureStatusPages()
    configureRequestValidation()
    configureRouting()
}
