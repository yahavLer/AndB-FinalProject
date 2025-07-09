package com.example.employeeapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.common.BaseActivity;
import com.example.common.Task;
import com.example.common.TaskAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.common.TaskExportUtils;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.fancyviews.LoadingButton;
import com.classy.pdflibrary.PdfExporter;


public class MainActivity extends BaseActivity {
    private static final int CREATE_PDF_REQUEST_CODE = 1001;

    private FirebaseFirestore firestore;

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList;
    private Button exportPdfButton;
    private Button exportPhotoToPdfButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showToast("ברוכים הבאים לאפליקציית העובדים!");
        setContentView(R.layout.activity_main);

        firestore = FirebaseFirestore.getInstance();
        initViews();
        taskList = new ArrayList<>();
        adapter = new TaskAdapter(taskList, true, task -> {
            FirebaseFirestore.getInstance().collection("tasks")
                    .document(task.getId())
                    .update("completed", true)
                    .addOnSuccessListener(unused -> {
                        task.setCompleted(true);
                        showToast("המשימה סומנה כהושלמה");
                        adapter.notifyDataSetChanged(); // או notifyItemChanged לפי מיקום
                    }).addOnFailureListener(e -> {
                        showToast("שגיאה בסימון משימה כהושלמה");
                        Log.e("EMPLOYEE", "Error updating task: " + task.getId(), e);
                    });
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        exportPdfButton.setOnClickListener(v -> requestCreatePdf());
        exportPhotoToPdfButton.setOnClickListener(v -> requestCreateScreenshotPdf());
        listenForTasks();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.taskRecyclerView);
        exportPdfButton = findViewById(R.id.exportPdfButton);
        exportPhotoToPdfButton = findViewById(R.id.exportAsPhotoToPdfButton);
    }

    private void requestCreatePdf() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "tasks_list.pdf");
        startActivityForResult(intent, CREATE_PDF_REQUEST_CODE);
    }

    private void requestCreateScreenshotPdf() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "tasks_cards.pdf");
        startActivityForResult(intent, 3003);  // מזהה שונה
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri pdfUri = data.getData();
            if (requestCode == CREATE_PDF_REQUEST_CODE) {
                exportTasksTableToPdf(pdfUri);
            } else if (requestCode == 3003) {
                exportTaskCardsToPdf(pdfUri);
            }
        }
    }

    private void exportTasksTableToPdf(Uri pdfUri) {
        exportDynamicTasksToPdf(pdfUri);
    }

    private void exportDynamicTasksToPdf(Uri uri) {
        TaskExportUtils.exportTasksAsTable(this, taskList, uri);
    }

    private void exportTaskCardsToPdf(Uri pdfUri) {
        TaskExportUtils.exportTasksAsCards(this, recyclerView, pdfUri);
    }

    private void listenForTasks() {
        firestore.collection("tasks")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        showToast("שגיאה בטעינת משימות");
                        Log.e("EMPLOYEE", "שגיאה בטעינת משימות", error);
                        return;
                    }

                    if (value == null) {
                        Log.w("EMPLOYEE", "Snapshot value == null");
                        return;
                    }
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                Task task = dc.getDocument().toObject(Task.class);
                                Log.d("EMPLOYEE", "נוספה משימה: " + task);
                                taskList.add(task);
                                adapter.notifyItemInserted(taskList.size() - 1);
                                break;
                            // אפשר בעתיד להוסיף גם MODIFIED, REMOVED
                        }
                    }
                    Log.d("EMPLOYEE", "סך כל המשימות: " + taskList.size());
                });
    }

}