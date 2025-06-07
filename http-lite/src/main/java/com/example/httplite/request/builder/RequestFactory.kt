package com.example.httplite.request.builder

import com.google.gson.Gson
import com.example.httplite.request.Request
import com.example.httplite.request.interceptor.RequestInterceptor
import java.net.URLEncoder
import java.util.UUID
import kotlin.collections.plus

internal class RequestFactory(
    private val baseUrl: String,
    private val baseHeaders: Map<String, String> = emptyMap(),
    private val baseQueryParams: Map<String, String> = emptyMap<String, String>(),
    private val requestInterceptors: List<RequestInterceptor> = emptyList(),
    private val gson: Gson = Gson()
){
    fun createJsonRequest(
        method: Request.Method,
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
        body: ByteArray? = null,
    ): Request {
        val request = createBasicRequest(
            method = method,
            url = url,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath,
            body = body,
            requestInterceptors = requestInterceptors
        )
        request.headers += ("Content-Type" to "application/json; charset=UTF-8")
        return request
    }

    fun createUrlEncodedJsonRequest(
        method: Request.Method,
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
        encodedBodyParams: Map<String, String>,
    ): Request {
        val encodedBody = encodedBodyParams.toUrlEncodedBody()
        val request = createBasicRequest(
            method = method,
            url = url,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath,
            body = encodedBody.toByteArray(),
            requestInterceptors = requestInterceptors
        )
        request.headers += ("Content-Type" to "application/x-www-form-urlencoded")
        return request
    }

    fun createFormDataRequest(
        method: Request.Method,
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
        dataKey: String,
        data: Map<String, Any>,
        boundary: String = UUID.randomUUID().toString(),
    ): Request {
        val builder = FormDataBodyBuilder(
            data = data,
            dataKey = dataKey,
            boundary = boundary,
            gson = gson
        )
        val request = Request(
            method = method,
            url = url,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath,
            body = builder.buildFormDataBody(),
            requestInterceptors = requestInterceptors
        )
        request.headers += ("Content-Type" to "multipart/form-data; boundary=$boundary")
        return request
    }

    private fun createBasicRequest(
        method: Request.Method,
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
        body: ByteArray? = null,
        requestInterceptors: List<RequestInterceptor>
    ): Request {
        return Request(
            method = method,
            url = url,
            headers = baseHeaders + headers,
            queryParams = baseQueryParams + queryParams,
            queryPath = queryPath,
            body = body,
            requestInterceptors = requestInterceptors
        )
    }

    private fun Map<String, String>.toUrlEncodedBody(): String {
        return entries.joinToString("&") { (key, value) ->
            "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}"
        }
    }
}
