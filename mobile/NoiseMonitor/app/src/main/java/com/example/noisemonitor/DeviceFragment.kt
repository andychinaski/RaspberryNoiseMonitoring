package com.example.noisemonitor

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.noisemonitor.databinding.FragmentDeviceBinding
import kotlinx.coroutines.launch
import com.example.noisemonitor.network.RetrofitClient

class DeviceFragment : Fragment() {

    private var _binding: FragmentDeviceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadDeviceInfo()
    }

    private fun loadDeviceInfo() {
        // дефолт – офлайн
        setOfflineState()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val info = RetrofitClient.api.getDeviceInfo()

                binding.deviceName.text = info.deviceName
                binding.deviceUptime.text = "${info.uptime} сек"
                binding.deviceFrequency.text = "${info.measurementFrequency} сек"
                binding.deviceStatus.text = "Онлайн"

            } catch (e: Exception) {
                Log.e("DEVICE", "Ошибка получения device-info", e)
                setOfflineState()
            }
        }
    }

    private fun setOfflineState() {
        binding.deviceName.text = "--"
        binding.deviceUptime.text = "--"
        binding.deviceFrequency.text = "--"
        binding.deviceStatus.text = "Офлайн"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
