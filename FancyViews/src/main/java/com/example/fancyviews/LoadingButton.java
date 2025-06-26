package com.example.fancyviews;
import static com.example.fancyviews.LoadingButton.ButtonState.DISABLED;
import static com.example.fancyviews.LoadingButton.ButtonState.ERROR;
import static com.example.fancyviews.LoadingButton.ButtonState.IDLE;
import static com.example.fancyviews.LoadingButton.ButtonState.SUCCESS;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import androidx.appcompat.widget.AppCompatButton;

public class LoadingButton extends FrameLayout {
    public enum ButtonState {
        IDLE, LOADING, SUCCESS, ERROR, DISABLED
    }
    private OnStateChangeListener onStateChangeListener;
    public void setOnStateChangeListener(OnStateChangeListener listener) {
        this.onStateChangeListener = listener;
    }

    // טקסטים לכל מצב
    private String textIdle = "שלח";
    private String textLoading = "טוען...";
    private String textSuccess = "✔ הצלחה";
    private String textError = "✖ שגיאה";
    private String textDisabled = "לא זמין";

    // אייקונים לכל מצב (nullable)
    private int iconSuccessRes = R.drawable.ic_check_circle;
    private int iconErrorRes = R.drawable.ic_error_outline;
    private int iconIdleRes = 0;
    private int iconLoadingRes = 0;
    private int iconDisabledRes = R.drawable.ic_circle_gray;

    // צבעי רקע לכל מצב
    private int bgColorIdle = 0xFF6200EE;
    private int bgColorLoading = 0xFFAAAAAA;
    private int bgColorSuccess = 0xFF4CAF50;
    private int bgColorError = 0xFFF44336;
    private int bgColorDisabled = 0xFFBDBDBD;

    // צבעי טקסט לכל מצב
    private int textColorIdle = 0xFFFFFFFF;
    private int textColorLoading = 0xFFFFFFFF;
    private int textColorSuccess = 0xFFFFFFFF;
    private int textColorError = 0xFFFFFFFF;
    private int textColorDisabled = 0xFFFFFFFF;


    private AppCompatButton button;
    private ProgressBar progressBar;
    private ButtonState state = IDLE;

    public LoadingButton(Context context) {
        super(context);
        init(context);
    }

    public LoadingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LoadingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        button = new AppCompatButton(context);
        button.setText("שלח");
        LayoutParams buttonParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        addView(button, buttonParams);

        progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);
        LayoutParams progressParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        progressParams.gravity = Gravity.CENTER;
        addView(progressBar, progressParams);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state == IDLE) {
                    setState(ButtonState.LOADING);
                }
            }
        });
    }

    public void setState(ButtonState newState) {
        this.state = newState;
        updateUI();
        if (onStateChangeListener != null) {
            onStateChangeListener.onStateChanged(newState);
        }
    }
    private void updateUI() {
        int iconRes = 0;
        String text = "";
        switch (state) {
            case IDLE:
                text = textIdle;
                iconRes = iconIdleRes;
                //button.setText("שלח");
                button.setEnabled(true);
                button.setAlpha(1f);
                progressBar.setVisibility(View.GONE);
                button.setBackgroundColor(bgColorIdle);
                button.setTextColor(textColorIdle);
                break;

            case LOADING:
                text = textLoading;
                iconRes = iconLoadingRes;
                //button.setText("טוען...");
                button.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                button.setBackgroundColor(bgColorLoading);
                button.setTextColor(textColorLoading);
                break;

            case SUCCESS:
                text = textSuccess;
                iconRes = iconSuccessRes;
                //button.setText("✔ הצלחה");
                button.setEnabled(false);
                progressBar.setVisibility(View.GONE);
                button.setBackgroundColor(bgColorSuccess);
                button.setTextColor(textColorSuccess);
                break;

            case ERROR:
                text = textError;
                iconRes = iconErrorRes;
                //button.setText("✖ שגיאה");
                button.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                button.setBackgroundColor(bgColorError);
                button.setTextColor(textColorError);
                break;

            case DISABLED:
                text = textDisabled;
                iconRes = iconDisabledRes;
                //button.setText("לא זמין");
                button.setEnabled(false);
                button.setAlpha(0.5f);
                progressBar.setVisibility(View.GONE);
                button.setBackgroundColor(bgColorDisabled);
                button.setTextColor(textColorDisabled);
                break;
        }
        button.setText(text);
        if (iconRes != 0) {
            button.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0);
        } else {
            button.setCompoundDrawables(null, null, null, null);
        }
    }
    public void setTextForState(ButtonState state, String text) {
        switch (state) {
            case IDLE: textIdle = text; break;
            case LOADING: textLoading = text; break;
            case SUCCESS: textSuccess = text; break;
            case ERROR: textError = text; break;
            case DISABLED: textDisabled = text; break;
        }
        updateUI(); // לשינוי בזמן ריצה
    }

    public void setIconForState(ButtonState state, int drawableResId) {
        switch (state) {
            case IDLE: iconIdleRes = drawableResId; break;
            case LOADING: iconLoadingRes = drawableResId; break;
            case SUCCESS: iconSuccessRes = drawableResId; break;
            case ERROR: iconErrorRes = drawableResId; break;
            case DISABLED: iconDisabledRes = drawableResId; break;
        }
        updateUI();
    }
    public void setBackgroundColorForState(ButtonState state, int color) {
        switch (state) {
            case IDLE: bgColorIdle = color; break;
            case LOADING: bgColorLoading = color; break;
            case SUCCESS: bgColorSuccess = color; break;
            case ERROR: bgColorError = color; break;
            case DISABLED: bgColorDisabled = color; break;
        }
        updateUI();
    }

    public void setTextColorForState(ButtonState state, int color) {
        switch (state) {
            case IDLE: textColorIdle = color; break;
            case LOADING: textColorLoading = color; break;
            case SUCCESS: textColorSuccess = color; break;
            case ERROR: textColorError = color; break;
            case DISABLED: textColorDisabled = color; break;
        }
        updateUI();
    }


}
