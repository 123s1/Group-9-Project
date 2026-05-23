package com.viakid.driver.data.local.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 订单数据访问对象，提供对本地数据库中订单信息的增删改查操作
 */
@Dao
interface OrderDao {
    /**
     * 获取所有订单列表，按创建时间降序排列，以Flow形式返回支持响应式更新
     *
     * @return Flow<List<OrderEntity>> 订单实体列表的Flow流
     */
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    /**
     * 根据接车日期获取订单列表，按接车时间升序排列，以Flow形式返回
     *
     * @param date 接车日期（格式：yyyy-MM-dd）
     * @return Flow<List<OrderEntity>> 指定日期的订单实体列表的Flow流
     */
    @Query("SELECT * FROM orders WHERE pickupDate = :date ORDER BY pickupTime ASC")
    fun getOrdersByDate(date: String): Flow<List<OrderEntity>>

    /**
     * 根据订单状态列表获取订单，按创建时间降序排列，以Flow形式返回
     *
     * @param statuses 订单状态列表，用于筛选符合条件的订单
     * @return Flow<List<OrderEntity>> 符合状态条件的订单实体列表的Flow流
     */
    @Query("SELECT * FROM orders WHERE status IN (:statuses) ORDER BY createdAt DESC")
    fun getOrdersByStatuses(statuses: List<String>): Flow<List<OrderEntity>>

    /**
     * 同步根据订单ID获取单个订单详情，适用于协程作用域内的单次查询
     *
     * @param orderId 订单ID
     * @return OrderEntity? 订单实体，如果不存在则返回null
     */
    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: String): OrderEntity?

    /**
     * 根据订单ID获取单个订单详情，以Flow形式返回支持响应式更新
     *
     * @param orderId 订单ID
     * @return Flow<OrderEntity?> 订单实体的Flow流，当数据变化时自动更新
     */
    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun getOrderByIdFlow(orderId: String): Flow<OrderEntity?>

    /**
     * 插入或替换单个订单，如果已存在相同主键的记录则进行替换
     *
     * @param order 要插入或更新的订单实体对象
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    /**
     * 批量插入或替换订单列表，如果已存在相同主键的记录则进行替换
     *
     * @param orders 要插入或更新的订单实体列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    /**
     * 更新已有的订单信息，要求订单实体必须存在于数据库中
     *
     * @param order 包含更新数据的订单实体对象
     */
    @Update
    suspend fun updateOrder(order: OrderEntity)

    /**
     * 更新指定订单的状态和更新时间
     *
     * @param orderId 订单ID
     * @param status 新的订单状态
     * @param updatedAt 更新时间戳（毫秒），默认为当前时间
     */
    @Query("UPDATE orders SET status = :status, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String, updatedAt: Long = System.currentTimeMillis())

    /**
     * 删除指定订单
     *
     * @param orderId 要删除的订单ID
     */
    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteOrder(orderId: String)

    /**
     * 删除所有订单数据，慎用此方法
     */
    @Query("DELETE FROM orders")
    suspend fun deleteAllOrders()

    /**
     * 获取指定日期的活跃订单数量（包括pending、assigned、departed、arrived、picked_up、in_transit、delivered状态）
     *
     * @param date 接车日期（格式：yyyy-MM-dd）
     * @return Flow<Int> 活跃订单数量的Flow流，当数据变化时自动更新
     */
    @Query(
        "SELECT COUNT(*) FROM orders WHERE pickupDate = :date AND status IN" +
                " ('pending', 'assigned', 'departed', 'arrived', 'picked_up', 'in_transit', 'delivered')"
    )
    fun getActiveOrderCountByDate(date: String): Flow<Int>

    /**
     * 获取指定日期的已完成订单数量（status为completed）
     *
     * @param date 接车日期（格式：yyyy-MM-dd）
     * @return Flow<Int> 已完成订单数量的Flow流，当数据变化时自动更新
     */
    @Query("SELECT COUNT(*) FROM orders WHERE pickupDate = :date AND status = 'completed'")
    fun getCompletedOrderCountByDate(date: String): Flow<Int>
}
