package com.awalk.walkarround.util.http;

import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;

import com.awalk.walkarround.retrofit.trace.HttpTrace;


public class HttpTaskPost extends HttpTask {

    
    Map<String, String> headerMap = new HashMap<String, String>();

    protected void addHeaderValue(String key, String value) {
        headerMap.put(key, value);
    }

    String content = "";

    protected void addEntityKeyValue(String key, String value) {
        try {
            content += URLEncoder.encode(key, "utf-8") + "=" + URLEncoder.encode(value, "utf-8") + "&";
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private byte[] bytes;

    public HttpTaskPost(Context context, onResultListener listener, String requestCode, String urlString, String contentStr, TaskType taskType) {
        super(context, listener, requestCode,urlString,taskType);
        content = contentStr;
    }
    public HttpTaskPost(Context context, onResultListener listener, String requestCode, String urlString, String contentStr) {
        super(context, listener, requestCode,urlString);
        content = contentStr;
    }

    public HttpTaskPost(Context context, onResultListener listener, String requestCode, String urlString, Map<String, String> headers, String contentStr) {
        super(context, listener, requestCode,urlString);
        headerMap = headers;
        content = contentStr;
    }

    public HttpTaskPost(Context context, onResultListener listener, String requestCode, String urlString, Map<String, String> headers, String contentStr, byte[] body) {
        super(context, listener, requestCode,urlString);
        headerMap = headers;
        content = contentStr;
        bytes = body;
    }
    
    @Override
    public void run() {
        // TODO Auto-generated method stub
        if (TextUtils.isEmpty(mUrlString)) {
            return ;
        }
        try {
            URL url = new URL(mUrlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setUseCaches(false);
            conn.addRequestProperty("Cookie", JSESSIONID);
            if (headerMap != null){
                for (Map.Entry<String, String> entry : headerMap.entrySet()){
                    conn.setRequestProperty(entry.getKey(),entry.getValue());
                }
            }

            DataOutputStream out = new DataOutputStream(conn.getOutputStream());

            if (!TextUtils.isEmpty(content)){
                out.writeBytes(content);
            }

            /*
            int lastIndex = -1;
            if ( !TextUtils.isEmpty(content)&&(lastIndex = content.lastIndexOf("&")) != -1) {
                content = content.substring(0, lastIndex);
                out.writeBytes(content);
            }
            if (bytes != null){
                out.write(bytes);
            }
            */

            out.flush();
            out.close();
            super.run();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            HttpTrace.handleHttpTraceInfor(mUrlString, "IOException", e.toString());
            doResultCallback(null, TaskResult.ERROR);
            e.printStackTrace();
        }

    }

}
