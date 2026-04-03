package com.tubes.nimons360.map

import androidx.lifecycle.LiveData
import com.tubes.nimons360.core.database.FavoriteLocationDao
import com.tubes.nimons360.core.database.FavoriteLocationEntity

class FavoriteLocationRepository(private val dao: FavoriteLocationDao) {
    fun getAll(): LiveData<List<FavoriteLocationEntity>> = dao.getAll()

    suspend fun save(label: String, lat: Double, lon: Double) =
        dao.insert(FavoriteLocationEntity(label = label, latitude = lat, longitude = lon))

    suspend fun delete(id: Int) = dao.deleteById(id)
}
