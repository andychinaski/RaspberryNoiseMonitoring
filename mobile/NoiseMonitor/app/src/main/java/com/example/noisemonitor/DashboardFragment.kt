package com.example.noisemonitor

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
        Log.d("DASHBOARD", "Запрос noise stats, date=$date")
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val stats = RetrofitClient.api.getNoiseStats(date)

                // Заполняем поля UI
                binding.currentNoise.text = stats.currentNoise.toString()
                binding.maxNoise.text = stats.maxNoise.toString()
                binding.minNoise.text = stats.minNoise.toString()
                binding.eventType.text = stats.eventType
                binding.lastMeasurement.text = stats.currentTimestamp

                // Отрисовываем график
                binding.noiseChart.setData(stats.last10minutes)

                Log.d(
                    "DASHBOARD",
                    "Ответ: current=${stats.currentNoise}, ts=${stats.currentTimestamp}"
                )

            } catch (e: Exception) {
                e.printStackTrace()
                // Если ошибка, ставим прочерки и статус offline
                binding.currentNoise.text = "--"
                binding.maxNoise.text = "--"
                binding.minNoise.text = "--"
                binding.eventType.text = "--"
                binding.lastMeasurement.text = "--"
                // Очищаем график при ошибке
                binding.noiseChart.clear()
            }

        }
    }

    private fun getCurrentDate(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
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
