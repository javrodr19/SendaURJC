package com.urjc.sendaurjc.presentation.route

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.urjc.sendaurjc.databinding.ItemRouteBinding
import com.urjc.sendaurjc.domain.model.Route

class RouteAdapter(
    private val onSelect: (Route) -> Unit
) : ListAdapter<Route, RouteAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemRouteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(route: Route) {
            val index = adapterPosition + 1
            binding.tvRouteTitle.text = if (route.isSafest)
                "Ruta más segura" else "Ruta alternativa $index"

            val securityPct = route.securityIndex.toInt()
            binding.tvSecurityIndex.text = "Seguridad: $securityPct%"
            binding.tvDuration.text = "~${route.estimatedMinutes} min"

            // Color coding: green >70, orange 40-70, red <40
            val color = when {
                securityPct >= 70 -> Color.parseColor("#4CAF50")
                securityPct >= 40 -> Color.parseColor("#FF9800")
                else -> Color.parseColor("#F44336")
            }
            binding.viewIndicator.setBackgroundColor(color)
            binding.tvSecurityIndex.setTextColor(color)

            binding.root.setOnClickListener { onSelect(route) }
            binding.btnSelect.setOnClickListener { onSelect(route) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemRouteBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    private class DiffCallback : DiffUtil.ItemCallback<Route>() {
        override fun areItemsTheSame(old: Route, new: Route) = old.id == new.id
        override fun areContentsTheSame(old: Route, new: Route) = old == new
    }
}
