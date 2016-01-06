package com.example.walkarround.util.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.text.TextUtils;


public class HttpTaskGet extends HttpTask {

    public HttpTaskGet(Context context, onResultListener listener, String requestCode, String url) {
        super(context, listener, requestCode,url);
        mUrlString = url;
    }

    public HttpTaskGet(Context context, onResultListener listener, String requestCode, String url, TaskType taskType) {
        super(context, listener, requestCode,url, taskType);
        mUrlString = url;
    }
    
    @Override
    public void run() {
        if(TextUtils.isEmpty(mUrlString)){
            return ;
        }
        try {
            URL url = new URL(mUrlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(false);
            conn.setDoInput(true);
            conn.setRequestProperty("Charset", "UTF-8");
            conn.addRequestProperty("Cookie", JSESSIONID);
            super.run();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            doResultCallback(null,TaskResult.ERROR);
        }

    }

}
