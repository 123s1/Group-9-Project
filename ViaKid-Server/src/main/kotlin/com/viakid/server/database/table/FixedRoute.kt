package com.viakid.server.database.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime

object FixedRoute : UUIDTable("fixed_route") {
    val driverId = reference("driver_id", Drivers)
    val routeName = varchar("route_name", 100)
    val routeType = integer("route_type").default(1)  // 1-上学 2-放学 3-往返
    val startPoint = varchar("start_point", 255)
    val startLat = decimal("start_lat", 10, 6)
    val startLng = decimal("start_lng", 10, 6)
    val endPoint = varchar("end_point", 255)
    val endLat = decimal("end_lat", 10, 6)
    val endLng = decimal("end_lng", 10, 6)
    val routePoints = text("route_points").nullable()  // JSON途经点
    val totalDistance = decimal("total_distance", 8, 1).nullable()
    val estimatedDuration = integer("estimated_duration").nullable()
    val weekDays = varchar("week_days", 20).nullable()  // "1,2,3,4,5"
    val departureTime = varchar("departure_time", 20).nullable()  // "HH:mm:ss"
    val status = integer("status").default(1)  // 1-启用 2-停用
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}
