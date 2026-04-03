package com.tubes.nimons360

import android.app.Application
import com.tubes.nimons360.core.database.AppDatabase
import com.tubes.nimons360.core.network.ApiService
import com.tubes.nimons360.core.network.NetworkMonitor
import com.tubes.nimons360.core.network.RetrofitClient
import com.tubes.nimons360.core.network.TokenManager

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
