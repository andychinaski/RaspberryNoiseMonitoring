package com.example.noisemonitor;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

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

            // This is the most robust way: wait for the layout pass to be complete
            bottomNav.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Attempt to set the initial position
                    boolean success = setInitialIndicatorPosition();
                    // If successful, we can remove the listener
                    if (success) {
                        bottomNav.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }

    private final BottomNavigationView.OnItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;
        int itemIndex = 0;

        int itemId = item.getItemId();
        if (itemId == R.id.nav_monitor) {
            selectedFragment = new MonitorFragment();
            itemIndex = 0;
        } else if (itemId == R.id.nav_history) {
            selectedFragment = new HistoryFragment();
            itemIndex = 1;
        } else if (itemId == R.id.nav_alerts) {
            selectedFragment = new AlertsFragment();
            itemIndex = 2;
        } else if (itemId == R.id.nav_device) {
            selectedFragment = new DeviceFragment();
            itemIndex = 3;
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            animateIndicator(itemIndex);
            return true;
        }
        return false;
    };

    private boolean setInitialIndicatorPosition() {
        ViewGroup menuView = (ViewGroup) bottomNav.getChildAt(0);
        if (menuView == null || menuView.getChildCount() == 0) return false;

        View initialTarget = menuView.getChildAt(0);
        if (initialTarget == null || initialTarget.getWidth() == 0) return false;

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) indicator.getLayoutParams();
        params.leftMargin = initialTarget.getLeft();
        params.width = initialTarget.getWidth();
        indicator.setLayoutParams(params);
        return true;
    }

    private void animateIndicator(int index) {
        ViewGroup menuView = (ViewGroup) bottomNav.getChildAt(0);
        if (menuView == null || index >= menuView.getChildCount()) return;

        View targetView = menuView.getChildAt(index);
        if (targetView == null || targetView.getWidth() == 0) return;

        int startPos = ((ViewGroup.MarginLayoutParams) indicator.getLayoutParams()).leftMargin;
        int startWidth = indicator.getWidth();
        int endPos = targetView.getLeft();
        int endWidth = targetView.getWidth();

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            int currentPos = (int) (startPos + (endPos - startPos) * fraction);
            int currentWidth = (int) (startWidth + (endWidth - startWidth) * fraction);

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) indicator.getLayoutParams();
            params.leftMargin = currentPos;
            params.width = currentWidth;
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
