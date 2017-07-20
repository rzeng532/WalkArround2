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
public class LikeSomeOneTask extends HttpTaskPost {

    public LikeSomeOneTask(Context context, onResultListener listener, String requestCode, String urlString, String contentStr, Map<String, String> header) {
        super(context, listener, requestCode, urlString, header, contentStr);
    }

    @Override
    public void run() {
        super.run();
    }

    /*
     * Return JSON parameters to String style.
     */
    public static String getParams(String from, String to) {

        if (TextUtils.isEmpty(from) || TextUtils.isEmpty(to)) {
            return null;
        }

        JSONObject param = new JSONObject();
        param.put(HttpUtil.HTTP_PARAM_LIKE_SOMEONE_FROM, from);
        param.put(HttpUtil.HTTP_PARAM_LIKE_SOMEONE_TO, to);

        return param.toString();
    }
}
