package com.example.managerapp;

import android.os.Bundle;
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
        adapter = new TaskAdapter(taskList);
        taskRecycler.setLayoutManager(new LinearLayoutManager(this));
        taskRecycler.setAdapter(adapter);

        addTaskButton.setOnClickListener(v -> {
            String title = taskTitleInput.getText().toString();
            String desc = taskDescInput.getText().toString();
            if (title.isEmpty() || desc.isEmpty()) {
                showToast("נא למלא כותרת ותיאור");
                return;
            }

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 2);

            // נשתמש ב-ID ייחודי על בסיס זמן
            Task task = new Task(String.valueOf(System.currentTimeMillis()), title, desc, cal.getTime());

            // שמירה בפיירבייס
            firestore.collection("tasks")
                    .document(task.getId())
                    .set(task)
                    .addOnSuccessListener(unused -> {
                        taskList.add(task);
                        adapter.notifyItemInserted(taskList.size() - 1);
                        showToast("המשימה נשמרה בהצלחה!");
                        taskTitleInput.setText("");
                        taskDescInput.setText("");
                    })
                    .addOnFailureListener(e -> {
                        showToast("שגיאה בשמירת המשימה");
                    });
        });
    }
}