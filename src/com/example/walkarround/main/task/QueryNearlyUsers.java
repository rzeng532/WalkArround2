package com.example.walkarround.main.task;

import android.content.Context;
import com.example.walkarround.util.http.HttpTaskPost;

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
}
