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
import androidx.recyclerview.widget.LinearLayoutManager
import com.urjc.sendaurjc.R
import com.urjc.sendaurjc.databinding.FragmentRouteSelectionBinding
import com.urjc.sendaurjc.domain.model.GeoPoint
import com.urjc.sendaurjc.domain.model.Route
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RouteSelectionFragment : Fragment() {

    private var _binding: FragmentRouteSelectionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RouteViewModel by viewModels()
    private lateinit var adapter: RouteAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRouteSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeState()

        val origin = arguments?.getParcelable<GeoPoint>("origin")
        val destination = arguments?.getParcelable<GeoPoint>("destination")

        if (origin != null && destination != null) {
            viewModel.calculateRoutes(origin, destination)
        } else {
            Toast.makeText(requireContext(), "Selecciona origen y destino", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        adapter = RouteAdapter { route ->
            viewModel.selectRoute(route)
        }
        binding.rvRoutes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RouteSelectionFragment.adapter
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is RouteUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.rvRoutes.visibility = View.GONE
                    }
                    is RouteUiState.RoutesReady -> {
                        binding.progressBar.visibility = View.GONE
                        binding.rvRoutes.visibility = View.VISIBLE
                        adapter.submitList(state.routes)
                    }
                    is RouteUiState.Navigating -> {
                        val bundle = Bundle().apply {
                            putParcelable("route", state.route)
                        }
                        findNavController().navigate(R.id.action_routeSelection_to_navigation, bundle)
                    }
                    is RouteUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
