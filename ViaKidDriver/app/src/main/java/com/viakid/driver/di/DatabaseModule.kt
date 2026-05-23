package com.viakid.driver.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.viakid.driver.data.local.TokenManager
import com.viakid.driver.data.local.database.CertificationDao
import com.viakid.driver.data.local.database.OrderDao
import com.viakid.driver.data.local.database.TrainingDao
import com.viakid.driver.data.local.database.UserDao
import com.viakid.driver.data.local.database.ViaKidDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据库相关的依赖注入模块，提供Room数据库、DataStore和数据访问对象的实例
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val Context.dataStore by preferencesDataStore(name = "via_kid_prefs")

    /**
     * 提供DataStore实例，用于存储应用偏好设置数据
     *
     * @param context 应用上下文，用于创建DataStore实例
     * @return DataStore<Preferences> 偏好设置数据存储实例
     */
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    /**
     * 提供Room数据库实例，配置数据库名称和迁移策略
     *
     * @param context 应用上下文，用于创建数据库实例
     * @return ViaKidDatabase 数据库实例，配置了破坏性迁移策略
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ViaKidDatabase {
        return Room.databaseBuilder(
            context,
            ViaKidDatabase::class.java,
            "via_kid_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    /**
     * 提供用户数据访问对象，用于操作用户相关数据
     *
     * @param database 数据库实例，从中获取UserDao
     * @return UserDao 用户数据访问对象实例
     */
    @Provides
    fun provideUserDao(database: ViaKidDatabase): UserDao = database.userDao()

    /**
     * 提供认证数据访问对象，用于操作认证相关数据
     *
     * @param database 数据库实例，从中获取CertificationDao
     * @return CertificationDao 认证数据访问对象实例
     */
    @Provides
    fun provideCertificationDao(database: ViaKidDatabase): CertificationDao = database.certificationDao()

    /**
     * 提供培训数据访问对象，用于操作培训进度相关数据
     *
     * @param database 数据库实例，从中获取TrainingDao
     * @return TrainingDao 培训数据访问对象实例
     */
    @Provides
    fun provideTrainingDao(database: ViaKidDatabase): TrainingDao = database.trainingDao()

    /**
     * 提供订单数据访问对象，用于操作订单相关数据
     *
     * @param database 数据库实例，从中获取OrderDao
     * @return OrderDao 订单数据访问对象实例
     */
    @Provides
    fun provideOrderDao(database: ViaKidDatabase): OrderDao = database.orderDao()

    /**
     * 提供Token管理器实例，用于管理用户认证令牌的生命周期和存储
     *
     * @param context 应用上下文，用于创建TokenManager实例（当前未使用）
     * @param dataStore 偏好设置数据存储，用于持久化存储token信息
     * @return TokenManager token管理器实例
     */
    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context, dataStore: DataStore<Preferences>): TokenManager {
        return TokenManager(dataStore)
    }
}
