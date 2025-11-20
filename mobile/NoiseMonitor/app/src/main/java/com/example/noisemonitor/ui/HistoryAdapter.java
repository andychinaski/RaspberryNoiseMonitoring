package com.example.noisemonitor.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noisemonitor.R;
import com.example.noisemonitor.api.HistoryEvent;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final Context context;
    private final List<HistoryEvent> eventList;

    public HistoryAdapter(Context context, List<HistoryEvent> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history_event, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryEvent event = eventList.get(position);

        holder.time.setText(event.getTime());
        holder.dbLevel.setText(String.format("%d dB", event.getDbLevel()));

        switch (event.getStatus()) {
            case CRITICAL:
                holder.statusText.setText("CRITICAL");
                holder.statusIndicator.setImageResource(R.drawable.ic_status_offline); // Red dot
                break;
            case WARNING:
                holder.statusText.setText("WARNING");
                holder.statusIndicator.setImageResource(R.drawable.ic_status_warning); // Yellow dot
                break;
            case NORMAL:
            default:
                holder.statusText.setText("NORMAL");
                holder.statusIndicator.setImageResource(R.drawable.ic_status_normal); // Gray dot
                break;
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView time, dbLevel, statusText;
        ImageView statusIndicator;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.text_view_time);
            dbLevel = itemView.findViewById(R.id.text_view_db_level);
            statusText = itemView.findViewById(R.id.text_view_status);
            statusIndicator = itemView.findViewById(R.id.image_view_status_indicator);
        }
    }
}
