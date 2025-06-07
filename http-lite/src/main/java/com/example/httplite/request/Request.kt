package com.example.httplite.request

import android.net.Uri
import com.example.httplite.request.interceptor.RequestInterceptor
import com.example.httplite.response.RawResponse

class Request(
    var method: Method,
    var url: String,
    var headers: Map<String, String> = emptyMap(),
    var queryParams: Map<String, String> = emptyMap(),
    var queryPath: String = "",
    var body: ByteArray? = null,
    var requestInterceptors: List<RequestInterceptor> = emptyList()
) {
    enum class Method {
        GET, POST, PUT, DELETE
    }

    val fullUrl: String
        get() {
            val base = url.trimEnd('/')
            val path = queryPath.trimStart('/')
            val baseWithPath = if (path.isNotBlank()) "$base/$path" else base
            val uriBuilder = Uri
                .parse(baseWithPath)
                .buildUpon()

            queryParams.forEach { (key, value) ->
                uriBuilder.appendQueryParameter(key, value)
            }

            return uriBuilder.build().toString()
        }

    private val requestExecutor = RequestExecutor(
        this,
        requestInterceptors
    )

    internal suspend fun executeWithInterceptors(): RawResponse {
        return requestExecutor.execute()
    }

    fun execute(): RawResponse {
        return requestExecutor.doNetworkCall()
    }
}
