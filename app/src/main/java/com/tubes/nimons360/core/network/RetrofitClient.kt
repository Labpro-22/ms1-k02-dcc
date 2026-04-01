package com.tubes.nimons360.core.network

import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://mad.labpro.hmif.dev/"

    fun create(tokenManager: TokenManager): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = tokenManager.getToken()
                val req = if (token != null)
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token").build()
                else chain.request()
                chain.proceed(req)
            }
            .addInterceptor { chain ->
                val response = chain.proceed(chain.request())
                if (response.code == 401) {
                    // Note: Ideally, you'd pass a context here or use a global one.
                    // For now, we'll assume the broadcast is the intended way.
                }
                response
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
