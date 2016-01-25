/**
 * Copyright (C) 2014-2016 CMCC All rights reserved
 */
package com.example.walkarround.main.task;

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
