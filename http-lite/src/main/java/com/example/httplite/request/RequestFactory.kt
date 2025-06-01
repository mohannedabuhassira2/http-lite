package com.example.httplite.request

import com.google.gson.Gson
import com.example.httplite.model.MediaData
import com.example.httplite.request.Request.Method
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.UUID

class RequestFactory(
    private val baseUrl: String,
    private val baseQueryParams: Map<String, String> = emptyMap<String, String>()
){
    private val gson: Gson = Gson()

    fun createJsonRequest(
        method: Method,
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
            queryParams = baseQueryParams + queryParams,
            queryPath = queryPath,
            body = body
        )
        request.headers += ("Content-Type" to "application/json; charset=UTF-8")
        return request
    }

    fun createUrlEncodedJsonRequest(
        method: Method,
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
            headers = headers + ("Content-Type" to "application/x-www-form-urlencoded"),
            queryParams = baseQueryParams + queryParams,
            queryPath = queryPath,
            body = encodedBody.toByteArray()
        )
        return request
    }

    fun createFormDataRequest(
        method: Method,
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
            queryParams = baseQueryParams + queryParams,
            queryPath = queryPath,
            body = body
        )
        request.headers += ("Content-Type" to "multipart/form-data; boundary=$boundary")
        return request
    }

    private fun createBasicRequest(
        method: Method,
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
        val body = ByteArrayOutputStream()
        val newline = "\r\n"

        data.forEach { (key, value) ->
            body.writeString("--$boundary$newline")

            when (value) {
                is MediaData -> {
                    val filename = value.file.name
                    val contentDisposition = "Content-Disposition: form-data; name=\"$dataKey[$key]\"; filename=\"$filename\"$newline"
                    val contentType = "Content-Type: ${value.contentType}$newline$newline"

                    body.writeString(contentDisposition)
                    body.writeString(contentType)
                    body.write(value.file.readBytes())
                }
                else -> {
                    body.writeString("Content-Disposition: form-data; name=\"$dataKey[$key]\"$newline$newline")
                    val json = gson.toJson(value)
                    body.writeString(json)
                }
            }

            body.writeString(newline)
        }

        body.writeString("--$boundary--$newline")
        return body.toByteArray()
    }

    private fun ByteArrayOutputStream.writeString(text: String) {
        this.write(text.toByteArray())
    }

    private fun Map<String, String>.toUrlEncodedBody(): String {
        val urlEncoded = entries.joinToString("&") { (key, value) ->
            "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}"
        }
        return urlEncoded
    }
}
