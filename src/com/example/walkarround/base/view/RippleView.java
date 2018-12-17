/**
 * Copyright (C) 2014-2016 All rights reserved
 */
package com.example.walkarround.base.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import com.example.walkarround.R;
import com.example.walkarround.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: A public view for APP to display ripple UI result.
 * Date: 2016-05-19
 *
 * @author Richard
 */
public class RippleView extends View {

    private Logger MY_LOGGER = Logger.getLogger(RippleView.class.getSimpleName());

    private static final int INIT_CIRCLE_RADIUS_EXTEND_VALUE = 200;
    private int mInitCircleRadiusStep = 1;
    private int mInitCircleAlphaStep = 1;
    private static final int CIRCLE_COUNT = 6;

    private Paint paint;
    private int maxRadius = 0;
    private int mMaxRadiusListSize;
    // 是否运行
    private boolean isStarting = false;
    private List<Integer> alphaList = new ArrayList<>();
    private List<Integer> radiusList = new ArrayList<>();

    private int mInitAlphaValue = 255;

    //1/2 size of real width value
    private int mPortraitWidth = 0;

    private View mPortraitIv;
    private Handler mRedrawHandler =  new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    //MY_LOGGER.d("handleMessage 100 ");
                    invalidate();
                    break;
                default:
                    break;
            }
        }
    };

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
        // 设置颜色
        paint.setColor(getResources().getColor(R.color.ripple_line_cor));
        alphaList.add(mInitAlphaValue);// 圆心的不透明度
        radiusList.add(0);
    }

    /*
     * This method should be invoked before starting.
     */
    public void setInitColor(int colorResId) {
        if(isStarting == true || paint == null) {
            return;
        }

        paint.setColor(getResources().getColor(colorResId));
    }

    /*
     * Input parameters: 0xff (255) means transparent and 0 means NO-transparent.
     * Comments: this API should be invoked before start method.
     */
    public void setInitAlphaValue(int alpha) {
        mInitAlphaValue = alpha;

        if(alphaList != null && alphaList.size() >= 1) {
            alphaList.remove(alphaList.size() - 1);
            alphaList.add(mInitAlphaValue);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void initParamsBeforeDraw() {
        //首次绘制前更新相关UI尺寸
        mPortraitWidth = ((mPortraitIv != null && mPortraitIv.getWidth() != 0)
                            ? mPortraitIv.getWidth() : INIT_CIRCLE_RADIUS_EXTEND_VALUE) / 2;
        maxRadius = getWidth() / 2 - mPortraitWidth ;

        if(maxRadius <= 255) {

        } else if(maxRadius > 255 && maxRadius < 255 * 2) {
            int intervalValue = (maxRadius - 255);
            mPortraitWidth += intervalValue / 2;
        } else if(maxRadius >= 255 * 2) {
            //mPortraitWidth += 100;
            mInitCircleAlphaStep = 2;
            mInitCircleRadiusStep = 2;
        }

        if(radiusList != null && radiusList.size() > 0) {
            radiusList.remove(0);
            radiusList.add(0, mPortraitWidth);
        }

        //MY_LOGGER.d("mPortraitWidth = " + mPortraitWidth + ", maxRadius = " + maxRadius);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(maxRadius == 0) {
            initParamsBeforeDraw();
        }

        setBackgroundColor(Color.TRANSPARENT);// 颜色：完全透明
        // 依次绘制同心圆
        int alpha;
        int iNextAlpha = 0;
        for (int i = 0; i < alphaList.size(); i++) {
            alpha = alphaList.get(i);
            paint.setAlpha(alpha);

            // 画出当前圆圈
            canvas.drawCircle(getWidth() / 2, getHeight() / 2,
                    radiusList.get(i),
                    paint);
            // 准备下一个圆圈参数：半径 + 透明度（递减）
            if (isStarting && alpha > 0) {
                //透明度递减
                iNextAlpha = alpha - mInitCircleAlphaStep;
                if(iNextAlpha > 0) {
                    alphaList.set(i, iNextAlpha);
                } else {
                    alphaList.set(i, 0);
                }

                //半径递增
                //MY_LOGGER.d("radiusList.set = " + radiusList.get(i) + mInitCircleRadiusStep);
                radiusList.set(i, radiusList.get(i) + mInitCircleRadiusStep);
            }
        }

        // 同心圆数量达到Max，删除最外层圆
        if (isStarting && radiusList.size() == (CIRCLE_COUNT + 1)) {
            radiusList.remove(0);
            alphaList.remove(0);
        }

        //半径永远都是偶数，必须确保每个圆圈间隔也是偶数值
        int iTemp = maxRadius / CIRCLE_COUNT;
        if(iTemp % 2 != 0) {
            iTemp += 1;
        }
        if (isStarting
                && radiusList.get(radiusList.size() - 1) == (mPortraitWidth + iTemp)) {
            alphaList.add(mInitAlphaValue);
            radiusList.add(mPortraitWidth);
        }

        // 刷新界面
        if (isStarting) {
            mRedrawHandler.sendEmptyMessageDelayed(100, 10);
        }
    }

    // 从上一次结束的地方开始执行动画
    public void start() {
        if(isStarting) {
            return;
        }

        isStarting = true;
        invalidate();
    }

    //重新开始执行动画
    public void reStart() {
        radiusList.clear();
        radiusList.add(0);
        alphaList.clear();
        alphaList.add(mInitAlphaValue);

        isStarting = true;
    }

    /**
     * 该函数一定要在onStart之前完成
     * @param portrait
     */
    public void setInitRadiusByPortraitWidth(View portrait) {
        mPortraitIv = portrait;
    }

    // 停止动画
    public void stop() {
        isStarting = false;
    }

    // 判断是都在不在执行
    public boolean isStarting() {
        return isStarting;
    }

    private void setMaxRadius() {

    }
}
