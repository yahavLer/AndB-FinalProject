package com.example.common;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> tasks;
    private final boolean isEmployeeMode;
    private final OnTaskActionListener actionListener;

    public interface OnTaskActionListener {
        void onMarkCompleted(Task task);
    }

    /**
     * Adapter for displaying tasks in a RecyclerView.
     *
     * @param tasks          List of tasks to display.
     * @param isEmployeeMode True if the adapter is in employee mode, false for manager mode.
     * @param listener       Listener for task actions (e.g., marking as completed).
     */
    public TaskAdapter(List<Task> tasks, boolean isEmployeeMode, OnTaskActionListener listener) {
        this.tasks = tasks;
        this.isEmployeeMode = isEmployeeMode;
        this.actionListener = listener;
    }

    /**
     * Creates a new ViewHolder for the task item.
     *
     * @param parent   The parent ViewGroup.
     * @param viewType The view type of the new View.
     * @return A new TaskViewHolder instance.
     */
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
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        if (task.getCreatedDate() != null) {
            holder.taskCreatedDate.setText("נוצר בתאריך: " + sdf.format(task.getCreatedDate()));
        }

        if (task.getDueDate() != null) {
            holder.taskDueDate.setText("יעד סיום: " + sdf.format(task.getDueDate()));
        }

        if (!task.isCompleted() && task.getDueDate() != null && task.getDueDate().before(new Date())) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFCDD2")); // אדום בהיר
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT); // או כל רקע אחר
        }

        if (task.isSpecial()) {
            holder.itemView.setBackgroundResource(R.drawable.special_task_background);
            holder.title.setTextColor(0xFFD32F2F); // אדום חזק
            holder.title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star, 0, 0, 0);
            holder.title.setTypeface(holder.title.getTypeface(), Typeface.BOLD);
            holder.status.setText("⭐ משימה מיוחדת ⭐");
        } else {
            holder.itemView.setBackgroundResource(R.drawable.normal_task_background);
            holder.title.setTextColor(0xFF222222);
            holder.title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            holder.status.setText(task.isCompleted() ? "✔️ הושלמה" : "⏳ ממתינה לביצוע");
        }

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
        TextView title, desc, date, status, taskCreatedDate, taskDueDate;
        Button completeButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.taskTitle);
            desc = itemView.findViewById(R.id.taskDesc);
            status = itemView.findViewById(R.id.taskStatus);
            completeButton = itemView.findViewById(R.id.completeTaskButton);
            taskCreatedDate = itemView.findViewById(R.id.taskCreatedDate);
            taskDueDate = itemView.findViewById(R.id.taskDueDate);
        }
    }
}
