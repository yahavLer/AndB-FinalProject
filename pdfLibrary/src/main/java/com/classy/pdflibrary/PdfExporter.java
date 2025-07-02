package com.classy.pdflibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class PdfExporter {

    /**
     * מייצא View כ־PDF ומחזיר true אם הצליח, false אם נכשל.
     * @param context Context
     * @param view ה־View שרוצים להמיר
     * @param pdfUri ה־Uri אליו יישמר ה־PDF (מה־ACTION_CREATE_DOCUMENT)
     * @return true/false
     */
    public static boolean exportViewToPdf(Context context, View view, Uri pdfUri) {
        try {
            Bitmap bitmap = getBitmapFromView(view);

            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                    bitmap.getWidth(), bitmap.getHeight(), 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            canvas.drawBitmap(bitmap, 0, 0, null);
            document.finishPage(page);

            OutputStream out = context.getContentResolver().openOutputStream(pdfUri);
            document.writeTo(out);
            document.close();
            if (out != null) out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "שגיאה ביצוא PDF", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * פותח קובץ PDF (קורא) – יש לשלוח Uri חוקי.
     * @param activity אקטיביטי
     * @param pdfUri ה־Uri של הקובץ
     */
    public static void openPdf(Activity activity, Uri pdfUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            activity.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(activity, "אין אפליקציה מתאימה להצגת PDF", Toast.LENGTH_SHORT).show();
        }
    }

    // יצירת Bitmap מתוך View (פנימי)
    private static Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(
                view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        view.draw(canvas);
        return returnedBitmap;
    }

    /**
     * ייצוא רשימת משימות כטבלה ב־PDF
     * @param context הקשר
     * @param tasks רשימת משימות (TaskInfo - מחלקה פשוטה עם שדות של המשימה)
     * @param pdfUri מיקום לשמירה
     * @return true/false
     */
    public static boolean exportTasksTableToPdf(Context context, List<TaskInfo> tasks, Uri pdfUri) {
        int pageWidth = 595; // A4 ב־72 dpi
        int pageHeight = 842;
        int startX = 40, startY = 60;
        int rowHeight = 40;
        int col1Width = 120; // שם משימה
        int col2Width = 250; // תיאור
        int col3Width = 140; // דדליין

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        paint.setTextSize(16);
        paint.setFakeBoldText(true);

        // Header row
        int y = startY;
        canvas.drawText("שם משימה", startX, y, paint);
        canvas.drawText("תיאור", startX + col1Width, y, paint);
        canvas.drawText("דדליין", startX + col1Width + col2Width, y, paint);

        // קו מתחת לכותרת
        paint.setStrokeWidth(2);
        canvas.drawLine(startX, y + 8, pageWidth - startX, y + 8, paint);

        paint.setFakeBoldText(false);
        y += rowHeight;

        // Rows
        paint.setTextSize(14);
        for (TaskInfo task : tasks) {
            canvas.drawText(task.title, startX, y, paint);
            canvas.drawText(task.desc, startX + col1Width, y, paint);
            canvas.drawText(task.deadline, startX + col1Width + col2Width, y, paint);
            y += rowHeight;
            if (y > pageHeight - 40) { // עמוד חדש
                document.finishPage(page);
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = startY + rowHeight;
            }
        }

        document.finishPage(page);

        // כתיבה לקובץ
        try {
            OutputStream out = context.getContentResolver().openOutputStream(pdfUri);
            document.writeTo(out);
            document.close();
            if (out != null) out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "שגיאה ביצוא PDF", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // מחלקה פשוטה להעברת נתוני משימה (בלי קשר למחלקת Task שלך)
    public static class TaskInfo {
        public String title, desc, deadline;
        public TaskInfo(String title, String desc, String deadline) {
            this.title = title;
            this.desc = desc;
            this.deadline = deadline;
        }
    }
}
