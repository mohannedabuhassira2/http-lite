package com.example.httplite.response

data class ApiResponse<T>(
    val body: T?,
    val code: Int,
    val headers: Map<String, String>,
    val errorBody: String?
) {
    val isSuccessful: Boolean get() = code in 200..299
}
