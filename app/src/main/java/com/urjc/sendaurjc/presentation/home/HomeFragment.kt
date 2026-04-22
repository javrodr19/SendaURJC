package com.urjc.sendaurjc.presentation.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.urjc.sendaurjc.R
import com.urjc.sendaurjc.databinding.FragmentHomeBinding
import com.urjc.sendaurjc.domain.model.GeoPoint
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint as OsmGeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    private var locationOverlay: MyLocationNewOverlay? = null
    private var originMarker: Marker? = null
    private var destinationMarker: Marker? = null

    private val locationPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            enableLocation()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        Configuration.getInstance().userAgentValue = requireContext().packageName
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMap()
        setupButtons()
        observeState()
        requestLocationPermission()
    }

    private fun setupMap() {
        binding.map.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(17.0)
            // Default: URJC Móstoles campus
            controller.setCenter(OsmGeoPoint(40.3289, -3.8737))
        }
    }

    private fun setupButtons() {
        // RF29: select origin (tap on map) – simplified as button
        binding.btnSetOrigin.setOnClickListener {
            val center = binding.map.mapCenter
            val point = GeoPoint(center.latitude, center.longitude)
            viewModel.setOrigin(point)
            placeOriginMarker(point)
        }

        binding.btnSetDestination.setOnClickListener {
            val center = binding.map.mapCenter
            val point = GeoPoint(center.latitude, center.longitude)
            viewModel.setDestination(point)
            placeDestinationMarker(point)
        }

        binding.btnStartRoute.setOnClickListener {
            val state = viewModel.state.value
            if (state.origin != null && state.destination != null) {
                val bundle = Bundle().apply {
                    putParcelable("origin", state.origin)
                    putParcelable("destination", state.destination)
                    putBoolean("companionRequested", state.companionRequested)
                }
                findNavController().navigate(R.id.action_home_to_routeSelection, bundle)
            }
        }

        // RF: companion toggle
        binding.switchCompanion.setOnCheckedChangeListener { _, checked ->
            viewModel.setCompanionRequested(checked)
        }

        binding.btnProfile.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_profile)
        }

        binding.btnVolunteer.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_volunteer)
        }

        binding.btnIncidents.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_incidents)
        }

        // Scheduled departure picker
        binding.btnSchedule.setOnClickListener {
            showTimePicker()
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.tvWelcome.text = state.user?.let {
                    getString(R.string.welcome_user, it.name)
                } ?: getString(R.string.app_name)
                binding.btnStartRoute.isEnabled = state.origin != null && state.destination != null
            }
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            enableLocation()
        } else {
            locationPermission.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    private fun enableLocation() {
        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), binding.map).apply {
            enableMyLocation()
            enableFollowLocation()
            runOnFirstFix {
                requireActivity().runOnUiThread {
                    val loc = myLocation ?: return@runOnUiThread
                    viewModel.updateUserLocation(GeoPoint(loc.latitude, loc.longitude))
                    binding.map.controller.setCenter(OsmGeoPoint(loc.latitude, loc.longitude))
                }
            }
        }
        binding.map.overlays.add(locationOverlay)
    }

    private fun placeOriginMarker(point: GeoPoint) {
        originMarker?.let { binding.map.overlays.remove(it) }
        originMarker = Marker(binding.map).apply {
            position = OsmGeoPoint(point.latitude, point.longitude)
            title = getString(R.string.origin)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        binding.map.overlays.add(originMarker)
        binding.map.invalidate()
    }

    private fun placeDestinationMarker(point: GeoPoint) {
        destinationMarker?.let { binding.map.overlays.remove(it) }
        destinationMarker = Marker(binding.map).apply {
            position = OsmGeoPoint(point.latitude, point.longitude)
            title = getString(R.string.destination)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        binding.map.overlays.add(destinationMarker)
        binding.map.invalidate()
    }

    private fun showTimePicker() {
        val picker = android.app.TimePickerDialog(requireContext(), { _, hour, minute ->
            val cal = java.util.Calendar.getInstance().apply { set(java.util.Calendar.HOUR_OF_DAY, hour); set(java.util.Calendar.MINUTE, minute) }
            viewModel.setScheduledDeparture(cal.timeInMillis)
        }, 8, 0, true)
        picker.show()
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
