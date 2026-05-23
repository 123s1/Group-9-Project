package com.viakid.server.model

import kotlinx.serialization.Serializable

@Serializable
data class FixedRouteDto(
    val id: String,
    val name: String,
    val startPoint: LocationDto,
    val endPoint: LocationDto,
    val stops: List<RouteStopDto> = emptyList(),
    val distance: Double = 0.0,
    val estimatedDuration: Int = 0,
    val schoolName: String = "",
    val assignedDriverId: String? = null
)

@Serializable
data class RouteStopDto(
    val name: String,
    val location: LocationDto,
    val estimatedTime: String = "",
    val order: Int = 0
)

@Serializable
data class FixedRouteListData(
    val items: List<FixedRouteDto> = emptyList(),
    val total: Int = 0
)

@Serializable
data class BindRouteRequest(
    val routeId: String
)
