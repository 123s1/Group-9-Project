package com.viakid.server.route

import com.viakid.server.model.*
import com.viakid.server.service.OrderService
import com.viakid.server.util.extractUserId
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.*

fun Route.orderRoutes() {
    val orderService by inject<OrderService>()

    authenticate("auth-jwt") {
        route("/orders") {
            // 获取今日任务概览
            get("/overview") {
                val driverId = extractUserId(call)
                val data = orderService.getOverview(driverId)
                call.respond(ApiResponse(code = 0, message = "success", data = data))
            }

            // 获取可抢订单列表
            get("/grab") {
                val driverId = extractUserId(call)
                val sort = call.request.queryParameters["sort"] ?: "distance"
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
                val data = orderService.getGrabOrders(driverId, sort, page, size)
                call.respond(ApiResponse(code = 0, message = "success", data = data))
            }

            // 获取订单列表
            get {
                val driverId = extractUserId(call)
                val status = call.request.queryParameters["status"]
                val date = call.request.queryParameters["date"]
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                val data = orderService.getOrders(driverId, status, date, page, size)
                call.respond(ApiResponse(code = 0, message = "success", data = data))
            }

            // 获取订单详情
            get("/{orderId}") {
                val orderId = call.parameters["orderId"]?.let { UUID.fromString(it) }
                    ?: throw IllegalArgumentException("无效的订单ID")
                val data = orderService.getOrderDetail(orderId)
                call.respond(ApiResponse(code = 0, message = "success", data = data))
            }

            // 接单
            post("/{orderId}/accept") {
                val driverId = extractUserId(call)
                val orderId = call.parameters["orderId"]?.let { UUID.fromString(it) }
                    ?: throw IllegalArgumentException("无效的订单ID")
                orderService.acceptOrder(orderId, driverId)
                call.respond(ApiResponse<Unit>(code = 0, message = "success"))
            }

            // 拒单
            post("/{orderId}/reject") {
                val driverId = extractUserId(call)
                val orderId = call.parameters["orderId"]?.let { UUID.fromString(it) }
                    ?: throw IllegalArgumentException("无效的订单ID")
                val req = call.receive<RejectRequest>()
                orderService.rejectOrder(orderId, driverId, req)
                call.respond(ApiResponse<Unit>(code = 0, message = "success"))
            }

            // 更新订单状态
            post("/{orderId}/status") {
                val driverId = extractUserId(call)
                val orderId = call.parameters["orderId"]?.let { UUID.fromString(it) }
                    ?: throw IllegalArgumentException("无效的订单ID")
                val req = call.receive<OrderStatusRequest>()
                orderService.updateOrderStatus(orderId, driverId, req.status)
                call.respond(ApiResponse<Unit>(code = 0, message = "success"))
            }

            // 抢单
            post("/{orderId}/grab") {
                val driverId = extractUserId(call)
                val orderId = call.parameters["orderId"]?.let { UUID.fromString(it) }
                    ?: throw IllegalArgumentException("无效的订单ID")
                val data = orderService.grabOrder(orderId, driverId)
                call.respond(ApiResponse(code = 0, message = "success", data = data))
            }
        }
    }
}
