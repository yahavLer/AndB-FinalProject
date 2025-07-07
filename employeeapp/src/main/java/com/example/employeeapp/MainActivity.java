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
import java.util.List;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showToast("ברוכים הבאים לאפליקציית העובדים!");
        setContentView(R.layout.activity_main);

        firestore = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.taskRecyclerView);
        exportPdfButton = findViewById(R.id.exportPdfButton);
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

        listenForTasks();
    }

    private void requestCreatePdf() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "tasks_list.pdf");
        startActivityForResult(intent, CREATE_PDF_REQUEST_CODE);
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

    private void exportTasksTableToPdf(Uri pdfUri) {
        List<PdfExporter.TaskInfo> infoList = convertTasksToInfoList(taskList); // taskList = כל המשימות!
        boolean success = PdfExporter.exportTasksTableToPdf(this, infoList, pdfUri);
        if (success) {
            showToast("הקובץ נשמר בהצלחה!");
            PdfExporter.openPdf(this, pdfUri);
        }
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