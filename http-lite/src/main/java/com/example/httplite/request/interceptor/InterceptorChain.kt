package com.example.httplite.request.interceptor

import com.example.httplite.request.Request
import com.example.httplite.response.RawResponse

internal class InterceptorChain(
    private val interceptors: List<RequestInterceptor>,
    private val index: Int,
    override val request: Request,
    private val networkCall: () -> RawResponse
) : RequestInterceptor.Chain {
    override suspend fun proceed(request: Request): RawResponse {
        if (index >= interceptors.size) {
            return networkCall()
        }

        val next = InterceptorChain(
            interceptors,
            index + 1,
            request,
            networkCall
        )
        val currentInterceptor = interceptors[index]
        return currentInterceptor.intercept(next)
    }
}
