package com.viakid.driver.di

import com.viakid.driver.data.local.TokenManager
import com.viakid.driver.data.local.database.CertificationDao
import com.viakid.driver.data.local.database.OrderDao
import com.viakid.driver.data.local.database.UserDao
import com.viakid.driver.data.remote.ApiClient
import com.viakid.driver.data.remote.AuthApi
import com.viakid.driver.data.remote.DriverApi
import com.viakid.driver.data.remote.OrderApi
import com.viakid.driver.data.remote.TrainingApi
import com.viakid.driver.data.repository.AuthRepository
import com.viakid.driver.data.repository.DriverRepository
import com.viakid.driver.data.repository.OrderRepository
import com.viakid.driver.data.repository.TrainingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据仓库相关的依赖注入模块，提供各种Repository实例用于业务逻辑处理
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * 提供认证数据仓库实例，负责处理用户认证相关的业务逻辑和数据操作
     *
     * @param userDao 用户数据访问对象，用于本地用户数据操作
     * @param tokenManager Token管理器，用于管理认证令牌的生命周期和存储
     * @param authApi 认证API接口，用于执行远程认证请求
     * @param apiClient API客户端，用于管理网络通信（当前未直接使用）
     * @return AuthRepository 认证数据仓库实例
     */
    @Provides
    @Singleton
    fun provideAuthRepository(
        userDao: UserDao,
        tokenManager: TokenManager,
        authApi: AuthApi,
        apiClient: ApiClient
    ): AuthRepository {
        return AuthRepository(userDao, tokenManager, authApi, apiClient)
    }

    /**
     * 提供司机数据仓库实例，负责处理司机相关信息和业务逻辑的数据操作
     *
     * @param certificationDao 认证数据访问对象，用于本地认证数据操作
     * @param driverApi 司机API接口，用于执行远程司机相关请求
     * @return DriverRepository 司机数据仓库实例
     */
    @Provides
    @Singleton
    fun provideDriverRepository(
        certificationDao: CertificationDao,
        driverApi: DriverApi
    ): DriverRepository {
        return DriverRepository(certificationDao, driverApi)
    }

    /**
     * 提供订单数据仓库实例，负责处理订单相关业务逻辑和数据操作，支持本地缓存和远程同步
     *
     * @param orderDao 订单数据访问对象，用于本地订单数据操作和缓存
     * @param orderApi 订单API接口，用于执行远程订单相关请求
     * @return OrderRepository 订单数据仓库实例
     */
    @Provides
    @Singleton
    fun provideOrderRepository(
        orderDao: OrderDao,
        orderApi: OrderApi
    ): OrderRepository {
        return OrderRepository(orderDao, orderApi)
    }

    /**
     * 提供培训数据仓库实例，负责处理培训课程、考试和证书相关的业务逻辑和数据操作
     *
     * @param trainingApi 培训API接口，用于执行远程培训相关请求（课程、考试、证书等）
     * @return TrainingRepository 培训数据仓库实例
     */
    @Provides
    @Singleton
    fun provideTrainingRepository(
        trainingApi: TrainingApi
    ): TrainingRepository {
        return TrainingRepository(trainingApi)
    }
}
