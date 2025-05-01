package com.example.exam;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.graphics.drawable.GradientDrawable;
import java.util.ArrayList;
import java.util.Calendar;
import com.google.android.material.chip.Chip;
import com.google.android.material.checkbox.MaterialCheckBox;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> tasks;
    private Runnable onTasksChanged;
    private OnTaskClickListener onTaskClickListener;
    private boolean multiSelectMode = false;
    private final List<Integer> selectedPositions = new ArrayList<>();

    public interface OnTaskClickListener {
        void onTaskClick(int position);
    }

    public interface OnTaskContextMenuListener {
        void onEditTask(int position);
        void onDeleteTask(int position);
        void onMarkDoneTask(int position);
    }
    private OnTaskContextMenuListener contextMenuListener;

    public void setOnTaskContextMenuListener(OnTaskContextMenuListener listener) {
        this.contextMenuListener = listener;
    }

    public TaskAdapter(List<Task> tasks, Runnable onTasksChanged, OnTaskClickListener onTaskClickListener) {
        this.tasks = tasks;
        this.onTasksChanged = onTasksChanged;
        this.onTaskClickListener = onTaskClickListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        Context context = holder.itemView.getContext();
        
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(task.isDone());
        holder.textView.setText(task.getText());
        
        // Strike-through for completed tasks
        if (task.isDone()) {
            holder.textView.setPaintFlags(holder.textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.textView.setAlpha(0.7f);
            holder.itemView.setAlpha(0.8f);
        } else {
            holder.textView.setPaintFlags(holder.textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.textView.setAlpha(1.0f);
            holder.itemView.setAlpha(1.0f);
        }
        
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setDone(isChecked);
            notifyItemChanged(position);
            onTasksChanged.run();
        });
        
        // Multi-select highlight
        if (multiSelectMode && selectedPositions.contains(position)) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.selection_highlight));
        } else {
            holder.itemView.setBackgroundResource(0);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (multiSelectMode) {
                if (selectedPositions.contains(position)) {
                    selectedPositions.remove((Integer) position);
                    notifyItemChanged(position);
                } else {
                    selectedPositions.add(position);
                    notifyItemChanged(position);
                }
            } else if (onTaskClickListener != null) {
                onTaskClickListener.onTaskClick(position);
            }
        });
        
        holder.itemView.setOnLongClickListener(v -> {
            if (contextMenuListener != null) {
                androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(context, holder.itemView);
                popup.getMenu().add("Edit");
                popup.getMenu().add("Delete");
                if (!task.isDone()) {
                    popup.getMenu().add("Mark as Done");
                }
                popup.setOnMenuItemClickListener(item -> {
                    String title = item.getTitle().toString();
                    if (title.equals("Edit")) {
                        contextMenuListener.onEditTask(position);
                    } else if (title.equals("Delete")) {
                        contextMenuListener.onDeleteTask(position);
                    } else if (title.equals("Mark as Done")) {
                        contextMenuListener.onMarkDoneTask(position);
                    }
                    return true;
                });
                popup.show();
                return true;
            }
            return false;
        });
        
        // Handle date container and due date
        if (holder.dateContainer != null && holder.dueDate != null) {
            if (task.getDueDate() != null && !task.getDueDate().isEmpty()) {
                holder.dueDate.setText(task.getDueDate());
                holder.dateContainer.setVisibility(View.VISIBLE);
                
                // Check if task is overdue
                boolean isOverdue = isTaskOverdue(task);
                if (isOverdue && !task.isDone()) {
                    // Highlight overdue tasks
                    GradientDrawable drawable = (GradientDrawable) holder.dateContainer.getBackground();
                    drawable.setColor(ContextCompat.getColor(context, R.color.overdue_background));
                    holder.dueDate.setTextColor(ContextCompat.getColor(context, R.color.overdue_text));
                } else {
                    // Reset to default colors
                    GradientDrawable drawable = (GradientDrawable) holder.dateContainer.getBackground();
                    drawable.setColor(ContextCompat.getColor(context, R.color.date_background));
                    holder.dueDate.setTextColor(ContextCompat.getColor(context, R.color.date_text));
                }
            } else {
                holder.dateContainer.setVisibility(View.GONE);
            }
        }
        
        // Bind category
        if (holder.category != null) {
            if (task.getCategory() != null && !task.getCategory().isEmpty()) {
                holder.category.setText(task.getCategory());
                holder.category.setVisibility(View.VISIBLE);
            } else {
                holder.category.setVisibility(View.GONE);
            }
        }
        
        // Bind priority
        if (holder.priority != null) {
            if (task.getPriority() != null && !task.getPriority().isEmpty()) {
                holder.priority.setText(task.getPriority());
                holder.priority.setVisibility(View.VISIBLE);
                holder.priority.setChipBackgroundColorResource(getChipColorResource(task.getPriority()));
            } else {
                holder.priority.setVisibility(View.GONE);
            }
        }
    }

    private boolean isTaskOverdue(Task task) {
        if (task.getDueDate() == null || task.getDueDate().isEmpty()) {
            return false;
        }
        
        try {
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            
            String[] dateParts = task.getDueDate().split("-");
            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1; // Calendar months are 0-based
            int day = Integer.parseInt(dateParts[2]);
            
            Calendar dueDate = Calendar.getInstance();
            dueDate.set(year, month, day, 0, 0, 0);
            dueDate.set(Calendar.MILLISECOND, 0);
            
            return dueDate.before(today);
        } catch (Exception e) {
            return false;
        }
    }

    private int getChipColorResource(String priority) {
        if (priority == null) return R.color.priority_medium;
        switch (priority.toLowerCase()) {
            case "high": return R.color.priority_high;
            case "medium": return R.color.priority_medium;
            case "low": return R.color.priority_low;
            default: return R.color.priority_medium;
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    public List<Task> getTasks() {
        return new ArrayList<>(tasks);
    }

    public Task getTaskAt(int position) {
        return tasks.get(position);
    }

    public void setMultiSelectMode(boolean enabled) {
        this.multiSelectMode = enabled;
        if (!enabled) {
            selectedPositions.clear();
        }
        notifyDataSetChanged();
    }

    public void setSelectedPositions(List<Integer> positions) {
        this.selectedPositions.clear();
        this.selectedPositions.addAll(positions);
        
        // Use more specific notification instead of notifyDataSetChanged
        for (int i = 0; i < getItemCount(); i++) {
            notifyItemChanged(i);
        }
    }

    public List<Task> getSelectedTasks() {
        List<Task> selected = new ArrayList<>();
        for (int pos : selectedPositions) {
            if (pos >= 0 && pos < tasks.size()) {
                selected.add(tasks.get(pos));
            }
        }
        return selected;
    }

    public void clearSelection() {
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        final MaterialCheckBox checkBox;
        final TextView textView;
        final TextView dueDate;
        final LinearLayout dateContainer;
        final Chip category;
        final Chip priority;
        
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);
            textView = itemView.findViewById(R.id.text);
            dueDate = itemView.findViewById(R.id.dueDate);
            dateContainer = itemView.findViewById(R.id.dateContainer);
            category = itemView.findViewById(R.id.category);
            priority = itemView.findViewById(R.id.priority);
        }
    }
} 