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

import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PdfExporter {

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

    public static boolean exportTaskCardsToPdf(Context context, RecyclerView recyclerView, Uri pdfUri) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter == null) return false;

        int pageWidth = 595;  // A4 רוחב ב־72 dpi
        int pageHeight = 842; // A4 גובה
        int margin = 24;
        int y = margin;

        float scale = 0.45f; // הקטנה ל־60% כדי להכניס כמה כרטיסים
        Paint paint = new Paint();
        paint.setFilterBitmap(true); // חדות טובה יותר בביטמאפ מוקטן

        PdfDocument document = new PdfDocument();
        PdfDocument.Page page = null;
        Canvas canvas = null;

        for (int i = 0; i < adapter.getItemCount(); i++) {
            RecyclerView.ViewHolder holder = adapter.createViewHolder(recyclerView, adapter.getItemViewType(i));
            adapter.onBindViewHolder(holder, i);

            holder.itemView.measure(
                    View.MeasureSpec.makeMeasureSpec(pageWidth - 2 * margin, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            holder.itemView.layout(0, 0, holder.itemView.getMeasuredWidth(), holder.itemView.getMeasuredHeight());

            Bitmap originalBitmap = Bitmap.createBitmap(holder.itemView.getMeasuredWidth(), holder.itemView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas cardCanvas = new Canvas(originalBitmap);
            holder.itemView.draw(cardCanvas);

            int scaledWidth = (int) (originalBitmap.getWidth() * scale);
            int scaledHeight = (int) (originalBitmap.getHeight() * scale);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true);

            if (page == null || y + scaledHeight  > pageHeight - margin) {
                // עמוד חדש
                if (page != null) {
                    document.finishPage(page);
                }
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, (i + 1)).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = margin;
            }

            canvas.drawBitmap(scaledBitmap, margin, y, paint);
            y += scaledHeight + 12; // רווח בין כרטיסים
        }

        if (page != null) {
            document.finishPage(page);
        }

        try (OutputStream out = context.getContentResolver().openOutputStream(pdfUri)) {
            document.writeTo(out);
            document.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "שגיאה ביצוא PDF", Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    /**
     * ייצוא רשימת משימות כטבלה ב־PDF
     * @param context הקשר
     * @param pdfUri מיקום לשמירה
     * @return true/false
     */
    public static boolean exportDynamicTableToPdf(Context context, List<PdfRow> rows, Uri pdfUri) {
        if (rows == null || rows.isEmpty()) return false;

        Set<String> headers = rows.get(0).getData().keySet();

        int pageWidth = 595;
        int pageHeight = 842;
        int startX = 30, startY = 60;
        int padding = 8;
        int columnWidth = (pageWidth - 2 * startX) / headers.size();
        int minRowHeight = 40;

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        paint.setTextSize(14);
        paint.setFakeBoldText(true);
        paint.setAntiAlias(true);

        int x = startX;
        int y = startY;

        // ציור כותרות
        for (String header : headers) {
            canvas.drawText(header, x + padding, y, paint);
            x += columnWidth;
        }

        // קו מתחת לכותרות
        y += minRowHeight;
        paint.setStrokeWidth(2);
        canvas.drawLine(startX, y - 15, startX + columnWidth * headers.size(), y - 15, paint);

        paint.setFakeBoldText(false);
        paint.setTextSize(12);

        // ציור שורות
        for (PdfRow row : rows) {
            x = startX;

            // שלב 1: חשב את כל השורות מראש לכל עמודה
            Map<String, List<String>> rowLines = new LinkedHashMap<>();
            int maxLines = 1;
            for (String key : headers) {
                String value = row.getData().getOrDefault(key, "");
                List<String> lines = wrapText(value, columnWidth - 2 * padding, paint);
                rowLines.put(key, lines);
                if (lines.size() > maxLines) {
                    maxLines = lines.size();
                }
            }

            // שלב 2: חשב את הגובה האמיתי לשורה הזו
            int rowHeight = maxLines * 20 + 10;

            // שלב 3: בדוק אם צריך דף חדש
            if (y + rowHeight > pageHeight - 60) {
                document.finishPage(page);
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = startY;
            }

            // שלב 4: כתיבת כל עמודה בשורה הזו
            x = startX;
            for (String key : headers) {
                List<String> lines = rowLines.get(key);
                drawMultilineText(canvas, lines, x + padding, y, paint);
                x += columnWidth;
            }

            // שלב 5: ציור קווים אנכיים בין עמודות
            int baselinePadding = 10;
            x = startX;
            paint.setStrokeWidth(1);
            for (int i = 0; i <= headers.size(); i++) {
                canvas.drawLine(x, y - 15, x, y + rowHeight - baselinePadding, paint);
                x += columnWidth;
            }

            // שלב 6: ציור קו אופקי מתחת לשורה
            canvas.drawLine(startX, y + rowHeight - baselinePadding, startX + columnWidth * headers.size(), y + rowHeight - baselinePadding, paint);

            // שלב 7: עדכון Y לשורה הבאה
            y += rowHeight;
        }

        document.finishPage(page);

        try (OutputStream out = context.getContentResolver().openOutputStream(pdfUri)) {
            document.writeTo(out);
            document.close();
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

    public static class PdfRow {
        private final Map<String, String> data;

        public PdfRow(Map<String, String> data) {
            this.data = new LinkedHashMap<>(data); // שמירת סדר העמודות
        }

        public Map<String, String> getData() {
            return data;
        }
    }
}
