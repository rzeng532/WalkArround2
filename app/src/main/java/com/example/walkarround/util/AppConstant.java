/**
 * Copyright (C) 2014-2015 All rights reserved
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

    //Server table name
    public static String TABLE_LOCATION_INFOR = "Location";
    public static String TABLE_DYNAMIC_USER_DATA = "UserDynamicData";

    //APP LeanCloud values
    //public static String LEANCLOUD_APP_ID = "emXwzWMkKKW0SsSFdunvKr9K"; //test
    //public static String LEANCLOUD_APP_KEY = "WmSToMtVx4ijfo58VDEvBiDJ";//test

    public static String LEANCLOUD_APP_ID = "nddk6udki7vg06j1w2gqli72t0q64hxivnf2zvxdzm8sef55"; //release;
    public static String LEANCLOUD_APP_KEY = "qke60ogn5d7vpr4k3he9ykm9sibq6buifwxxrjkl2qytm480"; //release;

    public static final int MAX_IMAGE_LOADER_CACHE_SIZE = 50 * 1024 * 1024; //50MB

    public static final String CAMERA_TAKE_PIC_PATH = "/picture/";
    public static final String LOCATION_PIC_PATH = CAMERA_TAKE_PIC_PATH;
    public static final String APP_DATA_ROOT_PATH = "/com.example.prcs";

    /* 收藏信息文件存储位置 */
    public static final String FAVORITE_MSG_FILE_PATH = "/msg/collect/";

    /* 语音消息文件路径 */
    public static final String AUDIO_FILE_PATH = "/msg/audio/";

    /* 视频消息文件路径 */
    public static final String VIDEO_FILE_PATH = "/msg/video/";

    /* 发送地图时产生的位置图片 */

    /* 消息下载信息目录 */
    public static final String MSG_DOWNLOAD_PATH = "/msg/message/";

    /* 公众号消息下载信息目录 */
    public static final String PA_MSG_DOWNLOAD_PATH = "/msg/paMsg/";


    // A switcher to enable message method sending map picture
    public static final boolean IS_ENABLE_MSG_LOCATION_PIC = true;

    //Entrance activity start target, int value start from 1000
    public static final String KEY_START_TARGET_ACTIVITY = "key_start_target_activity";
    public static final int START_INVALID_VALUE = -1;
    public static final int START_LOGIN_ACTIVITY = 1000;
    public static final int START_MAIN_ACTIVITY = 1001;

    public static final int ACTIVITY_RETURN_CODE_CANCEL = 100;
    public static final int ACTIVITY_RETURN_CODE_OK = 101;

    /*
     * Key value for map location listener.
     */
    public static final long MAP_CONTINUE_LOC_INTERVAL = 3000;

    public static final String KEY_MAP_ASYNC_LISTERNER_MAIN = "main_activity_get_key";
    public static final String KEY_MAP_ASYNC_LISTERNER_SHOW_LOCATION = "show_location_activity_get_key";
    public static final String KEY_MAP_ASYNC_LISTERNER_SHOW_LOCATION_ONCLICK = "show_location_activity_get_key_onclick";

    public static final String KEY_MAP_ASYNC_LISTERNER_CONTINUE_LOC_DURING_WALK = "walkarround_activity_continue_to_loc";

    /**
     * Bugly APP ID
     */
    public static final String BUGLY_APP_ID = "ae216a3c1d";

    /**
     * 名字缩写长度
     */
    public static final int SHORTNAME_LEN = 7;

    public static final String PARAM_USR_OBJ_ID = "param_usr_obj_id";

    public static final String ANA_EVENT_LIKE = "like";
    public static final String ANA_EVENT_LOGIN = "login";
    public static final String ANA_EVENT_REGISTER = "register";
    public static final String ANA_EVENT_LOGOUT = "logout";
    public static final String ANA_EVENT_GEN_SMS = "gen_sms_code";
    public static final String ANA_EVENT_CHANGE_PORTRAIT = "change_portrait";
    public static final String ANA_EVENT_UPDATE_PSD = "update_password";
    public static final String ANA_EVENT_DISLIKE = "dislike";
    public static final String ANA_EVENT_MSG = "send_msg";
    public static final String ANA_EVENT_EVALUATE = "evaluate";
    public static final String ANA_TAG_MSG_TXT = "msg_tag_txt";
    public static final String ANA_TAG_MSG_VOICE = "msg_tag_voice";
    public static final String ANA_TAG_MSG_LOC = "msg_tag_location";
    public static final String ANA_TAG_MSG_LOC_AGREE = "msg_tag_location_agree";

    public static final String ANA_TAG_RET_OK = "operation_ok";
    public static final String ANA_TAG_RET_FAIL = "operation_fail";
}
