/**
 * Copyright (C) 2014-2016 All rights reserved
 */
package com.example.walkarround.myself.activity;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import com.example.walkarround.R;
import com.example.walkarround.base.view.wheelpicker.core.AbstractWheelPicker;
import com.example.walkarround.base.view.wheelpicker.view.WheelCrossPicker;
import com.example.walkarround.base.view.wheelpicker.widget.curved.WheelDayPicker;
import com.example.walkarround.base.view.wheelpicker.widget.curved.WheelMonthPicker;
import com.example.walkarround.base.view.wheelpicker.widget.curved.WheelYearPicker;

import java.util.Calendar;

/**
 * TODO: description
 * Date: 2016-11-09
 *
 * @author Administrator
 */
public class BirthdayDialog extends Dialog{
    private AbstractWheelPicker.OnWheelChangeListener wheelListener;
    private final WheelDayPicker wheelDay;
    private final WheelMonthPicker wheelMonth;
    private final WheelYearPicker wheelYear;

    private String year = "0";
    private String month = "0";
    private String day = "0";

    public BirthdayDialog(Context context) {
        super(context, R.style.dialog_noframe);

        setContentView(R.layout.dlg_birthday);
        wheelYear = (WheelYearPicker) findViewById(R.id.wheelYear);
        wheelMonth = (WheelMonthPicker) findViewById(R.id.wheelMonth);
        wheelDay = (WheelDayPicker) findViewById(R.id.wheelDay);
        wheelYear.setYearRange(1900, Calendar.getInstance().get(Calendar.YEAR));
        findViewById(R.id.tvConfirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        findViewById(R.id.tvCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        getWindow().setWindowAnimations(R.style.anim_bottom_dialog);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = context.getResources().getDisplayMetrics().widthPixels;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        getWindow().setAttributes(params);
    }

    public void setCurrentDate(int year,int month,int day){
        wheelYear.setCurrentYear(year);
        wheelMonth.setCurrentMonth(month);
        wheelDay.setCurrentDay(day);
    }

    public void setWheelListener(AbstractWheelPicker.OnWheelChangeListener listener){
        wheelListener = listener;
        initListener(wheelYear,0);
        initListener(wheelMonth,1);
        initListener(wheelDay,2);
    }

    private void initListener(final WheelCrossPicker picker, final int type) {
        picker.setOnWheelChangeListener(new AbstractWheelPicker.OnWheelChangeListener() {
            @Override
            public void onWheelScrolling(float deltaX, float deltaY) {
            }

            @Override
            public void onWheelSelected(int index, String data) {

                if (type == 0){ year = data;}

                if (type == 1){ month = data;}

                if (type == 2) {day = data;}
                if (!TextUtils.isEmpty(year) && !TextUtils.isEmpty(month) && !TextUtils.isEmpty(day)) {
                    if (type == 0 || type == 1){
                        wheelDay.setCurrentYearAndMonth(Integer.valueOf(year),
                                Integer.valueOf(month));
                    }
                    if (null != wheelListener){
                        wheelListener.onWheelSelected(-1, year + "-" + month + "-" + day);
                    }
                }
            }

            @Override
            public void onWheelScrollStateChanged(int state) {
            }
        });
    }

    public void setOnConfirmListener(View.OnClickListener listener){
        findViewById(R.id.tvConfirm).setOnClickListener(listener);
    }
}
