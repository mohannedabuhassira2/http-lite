package com.example.httplite.request

import android.net.Uri
import com.example.httplite.model.ApiException
import com.example.httplite.model.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class Request(
    val method: Method,
    val url: String,
    var headers: Map<String, String> = emptyMap(),
    val queryParams: Map<String, String> = emptyMap(),
    val queryPath: String = "",
    val body: ByteArray? = null
) {
    enum class Method {
        GET, POST, PUT, DELETE
    }

    @Throws(IOException::class, ApiException::class)
    suspend fun execute(): HttpResponse = withContext(Dispatchers.IO) {
        RequestExecutor(
            this@Request
        ).execute()
    }

    fun buildUrl(
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = ""
    ): String {
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

        return baseUri.build().toString()
    }
}