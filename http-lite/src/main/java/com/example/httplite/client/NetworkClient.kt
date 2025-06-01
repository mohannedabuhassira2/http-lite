package com.example.httplite.client

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.example.httplite.request.Request
import com.example.httplite.request.RequestFactory
import com.example.httplite.response.ApiException
import com.example.httplite.response.ApiResponse
import com.example.httplite.response.HttpResponse
import java.io.IOException
import java.util.UUID

class NetworkClient(
    val baseUrl: String,
    baseQueryParams: Map<String, String> = emptyMap<String, String>(),
    private val gson: Gson = Gson()
) {
    val requestFactory: RequestFactory = RequestFactory(
        baseUrl,
        baseQueryParams,
        gson
    )

    @Throws(
        IOException::class,
        ApiException::class,
        JsonSyntaxException::class
    )
    suspend inline fun <reified R> get(
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
    ): ApiResponse<R> {
        val request = requestFactory.createJsonRequest(
            url = url,
            method = Request.Method.GET,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath
        )
        val httpResponse = request.execute()
        return httpResponse.toApiResponse<R>()
    }

    @Throws(
        IOException::class,
        ApiException::class,
        JsonSyntaxException::class
    )
    suspend inline fun <reified T> post(
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
        val httpResponse = request.execute()
        return httpResponse.toApiResponse<T>()
    }

    @Throws(
        IOException::class,
        ApiException::class,
        JsonSyntaxException::class
    )
    suspend inline fun <reified T> postWithUrlEncoded(
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
        val httpResponse = request.execute()
        return httpResponse.toApiResponse<T>()
    }

    @Throws(
        IOException::class,
        ApiException::class,
        JsonSyntaxException::class
    )
    suspend inline fun <reified T> postWithFormData(
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
        val httpResponse = request.execute()
        return httpResponse.toApiResponse<T>()
    }

    @Throws(
        IOException::class,
        ApiException::class,
        JsonSyntaxException::class
    )
    suspend inline fun <reified T> put(
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
        val httpResponse = request.execute()
        return httpResponse.toApiResponse<T>()
    }

    @Throws(
        IOException::class,
        ApiException::class,
        JsonSyntaxException::class
    )
    suspend inline fun <reified T> putWithUrlEncoded(
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
        val httpResponse = request.execute()
        return httpResponse.toApiResponse<T>()
    }

    @Throws(
        IOException::class,
        ApiException::class,
        JsonSyntaxException::class
    )
    suspend inline fun <reified T> putWithFormData(
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
        val httpResponse = request.execute()
        return httpResponse.toApiResponse<T>()
    }

    @Throws(JsonSyntaxException::class)
    inline fun <reified T> HttpResponse.toApiResponse(): ApiResponse<T> {
        val data = gson.fromJson<T>(
            jsonBody,
            T::class.java
        )
        return ApiResponse<T>(
            data = data,
            headers = headers,
            statusCode = statusCode
        )
    }
}