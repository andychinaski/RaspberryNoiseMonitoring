package com.example.noisemonitor

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.noisemonitor.databinding.ItemHistoryEventBinding

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

        holder.binding.textViewTime.text = item.timestamp
        holder.binding.textViewDbLevel.text = "${item.noiseLevel} dB"
        holder.binding.textViewStatus.text = item.type

        val color = when(item.type) {
            "NORMAL" -> Color.GREEN
            "WARNING" -> Color.YELLOW
            "CRITICAL" -> Color.RED
            else -> Color.GRAY
        }
        holder.binding.textViewStatus.setTextColor(color)
    }

    override fun getItemCount(): Int = items.size
}

