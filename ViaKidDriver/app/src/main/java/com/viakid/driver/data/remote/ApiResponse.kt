package com.viakid.driver.data.remote

import kotlinx.serialization.Serializable

/**
 * API响应数据类，用于封装服务端返回的统一响应格式
 *
 * @param T 响应数据的类型
 * @property code 响应状态码，0表示成功
 * @property message 响应消息描述
 * @property data 响应数据 payload，可为空
 */
@Serializable
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T? = null
)

/**
 * API错误信息数据类，用于封装错误响应的详细信息
 *
 * @property code 错误代码
 * @property message 错误描述信息
 */
@Serializable
data class ApiError(
    val code: Int,
    val message: String
)

/**
 * API错误代码常量集合，定义了系统中所有可能的错误状态码
 */
object ErrorCodes {
    /** 成功 */
    const val SUCCESS: Int = 0

    /** 参数错误 */
    const val PARAM_ERROR: Int = 1001

    /** 缺少必要参数 */
    const val MISSING_PARAM: Int = 1002

    /** 用户不存在 */
    const val USER_NOT_FOUND: Int = 2001

    /** 密码错误 */
    const val PASSWORD_ERROR: Int = 2002

    /** Token已过期 */
    const val TOKEN_EXPIRED: Int = 2003

    /** 无权限访问 */
    const val NO_PERMISSION: Int = 2004

    /** 订单不存在 */
    const val ORDER_NOT_FOUND: Int = 3001

    /** 订单超时 */
    const val ORDER_TIMEOUT: Int = 3002

    /** 认证未通过 */
    const val CERTIFICATION_NOT_APPROVED: Int = 4001

    /** 服务器内部错误 */
    const val SERVER_ERROR: Int = 5001
}

/**
 * 验证码登录时手机号未注册的异常
 * ViewModel 据此判断需要展示注册表单
 *
 * @param message 错误信息
 */
class UserNotFoundException(message: String = "该手机号尚未注册") : Exception(message) {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
