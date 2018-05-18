package com.awalk.walkarround.base.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.awalk.walkarround.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * 选择时间View
 * Date: 2015-02-25
 *
 * @author mss
 */
public class DatePickerView extends LinearLayout implements NumericPickerWheelView.onSelectListener {

    public final static String TIME_FORMAT = "yyyy-MM-dd HH:mm";
    // 时间相关常量
    private final static int MONTH = 12;
    private final static String[] MONTHES_BIG = { "1", "3", "5", "7", "8", "10", "12" };
    private final static String[] MONTHES_LITTLE = { "4", "6", "9", "11" };
    private final static int BIG_MONTHES = 31;
    private final static int LITTLE_MONTHES = 30;
    private final static int HOUR = 24;
    private final static int MINUTE = 60;

    private static int mStartYear = 1990, mEndYear = 2100;
    /* 最小可设置的时间 */
    private Calendar mMinimDate;

    private NumericPickerWheelView mYearView;
    private NumericPickerWheelView mMonthView;
    private NumericPickerWheelView mDayView;
    private NumericPickerWheelView mHourView;
    private NumericPickerWheelView mMinuteView;

    public DatePickerView(Context context) {
        super(context);
        initView();
    }

    public DatePickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public DatePickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    /**
     * 初始化View
     */
    private void initView() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(infService);
        View view = inflater.inflate(R.layout.time_pick_view, this, true);

        // 年月日字体大小
        int SELECTED_TEXT_SIZE = getResources().getDimensionPixelSize(R.dimen.font_size3);
        int NORMAL_TEXT_SIZE = getResources().getDimensionPixelSize(R.dimen.font_size5);

        mYearView = (NumericPickerWheelView) view.findViewById(R.id.year_npv);
        mYearView.setData(generateNumStrList(mStartYear, mEndYear), SELECTED_TEXT_SIZE, NORMAL_TEXT_SIZE);
        mYearView.setOnSelectListener(this);

        mMonthView = (NumericPickerWheelView) view.findViewById(R.id.month_npv);
        mMonthView.setData(generateNumStrList(1, MONTH), SELECTED_TEXT_SIZE, NORMAL_TEXT_SIZE);
        mMonthView.setOnSelectListener(this);

        mDayView = (NumericPickerWheelView) view.findViewById(R.id.day_npv);
        mDayView.setData(generateNumStrList(1, BIG_MONTHES), SELECTED_TEXT_SIZE, NORMAL_TEXT_SIZE);

        // 时分字体大小
        int HOUR_SELECTED_TEXT_SIZE = getResources().getDimensionPixelSize(R.dimen.font_size8);
        int HOUR_NORMAL_TEXT_SIZE = SELECTED_TEXT_SIZE;

        mHourView = (NumericPickerWheelView) view.findViewById(R.id.hour_npv);
        mHourView.setData(generateNumStrList(0, HOUR - 1), HOUR_SELECTED_TEXT_SIZE, HOUR_NORMAL_TEXT_SIZE);

