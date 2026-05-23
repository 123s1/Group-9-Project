package com.viakid.server.database

import com.viakid.server.config.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun init(config: DatabaseConfig) {
        val dataSource = HikariDataSource(HikariConfig().apply {
            driverClassName = config.driver
            jdbcUrl = config.url
            username = config.user
            password = config.password
            maximumPoolSize = config.poolSize
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        })

        Flyway.configure()
            .dataSource(dataSource)
            .baselineOnMigrate(true)
            .load()
            .migrate()

        Database.connect(dataSource)
    }
}
