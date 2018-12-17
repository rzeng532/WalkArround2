package com.example.walkarround.base.view;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import com.example.walkarround.R;

/**
 * 默认为走完进度自动 dismiss dialog,可调用setAutoDismiss(boolean ) 更改
 * Created on 2015/8/3.
 */
public class ProgressDialogHorizontal extends AlertDialog {

    private SeekBar mSeekbar;
    private int mMaxValue = -1;
    private String mMessage = "";
    private String mHint = "";
    private TextView mTitle;
    private TextView mIndicator;
    private TextView mHintText;
    private boolean mAutoDismiss = true;

    /**
     *
     * @param context
     */
    public ProgressDialogHorizontal(Context context) {
        super(context);
    }

    /**
     *
     * @param context
     * @param maxValue
     * @param message
     * @param hint
     */
    public ProgressDialogHorizontal(Context context, int maxValue, String message, String hint) {
        super(context);
        this.mMaxValue = maxValue;
        this.mMessage = message;
        this.mHint = hint;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.setContentView(R.layout.dialog_progress_horizonal);
        setCancelable(false);
        mSeekbar = (SeekBar) findViewById(R.id.seekbar);
        mSeekbar.setEnabled(false);
        mSeekbar.setFocusable(false);
        mSeekbar.setMax(mMaxValue);
        mTitle = (TextView) findViewById(R.id.title);
        if (mTitle != null && mMessage != null) {
            mTitle.setText(mMessage);
        }
        mHintText = (TextView) findViewById(R.id.hint_tv);
        if (mHintText != null && mHint != null) {
            mHintText.setText(mHint);
        }

        mIndicator = (TextView) findViewById(R.id.indicator);
    }

    public void setProgress(int progress) {
        if (null == mSeekbar || mMaxValue < 0) {
            return;
        }

        if (progress < 0) {
            progress = 0;
        }

        if (progress > mMaxValue) {
            progress = mMaxValue;
        }

        mSeekbar.setProgress(progress);

        if (mIndicator != null) {
            StringBuilder builder = new StringBuilder("(");
            builder.append(progress);
            builder.append("/");
            builder.append(mMaxValue);
            builder.append(")");

            mIndicator.setText(builder.toString());
        }

        if (progress == mMaxValue && mAutoDismiss) {
            dismiss();
        }
    }

    public void setMessage(String message) {
        if (mTitle != null && message != null) {
            mTitle.setText(message);
        }
    }

    public void setMax(int max) {
        if (mSeekbar != null) {
            mSeekbar.setMax(max);
            mMaxValue = max;
        }
    }

    public void setIndeterminate(boolean indeterminate) {
        mSeekbar.setIndeterminate(indeterminate);
    }

    public void setCancelable(boolean flag) {
        super.setCancelable(flag);
    }

    public void setHintTextView(String hintText) {
        if (mHintText != null && hintText != null) {
            mHintText.setText(hintText);
        }
    }

    public TextView getHintTextView() {
        return mHintText;
    }

    public void setIndicatorVisibility(int visibility) {
        mIndicator.setVisibility(visibility);
    }

    public void setAutoDismiss(boolean autoDismiss) {
        mAutoDismiss = autoDismiss;
    }
}
