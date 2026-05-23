package com.viakid.server.route

import com.viakid.server.model.*
import com.viakid.server.service.RouteService
import com.viakid.server.util.extractUserId
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.*

fun Route.fixedRouteRoutes() {
    val routeService by inject<RouteService>()

    authenticate("auth-jwt") {
        route("/routes") {
            // 获取固定线路列表
            get {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                val data = routeService.getFixedRoutes(page, size)
                call.respond(ApiResponse(code = 0, message = "success", data = data))
            }

            // 绑定固定线路
            post("/bind") {
                val driverId = extractUserId(call)
                val req = call.receive<BindRouteRequest>()
                routeService.bindRoute(driverId, req.routeId)
                call.respond(ApiResponse<Unit>(code = 0, message = "success"))
            }
        }
    }
}
