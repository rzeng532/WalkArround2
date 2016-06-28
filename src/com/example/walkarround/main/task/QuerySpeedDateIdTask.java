/**
 * TODO: description
 * Date: 2016-01-25
 *
 * @author Administrator
 */

package com.example.walkarround.main.task;

import android.content.Context;
import android.text.TextUtils;
import com.alibaba.fastjson.JSONObject;
import com.example.walkarround.util.http.HttpTaskPost;
import com.example.walkarround.util.http.HttpUtil;

import java.util.Map;

/**
 * Created by Richard on 2016/1/8.
 */
public class QuerySpeedDateIdTask extends HttpTaskPost {

    public QuerySpeedDateIdTask(Context context, onResultListener listener, String requestCode, String urlString, String contentStr, Map<String, String> header) {
        super(context, listener, requestCode, urlString, header, contentStr);
    }

    @Override
    public void run() {
        super.run();
    }

    /*
     * Return JSON parameters to String style.
     */
    public static String getParams(String userId) {

        if (TextUtils.isEmpty(userId)) {
            return null;
        }

        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_USER_ID, userId);

        return param.toString();
    }
}
