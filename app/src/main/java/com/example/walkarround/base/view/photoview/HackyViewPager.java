package com.example.walkarround.base.view.photoview;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Richard on 2015/12/12.
 */
public class HackyViewPager extends ViewPager {

    public HackyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HackyViewPager(Context context) {
        super(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

}
