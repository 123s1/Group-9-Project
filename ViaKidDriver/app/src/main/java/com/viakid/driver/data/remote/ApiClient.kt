package com.viakid.driver.data.remote

import android.util.Log
import com.viakid.driver.BuildConfig
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiClient @Inject constructor() {
    companion object {
        private const val TAG = "ApiClient"
    }

    private var accessToken: String? = null
    private var refreshToken: String? = null

    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    val client: HttpClient = HttpClient(OkHttp) {
        engine {
            config {
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
                writeTimeout(30, TimeUnit.SECONDS)
            }
        }

        install(ContentNegotiation) {
            json(json)
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d(TAG, message)
                }
            }
            level = if (BuildConfig.DEBUG) LogLevel.HEADERS else LogLevel.NONE
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 30_000
        }

        defaultRequest {
            url(BuildConfig.BASE_URL)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)

            accessToken?.let { token ->
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        HttpResponseValidator {
            handleResponseExceptionWithRequest { exception, _ ->
                Log.e(TAG, "HTTP Error: ${exception.message}")
            }
        }
    }

    fun setTokens(access: String?, refresh: String?) {
        accessToken = access
        refreshToken = refresh
    }

    fun clearTokens() {
        accessToken = null
        refreshToken = null
    }

    fun getAccessToken(): String? = accessToken

    fun getRefreshToken(): String? = refreshToken
}
