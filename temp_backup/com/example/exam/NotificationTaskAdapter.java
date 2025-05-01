package com.example.exam;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import androidx.core.content.ContextCompat;

public class NotificationTaskAdapter extends RecyclerView.Adapter<NotificationTaskAdapter.TaskViewHolder> {
    private final List<Task> tasks;
    private final Context context;

    public NotificationTaskAdapter(Context context, List<Task> tasks) {
        this.context = context;
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.taskText.setText(task.getText());
        
        if (task.getDueDate() != null) {
            holder.taskDate.setText(task.getDueDate());
            holder.taskDate.setVisibility(View.VISIBLE);
        } else {
            holder.taskDate.setVisibility(View.GONE);
        }
        
        // Set priority indicator color
        int color = getPriorityColor(task.getPriority());
        holder.priorityIndicator.setBackgroundColor(color);
    }

    private int getPriorityColor(String priority) {
        if (priority == null) return ContextCompat.getColor(context, R.color.priority_medium);
        
        switch (priority) {
            case "High":
                return ContextCompat.getColor(context, R.color.priority_high);
            case "Medium":
                return ContextCompat.getColor(context, R.color.priority_medium);
            case "Low":
                return ContextCompat.getColor(context, R.color.priority_low);
            default:
                return ContextCompat.getColor(context, R.color.priority_medium);
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        final TextView taskText;
        final TextView taskDate;
        final View priorityIndicator;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskText = itemView.findViewById(R.id.notification_task_text);
            taskDate = itemView.findViewById(R.id.notification_task_date);
            priorityIndicator = itemView.findViewById(R.id.priority_indicator);
        }
    }
} 