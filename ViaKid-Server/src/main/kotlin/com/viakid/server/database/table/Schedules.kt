package com.viakid.server.database.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Schedules : Table("schedules") {
    val driverId = reference("driver_id", Drivers)
    val timeSlots = text("time_slots").nullable()          // JSON: [{week_day:1, start_time:"08:00", end_time:"18:00", is_available:1, max_order_count:10}]
    val workDays = text("work_days").nullable()             // JSON: [1,2,3,4,5]
    val unavailableDates = text("unavailable_dates").nullable()  // JSON: ["2026-05-01","2026-05-02"]
    val maxOrdersPerDay = integer("max_orders_per_day").default(5)
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(driverId)
}
