package com.tubes.nimons360.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.tubes.nimons360.BuildConfig
import com.tubes.nimons360.R
import com.tubes.nimons360.core.database.AppDatabase
import com.tubes.nimons360.core.database.FavoriteLocationEntity
import com.tubes.nimons360.core.network.NetworkMonitor
import com.tubes.nimons360.core.network.TokenManager
import com.tubes.nimons360.databinding.FragmentMapBinding
import com.tubes.nimons360.websocket.WebSocketManager
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class MapFragment : Fragment() {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MapViewModel
    private val otherMarkers = mutableMapOf<Int, Marker>()
    private var myMarker: Marker? = null
    private val favoriteOverlays = mutableListOf<Marker>()

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) viewModel.startTracking()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Configuration.getInstance().apply {
            load(requireContext(), requireActivity().getPreferences(android.content.Context.MODE_PRIVATE))
            userAgentValue = BuildConfig.APPLICATION_ID
        }
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.setMultiTouchControls(true)
        binding.mapView.controller.setZoom(5.0)
        binding.mapView.controller.setCenter(GeoPoint(-2.5, 118.0))

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = AppDatabase.getInstance(requireContext())
                return MapViewModel(
                    requireActivity().application,
                    LocationProvider(requireContext()),
                    OrientationProvider(requireContext()),
                    BatteryProvider(requireContext()),
                    WebSocketManager(TokenManager(requireContext())),
                    NetworkMonitor(requireContext()),
                    FavoriteLocationRepository(db.favoriteLocationDao())
                ) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[MapViewModel::class.java]

        // 1. Observe lokasiku sendiri
        viewModel.myLocation.observe(viewLifecycleOwner) { loc ->
            loc?.let {
                val point = GeoPoint(it.latitude, it.longitude)
                if (myMarker == null) {
                    myMarker = Marker(binding.mapView)
                    myMarker?.title = "Lokasiku"
                    binding.mapView.overlays.add(myMarker)
                    binding.mapView.controller.setZoom(18.0)
                    binding.mapView.controller.setCenter(point)
                }
                myMarker?.position = point
                binding.mapView.invalidate()
            }
        }

        // 2. Observe rotasi kompas HP
        viewModel.myAzimuth.observe(viewLifecycleOwner) { az ->
            myMarker?.rotation = -az
            binding.mapView.invalidate()
        }

        // 3. Observe lokasi member keluarga lain
        viewModel.members.observe(viewLifecycleOwner) { members ->
            val staleIds = otherMarkers.keys - members.keys
            staleIds.forEach { id ->
                binding.mapView.overlays.remove(otherMarkers[id])
                otherMarkers.remove(id)
            }
            members.forEach { (id, payload) ->
                var marker = otherMarkers[id]
                if (marker == null) {
                    marker = Marker(binding.mapView)
                    binding.mapView.overlays.add(marker)
                    otherMarkers[id] = marker
                }
                marker.position = GeoPoint(payload.latitude, payload.longitude)
                marker.rotation = -payload.rotation
                marker.title = "${payload.fullName} (Bat: ${payload.batteryLevel}%)"
            }
            binding.mapView.invalidate()
        }

        // 4. Long-press pada peta → save favorite
        binding.mapView.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint) = false
            override fun longPressHelper(p: GeoPoint): Boolean {
                showSaveFavoriteDialog(p.latitude, p.longitude)
                return true
            }
        }))

        // 5. Observe favorite locations → render star markers
        viewModel.favoriteLocations.observe(viewLifecycleOwner) { favorites ->
            favoriteOverlays.forEach { binding.mapView.overlays.remove(it) }
            favoriteOverlays.clear()
            favorites.forEach { fav ->
                val marker = Marker(binding.mapView).apply {
                    position = GeoPoint(fav.latitude, fav.longitude)
                    title = fav.label
                    icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_star_marker)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { _, _ -> showFavoriteInfoDialog(fav); true }
                }
                favoriteOverlays.add(marker)
                binding.mapView.overlays.add(marker)
            }
            binding.mapView.invalidate()
        }

        val hasLocation = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasLocation) {
            viewModel.startTracking()
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun showSaveFavoriteDialog(lat: Double, lon: Double) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_save_location, null)
        val etLabel = dialogView.findViewById<TextInputEditText>(R.id.etLocationLabel)
        val tvCoords = dialogView.findViewById<TextView>(R.id.tvCoordinates)
        tvCoords.text = "%.6f, %.6f".format(lat, lon)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnSaveFav).setOnClickListener {
            val label = etLabel.text.toString().trim()
            if (label.isEmpty()) {
                etLabel.error = "Label is required"
                return@setOnClickListener
            }
            viewModel.saveTappedLocationAsFavorite(label, lat, lon)
            dialog.dismiss()
            Toast.makeText(requireContext(), "Saved: $label", Toast.LENGTH_SHORT).show()
        }
        dialogView.findViewById<Button>(R.id.btnCancelFav).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showFavoriteInfoDialog(fav: FavoriteLocationEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(fav.label)
            .setMessage("%.6f, %.6f".format(fav.latitude, fav.longitude))
            .setPositiveButton("Close", null)
            .setNegativeButton("Delete") { _, _ ->
                viewModel.deleteFavoriteLocation(fav.id)
                Toast.makeText(requireContext(), "Removed: ${fav.label}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopTracking()
        _binding = null
    }
}
