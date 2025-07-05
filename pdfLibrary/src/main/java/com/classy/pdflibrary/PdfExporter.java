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
import java.util.ArrayList;
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
        int startX = 30, startY = 60;
        int minRowHeight = 40;
        int padding = 8;

        // רוחב עמודות מתאים יותר
        int col1Width = 140; // שם משימה (הוגדל)
        int col2Width = 280; // תיאור (הוגדל משמעותית)
        int col3Width = 100; // דדליין (קוטן קצת)


        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        paint.setAntiAlias(true);

        // Header row
        int y = startY;
        canvas.drawText("שם משימה", startX + padding, y, paint);
        canvas.drawText("תיאור", startX + col1Width + padding, y, paint);
        canvas.drawText("דדליין", startX + col1Width + col2Width + padding, y, paint);

        // קווים אנכיים לכותרת
        paint.setStrokeWidth(1);
        canvas.drawLine(startX, y - 20, startX, y + 10, paint);
        canvas.drawLine(startX + col1Width, y - 20, startX + col1Width, y + 10, paint);
        canvas.drawLine(startX + col1Width + col2Width, y - 20, startX + col1Width + col2Width, y + 10, paint);
        canvas.drawLine(startX + col1Width + col2Width + col3Width, y - 20, startX + col1Width + col2Width + col3Width, y + 10, paint);

        // קו מתחת לכותרת
        paint.setStrokeWidth(2);
        canvas.drawLine(startX, y + 12, startX + col1Width + col2Width + col3Width, y + 12, paint);

        paint.setFakeBoldText(false);
        paint.setTextSize(14);
        y += minRowHeight;

        // Rows
        for (TaskInfo task : tasks) {
            int rowStartY = y;

            // חישוב גובה השורה הנדרש
            List<String> titleLines = wrapText(task.title, col1Width - (padding * 2), paint);
            List<String> descLines = wrapText(task.desc, col2Width - (padding * 2), paint);
            List<String> deadlineLines = wrapText(task.deadline, col3Width - (padding * 2), paint);

            int maxLines = Math.max(titleLines.size(), Math.max(descLines.size(), deadlineLines.size()));
            int actualRowHeight = Math.max(minRowHeight, maxLines * 20 + padding);

            // בדיקה אם השורה תחרג מהעמוד
            if (y + actualRowHeight > pageHeight - 40) {
                document.finishPage(page);
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = startY + minRowHeight;
                rowStartY = y;
            }

            // ציור קווים אנכיים לשורה
            paint.setStrokeWidth(1);
            canvas.drawLine(startX, rowStartY - minRowHeight + 12, startX, rowStartY + actualRowHeight - minRowHeight + 12, paint);
            canvas.drawLine(startX + col1Width, rowStartY - minRowHeight + 12, startX + col1Width, rowStartY + actualRowHeight - minRowHeight + 12, paint);
            canvas.drawLine(startX + col1Width + col2Width, rowStartY - minRowHeight + 12, startX + col1Width + col2Width, rowStartY + actualRowHeight - minRowHeight + 12, paint);
            canvas.drawLine(startX + col1Width + col2Width + col3Width, rowStartY - minRowHeight + 12, startX + col1Width + col2Width + col3Width, rowStartY + actualRowHeight - minRowHeight + 12, paint);

            // ציור קו אופקי מתחת לשורה
            canvas.drawLine(startX, rowStartY + actualRowHeight - minRowHeight + 12, startX + col1Width + col2Width + col3Width, rowStartY + actualRowHeight - minRowHeight + 12, paint);

            // כתיבת הטקסט
            drawMultilineText(canvas, titleLines, startX + padding, y, paint);
            drawMultilineText(canvas, descLines, startX + col1Width + padding, y, paint);
            drawMultilineText(canvas, deadlineLines, startX + col1Width + col2Width + padding, y, paint);

            y += actualRowHeight;
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

    /**
     * פיצול טקסט לשורות מרובות בהתאם לרוחב העמודה
     */
    private static List<String> wrapText(String text, int maxWidth, Paint paint) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = new StringBuilder(testLine);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    // המילה ארוכה מידי - נחתוך אותה
                    lines.add(truncateText(word, maxWidth, paint));
                    currentLine = new StringBuilder();
                }
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines.isEmpty() ? List.of("") : lines;
    }

    /**
     * חיתוך טקסט ארוך מידי עם הוספת "..."
     */
    private static String truncateText(String text, int maxWidth, Paint paint) {
        if (paint.measureText(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        float ellipsisWidth = paint.measureText(ellipsis);

        for (int i = text.length() - 1; i > 0; i--) {
            String truncated = text.substring(0, i) + ellipsis;
            if (paint.measureText(truncated) <= maxWidth) {
                return truncated;
            }
        }

        return ellipsis;
    }

    /**
     * ציור טקסט מרובה שורות
     */
    private static void drawMultilineText(Canvas canvas, List<String> lines, int x, int y, Paint paint) {
        int currentY = y;
        for (String line : lines) {
            canvas.drawText(line, x, currentY, paint);
            currentY += 20; // מרווח בין שורות
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
