package com.example.httplite

data class HttpResponse(
    val statusCode: Int,
    val headers: Map<String, String>,
    val jsonBody: String
)

data class ApiResponse<T>(
    val data: T,
    val headers: Map<String, String> = emptyMap<String, String>()
)

class ApiException(message: String) : Exception(message)
