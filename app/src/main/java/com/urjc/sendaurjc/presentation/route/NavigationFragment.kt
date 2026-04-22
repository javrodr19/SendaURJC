package com.urjc.sendaurjc.presentation.route

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.urjc.sendaurjc.R
import com.urjc.sendaurjc.databinding.FragmentNavigationBinding
import com.urjc.sendaurjc.domain.model.GeoPoint
import com.urjc.sendaurjc.domain.model.IncidentType
import com.urjc.sendaurjc.domain.model.Route
import com.urjc.sendaurjc.presentation.incidents.IncidentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint as OsmGeoPoint
import org.osmdroid.views.overlay.Polyline

@AndroidEntryPoint
class NavigationFragment : Fragment() {

    private var _binding: FragmentNavigationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RouteViewModel by viewModels()
    private val incidentViewModel: IncidentViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNavigationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val route = arguments?.getParcelable<Route>("route")
        if (route != null) {
            viewModel.selectRoute(route)
            drawRouteOnMap(route)
        }

        setupButtons()
        observeState()
    }

    private fun setupButtons() {
        // RF: danger button accessible in correct positions (large, easy to tap under stress)
        binding.btnDanger.setOnClickListener {
            val state = viewModel.state.value as? RouteUiState.Navigating ?: return@setOnClickListener
            val loc = state.route.origin  // in production: use live GPS location
            incidentViewModel.reportIncident(IncidentType.OTRO, "ALERTA DE PELIGRO", loc)
        }

        binding.btnNextStep.setOnClickListener {
            viewModel.advanceStep()
        }

        binding.btnCancel.setOnClickListener {
            viewModel.cancelNavigation()
            findNavController().navigateUp()
        }

        // RF: dropdown menu for incidents (light, traffic, obstacles)
        binding.btnReportIncident.setOnClickListener {
            showIncidentMenu()
        }

        binding.btnRecalculate.setOnClickListener {
            viewModel.recalculateActiveRoute()
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is RouteUiState.Navigating -> {
                        val step = state.instructions.getOrNull(state.currentStep)
                        binding.tvInstruction.text = step?.text ?: getString(R.string.destination_reached)
                        binding.tvProgress.text = "${state.currentStep + 1}/${state.instructions.size}"
                        binding.tvSecurity.text =
                            getString(R.string.security_index, state.route.securityIndex.toInt())
                    }
                    is RouteUiState.Arrived -> {
                        binding.tvInstruction.text = getString(R.string.destination_reached)
                        showArrivalDialog()
                    }
                    is RouteUiState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun drawRouteOnMap(route: Route) {
        val polyline = Polyline().apply {
            outlinePaint.color = android.graphics.Color.parseColor("#1565C0")
            outlinePaint.strokeWidth = 8f
            setPoints(route.segments.flatMap { seg ->
                listOf(
                    OsmGeoPoint(seg.start.latitude, seg.start.longitude),
                    OsmGeoPoint(seg.end.latitude, seg.end.longitude)
                )
            })
        }
        binding.map.overlays.add(polyline)
        binding.map.controller.setCenter(
            OsmGeoPoint(route.origin.latitude, route.origin.longitude)
        )
        binding.map.invalidate()
    }

    private fun showIncidentMenu() {
        val types = arrayOf("Problema de iluminación", "Alta afluencia", "Obstáculo", "Otro")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Reportar incidencia")
            .setItems(types) { _, which ->
                val type = IncidentType.values()[which]
                val state = viewModel.state.value as? RouteUiState.Navigating ?: return@setItems
                incidentViewModel.reportIncident(type, types[which], state.route.origin)
                Toast.makeText(requireContext(), "Incidencia reportada", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showArrivalDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.arrived_title))
            .setMessage(getString(R.string.arrived_question))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                findNavController().navigate(R.id.action_navigation_to_home)
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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
