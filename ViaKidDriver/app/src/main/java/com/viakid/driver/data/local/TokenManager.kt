package com.viakid.driver.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Token管理器，负责管理用户认证令牌的生命周期和持久化存储
 *
 * @property dataStore DataStore实例，用于持久化存储token和用户信息
 */
@Singleton
class TokenManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
    }

    /**
     * 访问令牌流，用于响应式获取当前的AccessToken
     */
    val accessToken: Flow<String?> = dataStore.data.map {
        /** @param preferences DataStore中的Preferences对象 */
            preferences ->
        preferences[ACCESS_TOKEN_KEY]
    }

    /**
     * 刷新令牌流，用于响应式获取当前的RefreshToken
     */
    val refreshToken: Flow<String?> = dataStore.data.map {
        /** @param preferences DataStore中的Preferences对象 */
            preferences ->
        preferences[REFRESH_TOKEN_KEY]
    }

    /**
     * 用户ID流，用于响应式获取当前登录用户的ID
     */
    val userId: Flow<String?> = dataStore.data.map {
        /** @param preferences DataStore中的Preferences对象 */
            preferences ->
        preferences[USER_ID_KEY]
    }

    /**
     * 登录状态流，用于响应式获取用户是否已登录
     */
    val isLoggedIn: Flow<Boolean> = dataStore.data.map {
        /** @param preferences DataStore中的Preferences对象 */
            preferences ->
        preferences[IS_LOGGED_IN_KEY] ?: false
    }

    /**
     * 保存用户的认证令牌和ID，并标记为已登录状态
     *
     * @param accessToken 访问令牌
     * @param refreshToken 刷新令牌
     * @param userId 用户ID
     */
    suspend fun saveTokens(accessToken: String, refreshToken: String, userId: String) {
        dataStore.edit {
            /** @param preferences DataStore中的Preferences对象 */
                preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
            preferences[USER_ID_KEY] = userId
            preferences[IS_LOGGED_IN_KEY] = true
        }
    }

    /**
     * 仅更新访问令牌，通常在刷新Token时调用
     *
     * @param accessToken 新的访问令牌
     */
    suspend fun updateAccessToken(accessToken: String) {
        dataStore.edit {
            /** @param preferences DataStore中的Preferences对象 */
                preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
        }
    }

    /**
     * 清除所有存储的令牌和用户信息，通常在用户登出时调用
     */
    suspend fun clearAll() {
        dataStore.edit {
            /** @param preferences DataStore中的Preferences对象 */
                preferences ->
            preferences.clear()
        }
    }

    /**
     * 同步获取当前的访问令牌，适用于非响应式场景
     *
     * @return String? 当前的访问令牌，如果未登录则返回null
     */
    suspend fun getAccessTokenSync(): String? {
        var token: String? = null
        dataStore.data.collect {
            /** @param preferences DataStore中的Preferences对象 */
                preferences ->
            token = preferences[ACCESS_TOKEN_KEY]
        }
        return token
    }
}
