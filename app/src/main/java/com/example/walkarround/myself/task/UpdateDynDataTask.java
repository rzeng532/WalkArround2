package com.example.walkarround.myself.task;

import android.content.Context;
import android.text.TextUtils;
import com.alibaba.fastjson.JSONObject;
import com.example.walkarround.util.http.HttpTaskPost;
import com.example.walkarround.util.http.HttpUtil;

import java.util.Map;

/**
 * Created by cmcc on 16/1/26.
 */
public class UpdateDynDataTask extends HttpTaskPost {
    public UpdateDynDataTask(Context context, onResultListener listener, String requestCode, String urlString, String contentStr, Map<String, String> header) {
        super(context, listener, requestCode, urlString, header, contentStr);
    }

    @Override
    public void run() {
        super.run();
    }

    /*
     * Return JSON parameters to String style.
     */
    public static String getParams(String userId, double latitude, double longitude) {

        if (TextUtils.isEmpty(userId)) {
            return null;
        }

        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_USER_ID, userId);
        //param.put(HttpUtil.HTTP_PARAM_DYN_DATA_DATE_STATE, datingStatus);
        param.put(HttpUtil.HTTP_PARAM_DYN_DATA_LATITUDE, latitude);
        param.put(HttpUtil.HTTP_PARAM_DYN_DATA_LONGITUDE, longitude);

        return param.toString();
    }
}
