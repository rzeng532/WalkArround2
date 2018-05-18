package com.awalk.walkarround.login.task;

import android.content.Context;
import android.text.TextUtils;
import com.alibaba.fastjson.JSONObject;
import com.awalk.walkarround.util.http.HttpTaskPost;
import com.awalk.walkarround.util.http.HttpUtil;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by cmcc on 16/1/26.
 */
public class CheckIfVerifiedTask extends HttpTaskPost {
    public CheckIfVerifiedTask(Context context, onResultListener listener, String requestCode, String urlString, String contentStr, Map<String, String> header) {
        super(context, listener, requestCode, urlString, header, contentStr);
    }

    @Override
    public void run() {
        super.run();
    }

    /*
     * Return JSON parameters to String style.
     */
    public static String getParams(String name, String password, String gender, String phone) {

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(gender) || TextUtils.isEmpty(phone)) {
            return null;
        }

        JSONObject param = new JSONObject();
        try {
            param.put(HttpUtil.HTTP_PARAM_USER_NAME, new String(name.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        param.put(HttpUtil.HTTP_PARAM_PASSWORD, password);
        param.put(HttpUtil.HTTP_PARAM_GENDER, gender);
        param.put(HttpUtil.HTTP_PARAM_PHONE, phone);

        return param.toString();
    }
}
