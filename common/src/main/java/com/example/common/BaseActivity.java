package com.example.common;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logEvent("onCreate called in " + getClass().getSimpleName());
    }

    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void logEvent(String message) {
        // ניתן להרחיב ללוגים או אנליטיקות בעתיד
        System.out.println("[LOG] " + message);
    }
}
