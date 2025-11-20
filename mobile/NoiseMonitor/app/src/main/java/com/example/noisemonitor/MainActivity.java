package com.example.noisemonitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.noisemonitor.ui.AlertsFragment;
import com.example.noisemonitor.ui.DeviceFragment;
import com.example.noisemonitor.ui.HistoryFragment;
import com.example.noisemonitor.ui.MonitorFragment;
import com.example.noisemonitor.ui.SettingsDialogFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "NoiseMonitorPrefs";
    private static final String KEY_THEME_POSITION = "theme_position";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MonitorFragment()).commit();
        }
    }

    private void applyTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int themePosition = prefs.getInt(KEY_THEME_POSITION, 0); // 0 for Светлая, 1 for Темная
        if (themePosition == 0) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    private final BottomNavigationView.OnItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;

        int itemId = item.getItemId();
        if (itemId == R.id.nav_monitor) {
            selectedFragment = new MonitorFragment();
        } else if (itemId == R.id.nav_history) {
            selectedFragment = new HistoryFragment();
        } else if (itemId == R.id.nav_alerts) {
            selectedFragment = new AlertsFragment();
        } else if (itemId == R.id.nav_device) {
            selectedFragment = new DeviceFragment();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            return true;
        }
        return false;
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            SettingsDialogFragment settingsDialog = new SettingsDialogFragment();
            settingsDialog.show(getSupportFragmentManager(), "SettingsDialog");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
