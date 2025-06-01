package com.example.httplite

import com.google.gson.Gson
import com.example.httplite.model.ApiException
import com.example.httplite.model.HttpResponse
import com.example.httplite.request.Request
import com.example.httplite.request.RequestFactory
import java.io.IOException
import java.util.UUID
import kotlin.String

class NetworkManager(
    val baseUrl: String,
    baseQueryParams: Map<String, String> = emptyMap<String, String>()
) {
    val requestFactory: RequestFactory = RequestFactory(
        baseUrl,
        baseQueryParams
    )
    val gson: Gson = Gson()

    @Throws(IOException::class, ApiException::class)
    suspend fun get(
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
    ): HttpResponse {
        return requestFactory.createJsonRequest(
            url = url,
            method = Request.Method.GET,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath
        ).execute()
    }

    @Throws(IOException::class, ApiException::class)
    suspend inline fun <reified T> post(
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
        body: T, // TODO: Ensure T is serializable
    ): HttpResponse {
        return requestFactory.createJsonRequest(
            method = Request.Method.POST,
            url = url,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath,
            body = gson.toJson(body).toByteArray()
        ).execute()
    }

    @Throws(IOException::class, ApiException::class)
    suspend inline fun postWithUrlEncoded(
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
        encodedBodyParams: Map<String, String>,
    ): HttpResponse {
        return requestFactory.createUrlEncodedJsonRequest(
            method = Request.Method.POST,
            url = url,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath,
            encodedBodyParams = encodedBodyParams
        ).execute()
    }

    @Throws(IOException::class, ApiException::class)
    suspend inline fun postWithFormData(
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
        data: Map<String, Any>,
        dataKey: String,
        boundary: String = UUID.randomUUID().toString()
    ): HttpResponse {
        return requestFactory.createFormDataRequest(
            method = Request.Method.POST,
            url = url,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath,
            data = data,
            dataKey = dataKey,
            boundary = boundary
        ).execute()
    }

    @Throws(IOException::class, ApiException::class)
    suspend inline fun <reified T> put(
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
        body: T, // TODO: Ensure T is serializable
    ): HttpResponse {
        return requestFactory.createJsonRequest(
            method = Request.Method.PUT,
            url = url,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath,
            body = gson.toJson(body).toByteArray()
        ).execute()
    }

    @Throws(IOException::class, ApiException::class)
    suspend inline fun putWithUrlEncoded(
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
        encodedBodyParams: Map<String, String>,
    ): HttpResponse {
        return requestFactory.createUrlEncodedJsonRequest(
            method = Request.Method.PUT,
            url = url,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath,
            encodedBodyParams = encodedBodyParams
        ).execute()
    }

    @Throws(IOException::class, ApiException::class)
    suspend inline fun putWithFormData(
        url: String = baseUrl,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
        queryPath: String = "",
        data: Map<String, Any>,
        dataKey: String,
        boundary: String = UUID.randomUUID().toString()
    ): HttpResponse {
        return requestFactory.createFormDataRequest(
            method = Request.Method.PUT,
            url = url,
            headers = headers,
            queryParams = queryParams,
            queryPath = queryPath,
            data = data,
            dataKey = dataKey,
            boundary = boundary
        ).execute()
    }
}