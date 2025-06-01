package com.example.httplite.client

import com.example.httplite.request.Request
import com.example.httplite.request.RequestFactory
import com.example.httplite.response.ApiResult
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.IOException
import java.util.UUID

class NetworkClient(
    val baseUrl: String,
    baseQueryParams: Map<String, String> = emptyMap<String, String>(),
    val gson: Gson = Gson()
) {
    val requestFactory: RequestFactory = RequestFactory(
        baseUrl,
        baseQueryParams,
        gson
    )

    suspend inline fun <reified T> get(
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
        return apiCall<T>(request)
    }

    suspend inline fun <reified T> post(
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
        return apiCall<T>(request)
    }

    suspend inline fun <reified T> postWithUrlEncoded(
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
        return apiCall<T>(request)
    }

    suspend inline fun <reified T> postWithFormData(
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
        return apiCall<T>(request)
    }

    suspend inline fun <reified T> put(
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
        return apiCall<T>(request)
    }

    suspend inline fun <reified T> putWithUrlEncoded(
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
        return apiCall<T>(request)
    }

    suspend inline fun <reified T> putWithFormData(
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
        return apiCall<T>(request)
    }

    suspend inline fun <reified T> apiCall(
        request: Request
    ): ApiResult<T> {
        return try {
            val httpResponse = request.execute()

            val parsedHttpResponse = gson.fromJson<T>(
                httpResponse.jsonBody,
                T::class.java
            )

            ApiResult.Response<T>(
                data = parsedHttpResponse,
                headers = httpResponse.headers,
                statusCode = httpResponse.statusCode
            )
        } catch (jsonException: JsonSyntaxException) {
            ApiResult.SerializationFailed(jsonException)
        } catch (ioException: IOException) {
            ApiResult.NetworkFailed(ioException)
        }
    }
}