package com.example.managerapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.common.BaseActivity;
import com.example.common.Task;
import com.example.common.TaskAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MainActivity extends BaseActivity {
    private FirebaseFirestore firestore;

    private EditText taskTitleInput, taskDescInput;
    private Button addTaskButton;
    private RecyclerView taskRecycler;
    private TaskAdapter adapter;
    private List<Task> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showToast("ברוכים הבאים לאפליקציית המנהלים!");
        firestore = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_main);

        taskTitleInput = findViewById(R.id.taskTitleInput);
        taskDescInput = findViewById(R.id.taskDescInput);
        addTaskButton = findViewById(R.id.addTaskButton);
        taskRecycler = findViewById(R.id.managerTaskRecycler);

        taskList = new ArrayList<>();
        adapter = new TaskAdapter(taskList, false, task -> {});
        taskRecycler.setLayoutManager(new LinearLayoutManager(this));
        taskRecycler.setAdapter(adapter);
        listenForTasks();
        addTaskButton.setOnClickListener(v -> {
            String title = taskTitleInput.getText().toString();
            String desc = taskDescInput.getText().toString();
            Log.d("MANAGER", "ניסיון להוספת משימה: כותרת=" + title + ", תיאור=" + desc);

            if (title.isEmpty() || desc.isEmpty()) {
                showToast("נא למלא כותרת ותיאור");
                return;
            }

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 2);
            String taskId = String.valueOf(System.currentTimeMillis());

            // נשתמש ב-ID ייחודי על בסיס זמן
            //Task task = new Task(String.valueOf(taskList.size() + 1), title, desc, cal.getTime());
            Task task = new Task(taskId, title, desc, cal.getTime());

            // שמירה בפיירבייס
            firestore.collection("tasks")
                    .document(task.getId())
                    .set(task)
                    .addOnSuccessListener(unused -> {
                        showToast("המשימה נשמרה בהצלחה!");
                        Log.d("MANAGER", "משימה נשמרה בהצלחה: " + task);
                        taskTitleInput.setText("");
                        taskDescInput.setText("");
                    })
                    .addOnFailureListener(e -> {
                        showToast("שגיאה בשמירת המשימה");
                        Log.e("MANAGER", "שגיאה בשמירת המשימה", e);
                    });
        });
    }
    private void listenForTasks() {
        firestore.collection("tasks")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        showToast("שגיאה בטעינת המשימות");
                        Log.e("MANAGER", "שגיאה בטעינת משימות", error);
                        return;
                    }

                    if (value == null) {
                        Log.w("MANAGER", "Snapshot value == null");
                        return;
                    }
                    taskList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        Task task = doc.toObject(Task.class);
                        Log.d("MANAGER", "משימה טעונה: " + task);
                        taskList.add(task);
                    }
                    Log.d("MANAGER", "נטענו " + taskList.size() + " משימות");
                    adapter.notifyDataSetChanged();
                });
    }

}