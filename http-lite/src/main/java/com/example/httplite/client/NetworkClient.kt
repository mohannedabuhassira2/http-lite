package com.example.httplite.client

import android.net.http.HttpResponseCache
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.example.httplite.request.Request
import com.example.httplite.response.ApiResponse
import core.api.request.builder.RequestFactory
import java.io.File
import java.io.IOException
import java.util.UUID

class NetworkClient(
    private val baseUrl: String,
    baseHeaders: Map<String, String> = emptyMap<String, String>(),
    baseQueryParams: Map<String, String> = emptyMap<String, String>(),
    private val gson: Gson = Gson(),
    private val cacheDirectory: String? = null,
    private val cacheSizeBytes: Long = CACHE_SIZE_BYTES
) {
    private val requestFactory: RequestFactory = RequestFactory(
        baseUrl,
        baseHeaders,
        baseQueryParams,
        gson
    )

    init {
        setUpCache()
    }

    @Throws (IOException::class, JsonSyntaxException::class)
    suspend fun <T> get(
        responseClass: Class<T>,
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
    ): ApiResponse<T> {
        val request = requestFactory.createJsonRequest(
            url = url,
            method = Request.Method.GET,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath
        )
        return parseToApiResult<T>(
            responseClass,
            request
        )
    }

    @Throws (IOException::class, JsonSyntaxException::class)
    suspend fun <T> post(
        responseClass: Class<T>,
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
        body: Any,
    ): ApiResponse<T> {
        val request = requestFactory.createJsonRequest(
            method = Request.Method.POST,
            url = url,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath,
            body = gson.toJson(body).toByteArray()
        )
        return parseToApiResult<T>(
            responseClass,
            request
        )
    }

    @Throws (IOException::class, JsonSyntaxException::class)
    suspend fun <T> postWithUrlEncoded(
        responseClass: Class<T>,
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
        encodedBodyParams: Map<String, String>,
    ): ApiResponse<T> {
        val request = requestFactory.createUrlEncodedJsonRequest(
            method = Request.Method.POST,
            url = url,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath,
            encodedBodyParams = encodedBodyParams
        )
        return parseToApiResult<T>(
            responseClass,
            request
        )
    }

    @Throws (IOException::class, JsonSyntaxException::class)
    suspend fun <T> postWithFormData(
        responseClass: Class<T>,
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
        data: Map<String, Any>,
        dataKey: String,
        boundary: String = UUID.randomUUID().toString()
    ): ApiResponse<T> {
        val request = requestFactory.createFormDataRequest(
            method = Request.Method.POST,
            url = url,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath,
            data = data,
            dataKey = dataKey,
            boundary = boundary
        )
        return parseToApiResult<T>(
            responseClass,
            request
        )
    }

    @Throws (IOException::class, JsonSyntaxException::class)
    suspend fun <T> put(
        responseClass: Class<T>,
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
        body: Any,
    ): ApiResponse<T> {
        val request = requestFactory.createJsonRequest(
            method = Request.Method.PUT,
            url = url,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath,
            body = gson.toJson(body).toByteArray()
        )
        return parseToApiResult<T>(
            responseClass,
            request
        )
    }

    @Throws (IOException::class, JsonSyntaxException::class)
    suspend fun <T> putWithUrlEncoded(
        responseClass: Class<T>,
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
        encodedBodyParams: Map<String, String>,
    ): ApiResponse<T> {
        val request = requestFactory.createUrlEncodedJsonRequest(
            method = Request.Method.PUT,
            url = url,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath,
            encodedBodyParams = encodedBodyParams
        )
        return parseToApiResult<T>(
            responseClass,
            request
        )
    }

    @Throws (IOException::class, JsonSyntaxException::class)
    suspend fun <T> putWithFormData(
        responseClass: Class<T>,
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
        data: Map<String, Any>,
        dataKey: String,
        boundary: String = UUID.randomUUID().toString()
    ): ApiResponse<T> {
        val request = requestFactory.createFormDataRequest(
            method = Request.Method.PUT,
            url = url,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath,
            data = data,
            dataKey = dataKey,
            boundary = boundary
        )
        return parseToApiResult<T>(
            responseClass,
            request
        )
    }

    @Throws (IOException::class, JsonSyntaxException::class)
    private suspend fun <T> parseToApiResult(
        responseClass: Class<T>,
        request: Request
    ): ApiResponse<T> {
        val httpResponse = request.execute()
        val jsonBody = httpResponse.body
        val errorBody = httpResponse.errorBody
        val code = httpResponse.code
        val headers = httpResponse.headers

        if (httpResponse.code !in 200..299) {
            return ApiResponse<T>(
                body = null,
                code = code,
                headers = headers,
                errorBody = errorBody
            )
        }

        val parsedBody = parseBody<T>(
            responseClass,
            jsonBody,
        )

        return ApiResponse<T>(
            body = parsedBody,
            code = code,
            headers = headers,
            errorBody = errorBody
        )
    }

    @Throws (JsonSyntaxException::class)
    private fun <T> parseBody(
        responseClass: Class<T>,
        jsonBody: String?,
    ): T? {
        if (responseClass == Void::class.java) {
            return null
        }

        return gson.fromJson(
            jsonBody,
            responseClass
        )
    }

    @Throws (IOException::class)
    private fun setUpCache() {
        if (cacheDirectory == null || HttpResponseCache.getInstalled() != null) {
            return
        }

        val httpCacheDir = File(
            cacheDirectory,
            "http-cache"
        )
        val httpCacheFile = File(
            httpCacheDir,
            "http"
        )
        HttpResponseCache.install(
            httpCacheFile,
            cacheSizeBytes
        )
    }

    private companion object {
        const val CACHE_SIZE_BYTES = 128 * 1024L
    }
}

