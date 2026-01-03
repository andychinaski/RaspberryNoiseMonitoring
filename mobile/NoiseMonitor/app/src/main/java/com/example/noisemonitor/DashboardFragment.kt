package com.example.noisemonitor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.noisemonitor.databinding.FragmentDashboardBinding

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Заглушка для теста
        val apiResponse = mapOf(
            "current_noise" to 65,
            "event_type" to "NORMAL",
            "min_noise" to 50,
            "max_noise" to 80,
            "notifications_sent" to 3,
            "current_timestamp" to "2026-01-03 20:00"
        )

        // Заполняем данные через binding
        binding.currentNoise.text = "${apiResponse["current_noise"]} дБ"
        binding.eventType.text = apiResponse["event_type"].toString()
        binding.minMax.text = "Мин: ${apiResponse["min_noise"]} дБ | Макс: ${apiResponse["max_noise"]} дБ"
        binding.notificationsSent.text = "Отправлено уведомлений: ${apiResponse["notifications_sent"]}"
        binding.lastMeasurement.text = "Последний замер: ${apiResponse["current_timestamp"]}"

        // chartContainer оставляем пустым пока
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
