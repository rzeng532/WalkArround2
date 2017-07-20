/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.example.walkarround.util;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Environment;
import android.text.TextUtils;
import com.example.walkarround.R;
import com.example.walkarround.base.WalkArroundApp;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Date: 2015-11-26
 *
 * @author Richard Zeng
 *
 * It is a UTIL class to provide some static method for common usage.
 */

public class CommonUtils {

    public static final int ACTIVITY_FINISH_NORMAL_FINISH = 100;
    public static final int ACTIVITY_FINISH_BACK = 101;

    public static final String PROFILE_GENDER_MEN = "0";
    public static final String PROFILE_GENDER_FEMALE = "1";

    // 检查手机号是否有效
    public static boolean validatePhoneNum(String phoneNum) {
        // return phoneNum.matches("^(\\+86)?1[0-9]{10}$");
        Pattern p = null;
        Matcher m = null;
        boolean b = false;
        p = Pattern.compile("13\\d{9}|14[57]\\d{8}|15[012356789]\\d{8}|18[012356789]\\d{8}|17[0678]\\d{8}");
//        p = Pattern.compile("^(\\+86)?[0-9]{3,20}$");
        m = p.matcher(phoneNum);
        b = m.matches();
        return b;
    }

    public static boolean isNoticeNum(String phoneNum) {
        if (TextUtils.isEmpty(phoneNum)) {
            return false;
        }

        String newNum = phoneNum.replaceAll(" ", "");
        newNum = newNum.replaceAll("-", "");
        newNum = newNum.replaceAll("\t", "");

        final String[] PATTERN_STR = new String[]{"^(106)[0-9]+$", "^(100)[0-9]{2}$", "^(1118)[3,5]$", "^(11)[0,9]$",
                "^(12)[0,2]$", "^(123)[0-9]{2}$", "^(95)[0-9]{3}$", "^(96)[0-9]{3,4}$"};
        boolean isMatch = false;
        for (int i = 0; i < PATTERN_STR.length; i++) {
            Pattern pattern = Pattern.compile(PATTERN_STR[i]);
            if (pattern.matcher(newNum).matches()) {
                isMatch = true;
                break;
            }
        }
        return isMatch;
    }

    // 将手机号转换为+86开头的格式
    public static String formatPhoneNum(String phoneNum) {
        if (TextUtils.isEmpty(phoneNum)) {
            return "";
        } else if (!phoneNum.startsWith("+86") && !phoneNum.startsWith("%2b86") && !phoneNum.startsWith("%2B86")) {
            return "+86" + phoneNum;
        } else {
            return phoneNum;
        }
    }

    //将手机号转换为0086开头的格式
    public static String format0086PhoneNum(String phoneNum) {
        if (TextUtils.isEmpty(phoneNum)) {
            return "";
        } else if (!phoneNum.startsWith("0086")) {
            return "0086" + phoneNum;
        } else {
            return phoneNum;
        }
    }

