package com.example.common;

import android.content.Context;
import android.net.Uri;

import androidx.recyclerview.widget.RecyclerView;

import com.classy.pdflibrary.PdfExporter;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskExportUtils {

    public static String formatDate(Date date) {
        return date != null ? android.text.format.DateFormat.format("dd/MM/yyyy", date).toString() : "לא ידוע";
    }

    public static void exportTasksAsTable(Context context, List<Task> taskList, Uri uri) {
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

        boolean success = PdfExporter.exportDynamicTableToPdf(context, rows, uri);
        if (success && context instanceof BaseActivity) {
            ((BaseActivity) context).showToast("PDF נוצר בהצלחה!");
            PdfExporter.openPdf((BaseActivity) context, uri);
        }
    }

    public static void exportTasksAsCards(Context context, RecyclerView recyclerView, Uri uri) {
        boolean success = PdfExporter.exportTaskCardsToPdf(context, recyclerView, uri);
        if (context instanceof BaseActivity) {
            if (success) {
                ((BaseActivity) context).showToast("צילום המסך נשמר כ-PDF בהצלחה!");
                PdfExporter.openPdf((BaseActivity) context, uri);
            } else {
                ((BaseActivity) context).showToast("שגיאה ביצוא צילום מסך ל-PDF");
            }
        }
    }
}
