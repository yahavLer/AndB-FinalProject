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

public class MainActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showToast("ברוכים הבאים לאפליקציית העובדים!");
        setContentView(R.layout.activity_main);
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
}