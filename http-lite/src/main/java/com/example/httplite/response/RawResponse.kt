package com.example.httplite.response

data class RawResponse(
    var body: String? = null,
    var code: Int,
    var headers: Map<String, String> = emptyMap(),
    var errorBody: String? = null
) {
    val isSuccessful: Boolean get() = code in 200..299
}
