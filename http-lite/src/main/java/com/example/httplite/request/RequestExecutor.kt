package com.example.httplite.request

import com.example.httplite.request.interceptor.InterceptorChain
import com.example.httplite.request.interceptor.RequestInterceptor
import com.example.httplite.response.RawResponse
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

internal class RequestExecutor(
    private val request: Request,
    private val requestInterceptor: List<RequestInterceptor> = emptyList()
) {
    @Throws(IOException::class)
    suspend fun execute(): RawResponse {
        return if (requestInterceptor.isNotEmpty()) {
            InterceptorChain(
                interceptors = requestInterceptor,
                index = 0,
                request = request,
                networkCall = ::doNetworkCall
            ).proceed(request)
        } else {
            doNetworkCall()
        }
    }

    @Throws(IOException::class)
    fun doNetworkCall(): RawResponse {
        return try {
            val connection = startConnection(request)

            val stream = if (connection.isSuccessful) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val rawBody = connection
                .readStream(stream)
                .decodeToString()

            RawResponse(
                code = connection.responseCode,
                headers = connection.headerFields.asHeaders(),
                body = rawBody.takeIf { connection.isSuccessful },
                errorBody = rawBody.takeUnless { connection.isSuccessful }
            )
        } catch (e: Exception) {
            // Treat other rare exceptions as IOException. This include exceptions
            // like MalformedURLException that is thrown if the URL is invalid
            throw IOException(e)
        }
    }

    @Throws(IOException::class)
    private fun startConnection(
        request: Request
    ): HttpsURLConnection {
        val fullUrl = URL(request.fullUrl)
        val connection = fullUrl.openConnection() as HttpsURLConnection

        connection.requestMethod = request.method.toString()
        connection.connectTimeout = CONNECTION_TIMEOUT_MS
        connection.readTimeout = CONNECTION_TIMEOUT_MS
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
    private fun HttpsURLConnection.readStream(
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

    private val HttpsURLConnection.isSuccessful: Boolean
        get() = responseCode in 200..299

    private companion object {
        const val BUFFER_SIZE = 1024
        const val CONNECTION_TIMEOUT_MS = 3_000
    }
}
