/**
 * Copyright (C) 2014-2015 CMCC All rights reserved
 */
package com.example.walkarround.message.activity;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.avos.avoscloud.AVAnalytics;
import com.example.walkarround.R;
import com.example.walkarround.message.util.EmojiParser;

import java.lang.reflect.Field;

/**
 * 消息放大查看
 * Date: 2015-10-16
 *
 * @author mashanshan
 */
public class PlaintTextDetailActivity extends Activity implements OnClickListener {

    public static final String INTENT_CONTENT = "display content";
    private static final int STATUS_BAR_DEFAULT_HEIGHT = 25;
    // 显示内容
    private String mDisplayContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mDisplayContent = savedInstanceState.getString(INTENT_CONTENT);
        } else {
            mDisplayContent = getIntent().getStringExtra(INTENT_CONTENT);
        }
        setContentView(R.layout.activity_plaint_text_detail);
        TextView content = (TextView) findViewById(R.id.content_tv);
        content.setOnClickListener(this);
        content.setText(EmojiParser.getInstance(this).addSmileySpans(mDisplayContent));

        // 设置高度，保证超出屏幕可以滚动，不满屏是可以居中
        DisplayMetrics dm = getResources().getDisplayMetrics();
        content.setMinHeight(dm.heightPixels - getStatusBarHeight());

        // 单行则屏幕居中，多行则上下居中显示
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(getResources().getDimension(R.dimen.font_size8));
        int viewWidth = dm.widthPixels - content.getPaddingLeft() - content.getPaddingRight();
        DynamicLayout textLayout = new DynamicLayout(EmojiParser.getInstance(this).addSmileySpans(mDisplayContent),
                textPaint, viewWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
        if (textLayout.getLineCount() > 1) {
            content.setGravity(Gravity.CENTER_VERTICAL);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AVAnalytics.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AVAnalytics.onPause(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(INTENT_CONTENT, mDisplayContent);
    }

    @Override
    public void onClick(View view) {
        finish();
    }

    /**
     * 获取状态栏高度
     *
     * @return 状态栏高度
     */
    private int getStatusBarHeight() {
        Rect frame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        if (frame.top > 0) {
            return frame.top;
        }
        // 反射方法获取高度
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            return getResources().getDimensionPixelSize(STATUS_BAR_DEFAULT_HEIGHT);
        }
    }
}