    /**
     * 返回系统格式的号码，如+86 xxx xxxx xxxx
     *
     * @param number
     * @return
     */
    public static String getAndroidFormatNumber(String number) {
        if (TextUtils.isEmpty(number)) {
            return number;
        }

        number = number.replaceAll(" ", "");

        if (number.startsWith("+86")) {
            number = number.substring(3);
        }

        if (number.length() != 11) {
            return number;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("+86 ");
        builder.append(number.substring(0, 3));
        builder.append(" ");
        builder.append(number.substring(3, 7));
        builder.append(" ");
        builder.append(number.substring(7));
        return builder.toString();
    }

    public static String getPhoneNum(String formatedPhoneNum) {
        if (TextUtils.isEmpty(formatedPhoneNum)) {
            return "";
        } else {
            if (formatedPhoneNum.startsWith("+86")) {
                formatedPhoneNum = formatedPhoneNum.substring(3, formatedPhoneNum.length());

            } else if (formatedPhoneNum.startsWith("0086")) {
                formatedPhoneNum = formatedPhoneNum.substring(4, formatedPhoneNum.length());

            } else if (formatedPhoneNum.startsWith("+0086")) {
                formatedPhoneNum = formatedPhoneNum.substring(5, formatedPhoneNum.length());
            }

            return formatedPhoneNum.replaceAll(" ", "").replaceAll(" \t", "");
        }
    }

    // 检查两个手机号是否相等
    public static boolean isPhoneNumSame(String numA, String numB) {
        boolean isSame = false;

        if (validatePhoneNum(numA) && validatePhoneNum(numB)) {
            String formatNumA = formatPhoneNum(numA);
            String formatNumB = formatPhoneNum(numB);
            if (formatNumA.equals(formatNumB)) {
                isSame = true;
            } else {
                isSame = false;
            }
        }

        return isSame;
    }

    /**
     * Return a localized string from the application's package's default string table.
     *
     * @param name The name of string
     */
//    public static String getStringResByName(String name) {
//        try {
//            Application application = RCSApp.getInstance();
//            ApplicationInfo appInfo = application.getApplicationInfo();
//            int resId = application.getResources().getIdentifier(name, "string", appInfo.packageName);
//            String str = application.getString(resId);
//            return str == null ? "" : str;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "";
//        }
//    }
//
//    public static String getErrorMessage(int errorCode) {
//        String msg = getStringResByName("error_" + errorCode) + "(" + errorCode + ")";
//        if (TextUtils.isEmpty(msg)) {
//            msg = String.format(getStringResByName("error"), errorCode);
//        }
//        return msg;
//    }

    /**
     * Get the millisecond from serverTime
     *
     * @param time       the string of time
     * @param timeFormat the format of time such as "yyyy-MM-dd HH:mm:ss"
     * @return
     */
    public static long parseTime(String time, String timeFormat) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(timeFormat, Locale.getDefault());
            Date date = sdf.parse(time);
            return date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return System.currentTimeMillis();
        }
    }

    public static String getCurrentTime(String timeFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(timeFormat, Locale.getDefault());
        Date date = new Date();
        return sdf.format(date);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static String getJsonStringValue(String key, String defaultValue, JSONObject data) {
        try {
            return data.getString(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean getJsonBooleanValue(String key, boolean defaultValue, JSONObject data) {
        try {
            return data.getBoolean(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static int getJsonIntValue(String key, int defaultValue, JSONObject data) {
        try {
            return data.getInt(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 将lookupKey里面包含的key分离出，拼接成一个改变key颜色的html字符串
     *
     * @param key       输入需要搜索的key
     * @param lookupKey ContactInfo类里面包含的key
     * @param color     需要改变的颜色
     * @return 拼接的html字符串
     */
    public static String getSearchTextHtml(Context context, String key, String lookupKey, int color) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<html><body>");
        int startPos = lookupKey.indexOf(key);
        int endPos = startPos + key.length();
        if (startPos > -1) {
            stringBuilder.append(lookupKey.substring(0, startPos));
            stringBuilder.append("<font color=" + context.getResources().getColor(color) + ">");
            stringBuilder.append(key);
            stringBuilder.append("</font>");
            stringBuilder.append(lookupKey.substring(endPos));
        } else {
            stringBuilder.append(lookupKey);
        }
        stringBuilder.append("</body></html>");
        return stringBuilder.toString();
    }

    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    public static long stringToLong(String strTime, String formatType) {
        Date date = null;
        try {
            date = stringToDate(strTime, formatType);
        } catch (ParseException e) {
            e.printStackTrace();
        } // String类型转成date类型
        if (date == null) {
            return 0;
        } else {
            long currentTime = dateToLong(date); // date类型转成long类型
            return currentTime;
        }
    }

    public static Date stringToDate(String strTime, String formatType) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;

        date = formatter.parse(strTime);
        return date;
    }

    public static long dateToLong(Date date) {
        return date.getTime();
    }

    /**
     * check mail validity
     *
     * @param email
     * @return
     */
    public static boolean checkEmail(String email) {
        String format = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        if (email.matches(format)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * check phone number validity
     *
     * @param
     * @return
     */
    public static boolean checkPhoneNumber(String phoneNumber) {
        String format = "^[0-9/#/*]*$";
        if (phoneNumber.matches(format)) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * @param str
     * @param split 切割符
     * @return
     * @方法名：stringToList
     * @描述：字符串转化成list
     * @输出：List
     * @作者：Administrator
     */
    public static ArrayList<String> stringToList(String str, String split) {
        ArrayList<String> list = new ArrayList<String>();
        try {
            String[] strs = str.split(split);

            for (String s : strs) {
                list.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static class ImageGetter implements android.text.Html.ImageGetter {

        private Context mContext;

        public ImageGetter(Context context) {
            mContext = context;
        }

        @Override
        public Drawable getDrawable(String source) {
            Drawable d;
            try {
                int id = Integer.parseInt(source);
                d = mContext.getResources().getDrawable(id);
                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            } catch (Exception e) {
                return null;
            }
            return d;
        }
    }

    /**
     * 将long类型的time 转化成format的string
     */
    public static String parseTime(long date, String dateFormater) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormater, Locale.getDefault());
        Date newdate = new Date(date);
        String time = dateFormat.format(newdate);
        return time;
    }

    /**
     * 根据名字获取头像背景颜色值
     *
     * @return
     */
    public static int switchNameToColor(Context context, String name) {
        int[] BACKGROUND_COLOR = context.getResources().getIntArray(R.array.logo_name_color);

        // if (TextUtils.isEmpty(name)) {
        // int random = (int) (Math.random() * BACKGROUND_COLOR.length);
        // return random >= BACKGROUND_COLOR.length ? 0 : random;
        // }

        char[] nameChars = name.toCharArray();
        char nameChar = nameChars[nameChars.length - 1];
        int nameCharIndex = nameChar - 'a';
        nameCharIndex = nameCharIndex > 0 ? nameCharIndex : -nameCharIndex;

        return BACKGROUND_COLOR[nameCharIndex % BACKGROUND_COLOR.length];
    }

    /**
     * 序列化对象转化为byte[]
     *
     * @param obj
     * @return
     * @throws Exception
     */
    public static byte[] getBytesFromObject(Serializable obj) throws Exception {
        if (obj == null) {
            return null;
        }
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bo);
        oo.writeObject(obj);
        return bo.toByteArray();
    }

    public static boolean checkUserName(String userName) {
        if (!TextUtils.isEmpty(userName))
            return userName.matches("[0-9A-Za-z_]{1,50}$");
        else
            return false;
    }

    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    //Log.i("后台", appProcess.processName);
                    return true;
                } else {
                    //Log.i("前台", appProcess.processName);
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 获取拍照图片路径
     *
     * @return
     */
    public static String createCameraTakePicFile() {
        StringBuilder fileName = new StringBuilder();
        fileName.append(WalkArroundApp.MTC_DATA_PATH);
        fileName.append(AppConstant.CAMERA_TAKE_PIC_PATH);
        File folder = new File(fileName.toString());
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                return null;
            }
        }
        // 指定照片路径
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        fileName.append("IMG_").append(timeStamp).append(".jpg");
        return fileName.toString();
    }

    public static boolean hasSdcard() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 根据名字获取头像背景资源id
     *
     * @param name
     * @return
     */
    public static int getPhotoBgResId(String name) {
        if (TextUtils.isEmpty(name)) {
            return 0;
        }
        char[] nameChars = name.toCharArray();
        char nameChar = nameChars[nameChars.length - 1];
        int nameCharIndex = nameChar - 'a';
        nameCharIndex = nameCharIndex > 0 ? nameCharIndex : -nameCharIndex;
        int resId = R.drawable.photo_bg_color0;
        switch (nameCharIndex) {
            case 0:
                resId = R.drawable.photo_bg_color0;
                break;
            case 1:
                resId = R.drawable.photo_bg_color1;
                break;
            case 2:
                resId = R.drawable.photo_bg_color2;
                break;
            case 3:
                resId = R.drawable.photo_bg_color3;
                break;
            default:
                break;
        }
        return resId;
    }

    /**
     * Calculate distance between (lat_1, long_1) and (lat_2, long_2) and return kilometers
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results=new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }

    /**
     * Return a string by input distance meter.
     * @param distance, original unit is meter.
     * @return
     */
    public static String getDistanceStr(int distance) {
        String disanceStr = null;

        if(distance < 0) {
            return "";
        }

        if(distance < 1000) {
            disanceStr = distance + WalkArroundApp.getInstance().getResources().getString(R.string.common_distance_unit_meter);
        } else {
            disanceStr = (distance / 1000) + WalkArroundApp.getInstance().getResources().getString(R.string.common_distance_unit_kilometer);
        }

        return disanceStr;
    }
}
