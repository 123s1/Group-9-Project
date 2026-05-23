package com.viakid.server.di

import com.viakid.server.config.AppConfig
import com.viakid.server.database.DatabaseFactory
import com.viakid.server.service.AuthService
import com.viakid.server.service.DriverService
import com.viakid.server.service.FileService
import com.viakid.server.service.OrderService
import com.viakid.server.service.RouteService
import com.viakid.server.service.TrainingService
import io.ktor.server.config.*
import org.koin.dsl.module

fun appModule(appConfig: ApplicationConfig) = module {
    single { AppConfig.from(appConfig) }
    single(createdAtStart = true) {
        DatabaseFactory.init(get<AppConfig>().database)
    }
    single { AuthService(get()) }
    single { FileService("uploads") }
    single { DriverService(get()) }
    single { TrainingService() }
    single { OrderService() }
}
