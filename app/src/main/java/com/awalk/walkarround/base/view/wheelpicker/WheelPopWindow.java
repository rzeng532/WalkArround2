package com.awalk.walkarround.base.view.wheelpicker;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import com.awalk.walkarround.R;
import com.awalk.walkarround.base.view.wheelpicker.core.AbstractWheelPicker;
import com.awalk.walkarround.base.view.wheelpicker.view.WheelCurvedPicker;

import java.util.List;

/**
 * 在此写用途
 *
 * @FileName: me.khrystal.widget.WheelPopWindow.java
 * @author: kHRYSTAL
 * @email: 723526676@qq.com
 * @date: 2016-01-13 12:33
 */
public class WheelPopWindow extends PopupWindow implements AbstractWheelPicker.OnWheelChangeListener {
    private View rootView;
    private View btnSubmit, btnCancel;
    private static final String TAG_SUBMIT = "submit";
    private static final String TAG_CANCEL = "cancel";
    private List<String> mData;
    private WheelCurvedPicker mPicker;
    private String currentData;

    public WheelPopWindow(Context context,List<String> data){
        super(context);
        mData = data;
        this.setWidth(ViewGroup.LayoutParams.FILL_PARENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setBackgroundDrawable(new BitmapDrawable());
        this.setOutsideTouchable(true);
        this.setAnimationStyle(R.style.timepopwindow_anim_style);
        LayoutInflater mLayoutInflater = LayoutInflater.from(context);
        rootView = mLayoutInflater.inflate(R.layout.pw_wheel, null);
        mPicker = (WheelCurvedPicker)rootView.findViewById(R.id.pop_wheel_curved);
        if (mPicker!=null){
            mPicker.setData(data);
            mPicker.setOnWheelChangeListener(this);
        }
        setContentView(rootView);

    }

    @Override
    public void onWheelScrolling(float deltaX, float deltaY) {

    }

    @Override
    public void onWheelSelected(int index, String data) {
        currentData = data;
    }

    @Override
    public void onWheelScrollStateChanged(int state) {

    }
}
