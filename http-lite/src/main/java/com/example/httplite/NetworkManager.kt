package com.example.httplite

import kotlinx.serialization.json.Json
import java.io.IOException

class NetworkManager(
    val baseQueryParams: Map<String, String> = emptyMap<String, String>(),
    val baseUrl: String
) {
    @Throws(IOException::class, ApiException::class)
    suspend fun get(
        url: String = baseUrl,
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = ""
    ): HttpResponse {
        return Request(
            method = Request.Method.GET,
            url = url,
            queryParams = baseQueryParams + queryParams,
            queryPath = queryPath
        ).execute()
    }

    @Throws(IOException::class, ApiException::class)
    suspend inline fun <reified T> post(
        url: String = baseUrl,
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
        body: T, // TODO: Ensure T is serializable
    ): HttpResponse {
        return Request(
            method = Request.Method.POST,
            url = url,
            queryParams = baseQueryParams + queryParams,
            queryPath = queryPath,
            body = Json.encodeToString(body).toByteArray()
        ).execute()
    }
}