package com.example.httplite.response

import java.io.IOException

data class HttpResponse(
    val statusCode: Int,
    val headers: Map<String, String>,
    val jsonBody: String
)

data class ApiResponse<T>(
    val data: T,
    val headers: Map<String, String> = emptyMap<String, String>(),
    val statusCode: Int,
)

internal class ApiException(message: String) : Exception(message)

sealed class ApiResult<out T> {
    data class Response<T>(
        val data: T,
        val headers: Map<String, String>,
        val statusCode: Int,
    ) : ApiResult<T>()
    data class SerializationFailed(val exception: Exception) : ApiResult<Nothing>()
    data class NetworkFailed(val exception: IOException) : ApiResult<Nothing>()
}
