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
import com.example.noisemonitor.api.AlertEvent;

import java.util.List;

public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.AlertViewHolder> {

    private final Context context;
    private final List<AlertEvent> alertList;

    public AlertsAdapter(Context context, List<AlertEvent> alertList) {
        this.context = context;
        this.alertList = alertList;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_alert_event, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        AlertEvent event = alertList.get(position);
        holder.time.setText(event.getFormattedSentAt());
        holder.message.setText(event.getMessage());

        if ("sent".equalsIgnoreCase(event.getStatus())) {
            holder.deliveryStatus.setImageResource(R.drawable.ic_status_sent);
        } else {
            holder.deliveryStatus.setImageResource(R.drawable.ic_status_failed);
        }
    }

    @Override
    public int getItemCount() {
        return alertList.size();
    }

    public static class AlertViewHolder extends RecyclerView.ViewHolder {
        TextView time, message;
        ImageView deliveryStatus;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.text_view_alert_time);
            message = itemView.findViewById(R.id.text_view_alert_message);
            deliveryStatus = itemView.findViewById(R.id.icon_delivery_status);
        }
    }
}
