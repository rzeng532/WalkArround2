package com.awalk.walkarround.main.task;

import android.content.Context;
import com.alibaba.fastjson.JSONObject;
import com.awalk.walkarround.util.http.HttpTaskPost;
import com.awalk.walkarround.util.http.HttpUtil;

import java.util.Map;

/**
 * Created by Richard on 2016/1/8.
 */
public class QueryNearlyUsers extends HttpTaskPost{

    public QueryNearlyUsers(Context context, onResultListener listener, String requestCode, String urlString, String contentStr, Map<String, String> header) {
        super(context, listener, requestCode, urlString, header, contentStr);
    }

    @Override
    public void run() {
        super.run();
    }

    /*
     * Return JSON parameters to String style.
     */
    public static String getParams(String userid) {
        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_QUERY_NEARLY_USERS_ID, userid);

        return param.toString();
    }
}
