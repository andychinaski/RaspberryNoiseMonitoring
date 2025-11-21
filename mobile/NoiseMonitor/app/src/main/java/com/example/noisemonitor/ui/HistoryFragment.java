package com.example.noisemonitor.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
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

    private Calendar selectedDate = Calendar.getInstance();
    private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiService.getInstance(requireContext());
        bindViews(view);
        setupRecyclerView();
        setupButtons();

        fetchHistoryData(); // Initial data fetch for today
    }

    private void bindViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_history);
        progressBar = view.findViewById(R.id.progress_bar_history);
        criticalOnlySwitch = view.findViewById(R.id.switch_critical_only);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new HistoryAdapter(requireContext(), eventList);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));
    }

    private void setupButtons() {
        getView().findViewById(R.id.button_refresh).setOnClickListener(v -> fetchHistoryData());
        criticalOnlySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> fetchHistoryData());

        getView().findViewById(R.id.button_today).setOnClickListener(v -> {
            selectedDate = Calendar.getInstance();
            fetchHistoryData();
        });

        getView().findViewById(R.id.button_yesterday).setOnClickListener(v -> {
            selectedDate = Calendar.getInstance();
            selectedDate.add(Calendar.DAY_OF_YEAR, -1);
            fetchHistoryData();
        });

        getView().findViewById(R.id.button_select_date).setOnClickListener(v -> showDatePicker());
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
                // Safety check: Make sure the fragment is still attached to an activity
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    eventList.clear();
                    eventList.addAll(result);
                    adapter.notifyDataSetChanged();
                    setLoading(false);
                    if (result.isEmpty()) {
                        Toast.makeText(getContext(), "No events found for this day.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                // Safety check: Make sure the fragment is still attached to an activity
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(getContext(), "API Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }
}
