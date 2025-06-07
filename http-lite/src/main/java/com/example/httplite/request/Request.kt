package com.example.httplite.request

import android.net.Uri
import com.example.httplite.response.RawResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class Request(
    var method: Method,
    var url: String,
    var headers: Map<String, String> = emptyMap(),
    var queryParams: Map<String, String> = emptyMap(),
    var queryPath: String = "",
    var body: ByteArray? = null,
) {
    enum class Method {
        GET, POST, PUT, DELETE
    }

    suspend fun execute(): RawResponse = withContext(Dispatchers.IO) {
        RequestExecutor(
            request = this@Request
        ).execute()
    }

    fun buildUrl(): String {
        val baseUri = Uri
            .parse(url)
            .buildUpon()

        if (queryPath.isNotBlank()) {
            queryPath
                .split("/")
                .filter { it.isNotBlank() }
                .forEach { baseUri.appendPath(it) }
        }

        queryParams.forEach { (key, value) ->
            baseUri.appendQueryParameter(key, value)
        }

        return baseUri
            .build()
            .toString()
    }
}