package com.example.noisemonitor.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noisemonitor.R;
import com.example.noisemonitor.api.HistoryEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private final List<HistoryEvent> eventList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView(view);
        setupButtons(view);

        fetchHistoryData(); // Initial data fetch
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_history);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);

        // Add dividers
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        adapter = new HistoryAdapter(requireContext(), eventList);
        recyclerView.setAdapter(adapter);
    }

    private void setupButtons(View view) {
        view.findViewById(R.id.button_refresh).setOnClickListener(v -> fetchHistoryData());
        view.findViewById(R.id.button_today).setOnClickListener(v -> Toast.makeText(requireContext(), "Fetching today's data...", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.button_yesterday).setOnClickListener(v -> Toast.makeText(requireContext(), "Fetching yesterday's data...", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.button_select_date).setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (datePicker, year, month, day) -> {
                    String selectedDate = day + "/" + (month + 1) + "/" + year;
                    Toast.makeText(requireContext(), "Fetching data for: " + selectedDate, Toast.LENGTH_SHORT).show();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void fetchHistoryData() {
        if (getContext() == null) return; // Extra safety check
        Toast.makeText(requireContext(), "Updating...", Toast.LENGTH_SHORT).show();
        // --- API Call Simulation ---
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            eventList.clear();
            // Add dummy data
            eventList.add(new HistoryEvent("12:43", 76, HistoryEvent.Status.CRITICAL));
            eventList.add(new HistoryEvent("12:40", 54, HistoryEvent.Status.NORMAL));
            eventList.add(new HistoryEvent("12:35", 68, HistoryEvent.Status.WARNING));
            eventList.add(new HistoryEvent("10:10", 65, HistoryEvent.Status.NORMAL));
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }, 1000);
    }
}
