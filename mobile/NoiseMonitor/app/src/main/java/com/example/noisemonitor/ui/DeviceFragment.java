package com.example.noisemonitor.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.noisemonitor.R;
import com.example.noisemonitor.api.ApiService;
import com.example.noisemonitor.api.Device;

public class DeviceFragment extends Fragment {

    private TextView deviceName, status, uptime, frequency, warning, critical;
    private ImageView statusIcon;
    private Button updateButton, rebootButton, shutdownButton;
    private ProgressBar progressBar;
    private Group contentGroup;

    private ApiService apiService;

    public DeviceFragment() {
        super(R.layout.fragment_device);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        apiService = ApiService.getInstance(requireContext());

        updateButton.setOnClickListener(v -> fetchDeviceData());
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchDeviceData();
    }

    private void bindViews(@NonNull View view) {
        deviceName = view.findViewById(R.id.value_device_name);
        status = view.findViewById(R.id.value_status);
        statusIcon = view.findViewById(R.id.icon_status);
        uptime = view.findViewById(R.id.value_uptime);
        frequency = view.findViewById(R.id.value_frequency);
        warning = view.findViewById(R.id.value_warning);
        critical = view.findViewById(R.id.value_critical);
        updateButton = view.findViewById(R.id.button_update);
        rebootButton = view.findViewById(R.id.button_reboot);
        shutdownButton = view.findViewById(R.id.button_shutdown_sensor);
        progressBar = view.findViewById(R.id.progress_bar);
        contentGroup = view.findViewById(R.id.content_group);
    }

    private void fetchDeviceData() {
        setLoadingState(true);

        apiService.getDeviceData(new ApiService.ApiCallback<Device>() {
            @Override
            public void onSuccess(Device result) {
                if (!isAdded()) return; 
                
                requireActivity().runOnUiThread(() -> {
                    setLoadingState(false);
                    updateUiWithSuccessData(result);
                });
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                
                requireActivity().runOnUiThread(() -> {
                    setLoadingState(false);
                    updateUiWithFailureState();
                    Toast.makeText(getContext(), "API Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setLoadingState(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        contentGroup.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        
        updateButton.setEnabled(!isLoading);
        rebootButton.setEnabled(!isLoading);
        shutdownButton.setEnabled(!isLoading);
    }

    private void updateUiWithSuccessData(Device device) {
        deviceName.setText(device.getName());
        status.setText(getString(R.string.device_status_online));
        status.setTextColor(ContextCompat.getColor(requireContext(), R.color.success));
        statusIcon.setImageResource(R.drawable.ic_status_online);

        uptime.setText(device.getUptime());
        frequency.setText(String.format("%d sec", device.getMeasurementFrequency()));
        warning.setText(String.format("%d dB", device.getWarningThreshold()));
        critical.setText(String.format("%d dB", device.getCriticalThreshold()));

        rebootButton.setEnabled(true);
        shutdownButton.setEnabled(true);
    }

    private void updateUiWithFailureState() {
        deviceName.setText(getString(R.string.undefined));
        status.setText(getString(R.string.device_status_offline));
        status.setTextColor(ContextCompat.getColor(requireContext(), R.color.error));
        statusIcon.setImageResource(R.drawable.ic_status_offline);

        String placeholder = getString(R.string.placeholder_value);
        uptime.setText(placeholder);
        frequency.setText(placeholder);
        warning.setText(placeholder);
        critical.setText(placeholder);

        rebootButton.setEnabled(false);
        shutdownButton.setEnabled(false);
    }
}
