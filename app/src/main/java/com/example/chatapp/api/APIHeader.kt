package com.example.chatapp.api

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class APIHeader : Interceptor {

    companion object {
        const val CONTENT_TYPE = "Content-Type"
        const val APPLICATION_JSON = "application/json"

        const val ACCEPT = "Accept"
        const val ACCEPT_CHARSET = "Accept_Charset"
        const val UTF8 = "UTF-8"
    }


    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
            .newBuilder()
            .addHeader(CONTENT_TYPE, APPLICATION_JSON)
            .addHeader(ACCEPT, APPLICATION_JSON)
            .addHeader(ACCEPT_CHARSET, UTF8)
            .build()
        return chain.proceed(request)
    }
}