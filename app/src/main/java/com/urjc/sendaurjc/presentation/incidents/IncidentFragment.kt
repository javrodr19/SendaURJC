package com.urjc.sendaurjc.presentation.incidents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.urjc.sendaurjc.R
import com.urjc.sendaurjc.databinding.FragmentIncidentBinding
import com.urjc.sendaurjc.domain.model.GeoPoint
import com.urjc.sendaurjc.domain.model.IncidentType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class IncidentFragment : Fragment() {

    private var _binding: FragmentIncidentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: IncidentViewModel by viewModels()
    private lateinit var adapter: IncidentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentIncidentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSpinner()
        setupRecyclerView()
        setupButtons()
        observeState()
    }

    private fun setupSpinner() {
        val types = resources.getStringArray(R.array.incident_types)
        binding.spinnerType.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            types
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

    private fun setupRecyclerView() {
        adapter = IncidentAdapter()
        binding.rvIncidents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@IncidentFragment.adapter
        }
    }

    private fun setupButtons() {
        // RF42: report incident
        binding.btnReport.setOnClickListener {
            val description = binding.etDescription.text.toString()
            val typeIndex = binding.spinnerType.selectedItemPosition
            val type = IncidentType.values()[typeIndex]
            // Use a default campus location; in production use live GPS
            val location = GeoPoint(40.3289, -3.8737)
            viewModel.reportIncident(type, description, location)
            binding.etDescription.text.clear()
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                adapter.submitList(state.incidents)
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                state.message?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    viewModel.clearMessage()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
