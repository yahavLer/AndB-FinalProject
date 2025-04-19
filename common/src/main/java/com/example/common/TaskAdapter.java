package com.example.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> tasks;
    private final boolean isEmployeeMode;
    private final OnTaskActionListener actionListener;
    public interface OnTaskActionListener {
        void onMarkCompleted(Task task);
    }
    public TaskAdapter(List<Task> tasks, boolean isEmployeeMode, OnTaskActionListener listener) {
        this.tasks = tasks;
        this.isEmployeeMode = isEmployeeMode;
        this.actionListener = listener;
    }
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.title.setText(task.getTitle());
        holder.desc.setText(task.getDescription());
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        holder.date.setText(format.format(task.getDueDate()));

        if (isEmployeeMode) {
            holder.completeButton.setVisibility(View.VISIBLE);
            holder.completeButton.setEnabled(!task.isCompleted());
            holder.completeButton.setText(task.isCompleted() ? "הושלמה ✅" : "בוצע");

            holder.completeButton.setOnClickListener(v -> {
                actionListener.onMarkCompleted(task);
            });
        } else {
            holder.completeButton.setVisibility(View.GONE); // מוסתר למנהל
        }
        holder.status.setText(task.isCompleted() ? "✔️ הושלמה" : "⏳ ממתינה לביצוע");

    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title, desc, date, status;
        Button completeButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.taskTitle);
            desc = itemView.findViewById(R.id.taskDesc);
            date = itemView.findViewById(R.id.taskDate);
            status = itemView.findViewById(R.id.taskStatus);
            completeButton = itemView.findViewById(R.id.completeTaskButton);
        }
    }
}
