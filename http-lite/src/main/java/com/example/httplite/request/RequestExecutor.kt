package com.example.httplite.request

import com.example.httplite.response.RawResponse
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

internal class RequestExecutor(
    private val request: Request,
) {
    @Throws(IOException::class)
    fun execute(): RawResponse {
        val connection = startConnection(request)
        val code = connection.responseCode
        val headers = connection.headerFields.asHeaders()
        val isSuccessful = code in 200..299

        val stream = if (isSuccessful) {
            connection.inputStream
        } else {
            connection.errorStream
        }
        val rawBody = connection
            .readStream(stream)
            .decodeToString()

        return RawResponse(
            code = code,
            headers = headers,
            body = rawBody.takeIf { isSuccessful },
            errorBody = rawBody.takeIf { !isSuccessful }
        )
    }

    @Throws(IOException::class)
    private fun startConnection(
        request: Request
    ): HttpURLConnection {
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
    }

    @Throws(IOException::class)
    private fun HttpURLConnection.readStream(
        inputStream: InputStream
    ): ByteArray {
        return try {
            val outputStream = ByteArrayOutputStream()
            inputStream.copyTo(outputStream, BUFFER_SIZE)
            outputStream.toByteArray()
        } finally {
            disconnect()
        }
    }

    private fun Map<String?, List<String>>.asHeaders(): Map<String, String> {
        return filterKeys { it != null }
            .mapKeys { it.key!!.trim() }
            .mapValues { it.value.firstOrNull()?.trim().orEmpty() }
    }

    private companion object {
        const val BUFFER_SIZE = 1024
        const val CONNECTION_TIMEOUT_MS = 10_000
    }
}
