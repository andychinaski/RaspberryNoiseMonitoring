package com.example.noisemonitor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noisemonitor.databinding.FragmentHistoryBinding
import com.example.noisemonitor.network.RetrofitClient
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private var selectedDate: String = getTodayDate()
    private var onlyCritical: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1️⃣ Настраиваем RecyclerView
        val layoutManager = LinearLayoutManager(requireContext())
        binding.measurementsRecycler.layoutManager = layoutManager
        
        // Добавляем разделитель
        val dividerItemDecoration = DividerItemDecoration(
            binding.measurementsRecycler.context,
            layoutManager.orientation
        )
        // Устанавливаем кастомный разделитель (drawable), который мы создали
        ContextCompat.getDrawable(requireContext(), R.drawable.divider_drawable)?.let {
            dividerItemDecoration.setDrawable(it)
        }
        
        binding.measurementsRecycler.addItemDecoration(dividerItemDecoration)

        binding.filterSwitch.setOnCheckedChangeListener { _, isChecked ->
            onlyCritical = if (isChecked) 1 else 0
            loadEvents()
        }

        binding.buttonToday.setOnClickListener {
            selectedDate = getTodayDate()
            loadEvents()
        }

        binding.buttonYesterday.setOnClickListener {
            selectedDate = getYesterdayDate()
            loadEvents()
        }

        binding.buttonPickDate.setOnClickListener {
            loadEvents()
        }

        binding.refreshButton.setOnClickListener {
            loadEvents()
        }

        loadEvents()
    }

    private fun loadEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val events = RetrofitClient.api.getEvents(
                    date = selectedDate,
                    onlyCritical = onlyCritical
                )
                binding.measurementsRecycler.adapter = HistoryAdapter(events)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getYesterdayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return sdf.format(cal.time)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
