class NimonsApplication : Application() {
    lateinit var tokenManager: TokenManager
    lateinit var apiService: ApiService
    lateinit var networkMonitor: NetworkMonitor
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        tokenManager = TokenManager(this)
        apiService = RetrofitClient.create(tokenManager)
        networkMonitor = NetworkMonitor(this)
        networkMonitor.register()
        database = AppDatabase.getInstance(this)
    }
}