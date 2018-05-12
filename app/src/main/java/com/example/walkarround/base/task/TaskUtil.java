/**
 * Copyright (C) 2014-2016 All rights reserved
 */
package com.example.walkarround.base.task;

import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.http.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: description
 * Date: 2016-01-25
 *
 * @author Administrator
 */
public class TaskUtil {

    public static final String RESPONSE_USR_STATUS_SINGLE = "1"; //请求
    public static final String RESPONSE_USR_STATUS_ACCEPT = "2"; //接收
    public static final String RESPONSE_USR_STATUS_TOGETHER = "3"; //走走
    public static final String RESPONSE_USR_STATUS_IMPRESSION = "4"; //评价
    public static final String RESPONSE_USR_STATUS_FINISH = "5"; //结束

    /*
     * Return JSON parameters to String style.
     */
    public static Map<String, String> getTaskHeader() {
        Map<String, String> header = new HashMap<>();
        header.put(HttpUtil.HTTP_REQ_HEADER_LC_ID, AppConstant.LEANCLOUD_APP_ID);
        header.put(HttpUtil.HTTP_REQ_HEADER_LC_KEY, AppConstant.LEANCLOUD_APP_KEY);
        header.put(HttpUtil.HTTP_REQ_HEADER_CONTENT_TYPE, HttpUtil.HTTP_REQ_HEADER_CONTENT_TYPE_JSON);

        return header;
    }
}
