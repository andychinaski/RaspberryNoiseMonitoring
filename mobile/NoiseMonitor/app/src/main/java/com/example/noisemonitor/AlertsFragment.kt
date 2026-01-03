package com.example.noisemonitor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.noisemonitor.databinding.FragmentAlertsBinding
import androidx.recyclerview.widget.LinearLayoutManager

class AlertsFragment : Fragment() {

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    private val dummyList = listOf(
        AlertEvent("03/01/2026 12:15", "DELIVERED"),
        AlertEvent("03/01/2026 12:20", "FAILED"),
        AlertEvent("03/01/2026 12:25", "PENDING")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.alertsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.alertsRecycler.adapter = AlertsAdapter(dummyList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}