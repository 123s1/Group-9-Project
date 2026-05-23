package com.viakid.driver.di

import com.viakid.driver.data.remote.ApiClient
import com.viakid.driver.data.remote.AuthApi
import com.viakid.driver.data.remote.DriverApi
import com.viakid.driver.data.remote.OrderApi
import com.viakid.driver.data.remote.TrainingApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 网络相关的依赖注入模块，提供API客户端和各种API接口的实例
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * 提供API客户端单例实例，用于管理HTTP请求和网络通信
     *
     * @return ApiClient API客户端实例
     */
    @Provides
    @Singleton
    fun provideApiClient(): ApiClient {
        return ApiClient()
    }

    /**
     * 提供认证API接口实例，用于处理用户登录、注册等认证相关操作
     *
     * @param apiClient API客户端，用于执行网络请求
     * @return AuthApi 认证API接口实例
     */
    @Provides
    @Singleton
    fun provideAuthApi(apiClient: ApiClient): AuthApi {
        return AuthApi(apiClient)
    }

    /**
     * 提供司机API接口实例，用于处理司机相关信息和操作
     *
     * @param apiClient API客户端，用于执行网络请求
     * @return DriverApi 司机API接口实例
     */
    @Provides
    @Singleton
    fun provideDriverApi(apiClient: ApiClient): DriverApi {
        return DriverApi(apiClient)
    }

    /**
     * 提供订单API接口实例，用于处理订单相关的查询、创建和管理操作
     *
     * @param apiClient API客户端，用于执行网络请求
     * @return OrderApi 订单API接口实例
     */
    @Provides
    @Singleton
    fun provideOrderApi(apiClient: ApiClient): OrderApi {
        return OrderApi(apiClient)
    }

    /**
     * 提供培训API接口实例，用于处理培训课程、考试和证书相关操作
     *
     * @param apiClient API客户端，用于执行网络请求
     * @return TrainingApi 培训API接口实例
     */
    @Provides
    @Singleton
    fun provideTrainingApi(apiClient: ApiClient): TrainingApi {
        return TrainingApi(apiClient)
    }
}
