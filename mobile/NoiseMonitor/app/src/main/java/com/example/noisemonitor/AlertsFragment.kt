package com.example.noisemonitor

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.noisemonitor.databinding.FragmentAlertsBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.noisemonitor.network.RetrofitClient
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.core.content.ContextCompat
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AlertsFragment : Fragment() {

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    private var selectedDate: String = getTodayDate()
    private var onlySent: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("ALERTS", "AlertsFragment открыт")

        val layoutManager = LinearLayoutManager(requireContext())
        binding.alertsRecycler.layoutManager = layoutManager

        // Добавляем разделитель
        val dividerItemDecoration = DividerItemDecoration(
            binding.alertsRecycler.context,
            layoutManager.orientation
        )
        ContextCompat.getDrawable(requireContext(), R.drawable.divider_drawable)?.let {
            dividerItemDecoration.setDrawable(it)
        }
        binding.alertsRecycler.addItemDecoration(dividerItemDecoration)

        binding.filterSwitch.setOnCheckedChangeListener { _, isChecked ->
            onlySent = if (isChecked) 1 else 0
            loadAlerts()
        }

        binding.buttonToday.setOnClickListener {
            selectedDate = getTodayDate()
            loadAlerts()
        }

        binding.buttonYesterday.setOnClickListener {
            selectedDate = getYesterdayDate()
            loadAlerts()
        }

        binding.buttonPickDate.setOnClickListener {
            showDatePicker()
        }

        binding.refreshButton.setOnClickListener {
            loadAlerts()
        }

        loadAlerts()
    }

    private fun loadAlerts() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val alerts = RetrofitClient.getApi(requireContext()).getNotifications(
                    date = selectedDate,
                    onlySent = onlySent
                )
                binding.alertsRecycler.adapter = AlertsAdapter(alerts)
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

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.pick_date))
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setTheme(R.style.ThemeOverlay_NoiseMonitor_DatePicker)
            .build()

        picker.show(parentFragmentManager, "DATE_PICKER")

        picker.addOnPositiveButtonClickListener { millis ->
            selectedDate = formatDate(millis)
            loadAlerts()
        }
    }

    private fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(millis))
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
