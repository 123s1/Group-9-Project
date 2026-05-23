package com.viakid.driver.data.local.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 认证数据访问对象，提供对本地数据库中认证信息的增删改查操作
 */
@Dao
interface CertificationDao {
    /**
     * 以Flow形式获取指定用户的认证信息，支持响应式更新
     *
     * @param userId 用户ID，用于查询对应的认证信息
     * @return Flow<CertificationEntity?> 认证实体的Flow流，当数据变化时自动更新
     */
    @Query("SELECT * FROM certifications WHERE userId = :userId")
    fun getCertification(userId: String): Flow<CertificationEntity?>

    /**
     * 同步获取指定用户的认证信息，适用于协程作用域内的单次查询
     *
     * @param userId 用户ID，用于查询对应的认证信息
     * @return CertificationEntity? 认证实体，如果不存在则返回null
     */
    @Query("SELECT * FROM certifications WHERE userId = :userId")
    suspend fun getCertificationSync(userId: String): CertificationEntity?

    /**
     * 插入或替换认证信息，如果已存在相同主键的记录则进行替换
     *
     * @param certification 要插入或更新的认证实体对象
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertification(certification: CertificationEntity)

    /**
     * 更新已有的认证信息，要求认证实体必须存在于数据库中
     *
     * @param certification 包含更新数据的认证实体对象
     */
    @Update
    suspend fun updateCertification(certification: CertificationEntity)

    /**
     * 删除指定用户的认证信息
     *
     * @param userId 用户ID，用于删除对应的认证记录
     */
    @Query("DELETE FROM certifications WHERE userId = :userId")
    suspend fun deleteCertification(userId: String)
}
