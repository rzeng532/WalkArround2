package com.example.walkarround.util;

import android.content.Context;
import com.example.walkarround.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 格式化时间
 * Date: 2015-03-25
 *
 * @author mss
 */
public class TimeFormattedUtil {

    private static final String CALL_DATE_FORMAT = "yyyy-MM-dd HH:mm";
    private static final String DATE_FORMAT_YEAR_DAY = "yyyy-MM-dd";
    private static final String DATE_FORMAT_DAY = "MM-dd";
    private static final String DETAIL_DATE_FORMAT_DAY = "MM-dd HH:mm";
    private static final String DATE_FORMAT_TODAY = "HH:mm";

    /**
     * 时间显示字符转换：今天的显示为小时和分钟，昨天的显示为“昨天”，
     * 昨天之前的显示日期
     *
     * @param time
     * @return
     */
    public static String getListDisplayTime(Context context, String time) {
        Date date;
        try {
            date = CommonUtils.stringToDate(time, CALL_DATE_FORMAT);
        } catch (ParseException e) {
            return null;
        }
        long callTime = date.getTime();
        return getListDisplayTime(context, callTime);
    }

    /**
     * 时间显示字符转换：今天的显示为小时和分钟，昨天的显示为“昨天”，
     * 昨天之前的显示日期，不是今年显示年月日
     *
     * @param time
     * @return
     */
    public static String getListDisplayTime(Context context, long time) {
        String display = "";
        int tMin = 60 * 1000;
        int tHour = 60 * tMin;
        int tDay = 24 * tHour;

        if (time > 0) {
            try {
                Date tDate = new Date(time);
                Date today = new Date();
                SimpleDateFormat thisYearDf = new SimpleDateFormat("yyyy");
                SimpleDateFormat todayDf = new SimpleDateFormat("yyyy-MM-dd");
                Date thisYear = new Date(thisYearDf.parse(thisYearDf.format(today)).getTime());
                Date yesterday = new Date(todayDf.parse(todayDf.format(today)).getTime());
                Date beforeYes = new Date(yesterday.getTime() - tDay);
                if (tDate != null) {
                    if (tDate.before(thisYear)) {
                        display = new SimpleDateFormat("yyyy-MM-dd").format(tDate);
                    } else {
                        if (tDate.after(yesterday)) {
                            display = new SimpleDateFormat("HH:mm").format(tDate);
                        } else {
                            if (tDate.after(beforeYes)) {
                                display = "昨天";
                            } else {
                                display = new SimpleDateFormat("MM-dd").format(tDate);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return display;
    }

    /**
     * 时间显示字符转换：今天的显示为小时和分钟，昨天的显示为“昨天 小时和分钟”，
     * 昨天之前的显示日期 小时和分钟
     *
     * @param time
     * @return
     */
    public static String getDetailDisplayTime(Context context, String time) {
        Date date;
        try {
            date = CommonUtils.stringToDate(time, CALL_DATE_FORMAT);
        } catch (ParseException e) {
            return null;
        }
        long callTime = date.getTime();
        return getDetailDisplayTime(context, callTime);
    }

    /**
     * 时间显示字符转换：今天的显示为小时和分钟，昨天的显示为“昨天 小时和分钟”，
     * 昨天之前的显示日期 小时和分钟
     *
     * @param time
     * @return
     */
    public static String getDetailDisplayTime(Context context, long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        Calendar callTime = Calendar.getInstance();
        callTime.setTimeInMillis(time);

        String displayDate;
        if (callTime.after(calendar)) {
            SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_TODAY, Locale.getDefault());
            displayDate = format.format(time);
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            if (callTime.after(calendar)) {
                displayDate = context.getString(R.string.msg_time_yestoday);
                SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_TODAY, Locale.getDefault());
                displayDate += format.format(time);
            } else {
                SimpleDateFormat format = new SimpleDateFormat(DETAIL_DATE_FORMAT_DAY, Locale.getDefault());
                displayDate = format.format(time);
            }
        }
        return displayDate;
    }

}
