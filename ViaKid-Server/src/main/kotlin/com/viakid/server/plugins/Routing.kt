package com.viakid.server.plugins

import com.viakid.server.route.authRoutes
import com.viakid.server.route.driverRoutes
import com.viakid.server.route.fixedRouteRoutes
import com.viakid.server.route.orderRoutes
import com.viakid.server.route.trainingRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }
        route("/api/v1") {
            authRoutes()
            driverRoutes()
            trainingRoutes()
            orderRoutes()
            fixedRouteRoutes()
        }
    }
}
