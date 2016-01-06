package com.example.walkarround.util.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Richard on 2016/1/6.
 */
public class HttpUtil {

    public static final String HTTP_BASE_SPLIT_SYM = "/";

    public static final String HTTP_BASE_URL = " https://leancloud.cn:443";

    public static final String HTTP_BASE_FUNC = "1.1/functions";

    public static final String HTTP_QUERY_NEARLY_USERS = "sendVerifyCode";

    private String addEntityKeyValue(String key, String value) {
        try {
            return (URLEncoder.encode(key, "utf-8") + "=" + URLEncoder.encode(value, "utf-8") + "&");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
