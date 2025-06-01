package com.example.httplite

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

internal class RequestTask(
    private val req: Http.Request
) : Runnable {
    override fun run() {
        try {
            val connection = getHttpURLConn()
            val parsedResponse = parseResponse(connection)

            req.sendResponse(parsedResponse, null)
        } catch (e: IOException) {
            req.sendResponse(null, e)
        }
    }

    @Throws(IOException::class)
    private fun getHttpURLConn(): HttpURLConnection {
        val url = URL(req.url)
        val connection = url.openConnection() as HttpURLConnection
        val method = req.method.toString()
        connection.requestMethod = method

        req.header.entries.forEach { header ->
            connection.setRequestProperty(header.key, header.value)
        }

        if (req.body != null) {
            val outputStream = connection.outputStream
            outputStream.write(req.body)
        }

        connection.connect()
        return connection
    }

    @Throws(IOException::class)
    private fun parseResponse(conn: HttpURLConnection): Response {
        try {
            val outputStream = ByteArrayOutputStream()
            val status = conn.responseCode
            val inputStream = if (status.isValidStatus()) conn.inputStream else conn.errorStream
            var currentRead: Int
            var totalRead = 0

            val buffer = ByteArray(BUFFER_SIZE)
            while (inputStream.read(buffer).also { currentRead = it } != -1) {
                outputStream.write(buffer, 0, currentRead)
                totalRead += currentRead
            }

            return Response(outputStream.toByteArray())
        } finally {
            conn.disconnect()
        }
    }

    private fun Int.isValidStatus() = this in 200..299

    private companion object {
        const val BUFFER_SIZE = 1024
    }
}
