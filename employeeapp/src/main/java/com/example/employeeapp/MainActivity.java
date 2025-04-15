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
        taskList = generateDummyTasks();
        adapter = new TaskAdapter(taskList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    private List<Task> generateDummyTasks() {
        List<Task> list = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.DAY_OF_MONTH, 2);
        list.add(new Task("1", "בדיקת אפליקציה", "לוודא שהכול עובד", cal.getTime()));

        cal.add(Calendar.DAY_OF_MONTH, 3);
        list.add(new Task("2", "הצגת פרויקט בכיתה", "ביום שלישי הקרוב", cal.getTime()));

        return list;
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