package com.example.walkarround.message.task;

import android.content.Context;
import android.text.TextUtils;
import com.alibaba.fastjson.JSONObject;
import com.example.walkarround.util.http.HttpTaskPost;
import com.example.walkarround.util.http.HttpUtil;

import java.util.Map;

/**
 * Created on 16/1/26.
 */
public class InActivieFriendTask extends HttpTaskPost {
    public InActivieFriendTask(Context context, onResultListener listener, String requestCode, String urlString, String contentStr, Map<String, String> header) {
        super(context, listener, requestCode, urlString, header, contentStr);
    }

    @Override
    public void run() {
        super.run();
    }

    /*
     * Return JSON parameters to String style.
     */
    public static String getParams(String curUsrId, String friendUsrId) {

        if (TextUtils.isEmpty(curUsrId) || TextUtils.isEmpty(friendUsrId)) {
            return null;
        }

        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_USER_ID, curUsrId);
        param.put(HttpUtil.HTTP_PARAM_FRIEND_USER_ID, friendUsrId);

        return param.toString();
    }
}
