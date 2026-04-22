package com.urjc.sendaurjc.presentation.volunteer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.urjc.sendaurjc.databinding.FragmentVolunteerBinding
import com.urjc.sendaurjc.domain.model.CompanionRequest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VolunteerFragment : Fragment() {

    private var _binding: FragmentVolunteerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VolunteerViewModel by viewModels()
    private lateinit var adapter: CompanionRequestAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVolunteerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeState()
    }

    private fun setupRecyclerView() {
        adapter = CompanionRequestAdapter(
            onAccept = { request -> viewModel.acceptRequest(request.id) },
            onReject = { request -> viewModel.rejectRequest(request.id) }
        )
        binding.rvRequests.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@VolunteerFragment.adapter
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                adapter.submitList(state.pendingRequests)
                binding.tvEmpty.visibility =
                    if (state.pendingRequests.isEmpty()) View.VISIBLE else View.GONE

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
