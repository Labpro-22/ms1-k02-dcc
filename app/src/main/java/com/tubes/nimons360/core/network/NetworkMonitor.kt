package com.tubes.nimons360.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NetworkMonitor(context: Context) {
    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _isOnline = MutableLiveData<Boolean>()
    val isOnline: LiveData<Boolean> = _isOnline

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) { _isOnline.postValue(true) }
        override fun onLost(network: Network) { _isOnline.postValue(false) }
    }

    fun register() {
        val req = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
        cm.registerNetworkCallback(req, callback)
        // set initial value
        val active = cm.activeNetwork
        val caps = active?.let { cm.getNetworkCapabilities(it) }
        _isOnline.postValue(caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true)
    }

    fun unregister() = cm.unregisterNetworkCallback(callback)

    fun isCurrentlyOnline(): Boolean {
        val caps = cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }
        return caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    fun getInternetType(): String {
        val caps = cm.activeNetwork?.let { cm.getNetworkCapabilities(it) } ?: return "none"
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "mobile"
            else -> "none"
        }
    }
}
