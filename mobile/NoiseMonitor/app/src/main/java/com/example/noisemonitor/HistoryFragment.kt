package com.example.noisemonitor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noisemonitor.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    // Тут пока заглушки, позже будет API
    private val dummyList = listOf(
        Measurement("03/01/2026 12:15", 65, "NORMAL"),
        Measurement("03/01/2026 12:20", 70, "WARNING"),
        Measurement("03/01/2026 12:25", 90, "CRITICAL")
    )

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
        binding.measurementsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.measurementsRecycler.adapter = HistoryAdapter(dummyList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
