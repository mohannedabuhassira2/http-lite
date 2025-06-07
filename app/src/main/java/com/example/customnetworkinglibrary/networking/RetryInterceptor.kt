package com.example.customnetworkinglibrary.networking

import android.util.Log
import com.example.httplite.request.interceptor.RequestInterceptor
import com.example.httplite.response.RawResponse
import kotlinx.coroutines.delay
import java.io.IOException

/**
 * A Simple retry request interceptor to retry non-2xx requests up to 3 times with an exponential backoff,
 * starting with 100ms delay. If all attempts are unsuccessful, preserver the behavior that networking connections
 * would still lead to IOExceptions, and non-successful responses would be returned as they are with the
 * status code and errorBody.
 */
class RetryInterceptor : RequestInterceptor {
    override suspend fun intercept(chain: RequestInterceptor.Chain): RawResponse {
        var currentDelay = START_DELAY_MILLIS
        var lastException: IOException? = null

        repeat(MAX_ATTEMPTS) { retryAttempt ->
            try {
                val request = chain.request
                val response = chain.proceed(request)
                val notSuccessfulOnLastAttempt = !response.isSuccessful && retryAttempt == MAX_ATTEMPTS - 1
                if (response.isSuccessful || notSuccessfulOnLastAttempt) {
                    return response
                }
            } catch (e: IOException) {
                if (retryAttempt == MAX_ATTEMPTS - 1) {
                    lastException = e
                }
            }

            Log.d(
                "Networking",
                "Request failed: url: ${chain.request.fullUrl}, retryAttempt: $retryAttempt",
            )

            currentDelay *= 2
            delay(currentDelay)
        }

        throw IOException(lastException)
    }

    private companion object {
        const val MAX_ATTEMPTS = 3
        const val START_DELAY_MILLIS = 100L
    }
}