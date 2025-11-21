package com.example.noisemonitor.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.example.noisemonitor.R;
import com.example.noisemonitor.api.ApiService;
import com.example.noisemonitor.api.Device;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class SettingsDialogFragment extends DialogFragment {

    private static final String PREFS_NAME = "NoiseMonitorPrefs";
    private static final String KEY_PUSH_NOTIFICATIONS = "push_notifications";
    private static final String KEY_AUTO_REFRESH_POSITION = "auto_refresh_position";
    private static final String KEY_THEME_POSITION = "theme_position";
    private static final String KEY_API_URL = "api";

    private SwitchMaterial switchPush;
    private Spinner spinnerAutoRefresh;
    private Spinner spinnerTheme;
    private TextInputEditText inputApiUrl;
    private ApiService apiService;

    public SettingsDialogFragment() {
        super(R.layout.dialog_settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiService.getInstance(requireContext());
        setupToolbar(view);
        bindViews(view);
        setupSpinners();
        loadSettings();

        view.findViewById(R.id.button_cancel).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.button_save).setOnClickListener(v -> {
            saveSettingsAndRefreshConnection();
            dismiss();
        });
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar_settings);
        toolbar.inflateMenu(R.menu.settings_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_close) {
                dismiss();
                return true;
            }
            return false;
        });
    }

    private void bindViews(View view) {
        switchPush = view.findViewById(R.id.switch_push);
        spinnerAutoRefresh = view.findViewById(R.id.spinner_auto_refresh);
        spinnerTheme = view.findViewById(R.id.spinner_theme);
        inputApiUrl = view.findViewById(R.id.input_edit_text_api_url);
    }

    private void setupSpinners() {
        // Auto-refresh spinner
        ArrayAdapter<CharSequence> refreshAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.auto_refresh_entries, android.R.layout.simple_spinner_item);
        refreshAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAutoRefresh.setAdapter(refreshAdapter);

        // Theme spinner
        ArrayAdapter<CharSequence> themeAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.theme_entries, android.R.layout.simple_spinner_item);
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTheme.setAdapter(themeAdapter);
    }

    private void loadSettings() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        switchPush.setChecked(prefs.getBoolean(KEY_PUSH_NOTIFICATIONS, true)); // Default to ON
        spinnerAutoRefresh.setSelection(prefs.getInt(KEY_AUTO_REFRESH_POSITION, 1)); // Default to "5 sec"
        spinnerTheme.setSelection(prefs.getInt(KEY_THEME_POSITION, 0)); // Default to "Светлая"
        inputApiUrl.setText(prefs.getString(KEY_API_URL, getString(R.string.api_server_url_hint)));
    }

    private void saveSettingsAndRefreshConnection() {
        SharedPreferences.Editor editor = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();

        editor.putBoolean(KEY_PUSH_NOTIFICATIONS, switchPush.isChecked());
        editor.putInt(KEY_AUTO_REFRESH_POSITION, spinnerAutoRefresh.getSelectedItemPosition());
        editor.putInt(KEY_THEME_POSITION, spinnerTheme.getSelectedItemPosition());
        editor.putString(KEY_API_URL, inputApiUrl.getText().toString());

        editor.apply();

        // Apply theme immediately
        int themePosition = spinnerTheme.getSelectedItemPosition();
        if (themePosition == 0) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        // Re-check device connection after saving settings
        Toast.makeText(getContext(), "Checking connection...", Toast.LENGTH_SHORT).show();
        apiService.refreshDeviceConnection(new ApiService.ApiCallback<Device>() {
            @Override
            public void onSuccess(Device result) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Connection successful!", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onError(Exception e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Connection failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }
}
