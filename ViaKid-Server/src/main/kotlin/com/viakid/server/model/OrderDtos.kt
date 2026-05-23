package com.viakid.server.model

import kotlinx.serialization.Serializable

@Serializable
data class TaskOverviewDto(
    val pendingCount: Int = 0,
    val inProgressCount: Int = 0,
    val completedCount: Int = 0,
    val todayIncome: Double = 0.0,
    val onlineStatus: Boolean = false
)

@Serializable
data class OrderListData(
    val items: List<OrderDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val size: Int = 20
)

@Serializable
data class OrderDto(
    val id: String,
    val orderNo: String,
    val status: String,
    val type: String,
    val pickupLocation: LocationDto,
    val dropOffLocation: LocationDto,
    val pickupTime: String,
    val pickupDate: String,
    val children: List<ChildDto> = emptyList(),
    val parent: ParentDto,
    val amount: AmountDto,
    val specialRequirements: String? = null,
    val distance: Double = 0.0,
    val schoolName: String = ""
)

@Serializable
data class LocationDto(
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val name: String = ""
)

@Serializable
data class ChildDto(
    val id: String,
    val name: String,
    val gender: String = "",
    val age: Int = 0,
    val grade: String = "",
    val classInfo: String = "",
    val allergies: String? = null,
    val specialNotes: String? = null
)

@Serializable
data class ParentDto(
    val id: String,
    val name: String,
    val phone: String,
    val rating: Double = 0.0
)

@Serializable
data class AmountDto(
    val total: Double = 0.0,
    val platformFee: Double = 0.0,
    val income: Double = 0.0
)

@Serializable
data class OrderDetail(
    val id: String,
    val orderNo: String,
    val status: String,
    val type: String,
    val pickupLocation: LocationDto,
    val dropOffLocation: LocationDto,
    val pickupTime: String,
    val pickupDate: String,
    val children: List<ChildDto>,
    val parent: ParentDto,
    val amount: AmountDto,
    val specialRequirements: String? = null,
    val createdAt: String = ""
)

@Serializable
data class RejectRequest(
    val reason: String,
    val remark: String? = null
)

@Serializable
data class OrderStatusRequest(
    val status: String
)

@Serializable
data class GrabOrderList(
    val mainOrder: OrderDto? = null,
    val nearbyOrders: List<OrderDto> = emptyList(),
    val countdownSeconds: Int = 60
)

@Serializable
data class GrabResult(
    val success: Boolean = true
)
