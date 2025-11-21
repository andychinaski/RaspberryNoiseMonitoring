package com.example.noisemonitor;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
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
    private View indicator;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNav = findViewById(R.id.bottom_navigation);
        indicator = findViewById(R.id.bottom_nav_animated_indicator);

        bottomNav.setOnItemSelectedListener(navListener);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MonitorFragment()).commit();

            // Use post() to reliably set the initial indicator position after the layout is complete.
            bottomNav.post(() -> setInitialIndicatorPosition());
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
            animateIndicator(itemId);
            return true;
        }
        return false;
    };

    private void setInitialIndicatorPosition() {
        View targetView = bottomNav.findViewById(R.id.nav_monitor);
        if (targetView == null) return;

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) indicator.getLayoutParams();
        params.leftMargin = targetView.getLeft();
        params.width = targetView.getWidth();
        indicator.setLayoutParams(params);
    }

    private void animateIndicator(@IdRes int itemId) {
        View targetView = bottomNav.findViewById(itemId);
        if (targetView == null) return;

        int start = indicator.getLeft();
        int end = targetView.getLeft();
        int widthStart = indicator.getWidth();
        int widthEnd = targetView.getWidth();

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            int newLeft = (int) (start + (end - start) * fraction);
            int newWidth = (int) (widthStart + (widthEnd - widthStart) * fraction);

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) indicator.getLayoutParams();
            params.leftMargin = newLeft;
            params.width = newWidth;
            indicator.setLayoutParams(params);
        });
        animator.start();
    }

    private void applyTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int themePosition = prefs.getInt(KEY_THEME_POSITION, 0);
        if (themePosition == 0) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

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
