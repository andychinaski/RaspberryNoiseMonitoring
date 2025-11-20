package com.example.noisemonitor.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noisemonitor.R;
import com.example.noisemonitor.api.AlertEvent;
import com.example.noisemonitor.api.ApiService;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AlertsFragment extends Fragment {

    private RecyclerView recyclerView;
    private AlertsAdapter adapter;
    private final List<AlertEvent> alertList = new ArrayList<>();
    private ApiService apiService;
    private ProgressBar progressBar;
    private SwitchMaterial sentOnlySwitch;

    private Calendar selectedDate = Calendar.getInstance();
    private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alerts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiService.getInstance(requireContext());
        bindViews(view);
        setupRecyclerView();
        setupButtons();

        fetchAlertsData(); // Initial data fetch for today
    }

    private void bindViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_alerts);
        progressBar = view.findViewById(R.id.progress_bar_alerts);
        sentOnlySwitch = view.findViewById(R.id.switch_sent_only);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AlertsAdapter(requireContext(), alertList);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));
    }

    private void setupButtons() {
        getView().findViewById(R.id.button_refresh_alerts).setOnClickListener(v -> fetchAlertsData());
        sentOnlySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> fetchAlertsData());

        getView().findViewById(R.id.button_today_alerts).setOnClickListener(v -> {
            selectedDate = Calendar.getInstance();
            fetchAlertsData();
        });

        getView().findViewById(R.id.button_yesterday_alerts).setOnClickListener(v -> {
            selectedDate = Calendar.getInstance();
            selectedDate.add(Calendar.DAY_OF_YEAR, -1);
            fetchAlertsData();
        });

        getView().findViewById(R.id.button_select_date_alerts).setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (datePicker, year, month, day) -> {
                    selectedDate.set(year, month, day);
                    fetchAlertsData();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void fetchAlertsData() {
        setLoading(true);
        String dateString = apiDateFormat.format(selectedDate.getTime());
        boolean sentOnly = sentOnlySwitch.isChecked();

        // Dummy data simulation
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                alertList.clear();
                alertList.add(new AlertEvent("12:43", "Sent notification of overtemperature"));
                alertList.add(new AlertEvent("11:12", "System switched to WARNING"));
                alertList.add(new AlertEvent("09:54", "Noise above normal"));
                adapter.notifyDataSetChanged();
                setLoading(false);
            });
        }, 1000);

    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }
}
