package com.example.noisemonitor

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import com.example.noisemonitor.databinding.ItemAlertEventBinding
import java.text.SimpleDateFormat
import java.util.Locale

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
        val context = holder.itemView.context

        holder.binding.textTime.text = formatTimestamp(item.sentAt)
        holder.binding.textMessage.text = item.message

        val colorRes = when(item.status.lowercase()) {
            "sent", "delivered" -> R.color.primary
            "pending" -> R.color.warning
            "failed" -> R.color.error
            else -> R.color.text_secondary
        }

        // Используем backgroundTintList, так как в xml это View с background
        holder.binding.statusIndicator.backgroundTintList =
            ColorStateList.valueOf(context.getColor(colorRes))
    }

    private fun formatTimestamp(timestamp: String): String {
        return try {
            // Предполагаем, что сервер присылает "yyyy-MM-dd HH:mm:ss"
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.getDefault())
            val date = inputFormat.parse(timestamp)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            timestamp // Если не удалось распарсить, возвращаем как есть
        }
    }

    override fun getItemCount(): Int = items.size
}
