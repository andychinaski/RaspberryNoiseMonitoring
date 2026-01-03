package com.example.noisemonitor

import android.view.LayoutInflater
import android.view.ViewGroup
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import com.example.noisemonitor.databinding.ItemAlertEventBinding

class AlertsAdapter(private val items: List<AlertEvent>) : RecyclerView.Adapter<AlertsAdapter.AlertsViewHolder>() {

    inner class AlertsViewHolder(val binding: ItemAlertEventBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertsViewHolder {
        val binding = ItemAlertEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlertsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertsViewHolder, position: Int) {
        val item = items[position]
        holder.binding.textViewTime.text = item.time
        holder.binding.textViewStatus.text = item.status

        val color = when(item.status) {
            "DELIVERED" -> Color.GREEN
            "FAILED" -> Color.RED
            "PENDING" -> Color.YELLOW
            else -> Color.GRAY
        }
        holder.binding.textViewStatus.setTextColor(color)
    }

    override fun getItemCount(): Int = items.size
}
