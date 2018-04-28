/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.example.walkarround.myself.util;

import android.text.TextUtils;
import com.avos.avoscloud.AVGeoPoint;
import com.example.walkarround.Location.model.GeoData;
import com.example.walkarround.R;
import com.example.walkarround.base.WalkArroundApp;
import com.example.walkarround.util.CommonUtils;

import java.util.Calendar;

/**
 * Date: 2015-12-08
 *
 * @author Richard
 */
public class ProfileUtil {
    public enum GENDLE {
        MEN, //0
        FEMALE  //1
    };

    public static final String EDIT_ACTIVITY_START_TYPE = "edit_activity_start_type";
    //Profile field
    public static final int REG_TYPE_USER_NAME = 0; //string
    public static final int REG_TYPE_SIGNATURE = 1; //string
    public static final int REG_TYPE_PORTRAIT = 2; //pointer:??
    public static final int REG_TYPE_GENDER = 3; //number, 0: men, 1: female
    public static final int REG_TYPE_BIRTH_DAY = 4; //string
    public static final int REG_TYPE_MOBILE = 5; //string
    public static final int REG_TYPE_LOCATION = 6; //geo pointer

    //Profile key
    public static final String REG_KEY_USER_NAME = "username"; //string
    public static final String REG_KEY_SIGNATURE = "signature"; //string
    public static final String REG_KEY_PORTRAIT = "portrait"; //pointer:??
    public static final String REG_KEY_GENDER = "gender"; //String, 0: men, 1: female
    public static final String REG_KEY_BIRTH_DAY = "birthday"; //string
    public static final String REG_KEY_LOCATION = "location"; //geo pointer + addr information
    public static final String REG_KEY_LOCATION_EX = "location_ex"; //geo pointer
    public static final String REG_KEY_LOCATION_ADDR = "address"; //a sub element for location(latitude, longitude, addr)

    public static final String REG_GENDER_MEN = "0"; //String, 0: men, 1: female
    public static final String REG_GENDER_FEMALE = "1"; //String, 0: men, 1: female

    //Dynamic data
    public static final String DYN_DATA_GEO = REG_KEY_LOCATION;
    public static final String DYN_DATA_ONLINE_STATE = "onlineStatus";
    public static final String DYN_DATA_USER_ID = "userId";
    public static final String DYN_DATA_DATING_STATE = "datingStatus";

    public static AVGeoPoint geodataConvert2AVObj(GeoData data) {
        if(data == null) {
            return null;
        }

        return (new AVGeoPoint(data.getLatitude(), data.getLongitude()));
    }

    /**
     * 根据日期数据计算年龄
     * @param birth
     * @return
     */
    public static String getAgeByBirth(String birth) {

        if(TextUtils.isEmpty(birth)) {
            return null;
        }

        //获取当前系统时间
        Calendar cal = Calendar.getInstance();

        //取出系统当前时间的年、月、日部分
        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH);
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);

        //解析生日数据
        String[] birthDate = birth.split("-");
        if(birthDate == null || birthDate.length != 3) {
            return null;
        }

        //取出出生日期的年、月、日部分
        int yearBirth = Integer.parseInt(birthDate[0]);
        int monthBirth = Integer.parseInt(birthDate[1]);
        int dayOfMonthBirth = Integer.parseInt(birthDate[2]);

        //当前年份与出生年份相减，初步计算年龄
        int ageInt = yearNow - yearBirth;
        //当前月份与出生日期的月份相比，如果月份小于出生月份，则年龄上减1，表示不满多少周岁
        if (monthNow <= monthBirth) {
            //如果月份相等，在比较日期，如果当前日，小于出生日，也减1，表示不满多少周岁
            if (monthNow == monthBirth) {
                if (dayOfMonthNow < dayOfMonthBirth) ageInt--;
            }else{
                ageInt--;
            }
        }

        //非法数据
        if(ageInt <= 0) {
            return null;
        }

        return Integer.toString(ageInt);
    }

    public static String getGenderDisplayName(String value) {

        if(TextUtils.isEmpty(value)) {
            return "";
        }

        if(value.equalsIgnoreCase(CommonUtils.PROFILE_GENDER_MEN)) {
            return WalkArroundApp.getInstance().getResources().getString(R.string.gender_men);
        } else {
            return WalkArroundApp.getInstance().getResources().getString(R.string.gender_female);
        }
    }

}
