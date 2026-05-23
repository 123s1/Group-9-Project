package com.viakid.driver.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * ViaKid司机端应用的Room数据库主类，管理所有数据表和数据访问对象
 *
 * 包含的实体表：
 * - users: 用户信息表
 * - certifications: 认证信息表
 * - training_progress: 培训进度表
 * - orders: 订单信息表
 */
@Database(
    entities = [
        UserEntity::class,
        CertificationEntity::class,
        TrainingProgressEntity::class,
        OrderEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ViaKidDatabase : RoomDatabase() {
    /**
     * 获取用户数据访问对象，用于操作用户相关信息
     *
     * @return UserDao 用户数据访问对象实例
     */
    abstract fun userDao(): UserDao

    /**
     * 获取认证数据访问对象，用于操作司机认证相关信息
     *
     * @return CertificationDao 认证数据访问对象实例
     */
    abstract fun certificationDao(): CertificationDao

    /**
     * 获取培训数据访问对象，用于操作培训课程和考试结果相关信息
     *
     * @return TrainingDao 培训数据访问对象实例
     */
    abstract fun trainingDao(): TrainingDao

    /**
     * 获取订单数据访问对象，用于操作订单相关信息
     *
     * @return OrderDao 订单数据访问对象实例
     */
    abstract fun orderDao(): OrderDao
}