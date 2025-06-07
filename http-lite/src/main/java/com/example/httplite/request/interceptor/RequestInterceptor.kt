package com.example.httplite.request.interceptor

import com.example.httplite.request.Request
import com.example.httplite.response.RawResponse

interface RequestInterceptor {
    suspend fun intercept(chain: Chain): RawResponse

    interface Chain {
        val request: Request
        suspend fun proceed(request: Request): RawResponse
    }
}
