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


class AlertsFragment : Fragment() {

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

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

        binding.alertsRecycler.layoutManager =
            LinearLayoutManager(requireContext())

        loadAlerts()
    }

    private fun loadAlerts() {
        Log.d("ALERTS", "Запрашиваем уведомления...")

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val alerts = RetrofitClient.api.getNotifications()
                binding.alertsRecycler.adapter = AlertsAdapter(alerts)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}