package com.tubes.nimons360.map

import android.app.Application
import androidx.lifecycle.*
import com.tubes.nimons360.core.database.FavoriteLocationEntity
import com.tubes.nimons360.core.network.NetworkMonitor
import com.tubes.nimons360.websocket.WebSocketManager
import com.tubes.nimons360.websocket.model.MemberPresencePayload
import com.tubes.nimons360.websocket.model.UpdatePresencePayload
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MapViewModel(
    application: Application,
    private val locationProvider: LocationProvider,
    private val orientationProvider: OrientationProvider,
    private val batteryProvider: BatteryProvider,
    private val webSocketManager: WebSocketManager,
    private val networkMonitor: NetworkMonitor,
    private val favoriteRepo: FavoriteLocationRepository
) : AndroidViewModel(application) {

    // LiveData untuk daftar anggota keluarga di peta
    private val _members = MutableLiveData<Map<Int, MemberPresencePayload>>(emptyMap())
    val members: LiveData<Map<Int, MemberPresencePayload>> = _members

    val myLocation = locationProvider.location
    val myAzimuth = orientationProvider.azimuth
    val favoriteLocations: LiveData<List<FavoriteLocationEntity>> = favoriteRepo.getAll()

    // Untuk melacak kapan terakhir kali member update (cleanup > 5 detik)
    private val lastUpdates = mutableMapOf<Int, Long>()

    init {
        // Terima data teman dari WebSocket
        webSocketManager.onPresenceUpdated = { payload ->
            val currentMap = _members.value?.toMutableMap() ?: mutableMapOf()
            currentMap[payload.userId] = payload
            _members.postValue(currentMap)
            lastUpdates[payload.userId] = System.currentTimeMillis()
        }

        // Loop untuk menghapus marker yang sudah mati / tidak update > 5 detik
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val now = System.currentTimeMillis()
                var hasChanges = false
                val currentMap = _members.value?.toMutableMap() ?: mutableMapOf()
                val iterator = currentMap.iterator()

                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    val lastUpdate = lastUpdates[entry.key] ?: 0
                    if (now - lastUpdate > 5000) { // Lebih dari 5 detik = buang
                        iterator.remove()
                        lastUpdates.remove(entry.key)
                        hasChanges = true
                    }
                }
                if (hasChanges) {
                    _members.postValue(currentMap)
                }
            }
        }
    }

    fun startTracking() {
        locationProvider.start()
        orientationProvider.start()
        webSocketManager.connect()

        // Broadcast lokasiku setiap 1 detik ke server
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                broadcastMyPresence()
            }
        }
    }

    private fun broadcastMyPresence() {
        val loc = myLocation.value ?: return
        val rot = myAzimuth.value ?: 0f
        val isConnected = networkMonitor.isOnline.value ?: false

        val payload = UpdatePresencePayload(
            name = "Nimons User", // Nanti bisa diganti dengan nama dari Profile
            latitude = loc.latitude,
            longitude = loc.longitude,
            rotation = rot,
            batteryLevel = batteryProvider.getBatteryLevel(),
            isCharging = batteryProvider.isCharging(),
            internetStatus = if (isConnected) "wifi" else "mobile"
        )
        webSocketManager.sendPresence(payload)
    }

    fun stopTracking() {
        locationProvider.stop()
        orientationProvider.stop()
        webSocketManager.disconnect()
    }

    fun saveCurrentLocationAsFavorite(label: String) {
        val loc = myLocation.value ?: return
        viewModelScope.launch {
            favoriteRepo.save(label, loc.latitude, loc.longitude)
        }
    }

    fun saveTappedLocationAsFavorite(label: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            favoriteRepo.save(label, lat, lon)
        }
    }

    fun deleteFavoriteLocation(id: Int) {
        viewModelScope.launch { favoriteRepo.delete(id) }
    }
}