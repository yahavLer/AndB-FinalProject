package com.example.employeeapp;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.common.BaseActivity;
import com.example.common.Task;
import com.example.common.TaskAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;


public class MainActivity extends BaseActivity {
    private FirebaseFirestore firestore;

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showToast("ברוכים הבאים לאפליקציית העובדים!");
        setContentView(R.layout.activity_main);

        firestore = FirebaseFirestore.getInstance();
        listenForTasks();

        recyclerView = findViewById(R.id.taskRecyclerView);
        taskList = new ArrayList<>();
        adapter = new TaskAdapter(taskList, true, task -> {
            FirebaseFirestore.getInstance().collection("tasks")
                    .document(task.getId())
                    .update("completed", true)
                    .addOnSuccessListener(unused -> {
                        task.setCompleted(true);
                        adapter.notifyDataSetChanged(); // או notifyItemChanged לפי מיקום
                    });
        });        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void listenForTasks() {
        firestore.collection("tasks")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        showToast("שגיאה בטעינת משימות");
                        return;
                    }

                    if (value == null) return;

                    for (DocumentChange dc : value.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                Task task = dc.getDocument().toObject(Task.class);
                                taskList.add(task);
                                adapter.notifyItemInserted(taskList.size() - 1);
                                break;
                            // אפשר בעתיד להוסיף גם MODIFIED, REMOVED
                        }
                    }
                });
    }
}