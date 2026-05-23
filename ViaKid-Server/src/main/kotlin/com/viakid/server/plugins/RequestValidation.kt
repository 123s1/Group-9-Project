package com.viakid.server.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*

fun Application.configureRequestValidation() {
    install(RequestValidation) {
        // TODO: 后续为各请求DTO添加校验规则
    }
}
