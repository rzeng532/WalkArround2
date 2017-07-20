package com.example.walkarround.base.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.example.walkarround.R;

public class EditPasswordView extends EditText {
//    private Drawable mRightDrawable;
    private Drawable mShowDrawable;
    private Drawable mNoShowDrawable;
    private boolean isShowPassword;

    public EditPasswordView(Context context) {
        this(context, null);
    }

    public EditPasswordView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditPasswordView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomPassword);
        mShowDrawable = a.getDrawable(R.styleable.CustomPassword_showPass);
        mNoShowDrawable =a.getDrawable(R.styleable.CustomPassword_noShowPass);
        a.recycle();
        init();
    }

    private void init() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        setClearDrawableVisible(false);

        super.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Log.e("EditTextWithDel", "afterTextChanged---------------------text lenght:" + getText().toString().length());
            }
        });

        super.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            }
        });

    }

    public void addTextChangedListener(final TextWatcher watcher) {
        super.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                watcher.onTextChanged(s, start, before, count);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                watcher.beforeTextChanged(s, start, count, after);
            }

            @Override
            public void afterTextChanged(Editable s) {
                watcher.afterTextChanged(s);
                ////Log.e("EditTextWithDel", "afterTextChanged---------------------text lenght:" + getText().toString().length());
            }
        });
    }

    @Override
    public void setOnFocusChangeListener(final OnFocusChangeListener listener) {
        super.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                listener.onFocusChange(v, hasFocus);

            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_UP:

                boolean isClean = (event.getX() > (getWidth() - getTotalPaddingRight())) && (event.getX() < (getWidth() - getPaddingRight()));
                if(isClean && isShowPassword) {
                    setClearDrawableVisible(false);
                    setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    CharSequence text = getText();
                    if(text!=null && text.length()!=0) {
                        Spannable spanText = (Spannable)text;
                        Selection.setSelection(spanText,text.length());
                    }
                }else if(isClean && !isShowPassword){
                    setClearDrawableVisible(true);
                    setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    CharSequence text = getText();
                    if(text!=null && text.length()!=0) {
                        Spannable spanText = (Spannable)text;
                        Selection.setSelection(spanText,text.length());
                    }
                }
                break;

            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private void setClearDrawableVisible(boolean isVisible) {
        Drawable rightDrawable = null;
        if(isVisible) {
            rightDrawable = mNoShowDrawable;
            isShowPassword = true;
        }else{
            rightDrawable = mShowDrawable;
            isShowPassword = false;
        }
        rightDrawable.setBounds(0,0,rightDrawable.getMinimumWidth(),rightDrawable.getMinimumHeight());

        setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1], rightDrawable, getCompoundDrawables()[3]);
    }

}
