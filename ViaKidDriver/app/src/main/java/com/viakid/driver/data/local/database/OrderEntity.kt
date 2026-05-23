package com.viakid.driver.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 订单信息实体类，用于在本地数据库中存储订单的详细信息
 *
 * @property id 订单ID，作为主键唯一标识每个订单
 * @property orderNo 订单编号，用于展示和查询
 * @property status 订单状态（如：pending, assigned, departed, arrived, picked_up, in_transit, delivered, completed等）
 * @property type 订单类型：single-单次订单，monthly-月度订单，semester-学期订单
 * @property pickupAddress 接车地址详细描述
 * @property pickupLatitude 接车地点纬度坐标
 * @property pickupLongitude 接车地点经度坐标
 * @property pickupLocationName 接车地点名称
 * @property dropOffAddress 送车地址详细描述
 * @property dropOffLatitude 送车地点纬度坐标
 * @property dropOffLongitude 送车地点经度坐标
 * @property dropOffLocationName 送车地点名称
 * @property pickupTime 接车时间（格式：HH:mm）
 * @property pickupDate 接车日期（格式：yyyy-MM-dd）
 * @property childrenJson 儿童信息的JSON字符串，包含乘车儿童的详细列表
 * @property parentId 家长/监护人ID
 * @property parentName 家长/监护人姓名
 * @property parentPhone 家长/监护人联系电话
 * @property parentRating 家长评分
 * @property totalAmount 订单总金额
 * @property platformFee 平台服务费
 * @property estimatedIncome 司机预估收入
 * @property specialRequirements 特殊要求或备注信息，可为空
 * @property distance 订单距离（公里）
 * @property schoolName 学校名称
 * @property createdAt 订单创建时间戳（毫秒）
 * @property updatedAt 订单最后更新时间戳（毫秒）
 */
@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    val id: String,
    val orderNo: String,
    val status: String,
    val type: String, // single, monthly, semester
    val pickupAddress: String,
    val pickupLatitude: Double,
    val pickupLongitude: Double,
    val pickupLocationName: String,
    val dropOffAddress: String,
    val dropOffLatitude: Double,
    val dropOffLongitude: Double,
    val dropOffLocationName: String,
    val pickupTime: String,
    val pickupDate: String,
    val childrenJson: String, // JSON string of children list
    val parentId: String,
    val parentName: String,
    val parentPhone: String,
    val parentRating: Double,
    val totalAmount: Double,
    val platformFee: Double,
    val estimatedIncome: Double,
    val specialRequirements: String?,
    val distance: Double,
    val schoolName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)