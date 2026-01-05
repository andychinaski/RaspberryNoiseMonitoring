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

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

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

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val events = RetrofitClient.api.getEvents()
                binding.measurementsRecycler.adapter = HistoryAdapter(events)
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
