package com.example.noisemonitor.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
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
import com.example.noisemonitor.api.NoiseStats;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MonitorFragment extends Fragment {

    private static final String PREFS_NAME = "NoiseMonitorPrefs";
    private static final String KEY_AUTO_REFRESH_POSITION = "auto_refresh_position";

    private TextView currentDb, currentStatus, lastUpdated, minToday, maxToday, notificationsToday;
    private ImageView currentStatusIndicator;
    private ProgressBar progressBar;
    private Group contentGroup;

    private ApiService apiService;
    private final Handler autoRefreshHandler = new Handler(Looper.getMainLooper());
    private Runnable autoRefreshRunnable;
    private int autoRefreshInterval = 0; // in seconds

    public MonitorFragment() {
        super(R.layout.fragment_monitor);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiService.getInstance(requireContext());
        bindViews(view);
        loadSettings();
    }

    @Override
    public void onResume() {
        super.onResume();
        startAutoRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoRefresh();
    }

    private void bindViews(@NonNull View view) {
        currentDb = view.findViewById(R.id.text_view_current_db);
        currentStatus = view.findViewById(R.id.text_view_current_status);
        currentStatusIndicator = view.findViewById(R.id.image_view_current_status_indicator);
        lastUpdated = view.findViewById(R.id.value_last_updated);
        minToday = view.findViewById(R.id.value_min_today);
        maxToday = view.findViewById(R.id.value_max_today);
        notificationsToday = view.findViewById(R.id.value_notifications);
        progressBar = view.findViewById(R.id.progress_bar_monitor);
        contentGroup = view.findViewById(R.id.content_group_monitor);
    }

    private void loadSettings() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int position = prefs.getInt(KEY_AUTO_REFRESH_POSITION, 1); // Default to "5 sec"
        switch (position) {
            case 1: autoRefreshInterval = 5; break;
            case 2: autoRefreshInterval = 10; break;
            case 3: autoRefreshInterval = 20; break;
            case 4: autoRefreshInterval = 30; break;
            default: autoRefreshInterval = 0; // none
        }
    }

    private void startAutoRefresh() {
        if (autoRefreshInterval > 0) {
            autoRefreshRunnable = () -> {
                fetchNoiseStats();
                autoRefreshHandler.postDelayed(autoRefreshRunnable, autoRefreshInterval * 1000L);
            };
            autoRefreshHandler.post(autoRefreshRunnable);
        } else {
            // If auto-refresh is off, fetch data once manually.
            fetchNoiseStats();
        }
    }

    private void stopAutoRefresh() {
        if (autoRefreshRunnable != null) {
            autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
        }
    }

    private void fetchNoiseStats() {
        setLoading(true);
        String dateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());

        apiService.getNoiseStats(dateString, new ApiService.ApiCallback<NoiseStats>() {
            @Override
            public void onSuccess(NoiseStats result) {
                if (!isAdded()) return; // Pre-check
                requireActivity().runOnUiThread(() -> {
                    // Final check before touching views
                    if (getContext() == null) return;
                    updateUi(result);
                    setLoading(false);
                });
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return; // Pre-check
                requireActivity().runOnUiThread(() -> {
                    // Final check before touching views
                    if (getContext() == null) return;
                    Toast.makeText(getContext(), "API Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setLoading(false);
                });
            }
        });
    }

    private void updateUi(NoiseStats stats) {
        Context context = getContext();
        if (context == null) return; // Final safety net

        currentDb.setText(String.format(Locale.getDefault(), "%d dB", stats.getCurrentNoise()));
        currentStatus.setText(stats.getEventType());

        switch (stats.getEventType().toUpperCase()) {
            case "CRITICAL":
                currentStatusIndicator.setImageResource(R.drawable.ic_status_offline);
                currentStatus.setTextColor(ContextCompat.getColor(context, R.color.error));
                break;
            case "WARNING":
                currentStatusIndicator.setImageResource(R.drawable.ic_status_warning);
                currentStatus.setTextColor(ContextCompat.getColor(context, R.color.warning));
                break;
            default: // NORMAL
                currentStatusIndicator.setImageResource(R.drawable.ic_status_normal);
                currentStatus.setTextColor(ContextCompat.getColor(context, R.color.dark_secondary_text_new));
                break;
        }

        lastUpdated.setText(stats.getCurrentTimestamp());
        minToday.setText(String.format(Locale.getDefault(), "%d dB", stats.getMinNoise()));
        maxToday.setText(String.format(Locale.getDefault(), "%d dB", stats.getMaxNoise()));
        notificationsToday.setText(String.valueOf(stats.getNotificationsSent()));
    }

    private void setLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (contentGroup != null) {
            contentGroup.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        }
    }
}
