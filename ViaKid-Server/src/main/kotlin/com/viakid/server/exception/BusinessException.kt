package com.viakid.server.exception

class BusinessException(val code: Int, message: String) : RuntimeException(message)
