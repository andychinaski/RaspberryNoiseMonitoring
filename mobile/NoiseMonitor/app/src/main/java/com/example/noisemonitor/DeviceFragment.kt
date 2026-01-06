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
import kotlinx.coroutines.delay
import androidx.appcompat.app.AlertDialog

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

        binding.rebootButton.setOnClickListener { 
            showRebootDialog()
        }

        binding.refreshButton.setOnClickListener {
            refreshDeviceInfo()
        }
    }

    private fun showRebootDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_reboot_progress, null)

        val dialog = AlertDialog.Builder(requireContext(), R.style.NoiseMonitor_AlertDialog)
            .setView(dialogView)
            .setCancelable(false) // Пользователь не может отменить диалог
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        viewLifecycleOwner.lifecycleScope.launch {
            delay(3000) // Задержка 3 секунды
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
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
                binding.rebootButton.isEnabled = true
                binding.disableSensorButton.isEnabled = true

            } catch (e: Exception) {
                Log.e("DEVICE", "Ошибка получения device-info", e)
                setOfflineState()
            }
        }
    }

    private fun refreshDeviceInfo() {
        showDeviceInfoLoading()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                delay(750)
                loadDeviceInfo()
            } catch (e: Exception) {
                setOfflineState()
            } finally {
                hideDeviceInfoLoading()
            }
        }
    }

    private fun setOfflineState() {
        binding.deviceName.text = "--"
        binding.deviceUptime.text = "--"
        binding.deviceFrequency.text = "--"
        binding.deviceStatus.text = "Офлайн"
        binding.rebootButton.isEnabled = false
        binding.disableSensorButton.isEnabled = false
    }

    private fun showDeviceInfoLoading() {
        binding.deviceInfoContent.visibility = View.INVISIBLE
        binding.deviceInfoProgress.visibility = View.VISIBLE
        binding.rebootButton.isEnabled = false
        binding.disableSensorButton.isEnabled = false
    }

    private fun hideDeviceInfoLoading() {
        binding.deviceInfoProgress.visibility = View.GONE
        binding.deviceInfoContent.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
