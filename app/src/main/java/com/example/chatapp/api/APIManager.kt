package com.set.app.entertainment.api

import com.facebook.stetho.okhttp3.BuildConfig
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.foundationPractice.api.HttpLogger
import com.google.gson.Gson

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class APIManager {
    companion object {
        private const val CONNECT_TIME_OUT = 10L
        private const val READ_TIME_OUT = 10L
        private const val WRITE_TIME_OUT = 10L


        fun getRetrofit(baseUrl: String): Retrofit {
            val okHttpBuilder = getOkHttpClient()
            if (BuildConfig.DEBUG) {
                val loggingInterceptor = HttpLoggingInterceptor(HttpLogger())
                loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
                okHttpBuilder.addInterceptor(loggingInterceptor)
                okHttpBuilder.addNetworkInterceptor(StethoInterceptor())
            }
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(Gson()))
                .client(okHttpBuilder.build())
                .build()
        }

        private fun getOkHttpClient(): OkHttpClient.Builder {
            return OkHttpClient().newBuilder()
                .connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIME_OUT, TimeUnit.SECONDS)
//                .addInterceptor(APIHeader())
        }
    }
}

