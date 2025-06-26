package com.classy.pdflibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfExporter {
    public static File exportViewToPdf(Context context, View view, String fileName) throws IOException {
        // צור Bitmap מה-View
        Bitmap bitmap = getBitmapFromView(view);

        // צור מסמך PDF
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        canvas.drawBitmap(bitmap, 0, 0, null);
        document.finishPage(page);

        // שמירה לקובץ
        File pdfDirPath = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "");
        if (!pdfDirPath.exists()) pdfDirPath.mkdirs();
        File file = new File(pdfDirPath, fileName);
        FileOutputStream fos = new FileOutputStream(file);
        document.writeTo(fos);
        document.close();
        fos.close();

        return file;
    }

    // יצירת Bitmap מתוך View
    private static Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        view.draw(canvas);
        return returnedBitmap;
    }
}
