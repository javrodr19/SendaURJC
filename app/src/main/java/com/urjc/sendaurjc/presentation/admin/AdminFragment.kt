package com.urjc.sendaurjc.presentation.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.urjc.sendaurjc.databinding.FragmentAdminBinding
import com.urjc.sendaurjc.domain.model.Incident
import com.urjc.sendaurjc.domain.model.IncidentStatus
import com.urjc.sendaurjc.presentation.incidents.IncidentAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminFragment : Fragment() {

    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeState()
    }

    private fun setupRecyclerView() {
        binding.rvIncidents.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                val adapter = AdminIncidentAdapter(
                    incidents = state.incidents,
                    onStatusChange = { incident ->
                        showStatusDialog(incident)
                    }
                )
                binding.rvIncidents.adapter = adapter

                binding.tvAlertCount.text = "Alertas sin procesar: ${state.unprocessedAlerts.size}"
                binding.tvTicketCount.text = "Tickets generados: ${state.tickets.size}"

                state.message?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    viewModel.clearMessage()
                }
            }
        }
    }

    private fun showStatusDialog(incident: Incident) {
        val statuses = IncidentStatus.values().map { it.name }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Cambiar estado")
            .setItems(statuses) { _, which ->
                viewModel.updateStatus(incident.id, IncidentStatus.values()[which])
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
