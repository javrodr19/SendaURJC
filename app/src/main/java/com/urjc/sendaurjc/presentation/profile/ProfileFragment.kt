package com.urjc.sendaurjc.presentation.profile

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
import com.urjc.sendaurjc.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()
        observeState()
    }

    private fun setupButtons() {
        binding.btnEdit.setOnClickListener {
            viewModel.setEditing(true)
        }

        binding.btnSave.setOnClickListener {
            viewModel.updateProfile(
                name = binding.etName.text.toString(),
                surname = binding.etSurname.text.toString(),
                isVolunteer = binding.switchVolunteer.isChecked
            )
        }

        binding.btnCancel.setOnClickListener {
            viewModel.setEditing(false)
        }

        // RF40: configure trusted contact
        binding.btnSaveTrustedContact.setOnClickListener {
            viewModel.setTrustedContact(
                name = binding.etContactName.text.toString(),
                phone = binding.etContactPhone.text.toString(),
                email = binding.etContactEmail.text.toString()
            )
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            findNavController().navigate(R.id.action_profile_to_login)
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                val user = state.user

                // Display data
                binding.tvEmail.text = user?.email ?: ""
                binding.tvRole.text = user?.role?.name ?: ""
                binding.tvStats.text = getString(
                    R.string.profile_stats,
                    user?.totalRoutes ?: 0,
                    user?.totalCompanions ?: 0
                )

                // Editing mode
                val editing = state.isEditing
                binding.groupEdit.visibility = if (editing) View.VISIBLE else View.GONE
                binding.groupDisplay.visibility = if (editing) View.GONE else View.VISIBLE

                if (editing && user != null) {
                    binding.etName.setText(user.name)
                    binding.etSurname.setText(user.surname)
                    binding.switchVolunteer.isChecked = user.isVolunteer
                }

                // Trusted contact
                state.trustedContact?.let { contact ->
                    binding.etContactName.setText(contact.contactName)
                    binding.etContactPhone.setText(contact.contactPhone)
                    binding.etContactEmail.setText(contact.contactEmail)
                }

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
