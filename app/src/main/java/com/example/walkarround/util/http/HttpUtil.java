package com.example.walkarround.util.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Richard on 2016/1/6.
 */
public class HttpUtil {

    //Base URL elements
    private static final String HTTP_BASE_SPLIT_SYM = "/";

    private static final String HTTP_BASE_URL = "https://leancloud.cn";

    private static final String HTTP_BASE_FUNC = "1.1/functions";

    public static final String HTTP_FUNC_QUERY_NEARLY_USERS = "queryNearlyUsers";
    public static final String HTTP_FUNC_LIKE_SOMEONE = "likeSomeone";
    public static final String HTTP_FUNC_GO_TOGETHER = "goTogether";
    public static final String HTTP_FUNC_QUERY_SPEED_DATE = "querySpeedDate";
    public static final String HTTP_FUNC_ADD_FRIEND = "addFriend";
    public static final String HTTP_FUNC_GET_FRIEND_LIST = "friendList";
    public static final String HTTP_FUNC_EVALUATE_EACH = "evaluationEach";
    public static final String HTTP_FUNC_UPDATE_SPEEDDATE_COLOR = "setColor";
    public static final String HTTP_FUNC_QUERY_USR_COORDINATE = "userCoordinate";
    public static final String HTTP_FUNC_END_SPEED_DATE = "endSpeedDate";
    public static final String HTTP_FUNC_CANCEL_SPEED_DATE = "cancelSpeedDate";
    public static final String HTTP_FUNC_INACTIVE_FRIEND = "friendInActive";
    public static final String HTTP_FUNC_CREATE_DYNC_DATA = "createUserDynamicData";
    public static final String HTTP_FUNC_QUERY_DYNC_DATA = "queryUserDynamicData";
    public static final String HTTP_FUNC_UPDATE_DYNC_DATA = "updateUserDynamicData";
    public static final String HTTP_FUNC_REGISTE = "registe";

    //Public HTTP tasks
    private static final String HTTP_TASK_PRE = HTTP_BASE_URL + HTTP_BASE_SPLIT_SYM +
            HTTP_BASE_FUNC + HTTP_BASE_SPLIT_SYM;

    public static final String HTTP_TASK_QUERY_NEARLY_USERS = HTTP_TASK_PRE +
            HTTP_FUNC_QUERY_NEARLY_USERS;

    public static final String HTTP_TASK_LIKE_SOMEONE = HTTP_TASK_PRE +
            HTTP_FUNC_LIKE_SOMEONE;

    public static final String HTTP_TASK_GO_TOGETHER = HTTP_TASK_PRE +
            HTTP_FUNC_GO_TOGETHER;

    public static final String HTTP_TASK_QUERY_SPEED_DATE = HTTP_TASK_PRE +
            HTTP_FUNC_QUERY_SPEED_DATE;

    public static final String HTTP_TASK_ADD_FRIEND = HTTP_TASK_PRE +
            HTTP_FUNC_ADD_FRIEND;

    public static final String HTTP_TASK_GET_FRIEND_LIST = HTTP_TASK_PRE +
            HTTP_FUNC_GET_FRIEND_LIST;

    public static final String HTTP_TASK_EVALUATION_EACH = HTTP_TASK_PRE +
            HTTP_FUNC_EVALUATE_EACH;

    public static final String HTTP_TASK_UPDATE_SPEEDDATE_COLOR = HTTP_TASK_PRE +
            HTTP_FUNC_UPDATE_SPEEDDATE_COLOR;

    public static final String HTTP_TASK_QUERY_USR_COORDINATE = HTTP_TASK_PRE +
            HTTP_FUNC_QUERY_USR_COORDINATE;

    public static final String HTTP_TASK_END_SPEED_DATE = HTTP_TASK_PRE +
            HTTP_FUNC_END_SPEED_DATE;

    public static final String HTTP_TASK_CANCEL_SPEED_DATE = HTTP_TASK_PRE +
            HTTP_FUNC_CANCEL_SPEED_DATE;

    public static final String HTTP_TASK_INACTIVE_FRIEND = HTTP_TASK_PRE +
            HTTP_FUNC_INACTIVE_FRIEND;

