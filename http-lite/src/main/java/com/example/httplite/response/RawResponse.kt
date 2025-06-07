package com.example.httplite.response

data class RawResponse(
    val code: Int,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null,
    val errorBody: String? = null
)
