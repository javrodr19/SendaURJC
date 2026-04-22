package com.urjc.sendaurjc.presentation.admin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.urjc.sendaurjc.databinding.ItemAdminIncidentBinding
import com.urjc.sendaurjc.domain.model.Incident
import com.urjc.sendaurjc.domain.model.IncidentStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminIncidentAdapter(
    private val incidents: List<Incident>,
    private val onStatusChange: (Incident) -> Unit
) : RecyclerView.Adapter<AdminIncidentAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemAdminIncidentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(incident: Incident) {
            binding.tvType.text = incident.type.name
            binding.tvDescription.text = incident.description
            binding.tvDate.text = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                .format(Date(incident.timestamp))
            binding.tvStatus.text = incident.status.name
            binding.tvStatus.setTextColor(
                when (incident.status) {
                    IncidentStatus.ABIERTA -> Color.parseColor("#F44336")
                    IncidentStatus.EN_GESTION -> Color.parseColor("#FF9800")
                    IncidentStatus.RESUELTA -> Color.parseColor("#4CAF50")
                }
            )
            incident.ticketId?.let { binding.tvTicketId.text = "Ticket: $it" }
            binding.btnChangeStatus.setOnClickListener { onStatusChange(incident) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemAdminIncidentBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(incidents[position])

    override fun getItemCount() = incidents.size
}
