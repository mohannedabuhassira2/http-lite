package com.example.httplite.request

import core.api.request.Request
import com.example.httplite.response.HttpResponse
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

internal class RequestExecutor(
    private val request: Request
) {
    @Throws(IOException::class)
    fun execute(): HttpResponse {
        val connection = startConnection()
        val rawResponseBody = parseResponse(connection)

        return HttpResponse(
            statusCode = connection.responseCode,
            headers = connection.headerFields.cleanHeaders(),
            jsonBody = rawResponseBody.decodeToString()
        )
    }

    @Throws(IOException::class)
    private fun startConnection(): HttpURLConnection {
        try {
            val fullUrl = URL(request.buildUrl())
            val connection = fullUrl.openConnection() as HttpURLConnection

            connection.requestMethod = request.method.toString()
            connection.connectTimeout = CONNECTION_TIMEOUT_MS
            connection.useCaches = true

            request.headers.forEach { (header, headerValue) ->
                connection.setRequestProperty(header, headerValue)
            }

            request.body?.let { requestBody ->
                connection.doOutput = true
                connection.outputStream.use { it.write(requestBody) }
            }

            return connection
        } catch (e: Exception) {
            throw IOException("Failed to create or configure HTTP connection: ${e.message}", e)
        }
    }

    @Throws(IOException::class)
    private fun parseResponse(connection: HttpURLConnection): ByteArray {
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
        const val CONNECTION_TIMEOUT_MS = 10_000
    }
}
