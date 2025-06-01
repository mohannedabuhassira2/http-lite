package com.example.httplite.request

import com.google.gson.Gson
import com.example.httplite.model.MediaData
import core.api.request.Request
import com.example.httplite.request.builder.FormDataOutputBuilder
import java.net.URLEncoder
import java.util.UUID
import kotlin.collections.plus

internal class RequestFactory(
    private val baseUrl: String,
    private val baseQueryParams: Map<String, String> = emptyMap<String, String>(),
    private val gson: Gson = Gson()
){
    fun createJsonRequest(
        method: Request.Method,
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
        body: ByteArray? = null
    ): Request {
        var request = createBasicRequest(
            method = method,
            url = url,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath,
            body = body
        )
        request.headers + ("Content-Type" to "application/json; charset=UTF-8")
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
            body = encodedBody.toByteArray()
        )
        request.headers + ("Content-Type" to "application/x-www-form-urlencoded")
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
        boundary: String = UUID.randomUUID().toString()
    ): Request {
        val body = buildFormDataBody(
            data = data,
            dataKey = dataKey,
            boundary = boundary
        )
        val request = Request(
            method = method,
            url = url,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath,
            body = body
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
        body: ByteArray? = null
    ): Request {
        return Request(
            method = method,
            url = url,
            headers = headers,
            queryParams = baseQueryParams + queryParams,
            queryPath = queryPath,
            body = body
        )
    }

    private fun buildFormDataBody(
        data: Map<String, Any>,
        dataKey: String,
        boundary: String
    ): ByteArray {
        val builder = FormDataOutputBuilder(boundary, gson)

        data.forEach { (key, value) ->
            val fieldName = "$dataKey[$key]"
            when (value) {
                is MediaData -> builder.writeMediaPart(fieldName, value)
                else -> builder.writeJsonPart(fieldName, value)
            }
        }

        return builder.writeEndBoundary().toByteArray()
    }

    private fun Map<String, String>.toUrlEncodedBody(): String {
        return entries.joinToString("&") { (key, value) ->
            "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}"
        }
    }
}
