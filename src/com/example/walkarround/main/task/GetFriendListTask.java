package com.example.walkarround.main.task;

import android.content.Context;
import android.text.TextUtils;
import com.alibaba.fastjson.JSONObject;
import com.example.walkarround.util.http.HttpTaskPost;
import com.example.walkarround.util.http.HttpUtil;

import java.util.Map;

/**
     * Created on 16/1/26.
     */
    public class GetFriendListTask extends HttpTaskPost {
        public GetFriendListTask(Context context, onResultListener listener, String requestCode, String urlString, String contentStr, Map<String, String> header) {
            super(context, listener, requestCode, urlString, header, contentStr);
        }

    @Override
    public void run() {
        super.run();
    }

    /*
     * Return JSON parameters to String style.
     */
    public static String getParams(String userId, int count) {

        if (TextUtils.isEmpty(userId) || count <= 0) {
            return null;
        }

        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_USER_ID, userId);
        param.put(HttpUtil.HTTP_PARAM_FRIEND_LIST_COUNT, count);

        return param.toString();
    }
}
