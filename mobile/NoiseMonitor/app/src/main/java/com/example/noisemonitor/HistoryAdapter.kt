package com.example.noisemonitor

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.noisemonitor.databinding.ItemHistoryEventBinding
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryAdapter(private val items: List<HistoryEvent>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(val binding: ItemHistoryEventBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryEventBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.binding.textViewTime.text = formatTimestamp(item.timestamp)
        holder.binding.textViewDbLevel.text = "${item.noiseLevel} dB"
        holder.binding.textViewStatus.text = item.type

        val colorRes = when(item.type) {
            "NORMAL" -> R.color.primary
            "WARNING" -> R.color.warning
            "CRITICAL" -> R.color.error
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
