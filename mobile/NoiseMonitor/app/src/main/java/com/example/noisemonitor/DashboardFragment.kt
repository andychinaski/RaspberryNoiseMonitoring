package com.example.noisemonitor

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.noisemonitor.databinding.FragmentDashboardBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.noisemonitor.network.RetrofitClient
import java.text.SimpleDateFormat
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun loadNoiseStats(date: String = getCurrentDate() ) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val stats = RetrofitClient.getApi(requireContext()).getNoiseStats(date)

                // Заполняем поля UI
                binding.currentNoise.text = "${stats.currentNoise.toString()} дБ"
                binding.maxNoise.text = "${stats.maxNoise.toString()} дБ"
                binding.minNoise.text = "${stats.minNoise.toString()} дБ"
                binding.eventType.text = stats.eventType
                binding.notificationsSent.text = stats.notificationsSent.toString()
                
                // Форматируем дату
                binding.lastMeasurement.text = formatTimestamp(stats.currentTimestamp)
                
                applyEventType(stats.eventType)

                // Отрисовываем график
                binding.noiseChart.setData(stats.last10minutes)

            } catch (e: Exception) {
                e.printStackTrace()
                // Если ошибка, ставим прочерки и статус offline
                binding.currentNoise.text = getString(R.string.no_value)
                binding.maxNoise.text = getString(R.string.no_value)
                binding.minNoise.text = getString(R.string.no_value)
                binding.eventType.text = getString(R.string.no_connect)
                binding.lastMeasurement.text = getString(R.string.no_value)
                binding.notificationsSent.text = getString(R.string.no_value)
                applyEventType()
                // Очищаем график при ошибке
                binding.noiseChart.clear()
            }

        }
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

    private fun applyEventType(type: String = "") {
        val color = when (type) {
            "NORMAL" -> R.color.primary
            "WARNING" -> R.color.warning
            "CRITICAL" -> R.color.error
            else -> R.color.text_secondary
        }

        binding.eventIndicator.imageTintList =
            ColorStateList.valueOf(requireContext().getColor(color))
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    override fun onResume() {
        super.onResume()
        loadNoiseStats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
