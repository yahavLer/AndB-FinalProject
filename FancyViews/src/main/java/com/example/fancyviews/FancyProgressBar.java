package com.example.fancyviews;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class FancyProgressBar extends ProgressBar {
    public enum ProgressState {
        IDLE,
        LOADING,
        SUCCESS,
        ERROR,
        DISABLED
    }

    private int[] colorSequence = {
            0xFFE91E63, // ורוד
            0xFF3F51B5, // כחול
            0xFF4CAF50, // ירוק
            0xFFFF9800  // כתום
    };
    private ProgressState state = ProgressState.IDLE;


    private int currentColorIndex = 0;
    private Handler handler = new Handler();
    private boolean isRunning = false;
    private long colorChangeInterval = 700; // מילישניות

    public FancyProgressBar(Context context) {
        super(context);
        init();
    }

    public FancyProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FancyProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //setIndeterminate(true);
        //startColorCycle();
        updateUI(); // לא מתחיל להסתובב אוטומטית
    }


    private final Runnable colorChanger = new Runnable() {
        @Override
        public void run() {
            if (!isRunning || getIndeterminateDrawable() == null)
                return;
            getIndeterminateDrawable().setTint(colorSequence[currentColorIndex]);
            currentColorIndex = (currentColorIndex + 1) % colorSequence.length;

            handler.postDelayed(this, colorChangeInterval);
        }
    };

    public void stopColorCycle() {
        isRunning = false;
        handler.removeCallbacks(colorChanger);
    }

    public void setColorSequence(int[] colors) {
        this.colorSequence = colors;
        currentColorIndex = 0;
    }
    private void startColorCycle() {
        isRunning = true;
        handler.postDelayed(colorChanger, colorChangeInterval);
    }
    public void setColorChangeInterval(long intervalMillis) {
        this.colorChangeInterval = intervalMillis;
    }
    public void restartColorCycle() {
        stopColorCycle();
        currentColorIndex = 0;
        startColorCycle();
    }
    public void setState(ProgressState newState) {
        this.state = newState;
        updateUI();
    }
    private void updateUI() {
        stopColorCycle();
        setIndeterminate(false);
        switch (state) {
            case LOADING:
                setIndeterminate(true);
                startColorCycle();
                break;
            case SUCCESS:
                setProgressDrawableCompat(R.drawable.ic_check_circle);
                break;
            case ERROR:
                setProgressDrawableCompat(R.drawable.ic_error_outline);
                break;
            case IDLE:
            case DISABLED:
                setProgressDrawableCompat(R.drawable.ic_circle_gray);
                break;
        }
        invalidate();
        requestLayout();
    }
    private void setProgressDrawableCompat(int drawableResId) {
        Drawable d = ContextCompat.getDrawable(getContext(), drawableResId);
        if (d != null) {
            setProgressDrawable(d);
            android.util.Log.d("FancyProgressBar", "Drawable loaded: " + drawableResId);
        } else {
            android.util.Log.e("FancyProgressBar", "Failed to load drawable: " + drawableResId);
        }
    }



}
