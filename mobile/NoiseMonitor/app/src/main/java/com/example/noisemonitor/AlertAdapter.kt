package com.example.noisemonitor

import android.view.LayoutInflater
import android.view.ViewGroup
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import com.example.noisemonitor.databinding.ItemAlertEventBinding

class AlertsAdapter(
    private val items: List<AlertEvent>
) : RecyclerView.Adapter<AlertsAdapter.AlertViewHolder>() {

    inner class AlertViewHolder(
        val binding: ItemAlertEventBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AlertViewHolder {

        val binding = ItemAlertEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: AlertViewHolder,
        position: Int
    ) {
        val item = items[position]

        holder.binding.textTime.text = item.sentAt
        holder.binding.textMessage.text = item.message
        holder.binding.textStatus.text = item.status.uppercase()

        val color = when (item.status.lowercase()) {
            "sent", "delivered" -> Color.GREEN
            "failed" -> Color.RED
            "pending" -> Color.YELLOW
            else -> Color.GRAY
        }

        holder.binding.textStatus.setTextColor(color)
    }

    override fun getItemCount(): Int = items.size
}
