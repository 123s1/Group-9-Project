package com.viakid.server.route

import com.viakid.server.model.*
import com.viakid.server.service.TrainingService
import com.viakid.server.util.extractUserId
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.*

fun Route.trainingRoutes() {
    val trainingService by inject<TrainingService>()

    authenticate("auth-jwt") {
        route("/training") {
            get("/courses") {
                val userId = extractUserId(call)
                val data = trainingService.getCourses(userId)
                call.respond(ApiResponse(code = 0, message = "success", data = data))
            }

            get("/courses/{courseId}") {
                val courseId = call.parameters["courseId"] ?: throw IllegalArgumentException("缺少courseId")
                val data = trainingService.getCourseDetail(courseId)
                call.respond(ApiResponse(code = 0, message = "success", data = data))
            }

            post("/courses/{courseId}/complete") {
                val userId = extractUserId(call)
                val courseId = call.parameters["courseId"] ?: throw IllegalArgumentException("缺少courseId")
                trainingService.markCourseComplete(userId, courseId)
                call.respond(ApiResponse<Unit>(code = 0, message = "success"))
            }

            get("/exam") {
                val userId = extractUserId(call)
                val data = trainingService.getExamInfo(userId)
                call.respond(ApiResponse(code = 0, message = "success", data = data))
            }

            get("/exam/questions") {
                val data = trainingService.getExamQuestions()
                call.respond(ApiResponse(code = 0, message = "success", data = data))
            }

            post("/exam/submit") {
                val userId = extractUserId(call)
                val req = call.receive<ExamSubmitRequest>()
                val data = trainingService.submitExam(userId, req.answers)
                call.respond(ApiResponse(code = 0, message = "success", data = data))
            }

            get("/certificate") {
                val userId = extractUserId(call)
                val data = trainingService.getCertificate(userId)
                call.respond(ApiResponse(code = 0, message = "success", data = data))
            }
        }
    }
}
