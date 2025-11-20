package com.example.noisemonitor.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.noisemonitor.R;

import java.util.Random;

public class DeviceFragment extends Fragment {

    private TextView deviceName, status, uptime, frequency, warning, critical;
    private ImageView statusIcon;
    private Button rebootButton, shutdownButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);

        Button updateButton = view.findViewById(R.id.button_update);
        updateButton.setOnClickListener(v -> fetchDeviceData());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fetch data every time the fragment is shown
        fetchDeviceData();
    }

    private void bindViews(View view) {
        deviceName = view.findViewById(R.id.value_device_name);
        status = view.findViewById(R.id.value_status);
        statusIcon = view.findViewById(R.id.icon_status);
        uptime = view.findViewById(R.id.value_uptime);
        frequency = view.findViewById(R.id.value_frequency);
        warning = view.findViewById(R.id.value_warning);
        critical = view.findViewById(R.id.value_critical);
        rebootButton = view.findViewById(R.id.button_reboot);
        shutdownButton = view.findViewById(R.id.button_shutdown_sensor);
    }

    private void fetchDeviceData() {
        // Show some loading state if you want
        setLoadingState();

        // --- API Call Simulation ---
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            boolean isApiSuccessful = new Random().nextBoolean(); // Randomly simulate success or failure

            if (isApiSuccessful) {
                // Simulate successful data retrieval
                updateUiWithSuccessData();
            } else {
                // Simulate API failure
                updateUiWithFailureState();
            }
        }, 1000); // 1-second delay to simulate network latency
    }

    private void setLoadingState() {
        String placeholder = getString(R.string.placeholder_value);
        deviceName.setText("Loading...");
        status.setText("Loading...");
        uptime.setText(placeholder);
        frequency.setText(placeholder);
        warning.setText(placeholder);
        critical.setText(placeholder);

        rebootButton.setEnabled(false);
        shutdownButton.setEnabled(false);
    }

    private void updateUiWithSuccessData() {
        deviceName.setText("Raspberry Pi Emulator");
        status.setText(getString(R.string.device_status_online));
        status.setTextColor(ContextCompat.getColor(requireContext(), R.color.success));
        statusIcon.setImageResource(R.drawable.ic_status_online);

        uptime.setText("03:12:55");
        frequency.setText("5 sec");
        warning.setText("60 dB");
        critical.setText("75 dB");

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