        mMinuteView = (NumericPickerWheelView) view.findViewById(R.id.minute_npv);
        mMinuteView.setData(generateNumStrList(0, MINUTE - 1), HOUR_SELECTED_TEXT_SIZE, HOUR_NORMAL_TEXT_SIZE);

    }

    /**
     * 设置最小可设置的时间
     * 
     * @param minDate
     */
    public void setMinDate(long minDate) {
        mMinimDate = Calendar.getInstance();
        mMinimDate.setTimeInMillis(minDate);
        int oldYear = mStartYear;
        mStartYear = mMinimDate.get(Calendar.YEAR);
        reSetYearView(mStartYear, mEndYear);
        checkDateAfterMinDate(R.id.year_npv, Integer.toString(mStartYear), Integer.toString(oldYear));
    }

    /**
     * 设置初始显示的年月时分
     * 
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     */
    public void setDefaultDate(int year, int month, int day, int hour, int minute) {
        String oldYear = mYearView.getSelectedValue();
        String oldMonth = mMonthView.getSelectedValue();
        checkDateAfterMinDate(R.id.year_npv, Integer.toString(year), oldYear);
        checkDateAfterMinDate(R.id.month_npv, Integer.toString(month), oldMonth);

        int yearPos = year - mStartYear;
        mYearView.setSelectedPosition(yearPos);

        int startMonth = Integer.parseInt(mMonthView.getListStartValue());
        mMonthView.setSelectedPosition(month - startMonth);

        int startDay = Integer.parseInt(mDayView.getListStartValue());
        mDayView.setSelectedPosition(day - startDay);

        mHourView.setSelectedPosition(hour);
        mMinuteView.setSelectedPosition(minute);

    }

    /**
     * 获取当前选择的时间
     *
     * @return
     */
    public String getTime() {
        StringBuffer time = new StringBuffer();
        time.append(mYearView.getSelectedValue());
        time.append("-");
        String selectValue = mMonthView.getSelectedValue();
        if (selectValue.length() == 1) {
            time.append("0");
        }
        time.append(selectValue);
        time.append("-");
        selectValue = mDayView.getSelectedValue();
        if (selectValue.length() == 1) {
            time.append("0");
        }
        time.append(selectValue);
        time.append(" ");
        selectValue = mHourView.getSelectedValue();
        if (selectValue.length() == 1) {
            time.append("0");
        }
        time.append(selectValue);
        time.append(":");
        selectValue = mMinuteView.getSelectedValue();
        if (selectValue.length() == 1) {
            time.append("0");
        }
        time.append(selectValue);
        return time.toString();
    }

    @Override
    public void onSelect(View view, String text, String oldText) {
        switch (view.getId()) {
            case R.id.year_npv:
                // 年变更了
                checkDateAfterMinDate(R.id.year_npv, text, oldText);
                break;
            case R.id.month_npv:
                // 月份变更了
                checkDateAfterMinDate(R.id.month_npv, text, oldText);
                break;
            case R.id.day_npv:
                break;
            case R.id.hour_npv:
                break;
            case R.id.minute_npv:
                break;
            default:
                break;
        }
    }

    /**
     * 确保可选择的日期在setMinDate()设定范围内
     *
     * @param changViewId 年份或者是月份变更后
     * @param newValue    新设定值
     * @param oldValue    原设定值
     */
    private void checkDateAfterMinDate(int changViewId, String newValue, String oldValue) {
        if (mMinimDate == null || newValue == null || newValue.equals(oldValue)) {
            return;
        }
        if (R.id.year_npv == changViewId) {
            // 年份变更了
            int year = Integer.parseInt(newValue);
            int oldYear = Integer.parseInt(oldValue);
            int minimYear = mMinimDate.get(Calendar.YEAR);
            if (oldYear == minimYear && year > minimYear) {
                // 由最小设置年份切换到其他年份
                int minimMonth = mMinimDate.get(Calendar.MONTH) + 1;
                if (minimMonth != 1) {
                    reSetMonthView(1);
                }
                int month = Integer.parseInt(mMonthView.getSelectedValue());
                if (minimMonth == month || month == 2) {
                    // 当前月份是最小日期月份或者二月
                    int startDay = 1;
                    reSetDayView(year, month, startDay);
                }
            } else if (year == minimYear) {
                // 由其他年份切换最小设置年份
                int minimMonth = mMinimDate.get(Calendar.MONTH) + 1;
                if (minimMonth != 1) {
                    reSetMonthView(minimMonth);
                }

                int month = Integer.parseInt(mMonthView.getSelectedValue());
                if (minimMonth == month || month == 2) {
                    // 当前月份是最小日期月份或者二月
                    int startDay = minimMonth == month ? mMinimDate.get(Calendar.DAY_OF_MONTH) : 1;
                    reSetDayView(year, month, startDay);
                }
            } else {
                // 二月天数切换
                int month = Integer.parseInt(mMonthView.getSelectedValue());
                if (month == 2) {
                    int daysInMonth = getDaysInMonth(year, month);
                    int oldDaysInMonth = getDaysInMonth(oldYear, month);
                    if (daysInMonth == oldDaysInMonth) {
                        return;
                    }
                    reSetDayView(1, daysInMonth);
                }
            }
        } else if (R.id.month_npv == changViewId) {
            // 月份变更了
            int year = Integer.parseInt(mYearView.getSelectedValue());
            int minimYear = mMinimDate.get(Calendar.YEAR);
            int month = Integer.parseInt(newValue);
            int minimMonth = mMinimDate.get(Calendar.MONTH) + 1;
            if (year == minimYear && month == minimMonth) {
                // 最小可设置年月份
                int minimDay = mMinimDate.get(Calendar.DAY_OF_MONTH);
                int daysInMonth = getDaysInMonth(year, month);
                reSetDayView(minimDay, daysInMonth);
            } else {
                int startDay = Integer.parseInt(mDayView.getListStartValue());
                int daysInMonth = getDaysInMonth(year, month);
                if (startDay != 1) {
                    // 之前不是1开头，重新设置
                    reSetDayView(1, daysInMonth);
                } else {
                    int oldMonth = Integer.parseInt(oldValue);
                    int oldDaysInMonth = getDaysInMonth(year, oldMonth);
                    if (daysInMonth != oldDaysInMonth) {
                        // 两个月份日期天数不一样
                        reSetDayView(1, daysInMonth);
                    }
                }
            }
        }
    }

    /**
     * 重新设定可选择年份范围
     * 
     * @param startYear
     * @param endYear
     */
    private void reSetYearView(int startYear, int endYear) {
        List<String> year = generateNumStrList(startYear, endYear);
        int selectYear = Integer.parseInt(mYearView.getSelectedValue());
        int selectedMonth = selectYear - startYear;
        selectedMonth = selectedMonth > 0 ? selectedMonth : 0;
        mYearView.reSetData(year, selectedMonth);
    }

    /**
     * 重新设定可选择月份范围
     * 
     * @param startMonth
     */
    private void reSetMonthView(int startMonth) {
        List<String> month = generateNumStrList(startMonth, MONTH);
        int selectMonth = Integer.parseInt(mMonthView.getSelectedValue());
        int selectedMonth = selectMonth - startMonth;
        selectedMonth = selectedMonth > 0 ? selectedMonth : 0;
        mMonthView.reSetData(month, selectedMonth);
    }

    /**
     * 重新设定可选择日期范围
     * 
     * @param year
     * @param month
     * @param startDay
     */
    private void reSetDayView(int year, int month, int startDay) {
        int endDay = getDaysInMonth(year, month);
        reSetDayView(startDay, endDay);
    }

    /**
     * 重新设定可选择日期范围
     * 
     * @param startDay
     * @param endDay
     */
    private void reSetDayView(int startDay, int endDay) {
        List<String> day = generateNumStrList(startDay, endDay);
        int selectDay = Integer.parseInt(mDayView.getSelectedValue());
        int selectedDay = selectDay - startDay;
        selectedDay = selectedDay > 0 ? selectedDay : 0;
        mDayView.reSetData(day, selectedDay);
    }

    /**
     * 获取某年某月有多少天
     * 
     * @param year
     * @param month
     * @return
     */
    private int getDaysInMonth(int year, int month) {
        int days;
        final List<String> list_big = Arrays.asList(MONTHES_BIG);
        final List<String> list_little = Arrays.asList(MONTHES_LITTLE);
        if (list_big.contains(String.valueOf(month))) {
            days = BIG_MONTHES;
        } else if (list_little.contains(String.valueOf(month))) {
            days = LITTLE_MONTHES;
        } else {
            if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
                days = 29;
            } else {
                days = 28;
            }
        }
        return days;
    }

    /**
     * 生成连续数字列表
     * 
     * @param startValue
     * @param endValue
     * @return
     */
    private List<String> generateNumStrList(int startValue, int endValue) {
        List<String> strList = new ArrayList<String>();
        for (int i = startValue; i <= endValue; i++) {
            strList.add(Integer.toString(i));
        }
        return strList;
    }
}
