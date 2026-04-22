package com.urjc.sendaurjc.presentation.volunteer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.urjc.sendaurjc.databinding.ItemCompanionRequestBinding
import com.urjc.sendaurjc.domain.model.CompanionRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CompanionRequestAdapter(
    private val onAccept: (CompanionRequest) -> Unit,
    private val onReject: (CompanionRequest) -> Unit
) : ListAdapter<CompanionRequest, CompanionRequestAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemCompanionRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(request: CompanionRequest) {
            binding.tvRequesterName.text = request.requesterName
            val fmt = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
            binding.tvScheduledTime.text = fmt.format(Date(request.scheduledTime))
            binding.tvOrigin.text = "Origen: ${String.format("%.4f", request.origin.latitude)}, " +
                    "${String.format("%.4f", request.origin.longitude)}"
            binding.tvDestination.text = "Destino: ${String.format("%.4f", request.destination.latitude)}, " +
                    "${String.format("%.4f", request.destination.longitude)}"
            binding.btnAccept.setOnClickListener { onAccept(request) }
            binding.btnReject.setOnClickListener { onReject(request) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemCompanionRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    private class DiffCallback : DiffUtil.ItemCallback<CompanionRequest>() {
        override fun areItemsTheSame(old: CompanionRequest, new: CompanionRequest) = old.id == new.id
        override fun areContentsTheSame(old: CompanionRequest, new: CompanionRequest) = old == new
    }
}
