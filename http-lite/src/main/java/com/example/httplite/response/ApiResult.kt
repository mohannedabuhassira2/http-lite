package com.example.httplite.response

import java.io.IOException

sealed class ApiResult<T> {
    data class Response<T>(
        val data: T,
        val headers: Map<String, String>,
        val statusCode: Int,
    ): ApiResult<T>()

    data class SerializationFailed<T>(
        val exception: Exception
    ): ApiResult<T>()

    data class NetworkFailed<T>(
        val exception: IOException
    ): ApiResult<T>()
}
