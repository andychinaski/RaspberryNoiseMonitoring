package com.example.noisemonitor.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noisemonitor.R;
import com.example.noisemonitor.api.ApiService;
import com.example.noisemonitor.api.HistoryEvent;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private final List<HistoryEvent> eventList = new ArrayList<>();
    private ApiService apiService;
    private ProgressBar progressBar;
    private SwitchMaterial criticalOnlySwitch;
    private ImageButton refreshButton;
    private Button todayButton, yesterdayButton, selectDateButton;

    private Calendar selectedDate = Calendar.getInstance();
    private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private boolean isInitialLoad = true;

    public HistoryFragment() {
        super(R.layout.fragment_history);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiService.getInstance(requireContext());
        bindViews(view);
        setupRecyclerView();
        setupButtons();
    }

    @Override
    public void onResume() {
        super.onResume();
        isInitialLoad = true; // Reset on resume
        fetchHistoryData();
    }

    private void bindViews(@NonNull View view) {
        recyclerView = view.findViewById(R.id.recycler_view_history);
        progressBar = view.findViewById(R.id.progress_bar_history);
        criticalOnlySwitch = view.findViewById(R.id.switch_critical_only);
        refreshButton = view.findViewById(R.id.button_refresh);
        todayButton = view.findViewById(R.id.button_today);
        yesterdayButton = view.findViewById(R.id.button_yesterday);
        selectDateButton = view.findViewById(R.id.button_select_date);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new HistoryAdapter(requireContext(), eventList);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));
    }

    private void setupButtons() {
        refreshButton.setOnClickListener(v -> fetchHistoryData());
        criticalOnlySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> fetchHistoryData());

        todayButton.setOnClickListener(v -> {
            selectedDate = Calendar.getInstance();
            fetchHistoryData();
        });

        yesterdayButton.setOnClickListener(v -> {
            selectedDate = Calendar.getInstance();
            selectedDate.add(Calendar.DAY_OF_YEAR, -1);
            fetchHistoryData();
        });

        selectDateButton.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (datePicker, year, month, day) -> {
                    selectedDate.set(year, month, day);
                    fetchHistoryData();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void fetchHistoryData() {
        setLoading(true);
        String dateString = apiDateFormat.format(selectedDate.getTime());
        boolean criticalOnly = criticalOnlySwitch.isChecked();

        apiService.getHistoryEvents(dateString, criticalOnly, new ApiService.ApiCallback<List<HistoryEvent>>() {
            @Override
            public void onSuccess(List<HistoryEvent> result) {
                if (!isAdded()) return; 
                requireActivity().runOnUiThread(() -> {
                    setLoading(false);
                    eventList.clear();
                    eventList.addAll(result);
                    adapter.notifyDataSetChanged();
                    isInitialLoad = false; // Mark initial load as done
                    if (result.isEmpty()) {
                        Toast.makeText(getContext(), "No events found for this day.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return; 
                requireActivity().runOnUiThread(() -> {
                    setLoading(false);
                    isInitialLoad = false;
                    Toast.makeText(getContext(), "API Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (progressBar != null && recyclerView != null) {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                if (isInitialLoad) {
                    // On first load, hide the content to avoid showing old data
                    recyclerView.setVisibility(View.INVISIBLE);
                }
            } else {
                progressBar.setVisibility(View.GONE);
                // Always make sure content is visible when not loading
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }
}
