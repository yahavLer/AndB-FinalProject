package com.example.managerapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.example.fancyviews.LoadingButton;
import com.classy.pdflibrary.PdfExporter;

import android.widget.TextView;

import java.util.Locale;
import java.util.Map;

public class MainActivity extends BaseActivity {
    private static final int CREATE_PDF_REQUEST_CODE = 1001;
    private FirebaseFirestore firestore;

    private EditText taskTitleInput, taskDescInput;
    private Button addTaskButton;
    private RecyclerView taskRecycler;
    private TaskAdapter adapter;
    private List<Task> taskList;

    private LoadingButton fancyAddTaskButton;
    private Button exportTablePdfButton;
    private Button exportPhotoToPdfButton;
    private TextView dueDateText ;
    private final Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showToast("ברוכים הבאים לאפליקציית המנהלים!");
        firestore = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_main);

        initViews();
        addTaskButton.setOnClickListener(v -> {
            Task task =createTask(false);
            if (task != null) {
                addTaskToFirestore(task);
            }
        });
        fancyAddTaskButton.setTextForState(LoadingButton.ButtonState.IDLE, "הוסף משימה מיוחדת");
        fancyAddTaskButton.setOnClickListener(v -> {
            Log.d("MAIN_ACTIVITY", "Fancy button clicked, current state: " + fancyAddTaskButton.getState());

            // רק אם הכפתור במצב IDLE נתחיל את התהליך
            if (fancyAddTaskButton.getState() == LoadingButton.ButtonState.IDLE) {
                Task task = createTask(true);
                if (task != null) {
                    addSpecialTaskToFirestore(task);
                }
            }
        });

        exportTablePdfButton.setOnClickListener(v -> requestCreatePdf());
        exportPhotoToPdfButton.setOnClickListener(v -> requestCreateScreenshotPdf());
        dueDateText.setOnClickListener(v -> openCalendarDialog());
        taskList = new ArrayList<>();
        adapter = new TaskAdapter(taskList, false, task -> {});
        taskRecycler.setLayoutManager(new LinearLayoutManager(this));
        taskRecycler.setAdapter(adapter);
        listenForTasks();
    }

    private void openCalendarDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    Date selectedDate = calendar.getTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    dueDateText.setText("תאריך יעד: " + sdf.format(selectedDate));
                    dueDateText.setTag(selectedDate); // נשמור את התאריך באובייקט עצמו
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void addSpecialTaskToFirestore(Task task) {
        firestore.collection("tasks")
                .document(task.getId())
                .set(task)
                .addOnCompleteListener(taskResult -> {
                    if (taskResult.isSuccessful()) {
                        // הצלחה
                        runOnUiThread(() -> {
                            fancyAddTaskButton.setState(LoadingButton.ButtonState.SUCCESS);
                            showToast("המשימה נשמרה בהצלחה!");
                            // איפוס השדות
                            taskTitleInput.setText("");
                            taskDescInput.setText("");

                            // חזרה למצב IDLE אחרי 2 שניות
                            fancyAddTaskButton.postDelayed(() -> {
                                fancyAddTaskButton.setState(LoadingButton.ButtonState.IDLE);
                            }, 2000);
                        });
                    } else {
                        // שגיאה
                        Exception e = taskResult.getException();
                        runOnUiThread(() -> {
                            fancyAddTaskButton.setState(LoadingButton.ButtonState.ERROR);
                            showToast("שגיאה בשמירה: " + (e != null ? e.getMessage() : "לא ידוע"));

                            // חזרה למצב IDLE אחרי 3 שניות
                            fancyAddTaskButton.postDelayed(() -> {
                                fancyAddTaskButton.setState(LoadingButton.ButtonState.IDLE);
                            }, 3000);
                        });
                    }
                });
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

    private void addTaskToFirestore(Task task) {
        firestore.collection("tasks")
                .document(task.getId())
                .set(task)
                .addOnSuccessListener(unused -> {
                    showToast("המשימה נשמרה בהצלחה!");
                    Log.d("DEBUG", "onSuccess called!");
                    taskTitleInput.setText("");
                    taskDescInput.setText("");
                    dueDateText.setText("תאריך יעד: לא נבחר");
                })
                .addOnFailureListener(e -> {
                    showToast("שגיאה בשמירת המשימה");
                    Log.e("DEBUG", "onFailure called: " + e.getMessage());
                });
    }

    private Task createTask(boolean isSpecial) {
        String title = taskTitleInput.getText().toString();
        String desc = taskDescInput.getText().toString();
        Date now = new Date();
        Date dueDate = (Date) dueDateText.getTag();
        if (dueDate == null) {
            dueDate = now; // אם לא נבחר תאריך, נשתמש בתאריך הנוכחי
        }
        if (title.isEmpty() || desc.isEmpty()) {
            showToast("נא למלא כותרת ותיאור");
            if (isSpecial) {
                runOnUiThread(() ->
                        fancyAddTaskButton.setState(LoadingButton.ButtonState.ERROR)
                );
                // חזרה למצב IDLE אחרי 2 שניות
                fancyAddTaskButton.postDelayed(() -> {
                    fancyAddTaskButton.setState(LoadingButton.ButtonState.IDLE);
                }, 2000);
            }
            return null; // חזרת null כדי לא להמשיך
        }

        // הגדרת מצב טעינה רק אם זה כפתור מיוחד
        if (isSpecial) {
            fancyAddTaskButton.setState(LoadingButton.ButtonState.LOADING);
        }

        String taskId = String.valueOf(System.currentTimeMillis());
        Task task = new Task(taskId, title, desc,dueDate ,isSpecial,now);
        return task;
    }

    private void initViews() {
        taskTitleInput = findViewById(R.id.taskTitleInput);
        taskDescInput = findViewById(R.id.taskDescInput);
        addTaskButton = findViewById(R.id.addTaskButton);
        taskRecycler = findViewById(R.id.managerTaskRecycler);
        fancyAddTaskButton = findViewById(R.id.fancyAddTaskButton);
        exportTablePdfButton = findViewById(R.id.exportAsTableToPdfButton);
        dueDateText = findViewById(R.id.dueDateText);
        exportPhotoToPdfButton = findViewById(R.id.exportAsPhotoToPdfButton);
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

    private void requestCreateScreenshotPdf() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "tasks_cards.pdf");
        startActivityForResult(intent, 3003);  // מזהה שונה
    }

    private void exportDynamicTasksToPdf(Uri uri) {
        List<PdfExporter.PdfRow> rows = new ArrayList<>();
        for (Task t : taskList) {
            Map<String, String> data = new LinkedHashMap<>();
            data.put("כותרת", t.getTitle());
            data.put("תיאור", t.getDescription());
            data.put("סטטוס", t.isCompleted() ? "בוצעה" : "לא בוצעה");
            data.put("נוצר בתאריך", formatDate(t.getCreatedDate()));
            data.put("יעד סיום", formatDate(t.getDueDate()));
            rows.add(new PdfExporter.PdfRow(data));
        }

        boolean success = PdfExporter.exportDynamicTableToPdf(this, rows, uri);
        if (success) {
            showToast("PDF נוצר בהצלחה!");
            PdfExporter.openPdf(this, uri);
        }
    }

    private String formatDate(Date date) {
        return date != null ? android.text.format.DateFormat.format("dd/MM/yyyy", date).toString() : "לא ידוע";
    }

    private void exportTasksTableToPdf(Uri pdfUri) {
        exportDynamicTasksToPdf(pdfUri); // קריאה לפונקציה החדשה
    }

    private void exportTaskCardsToPdf(Uri pdfUri) {
        boolean success = PdfExporter.exportTaskCardsToPdf(this, taskRecycler, pdfUri);
        if (success) {
            showToast("צילום המסך נשמר כ-PDF בהצלחה!");
            PdfExporter.openPdf(this, pdfUri);
        } else {
            showToast("שגיאה ביצוא צילום מסך ל-PDF");
        }
    }
}