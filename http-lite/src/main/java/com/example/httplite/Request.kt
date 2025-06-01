package com.example.httplite

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class Request(
    val method: Method,
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val queryParams: Map<String, String> = emptyMap(),
    val queryPath: String = "",
    val body: ByteArray? = null
) {
    enum class Method {
        GET, POST, DELETE, PUT
    }

    @Throws(IOException::class, ApiException::class)
    suspend fun execute(): HttpResponse = withContext(Dispatchers.IO) {
        RequestExecutor(
            this@Request,
            url.buildUrl(
                queryParams,
                queryPath
            ),
            body
        ).execute()
    }

    private fun String.buildUrl(
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = ""
    ): String {
        val baseUri = Uri.parse(this).buildUpon()

        if (queryPath.isNotBlank()) {
            queryPath
                .split("/")
                .filter { it.isNotBlank() }
                .forEach { baseUri.appendPath(it) }
        }

        queryParams.forEach { (key, value) ->
            baseUri.appendQueryParameter(key, value)
        }

        return baseUri.build().toString()
    }
}
