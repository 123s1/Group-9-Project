package com.viakid.driver.data.model

import kotlinx.serialization.Serializable

/**
 * 订单状态枚举，定义订单在其生命周期中的各种状态
 *
 * @property label 状态的中文显示标签
 */
enum class OrderStatus(val label: String) {
    /** 待派单：订单已创建，等待系统或管理员派单 */
    PENDING("待派单"),

    /** 已派单：订单已分配给司机 */
    ASSIGNED("已派单"),

    /** 已出发：司机已从当前位置出发前往接车点 */
    DEPARTED("已出发"),

    /** 已到达：司机已到达接车点 */
    ARRIVED("已到达"),

    /** 已接孩子：司机已接到孩子 */
    PICKED_UP("已接孩子"),

    /** 途中：司机正在送孩子前往目的地 */
    IN_TRANSIT("途中"),

    /** 已送达：孩子已安全送达目的地 */
    DELIVERED("已送达"),

    /** 已完成：订单流程全部结束 */
    COMPLETED("已完成"),

    /** 已取消：订单被取消 */
    CANCELLED("已取消")
}

/**
 * 订单类型枚举，定义不同的接送服务套餐类型
 *
 * @property label 类型的中文显示标签
 */
enum class OrderType(val label: String) {
    /** 单次接送：一次性接送服务 */
    SINGLE("单次接送"),

    /** 包月套餐：按月订阅的接送服务 */
    MONTHLY("包月套餐"),

    /** 包学期：按学期订阅的接送服务 */
    SEMESTER("包学期")
}

/**
 * 儿童信息数据类，存储乘车儿童的详细信息
 *
 * @property id 儿童ID，唯一标识
 * @property name 儿童姓名
 * @property gender 性别
 * @property age 年龄
 * @property grade 年级
 * @property classInfo 班级信息
 * @property allergies 过敏史信息
 * @property specialNotes 特殊注意事项
 */
@Serializable
data class Child(
    val id: String,
    val name: String,
    val gender: String,
    val age: Int,
    val grade: String,
    val classInfo: String = "",
    val allergies: String = "",
    val specialNotes: String = ""
)

/**
 * 家长/监护人信息数据类
 *
 * @property id 家长ID，唯一标识
 * @property name 家长姓名
 * @property phone 联系电话
 * @property rating 家长评分
 */
@Serializable
data class Parent(
    val id: String,
    val name: String,
    val phone: String,
    val rating: Double
)

/**
 * 位置信息数据类，存储地理位置相关数据
 *
 * @property address 详细地址描述
 * @property latitude 纬度坐标
 * @property longitude 经度坐标
 * @property name 地点名称（如学校名、小区名等）
 */
@Serializable
data class Location(
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val name: String = ""
)

/**
 * 订单数据类，存储完整的订单信息
 *
 * @property id 订单ID，唯一标识
 * @property orderNo 订单编号，用于展示和查询
 * @property status 订单当前状态
 * @property type 订单类型（单次/包月/包学期）
 * @property pickupLocation 接车地点信息
 * @property dropOffLocation 送车地点信息
 * @property pickupTime 计划接车时间
 * @property estimatedArrivalTime 预计到达时间
 * @property children 乘车儿童列表
 * @property parent 家长/监护人信息
 * @property totalAmount 订单总金额
 * @property platformFee 平台服务费
 * @property estimatedIncome 司机预估收入
 * @property specialRequirements 特殊要求或备注
 * @property distance 订单距离（公里）
 * @property schoolName 学校名称
 */
@Serializable
data class Order(
    val id: String,
    val orderNo: String,
    val status: OrderStatus,
    val type: OrderType,
    val pickupLocation: Location,
    val dropOffLocation: Location,
    val pickupTime: String,
    val estimatedArrivalTime: String = "",
    val children: List<Child>,
    val parent: Parent,
    val totalAmount: Double,
    val platformFee: Double,
    val estimatedIncome: Double,
    val specialRequirements: String = "",
    val distance: Double = 0.0,
    val schoolName: String = ""
)
