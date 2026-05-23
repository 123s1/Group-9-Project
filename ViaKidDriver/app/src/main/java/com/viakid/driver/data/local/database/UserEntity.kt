package com.viakid.driver.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户信息实体类，用于在本地数据库中存储司机的基本信息和认证状态
 *
 * @property id 用户ID，作为主键唯一标识每个用户
 * @property phone 手机号码，用于登录和联系
 * @property name 用户姓名
 * @property avatar 头像URL地址，可为空
 * @property status 用户状态：pending-待审核，approved-已通过，rejected-已拒绝，probation-试用期，formal-正式员工，suspended-已暂停
 * @property accessToken 访问令牌，用于API认证，可为空表示未登录
 * @property refreshToken 刷新令牌，用于获取新的访问令牌，可为空
 * @property createdAt 用户记录创建时间戳（毫秒）
 * @property updatedAt 用户记录最后更新时间戳（毫秒）
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val phone: String,
    val name: String,
    val avatar: String?,
    val status: String, // pending, approved, rejected, probation, formal, suspended
    val accessToken: String?,
    val refreshToken: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)