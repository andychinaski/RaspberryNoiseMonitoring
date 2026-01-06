package com.example.noisemonitor

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.noisemonitor.databinding.FragmentDeviceBinding
import kotlinx.coroutines.launch
import com.example.noisemonitor.network.RetrofitClient
import kotlinx.coroutines.delay
import androidx.appcompat.app.AlertDialog
import com.example.noisemonitor.network.NetworkConfig

class DeviceFragment : Fragment() {

    private var _binding: FragmentDeviceBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: SharedPreferences

    private val updatePeriods = listOf("none", "5 сек", "10 сек", "20 сек", "30 сек")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceBinding.inflate(inflater, container, false)

        binding.root.setOnTouchListener { _, _ ->
            binding.serverIpEditText.clearFocus()
            hideKeyboard()
            false
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        binding.serverIpEditText.setText(
            NetworkConfig.getServerIp(requireContext())
        )
        setupAutoUpdateSpinner()
        loadDeviceInfo()

        binding.rebootButton.setOnClickListener { 
            showRebootDialog()
        }

        binding.refreshButton.setOnClickListener {
            refreshDeviceInfo()
        }

        binding.serverIpEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.serverIpEditText.inputType = InputType.TYPE_CLASS_TEXT
            } else {
                binding.serverIpEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.serverIpEditText.typeface = Typeface.create("@font/montserrat_regular", Typeface.NORMAL)
            }
            binding.serverIpEditText.setSelection(
                binding.serverIpEditText.text?.length ?: 0
            )
        }

        binding.saveButton.setOnClickListener{
            val selectedPeriod = binding.autoUpdateSpinner.selectedItem as String
            prefs.edit().putString("auto_update_period", selectedPeriod).apply()

            val ip = binding.serverIpEditText.text.toString().trim()

            if (ip.isEmpty()) {
                Toast.makeText(requireContext(), "IP не может быть пустым", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            NetworkConfig.saveServerIp(requireContext(), ip)

            RetrofitClient.rebuild(requireContext())

            Toast.makeText(requireContext(), "Настройки сохранены", Toast.LENGTH_SHORT).show()
        }

        binding.discardButton.setOnClickListener{
            val savedPeriod = prefs.getString("auto_update_period", "none")

            binding.serverIpEditText.setText(
                NetworkConfig.getServerIp(requireContext())
            )

            binding.autoUpdateSpinner.setSelection(updatePeriods.indexOf(savedPeriod))
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
        rebootDevice()
        viewLifecycleOwner.lifecycleScope.launch {
            delay(3000) // Задержка 3 секунды
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            loadDeviceInfo()
        }
    }

    private fun loadDeviceInfo() {
        // дефолт – офлайн
        setOfflineState()

        viewLifecycleOwner.lifecycleScope.launch { 
            try {
                val info = RetrofitClient.getApi(requireContext()).getDeviceInfo()

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

    private fun rebootDevice(){
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val info = RetrofitClient.getApi(requireContext()).getRebootDevice()
            } catch (e: Exception) {
                Log.e("DEVICE", "Ошибка перезагрузки", e)
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

    private fun setupAutoUpdateSpinner() {
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, updatePeriods)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        binding.autoUpdateSpinner.adapter = adapter

        val savedPeriod = prefs.getString("auto_update_period", "none")
        binding.autoUpdateSpinner.setSelection(updatePeriods.indexOf(savedPeriod))
    }

    private fun hideKeyboard() {
        val imm = requireActivity()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        imm.hideSoftInputFromWindow(
            requireActivity().window.decorView.windowToken,
            0
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
