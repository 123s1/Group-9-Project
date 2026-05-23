package com.viakid.driver.data.local.database

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

/**
 * Room数据库类型转换器，用于在Kotlin复杂类型和数据库支持的简单类型之间进行转换
 */
class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * 将字符串列表转换为JSON字符串，用于存储到数据库中
     *
     * @param value 要转换的字符串列表
     * @return String JSON格式的字符串表示
     */
    @TypeConverter
    fun fromStringList(value: List<String>): String = json.encodeToString(value)

    /**
     * 将JSON字符串转换为字符串列表，用于从数据库中读取数据
     *
     * @param value 要转换的JSON字符串
     * @return List<String> 解析后的字符串列表，如果解析失败则返回空列表
     */
    @TypeConverter
    fun toStringList(value: String): List<String> = try {
        json.decodeFromString(value)
    } catch (e: Exception) {
        emptyList()
    }
}