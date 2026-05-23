package com.viakid.server.route

import com.viakid.server.model.*
import com.viakid.server.service.DriverService
import com.viakid.server.util.extractUserId
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.*

fun Route.driverRoutes() {
    val driverService by inject<DriverService>()

    authenticate("auth-jwt") {
        route("/driver") {
            get("/me") {
                val userId = extractUserId(call)
                val data = driverService.getMe(userId)
                call.respond(ApiResponse(code = 0, message = "success", data = data))
            }

            put("/profile") {
                val userId = extractUserId(call)
                val req = call.receive<ProfileRequest>()
                driverService.updateProfile(userId, req)
                call.respond(ApiResponse<Unit>(code = 0, message = "success"))
            }

            post("/avatar") {
                val userId = extractUserId(call)
                val multipart = call.receiveMultipart()
                var fileBytes: ByteArray? = null
                var fileName: String = "avatar.jpg"
                multipart.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        fileName = part.originalFileName ?: fileName
                        fileBytes = part.streamProvider().readBytes()
                    }
                    part.dispose()
                }
                val bytes = fileBytes ?: throw IllegalArgumentException("未上传文件")
                val url = driverService.uploadAvatar(userId, bytes, fileName)
                call.respond(ApiResponse(code = 0, message = "success", data = AvatarUploadResult(url)))
            }

            get("/certification") {
                val userId = extractUserId(call)
                val data = driverService.getCertification(userId)
                call.respond(ApiResponse(code = 0, message = "success", data = data))
            }

            post("/certification/certificate") {
                val userId = extractUserId(call)
                val type = call.request.queryParameters["type"]
                    ?: throw IllegalArgumentException("缺少type参数")
                val multipart = call.receiveMultipart()
                var fileBytes: ByteArray? = null
                var fileName: String = "cert.jpg"
                multipart.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        fileName = part.originalFileName ?: fileName
                        fileBytes = part.streamProvider().readBytes()
                    }
                    part.dispose()
                }
                val bytes = fileBytes ?: throw IllegalArgumentException("未上传文件")
                val data = driverService.uploadCertificate(userId, type, bytes, fileName)
                call.respond(ApiResponse(code = 0, message = "success", data = data))
            }

            get("/schedule") {
                val userId = extractUserId(call)
                val data = driverService.getSchedule(userId)
                call.respond(ApiResponse(code = 0, message = "success", data = data))
            }

            put("/schedule") {
                val userId = extractUserId(call)
                val req = call.receive<ScheduleInfo>()
                driverService.updateSchedule(userId, req)
                call.respond(ApiResponse<Unit>(code = 0, message = "success"))
            }

            post("/status") {
                val userId = extractUserId(call)
                val req = call.receive<OnlineStatusRequest>()
                driverService.updateOnlineStatus(userId, req.online)
                call.respond(ApiResponse<Unit>(code = 0, message = "success"))
            }
        }
    }
}
