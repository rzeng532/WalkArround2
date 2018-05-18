package com.awalk.walkarround.message.task;

import android.content.Context;
import android.text.TextUtils;
import com.alibaba.fastjson.JSONObject;
import com.awalk.walkarround.util.http.HttpTaskPost;
import com.awalk.walkarround.util.http.HttpUtil;

import java.util.Map;

/**
 * Created by cmcc on 16/1/26.
 */
public class EvaluateFriendTask extends HttpTaskPost {
    public EvaluateFriendTask(Context context, onResultListener listener, String requestCode, String urlString, String contentStr, Map<String, String> header) {
        super(context, listener, requestCode, urlString, header, contentStr);
    }

    @Override
    public void run() {
        super.run();
    }

    /*
     * Return JSON parameters to String style.
     */
    public static String getParams(String userId, int honesty, int talkative, int temperament, int seductive, String friendId) {

        if (TextUtils.isEmpty(userId)
                || TextUtils.isEmpty(friendId)
                || honesty <= 0 || talkative <= 0
                || temperament <= 0 || seductive <= 0) {
            return null;
        }

        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_USER_ID, userId);
        param.put(HttpUtil.HTTP_PARAM_LIKE_SOMEONE_TO, friendId);
        param.put(HttpUtil.HTTP_PARAM_EVALUATE_HONEST, honesty);
        param.put(HttpUtil.HTTP_PARAM_EVALUATE_TALK_STYLE, talkative);
        param.put(HttpUtil.HTTP_PARAM_EVALUATE_TEMPERAMENT, temperament);
        param.put(HttpUtil.HTTP_PARAM_EVALUATE_SEDUCTIVE, seductive);

        return param.toString();
    }

    /*
 * Return JSON parameters to String style.
 */
    public static String getParamsBetweenNoFriend(String userId, int honesty, int talkative, int temperament, int seductive, String speedDataId) {

        if (TextUtils.isEmpty(userId)
                || TextUtils.isEmpty(speedDataId)
                || honesty <= 0 || talkative <= 0
                || temperament <= 0 || seductive <= 0) {
            return null;
        }

        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_USER_ID, userId);
        param.put(HttpUtil.HTTP_PARAM_SPEED_DATA_ID, speedDataId);
        param.put(HttpUtil.HTTP_PARAM_EVALUATE_HONEST, honesty);
        param.put(HttpUtil.HTTP_PARAM_EVALUATE_TALK_STYLE, talkative);
        param.put(HttpUtil.HTTP_PARAM_EVALUATE_TEMPERAMENT, temperament);
        param.put(HttpUtil.HTTP_PARAM_EVALUATE_SEDUCTIVE, seductive);

        return param.toString();
    }
}
