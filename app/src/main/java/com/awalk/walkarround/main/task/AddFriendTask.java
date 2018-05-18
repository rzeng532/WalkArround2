package com.awalk.walkarround.main.task;

import android.content.Context;
import android.text.TextUtils;
import com.alibaba.fastjson.JSONObject;
import com.awalk.walkarround.util.http.HttpTaskPost;
import com.awalk.walkarround.util.http.HttpUtil;

import java.util.Map;

/**
 * Created by cmcc on 16/1/26.
 */
public class AddFriendTask extends HttpTaskPost {
    public AddFriendTask(Context context, onResultListener listener, String requestCode, String urlString, String contentStr, Map<String, String> header) {
        super(context, listener, requestCode, urlString, header, contentStr);
    }

    @Override
    public void run() {
        super.run();
    }

    /*
     * Return JSON parameters to String style.
     */
    public static String getParams(String userId, String friendId, String colorIndex) {

        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(friendId) || TextUtils.isEmpty(colorIndex)) {
            return null;
        }

        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_USER_ID, userId);
        param.put(HttpUtil.HTTP_PARAM_FRIEND_USER_ID, friendId);
        param.put(HttpUtil.HTTP_PARAM_SPEEDDATE_COLOR, colorIndex);

        return param.toString();
    }
}
