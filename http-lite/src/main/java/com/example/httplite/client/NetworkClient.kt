package com.example.httplite.client

import android.net.http.HttpResponseCache
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.example.httplite.request.Request
import com.example.httplite.request.builder.RequestFactory
import com.example.httplite.request.interceptor.RequestInterceptor
import com.example.httplite.response.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlin.jvm.Throws

class NetworkClient(
    private val baseUrl: String,
    baseHeaders: Map<String, String> = emptyMap<String, String>(),
    baseQueryParams: Map<String, String> = emptyMap<String, String>(),
    private val gson: Gson = Gson(),
    requestInterceptors: List<RequestInterceptor> = emptyList(),
    private val cacheDirectory: String? = null,
    private val cacheSizeBytes: Long = CACHE_SIZE_BYTES
) {
    private val requestFactory: RequestFactory = RequestFactory(
        baseUrl,
        baseHeaders,
        baseQueryParams,
        requestInterceptors,
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
        return apiCall<T>(
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
        return apiCall<T>(
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
        return apiCall<T>(
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
        return apiCall<T>(
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
        return apiCall<T>(
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
        return apiCall<T>(
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
        return apiCall<T>(
            responseClass,
            request
        )
    }

    private suspend fun <T> apiCall(
        responseClass: Class<T>,
        request: Request
    ): ApiResponse<T> = withContext(Dispatchers.IO) {
        val rawResponse = request.executeWithInterceptors()
        val jsonBody = rawResponse.body
        val errorBody = rawResponse.errorBody
        val code = rawResponse.code
        val headers = rawResponse.headers

        if (rawResponse.code !in 200..299) {
             ApiResponse<T>(
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

        ApiResponse<T>(
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
