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
                    val intent = Intent("com.tubes.nimons360.ACTION_TOKEN_EXPIRED")
                    // broadcast; MainActivity handles navigation to login
                    LocalBroadcastManager.getInstance(appContext).sendBroadcast(intent)
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