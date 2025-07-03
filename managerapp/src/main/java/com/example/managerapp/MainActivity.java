package com.example.managerapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
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

import com.example.fancyviews.LoadingButton;
import com.classy.pdflibrary.PdfExporter;
import android.widget.Toast;
import java.io.File;

public class MainActivity extends BaseActivity {
    private static final int CREATE_PDF_REQUEST_CODE = 1001;
    private FirebaseFirestore firestore;

    private EditText taskTitleInput, taskDescInput;
    private Button addTaskButton;
    private RecyclerView taskRecycler;
    private TaskAdapter adapter;
    private List<Task> taskList;

    private LoadingButton fancyAddTaskButton;
    private Button exportPdfButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showToast("ברוכים הבאים לאפליקציית המנהלים!");
        firestore = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_main);

        initViews();
        addTaskButton.setOnClickListener(v -> {
            addTaskToFirestore(false);
        });

        fancyAddTaskButton.setOnClickListener(v -> {
            addTaskToFirestore(true);
        });
        exportPdfButton.setOnClickListener(v -> requestCreatePdf());

        taskList = new ArrayList<>();
        adapter = new TaskAdapter(taskList, false, task -> {});
        taskRecycler.setLayoutManager(new LinearLayoutManager(this));
        taskRecycler.setAdapter(adapter);
        listenForTasks();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_PDF_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri pdfUri = data.getData();
                exportTasksTableToPdf(pdfUri);
            }
        }
    }

    private void addTaskToFirestore(boolean isSpecial) {
        String title = taskTitleInput.getText().toString();
        String desc = taskDescInput.getText().toString();

        if (title.isEmpty() || desc.isEmpty()) {
            showToast("נא למלא כותרת ותיאור");
            if (isSpecial) {
                fancyAddTaskButton.setState(LoadingButton.ButtonState.ERROR);
                fancyAddTaskButton.postDelayed(() ->
                        fancyAddTaskButton.setState(LoadingButton.ButtonState.IDLE), 1200);
            }
            return;
        }
        if (isSpecial) fancyAddTaskButton.setState(LoadingButton.ButtonState.LOADING);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 2);
        String taskId = String.valueOf(System.currentTimeMillis());
        Task task = new Task(taskId, title, desc, cal.getTime(),isSpecial);

        firestore.collection("tasks")
                .document(task.getId())
                .set(task)
                .addOnSuccessListener(unused -> {
                    showToast("המשימה נשמרה בהצלחה!");
                    if (isSpecial) {
                        fancyAddTaskButton.setState(LoadingButton.ButtonState.SUCCESS);
                        fancyAddTaskButton.postDelayed(() ->
                                fancyAddTaskButton.setState(LoadingButton.ButtonState.IDLE), 1200);
                    }
                    taskTitleInput.setText("");
                    taskDescInput.setText("");
                })
                .addOnFailureListener(e -> {
                    showToast("שגיאה בשמירת המשימה");
                    if (isSpecial) {
                        fancyAddTaskButton.setState(LoadingButton.ButtonState.ERROR);
                        fancyAddTaskButton.postDelayed(() ->
                                fancyAddTaskButton.setState(LoadingButton.ButtonState.IDLE), 1200);
                    }
                });
    }

    private void initViews() {
        taskTitleInput = findViewById(R.id.taskTitleInput);
        taskDescInput = findViewById(R.id.taskDescInput);
        addTaskButton = findViewById(R.id.addTaskButton);
        taskRecycler = findViewById(R.id.managerTaskRecycler);
        fancyAddTaskButton = findViewById(R.id.fancyAddTaskButton);
        exportPdfButton = findViewById(R.id.exportPdfButton);
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
    /*** --- ייצוא PDF של רשימת המשימות --- ***/
    private void requestCreatePdf() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "tasks_list.pdf");
        startActivityForResult(intent, CREATE_PDF_REQUEST_CODE);
    }

    private List<PdfExporter.TaskInfo> convertTasksToInfoList(List<Task> tasks) {
        List<PdfExporter.TaskInfo> infoList = new ArrayList<>();
        for (Task t : tasks) {
            // דוגמה להמרת Date ל־String
            String dateStr = android.text.format.DateFormat.format("dd/MM/yyyy", t.getDueDate()).toString();
            infoList.add(new PdfExporter.TaskInfo(t.getTitle(), t.getDescription(), dateStr));
        }
        return infoList;
    }

    private void exportTasksTableToPdf(Uri pdfUri) {
        List<PdfExporter.TaskInfo> infoList = convertTasksToInfoList(taskList); // taskList = כל המשימות!
        boolean success = PdfExporter.exportTasksTableToPdf(this, infoList, pdfUri);
        if (success) {
            showToast("הקובץ נשמר בהצלחה!");
            PdfExporter.openPdf(this, pdfUri);
        }
    }

}