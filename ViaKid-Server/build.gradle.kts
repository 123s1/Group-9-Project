plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.viakid"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    // Ktor Server
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.server.callLogging)
    implementation(ktorLibs.server.compression)
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.cors)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.requestValidation)
    implementation(ktorLibs.server.resources)
    implementation(ktorLibs.server.statusPages)
    implementation(libs.ktor.server.auth.jwt)

    // DI
    implementation(libs.insert.koin.koinKtor)
    implementation(libs.insert.koin.koinLoggerSlf4j)

    // Database
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)
    implementation(libs.hikari.cp)
    implementation(libs.mssql.jdbc)
    implementation(libs.flyway.core)
    implementation(libs.flyway.sqlserver)

    // Cache
    implementation(libs.lettuce.core)

    // Security
    implementation(libs.bcrypt)

    // Logging
    implementation(libs.logback.classic)

    // Test
    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
}
