/**
 * Copyright (C) 2014-2016 CMCC All rights reserved
 */
package com.example.walkarround.base.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.example.walkarround.R;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: description
 * Date: 2016-05-19
 *
 * @author Richard
 */
public class RippleView extends View {

    private static final int INIT_CIRCLE_RADIUS_EXTEND_VALUE = 100;
    private static final int INIT_CIRCLE_RADIUS_STEP_VALUE = 2;
    private static final int CIRCLE_COUNT = 5;

    private Paint paint;
    private int maxRadius = 255;
    private int mMaxRadiusListSize;
    // 是否运行
    private boolean isStarting = false;
    private List<Integer> alphaList = new ArrayList<>();
    private List<Integer> radiusList = new ArrayList<>();

    public RippleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public RippleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init();
    }

    public RippleView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init();
    }

    private void init() {
        paint = new Paint();
        // 设置博文的颜色
        paint.setColor(getResources().getColor(R.color.ripple_line_cor));
        alphaList.add(255);// 圆心的不透明度
        radiusList.add(0);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        maxRadius = getWidth() / 2 - INIT_CIRCLE_RADIUS_EXTEND_VALUE;
        //mMaxRadiusListSize = maxRadius
        Log.d("RippleView", "getWidth() = " + getWidth() + ", maxWid = " + maxRadius);
        setBackgroundColor(Color.TRANSPARENT);// 颜色：完全透明
        // 依次绘制同心圆
        int alpha;
        int iNextAlpha = 0;
        for (int i = 0; i < alphaList.size(); i++) {
            alpha = alphaList.get(i);
            // 圆半径
            int startWidth = radiusList.get(i);

            //圆圈透明度
            if (i == 0 && (startWidth + (maxRadius / CIRCLE_COUNT)) >= maxRadius) {
                paint.setAlpha(0);
            } else {
                paint.setAlpha(alpha);
            }

            // 画出当前圆圈
            canvas.drawCircle(getWidth() / 2, getHeight() / 2,
                    startWidth + INIT_CIRCLE_RADIUS_EXTEND_VALUE,
                    paint);
            // 准备下一个圆圈参数：半径 + 透明度（递减）
            if (isStarting && alpha > 0) {
                if (startWidth < (maxRadius - INIT_CIRCLE_RADIUS_STEP_VALUE)) {
                    iNextAlpha = alpha - 1;
                } else if (startWidth >= (maxRadius - INIT_CIRCLE_RADIUS_STEP_VALUE)) {
                    iNextAlpha = 0;
                }
                alphaList.set(i, iNextAlpha);
                radiusList.set(i, startWidth + INIT_CIRCLE_RADIUS_STEP_VALUE);
                Log.d("RippleView", "Set next circle " + startWidth);
            }

            Log.d("RippleView", "alphaList i = " + i + ", startWidth = " + radiusList.get(i));
        }

        //半径永远都是偶数，必须确保每个圆圈间隔也是偶数值
        int iTemp = maxRadius / CIRCLE_COUNT;
        if(iTemp % 2 != 0) {
            iTemp += 1;
        }
        if (isStarting
                && radiusList.get(radiusList.size() - 1) == iTemp) {
            alphaList.add(255);
            radiusList.add(0);
        }

        // 同心圆数量达到Max，删除最外层圆
        if (isStarting && radiusList.size() == (CIRCLE_COUNT + 1)) {
            Log.d("RippleView", " Remove Ripple circle.");
            radiusList.remove(0);
            alphaList.remove(0);
        }
        // 刷新界面
        Log.d("RippleView", " Ripple invalidate.");
        invalidate();
    }

    // 执行动画
    public void start() {
        isStarting = true;
    }

    // 停止动画
    public void stop() {
        isStarting = false;
    }

    // 判断是都在不在执行
    public boolean isStarting() {
        return isStarting;
    }

}
