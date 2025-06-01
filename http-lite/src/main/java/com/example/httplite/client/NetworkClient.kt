package com.example.httplite.client

import android.net.http.HttpResponseCache
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import core.api.request.Request
import com.example.httplite.request.RequestFactory
import com.example.httplite.response.ApiResult
import java.io.File
import java.io.IOException
import java.util.UUID

class NetworkClient(
    private val baseUrl: String,
    baseQueryParams: Map<String, String> = emptyMap<String, String>(),
    private val cacheDirectory: String? = null,
    private val cacheSizeBytes: Long = CACHE_SIZE_BYTES
) {
    private var gson: Gson = Gson()
    private val requestFactory: RequestFactory = RequestFactory(
        baseUrl,
        baseQueryParams,
        gson
    )

    init {
        setUpCache()
    }

    suspend fun <T> get(
        responseClass: Class<T>,
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
    ): ApiResult<T> {
        val request = requestFactory.createJsonRequest(
            url = url,
            method = Request.Method.GET,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath
        )
        return startApiCall(
            responseClass,
            request
        )
    }

    suspend fun <T> post(
        responseClass: Class<T>,
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
        body: Any,
    ): ApiResult<T> {
        val request = requestFactory.createJsonRequest(
            method = Request.Method.POST,
            url = url,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath,
            body = gson.toJson(body).toByteArray()
        )
        return startApiCall<T>(
            responseClass,
            request
        )
    }

    suspend fun <T> postWithUrlEncoded(
        responseClass: Class<T>,
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
        encodedBodyParams: Map<String, String>,
    ): ApiResult<T> {
        val request = requestFactory.createUrlEncodedJsonRequest(
            method = Request.Method.POST,
            url = url,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath,
            encodedBodyParams = encodedBodyParams
        )
        return startApiCall<T>(
            responseClass,
            request
        )
    }

    suspend fun <T> postWithFormData(
        responseClass: Class<T>,
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
        data: Map<String, Any>,
        dataKey: String,
        boundary: String = UUID.randomUUID().toString()
    ): ApiResult<T> {
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
        return startApiCall<T>(
            responseClass,
            request
        )
    }

    suspend fun <T> put(
        responseClass: Class<T>,
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
        body: Any,
    ): ApiResult<T> {
        val request = requestFactory.createJsonRequest(
            method = Request.Method.PUT,
            url = url,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath,
            body = gson.toJson(body).toByteArray()
        )
        return startApiCall<T>(
            responseClass,
            request
        )
    }

    suspend fun <T> putWithUrlEncoded(
        responseClass: Class<T>,
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
        encodedBodyParams: Map<String, String>,
    ): ApiResult<T> {
        val request = requestFactory.createUrlEncodedJsonRequest(
            method = Request.Method.PUT,
            url = url,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath,
            encodedBodyParams = encodedBodyParams
        )
        return startApiCall<T>(
            responseClass,
            request
        )
    }

    suspend fun <T> putWithFormData(
        responseClass: Class<T>,
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
        data: Map<String, Any>,
        dataKey: String,
        boundary: String = UUID.randomUUID().toString()
    ): ApiResult<T> {
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
        return startApiCall<T>(
            responseClass,
            request
        )
    }

    fun setGson(gson: Gson) {
        this.gson = gson
    }

    private suspend fun <T> startApiCall(
        responseClass: Class<T>,
        request: Request
    ): ApiResult<T> {
        return try {
            val httpResponse = request.execute()

            val parsedHttpResponse = gson.fromJson<T>(
                httpResponse.jsonBody,
                responseClass
            )

            ApiResult.Response<T>(
                data = parsedHttpResponse,
                headers = emptyMap<String, String>(),
                statusCode = httpResponse.statusCode
            )
        } catch (jsonException: JsonSyntaxException) {
            ApiResult.SerializationFailed(jsonException)
        } catch (ioException: IOException) {
            ApiResult.NetworkFailed(ioException)
        }
    }

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
