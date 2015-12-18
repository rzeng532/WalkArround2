/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.example.walkarround.myself.util;

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
    public static final String REG_KEY_GENDER = "gender"; //number, 0: men, 1: female
    public static final String REG_KEY_BIRTH_DAY = "birthday"; //string
    public static final String REG_KEY_LOCATION = "location"; //geo pointer + addr information
    public static final String REG_KEY_LOCATION_EX = "location_ex"; //geo pointer
    public static final String REG_KEY_LOCATION_ADDR = "address"; //a sub element for location(latitude, longitude, addr)

}
