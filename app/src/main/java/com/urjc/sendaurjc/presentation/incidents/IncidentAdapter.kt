package com.urjc.sendaurjc.presentation.incidents

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.urjc.sendaurjc.databinding.ItemIncidentBinding
import com.urjc.sendaurjc.domain.model.Incident
import com.urjc.sendaurjc.domain.model.IncidentStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IncidentAdapter : ListAdapter<Incident, IncidentAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemIncidentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(incident: Incident) {
            binding.tvType.text = incident.type.name
            binding.tvDescription.text = incident.description
            binding.tvDate.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(Date(incident.timestamp))
            binding.tvStatus.text = incident.status.name
            binding.tvStatus.setTextColor(
                when (incident.status) {
                    IncidentStatus.ABIERTA -> Color.parseColor("#F44336")
                    IncidentStatus.EN_GESTION -> Color.parseColor("#FF9800")
                    IncidentStatus.RESUELTA -> Color.parseColor("#4CAF50")
                }
            )
            incident.ticketId?.let {
                binding.tvTicket.text = "Ticket: $it"
                binding.tvTicket.visibility = android.view.View.VISIBLE
            } ?: run { binding.tvTicket.visibility = android.view.View.GONE }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemIncidentBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    private class DiffCallback : DiffUtil.ItemCallback<Incident>() {
        override fun areItemsTheSame(old: Incident, new: Incident) = old.id == new.id
        override fun areContentsTheSame(old: Incident, new: Incident) = old == new
    }
}
