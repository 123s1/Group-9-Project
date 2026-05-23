package com.viakid.server.service

import com.viakid.server.model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class RouteService {

    fun getFixedRoutes(page: Int, size: Int): FixedRouteListData = transaction {
        // 阶段1：返回模拟数据，后续从数据库查询
        val routes = listOf(
            FixedRouteDto(
                id = UUID.randomUUID().toString(),
                name = "阳光花园-实验小学线路A",
                startPoint = LocationDto(
                    address = "阳光花园小区东门",
                    latitude = 30.5728,
                    longitude = 104.0668,
                    name = "阳光花园东门"
                ),
                endPoint = LocationDto(
                    address = "实验小学",
                    latitude = 30.5780,
                    longitude = 104.0720,
                    name = "实验小学南门"
                ),
                stops = listOf(
                    RouteStopDto(name = "阳光花园东门", location = LocationDto(address = "阳光花园小区东门", latitude = 30.5728, longitude = 104.0668), estimatedTime = "07:30", order = 1),
                    RouteStopDto(name = "翠竹苑北门", location = LocationDto(address = "翠竹苑北门", latitude = 30.5745, longitude = 104.0685), estimatedTime = "07:35", order = 2),
                    RouteStopDto(name = "实验小学南门", location = LocationDto(address = "实验小学", latitude = 30.5780, longitude = 104.0720), estimatedTime = "07:45", order = 3)
                ),
                distance = 3.5,
                estimatedDuration = 15,
                schoolName = "实验小学"
            ),
            FixedRouteDto(
                id = UUID.randomUUID().toString(),
                name = "金沙湾-育才学校线路B",
                startPoint = LocationDto(
                    address = "金沙湾小区西门",
                    latitude = 30.5680,
                    longitude = 104.0550,
                    name = "金沙湾西门"
                ),
                endPoint = LocationDto(
                    address = "育才学校",
                    latitude = 30.5750,
                    longitude = 104.0650,
                    name = "育才学校正门"
                ),
                stops = listOf(
                    RouteStopDto(name = "金沙湾西门", location = LocationDto(address = "金沙湾小区西门", latitude = 30.5680, longitude = 104.0550), estimatedTime = "07:20", order = 1),
                    RouteStopDto(name = "碧水苑", location = LocationDto(address = "碧水苑南门", latitude = 30.5710, longitude = 104.0590), estimatedTime = "07:28", order = 2),
                    RouteStopDto(name = "育才学校正门", location = LocationDto(address = "育才学校", latitude = 30.5750, longitude = 104.0650), estimatedTime = "07:40", order = 3)
                ),
                distance = 5.2,
                estimatedDuration = 20,
                schoolName = "育才学校"
            )
        )

        FixedRouteListData(
            items = routes,
            total = routes.size
        )
    }

    fun bindRoute(driverId: UUID, routeId: String) = transaction {
        // 阶段1：记录绑定关系，后续写入数据库
        // Drivers.update({ Drivers.id eq driverId }) { it[boundRouteId] = UUID.fromString(routeId) }
    }
}
