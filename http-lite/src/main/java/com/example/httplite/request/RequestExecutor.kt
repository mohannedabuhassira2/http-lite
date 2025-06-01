package com.example.httplite.request

import com.example.httplite.model.ApiException
import com.example.httplite.model.HttpResponse
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class RequestExecutor(
    private val request: Request,
) {
    private val url: String = request.buildUrl(
        request.queryParams,
        request.queryPath
    )

    @Throws(IOException::class, ApiException::class)
    fun execute(): HttpResponse {
        val connection = getHttpURLConnection()
        val rawResponseBody = parseResponse(connection)

        return HttpResponse(
            statusCode = connection.responseCode,
            headers = connection.headerFields.cleanHeaders(),
            jsonBody = rawResponseBody.decodeToString()
        )
    }

    @Throws(IOException::class)
    private fun getHttpURLConnection(): HttpURLConnection {
        val connection = URL(url).openConnection() as HttpURLConnection

        connection.requestMethod = request.method.toString()

        request.headers.forEach { (header, headerValue) ->
            connection.setRequestProperty(header, headerValue)
        }

        request.body?.let { requestBody ->
            connection.doOutput = true
            connection.outputStream.use { it.write(requestBody) }
        }

        return connection
    }

    @Throws(ApiException::class)
    private fun parseResponse(connection: HttpURLConnection): ByteArray {
        val status = connection.responseCode
        if (status != HttpURLConnection.HTTP_OK) {
            throw ApiException("Request failed with status code: $status")
        }

        return try {
            val inputStream = connection.inputStream
            val outputStream = ByteArrayOutputStream()
            inputStream.copyTo(outputStream, BUFFER_SIZE)
            outputStream.toByteArray()
        } finally {
            connection.disconnect()
        }
    }

    private fun Map<String?, List<String>>.cleanHeaders(): Map<String, String> {
        return filterKeys { it != null }
            .mapKeys { it.key?.lowercase() ?: "" }
            .mapValues { it.value.firstOrNull() ?: "" }
    }

    private companion object {
        const val BUFFER_SIZE = 1024
    }
}