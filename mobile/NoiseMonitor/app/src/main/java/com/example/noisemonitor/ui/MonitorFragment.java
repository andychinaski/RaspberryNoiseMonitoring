package com.example.noisemonitor.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.noisemonitor.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MonitorFragment extends Fragment {

    private TextView currentDb, currentStatus, lastUpdated, minToday, maxToday, notificationsToday;
    private ImageView currentStatusIndicator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_monitor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        loadDummyData();
    }

    private void bindViews(View view) {
        currentDb = view.findViewById(R.id.text_view_current_db);
        currentStatus = view.findViewById(R.id.text_view_current_status);
        currentStatusIndicator = view.findViewById(R.id.image_view_current_status_indicator);
        lastUpdated = view.findViewById(R.id.value_last_updated);
        minToday = view.findViewById(R.id.value_min_today);
        maxToday = view.findViewById(R.id.value_max_today);
        notificationsToday = view.findViewById(R.id.value_notifications);
    }

    private void loadDummyData() {
        // --- Dummy Data --- //
        int dbValue = 76;
        String status = "CRITICAL";

        currentDb.setText(String.format(Locale.getDefault(), "%d dB", dbValue));
        currentStatus.setText(status);

        // Set color and icon based on status
        switch (status) {
            case "CRITICAL":
                currentStatusIndicator.setImageResource(R.drawable.ic_status_offline);
                currentStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.error));
                break;
            case "WARNING":
                currentStatusIndicator.setImageResource(R.drawable.ic_status_warning);
                currentStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.warning));
                break;
            default: // NORMAL
                currentStatusIndicator.setImageResource(R.drawable.ic_status_normal);
                currentStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_secondary_text_new)); // Or any other neutral color
                break;
        }

        // Set summary info
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
        lastUpdated.setText(sdf.format(new Date()));
        minToday.setText("34 dB");
        maxToday.setText("98 dB");
        notificationsToday.setText("5");
    }
}
