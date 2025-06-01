package com.example.httplite.response

data class HttpResponse(
    val statusCode: Int,
    val headers: Map<String, String>,
    val jsonBody: String
)
