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
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadNoiseStats()
    }

    private fun loadNoiseStats(date: String = getCurrentDate() ) {
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
                setupNoiseChart(stats.last10minutes)

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

    private fun setupNoiseChart(last10minutes: List<NoiseEvent>) {
        if (last10minutes.isEmpty()) {
            binding.noiseChart.clear()
            return
        }

        // Создаём список Entry: x - время в секундах/минутах (для простоты используем индекс), y - уровень шума
        val entries = last10minutes.mapIndexed { index, event ->
            Entry(index.toFloat(), event.noiseLevel.toFloat())
        }

        // Настраиваем DataSet
        val dataSet = LineDataSet(entries, "Уровень шума (дБ)")
        dataSet.color = android.graphics.Color.BLUE
        dataSet.valueTextColor = android.graphics.Color.BLACK
        dataSet.lineWidth = 2f
        dataSet.setDrawValues(false) // Не показываем значения на точках для чистоты
        dataSet.setDrawCircles(true)
        dataSet.circleRadius = 3f
        dataSet.circleColors = listOf(android.graphics.Color.BLUE)

        // Создаём LineData
        val lineData = LineData(dataSet)

        // Настраиваем график
        val chart = binding.noiseChart
        chart.data = lineData

        // Настройка осей
        chart.description.isEnabled = false // Отключаем описание
        chart.setTouchEnabled(true) // Включаем зум/скролл
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)

        // Ось X: метки по времени (если timestamps в формате "yyyy-MM-dd HH:mm:ss")
        val xAxis = chart.xAxis
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                if (index in last10minutes.indices) {
                    val timestamp = last10minutes[index].timestamp
                    // Парсим время и показываем только HH:mm
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val date = sdf.parse(timestamp)
                    return if (date != null) {
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
                    } else {
                        ""
                    }
                }
                return ""
            }
        }
        xAxis.granularity = 1f // Шаг по оси X
        xAxis.labelCount = last10minutes.size.coerceAtMost(10) // Не больше 10 меток

        // Ось Y: уровень шума
        val leftAxis = chart.axisLeft
        leftAxis.axisMinimum = 0f // Минимальный шум 0 дБ
        leftAxis.axisMaximum = (last10minutes.maxOf { it.noiseLevel } * 1.1f).toFloat() // Немного выше максимума

        chart.axisRight.isEnabled = false // Отключаем правую ось

        // Анимация и обновление
        chart.animateX(1000)
        chart.invalidate()
    }

    private fun getCurrentDate(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