    public static final String HTTP_TASK_CREATE_DYNC_DATA = HTTP_TASK_PRE +
            HTTP_FUNC_CREATE_DYNC_DATA;

    public static final String HTTP_TASK_QUERY_DYNC_DATA = HTTP_TASK_PRE +
            HTTP_FUNC_QUERY_DYNC_DATA;

    public static final String HTTP_TASK_UPDATE_DYNC_DATA = HTTP_TASK_PRE +
            HTTP_FUNC_UPDATE_DYNC_DATA;

    public static final String HTTP_TASK_REGISTE = HTTP_TASK_PRE +
            HTTP_FUNC_REGISTE;

    //Register
    public static final String HTTP_PARAM_USER_NAME = "username";
    public static final String HTTP_PARAM_PASSWORD = "password";
    public static final String HTTP_PARAM_GENDER = "gender";
    public static final String HTTP_PARAM_PHONE = "phone";

    public static final String HTTP_PARAM_QUERY_NEARLY_USERS_ID = "userDynamicDataId";
    public static final String HTTP_PARAM_LIKE_SOMEONE_FROM = "fromUserId";
    public static final String HTTP_PARAM_LIKE_SOMEONE_TO = "toUserId";
    public static final String HTTP_PARAM_SPEED_DATA_ID = "speedDateId";
    public static final String HTTP_PARAM_USER_ID = "userId";
    public static final String HTTP_PARAM_FRIEND_USER_ID = "friendUserId";
    public static final String HTTP_PARAM_FRIEND_LIST_COUNT = "count";
    public static final String HTTP_PARAM_SPEEDDATE_COLOR = "color";
    public static final String HTTP_PARAM_USR_COORDINATE = "userId";
    public static final String HTTP_PARAM_DYN_DATA_DATE_STATE = "datingStatus";
    public static final String HTTP_PARAM_DYN_DATA_LATITUDE = "latitude";
    public static final String HTTP_PARAM_DYN_DATA_LONGITUDE = "longitude";

    public static final String HTTP_PARAM_EVALUATE_HONEST = "honesty";
    public static final String HTTP_PARAM_EVALUATE_TALK_STYLE = "talkative";
    public static final String HTTP_PARAM_EVALUATE_TEMPERAMENT = "temperament";
    public static final String HTTP_PARAM_EVALUATE_SEDUCTIVE = "seductive";

    //Request header parameters
    public static final String HTTP_REQ_HEADER_LC_ID = "X-LC-Id";
    public static final String HTTP_REQ_HEADER_LC_KEY = "X-LC-Key";
    public static final String HTTP_REQ_HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HTTP_REQ_HEADER_CONTENT_TYPE_JSON = "application/json";

    //Response common result
    public static final String HTTP_RESPONSE_KEY_RESULT_RESULT = "result";
    public static final String HTTP_RESPONSE_KEY_RESULT_CODE = "code";
    public static final String HTTP_RESPONSE_KEY_RESULT_CODE_SUC = "200";
    public static final String HTTP_RESPONSE_KEY_RESULT_DATA = "results";

    //Response: like someone task
    public static final String HTTP_RESPONSE_KEY_LIKE_TO_USER = "toUser";
    public static final String HTTP_RESPONSE_KEY_LIKE_FROM_USER = "fromUser";
    public static final String HTTP_RESPONSE_KEY_LIKE_STATUS = "status";

    //Response: query speed date id
    public static final String HTTP_RESPONSE_KEY_OBJECT_ID = "objectId";
    public static final String HTTP_RESPONSE_KEY_COLOR = "color";

    //Response: query user coordinate
    public static final String HTTP_RESPONSE_KEY_LATITUDE = "latitude";
    public static final String HTTP_RESPONSE_KEY_LONGITUDE = "longitude";

    //Response: query user dynamic data
    public static final String HTTP_RESPONSE_KEY_DATING_STATUS = "datingStatus";

    public static final String HTTP_RESPONSE_KEY_MOBILE_VERIFIED = "mobilePhoneVerified";

    private String addEntityKeyValue(String key, String value) {
        try {
            return (URLEncoder.encode(key, "utf-8") + "=" + URLEncoder.encode(value, "utf-8") + "&");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
