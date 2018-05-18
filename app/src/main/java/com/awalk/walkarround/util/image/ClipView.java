/**
 * Copyright (C) 2014-2017 All rights reserved
 */
package com.awalk.walkarround.util.image;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ClipView extends View {
    public static final int BORDERDISTANCE = 0;
    private Paint mPaint;

    public ClipView(Context context) {
        this(context, (AttributeSet) null);
    }

    public ClipView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClipView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mPaint = new Paint();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = this.getWidth();
        int height = this.getHeight();
        int borderlength = width - 0;
        this.mPaint.setColor(0x80000000);
        canvas.drawRect(0.0F, 0.0F, (float) width, (float) ((height - borderlength) / 2), this.mPaint);
        canvas.drawRect(0.0F, (float) ((height + borderlength) / 2), (float) width, (float) height, this.mPaint);
        canvas.drawRect(0.0F, (float) ((height - borderlength) / 2), 0.0F, (float) ((height + borderlength) / 2), this.mPaint);
        canvas.drawRect((float) (borderlength + 0), (float) ((height - borderlength) / 2), (float) width, (float) ((height + borderlength) / 2), this.mPaint);
        this.mPaint.setColor(-1);
        this.mPaint.setStrokeWidth(2.0F);
        canvas.drawLine(0.0F, (float) ((height - borderlength) / 2), (float) (width - 0), (float) ((height - borderlength) / 2), this.mPaint);
        canvas.drawLine(0.0F, (float) ((height + borderlength) / 2), (float) (width - 0), (float) ((height + borderlength) / 2), this.mPaint);
        canvas.drawLine(0.0F, (float) ((height - borderlength) / 2), 0.0F, (float) ((height + borderlength) / 2), this.mPaint);
        canvas.drawLine((float) (width - 0), (float) ((height - borderlength) / 2), (float) (width - 0), (float) ((height + borderlength) / 2), this.mPaint);
    }
}
