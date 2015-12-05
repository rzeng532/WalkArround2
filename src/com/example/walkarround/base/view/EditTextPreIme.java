package com.example.walkarround.base.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;


public class EditTextPreIme extends EditText {
    private onImeBackPressedListener mBackPressedListener;

    public EditTextPreIme(Context context) {
        super(context);
    }

    public EditTextPreIme(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextPreIme(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            // when the softinput display
            // 处理事件
            if (mBackPressedListener != null) {
                mBackPressedListener.onImeBackPressed();
            }
        }
        return super.dispatchKeyEventPreIme(event);
    }

    public void setOnImeBackPressedListener(onImeBackPressedListener listener) {
        mBackPressedListener = listener;
    }

    public interface onImeBackPressedListener {
        void onImeBackPressed();
    }
}