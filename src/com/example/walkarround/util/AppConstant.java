/**
 * Copyright (C) 2014-2015 CMCC All rights reserved
 */
package com.example.walkarround.util;

/**
 * TODO: description
 * Date: 2015-11-26
 *
 * @author Administrator
 */
public class AppConstant {
    // Logger switcher. true == output, false == NOT output.
    public static boolean LOG_OUTPUT = true;

    //APP LeanCloud values
    public static String LEANCLOUD_APP_ID = "emXwzWMkKKW0SsSFdunvKr9K"; //test
    public static String LEANCLOUD_APP_KEY = "WmSToMtVx4ijfo58VDEvBiDJ";//test

//    public static String LEANCLOUD_APP_ID = "nddk6udki7vg06j1w2gqli72t0q64hxivnf2zvxdzm8sef55"; //release;
//    public static String LEANCLOUD_APP_KEY = "qke60ogn5d7vpr4k3he9ykm9sibq6buifwxxrjkl2qytm480"; //release;

    public static final int MAX_IMAGE_LOADER_CACHE_SIZE = 50 * 1024 * 1024; //50MB

    public static final String CAMERA_TAKE_PIC_PATH = "/picture/";
    public static final String LOCATION_PIC_PATH = CAMERA_TAKE_PIC_PATH;
    public static final String APP_DATA_ROOT_PATH = "/com.example.prcs";

    // A switcher to enable message method sending map picture
    public static final boolean IS_ENABLE_MSG_LOCATION_PIC = true;

    //Entrance activity start target, int value start from 1000
    public static final String KEY_START_TARGET_ACTIVITY = "key_start_target_activity";
    public static final int START_INVALID_VALUE = -1;
    public static final int START_LOGIN_ACTIVITY = 1000;

    public static final int ACTIVITY_RETURN_CODE_CANCEL = 100;
    public static final int ACTIVITY_RETURN_CODE_OK = 101;
}
