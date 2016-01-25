package com.example.walkarround.util.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Richard on 2016/1/6.
 */
public class HttpUtil {

    //Base URL elements
    private static final String HTTP_BASE_SPLIT_SYM = "/";

    private static final String HTTP_BASE_URL = "https://leancloud.cn:443";

    private static final String HTTP_BASE_FUNC = "1.1/functions";

    public static final String HTTP_FUNC_QUERY_NEARLY_USERS = "queryNearlyUsers";
    public static final String HTTP_FUNC_LIKE_SOMEONE = "likeSomeone";

    //Public HTTP tasks
    public static final String HTTP_TASK_QUERY_NEARLY_USERS = HTTP_BASE_URL + HTTP_BASE_SPLIT_SYM +
            HTTP_BASE_FUNC + HTTP_BASE_SPLIT_SYM +
            HTTP_FUNC_QUERY_NEARLY_USERS;

    public static final String HTTP_TASK_LIKE_SOMEONE = HTTP_BASE_URL + HTTP_BASE_SPLIT_SYM +
            HTTP_BASE_FUNC + HTTP_BASE_SPLIT_SYM +
            HTTP_FUNC_LIKE_SOMEONE;



    public static final String HTTP_PARAM_QUERY_NEARLY_USERS_ID = "userDynamicDataId";
    public static final String HTTP_PARAM_LIKE_SOMEONE_FROM = "fromUserId";
    public static final String HTTP_PARAM_LIKE_SOMEONE_TO = "toUserId";

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

    private String addEntityKeyValue(String key, String value) {
        try {
            return (URLEncoder.encode(key, "utf-8") + "=" + URLEncoder.encode(value, "utf-8") + "&");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
