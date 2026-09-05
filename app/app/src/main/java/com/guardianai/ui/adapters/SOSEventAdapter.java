package com.guardianai.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.guardianai.R;
import com.guardianai.data.models.SOSEventResponseDto;

import java.util.ArrayList;
import java.util.List;

public class SOSEventAdapter extends RecyclerView.Adapter<SOSEventAdapter.EventViewHolder> {

    private final List<SOSEventResponseDto> events = new ArrayList<>();

    public void setEvents(List<SOSEventResponseDto> newEvents) {
        events.clear();
        if (newEvents != null) {
            events.addAll(newEvents);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sos_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        SOSEventResponseDto event = events.get(position);
        holder.tvTitle.setText(event.getEventType());
        String detailsStr = "Actor: " + event.getActorType() + " | Status: " + event.getStatus();
        holder.tvDetails.setText(detailsStr);
        String timeStr = event.getCreatedAt() != null ? event.getCreatedAt() : "";
        if (timeStr.length() > 19) {
            timeStr = timeStr.substring(11, 19);
        }
        holder.tvTime.setText(timeStr);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDetails, tvTime;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_event_title);
            tvDetails = itemView.findViewById(R.id.tv_event_details);
            tvTime = itemView.findViewById(R.id.tv_event_time);
        }
    }
}
