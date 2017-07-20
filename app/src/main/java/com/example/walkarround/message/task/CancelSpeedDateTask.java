package com.example.walkarround.message.task;

import android.content.Context;
import android.text.TextUtils;
import com.alibaba.fastjson.JSONObject;
import com.example.walkarround.util.http.HttpTaskPost;
import com.example.walkarround.util.http.HttpUtil;

import java.util.Map;

/**
 * Created by cmcc on 16/1/26.
 */
public class CancelSpeedDateTask extends HttpTaskPost {
    public CancelSpeedDateTask(Context context, onResultListener listener, String requestCode, String urlString, String contentStr, Map<String, String> header) {
        super(context, listener, requestCode, urlString, header, contentStr);
    }

    @Override
    public void run() {
        super.run();
    }

    /*
     * Return JSON parameters to String style.
     */
    public static String getParams(String speedDataId) {

        if (TextUtils.isEmpty(speedDataId)) {
            return null;
        }

        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_SPEED_DATA_ID, speedDataId);

        return param.toString();
    }
}
