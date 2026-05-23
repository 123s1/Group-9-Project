package com.viakid.driver.data.local.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 用户数据访问对象，提供对本地数据库中用户信息的增删改查操作
 */
@Dao
interface UserDao {
    /**
     * 获取当前登录用户的信息，以Flow形式返回支持响应式更新
     *
     * @return Flow<UserEntity?> 用户实体的Flow流，当数据变化时自动更新
     */
    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>

    /**
     * 同步获取当前登录用户的信息，适用于协程作用域内的单次查询
     *
     * @return UserEntity? 用户实体，如果不存在则返回null
     */
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUserSync(): UserEntity?

    /**
     * 插入或替换用户信息，如果已存在相同主键的记录则进行替换
     *
     * @param user 要插入或更新的用户实体对象
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    /**
     * 更新已有的用户信息，要求用户实体必须存在于数据库中
     *
     * @param user 包含更新数据的用户实体对象
     */
    @Update
    suspend fun updateUser(user: UserEntity)

    /**
     * 删除所有用户数据，通常在用户登出时调用
     */
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    /**
     * 更新指定用户的认证令牌（AccessToken和RefreshToken）
     *
     * @param userId 用户ID
     * @param accessToken 新的访问令牌
     * @param refreshToken 新的刷新令牌
     * @param updatedAt 更新时间戳（毫秒），默认为当前时间
     */
    @Query("UPDATE users SET accessToken = :accessToken, refreshToken = :refreshToken, updatedAt = :updatedAt WHERE id = :userId")
    suspend fun updateTokens(userId: String, accessToken: String, refreshToken: String, updatedAt: Long = System.currentTimeMillis())
}
