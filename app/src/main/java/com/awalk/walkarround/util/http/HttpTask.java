package com.awalk.walkarround.util.http;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import com.awalk.walkarround.main.parser.WalkArroundJsonResultParser;
import com.awalk.walkarround.retrofit.trace.HttpTrace;


public class HttpTask extends HttpTaskBase {

    HttpURLConnection conn;
    BufferedReader bufferedReader;
    static String JSESSIONID = "";
    protected String mUrlString;

    public HttpTask(Context context, onResultListener listener, String requestCode, String urlString) {
        super(context, listener, requestCode);
        mUrlString = urlString;
    }

    public HttpTask(Context context, onResultListener listener, String requestCode, String urlString, TaskType taskType) {
        super(context, listener, requestCode, taskType);
        mUrlString = urlString;
    }

    @Override
    public void run() {

        conn.setReadTimeout(30000);
        conn.setConnectTimeout(30000);
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
            // 将cookie设置到本地，方便下次请求
            setCookie();
            StringBuffer sb = new StringBuffer();
            String msg;
            while ((msg = bufferedReader.readLine()) != null) {
                sb.append(msg);
            }
            bufferedReader.close();
            conn.disconnect();
            if (TextUtils.isEmpty(sb.toString())) {
                HttpTrace.handleHttpTraceInfor(mUrlString, "fail", "result empty");
                doResultCallback(null, TaskResult.FAILED);
                return;
            }
            String result = sb.toString();
            String returnCode = WalkArroundJsonResultParser.parseReturnCode(result);
            if (!HttpUtil.HTTP_RESPONSE_KEY_RESULT_CODE_SUC.equals(returnCode)) {
                // 非200错误
                HttpTrace.handleHttpTraceInfor(mUrlString, returnCode, result);
            }
            doResultCallback(sb.toString(),TaskResult.SUCCEESS);
        } catch (IOException e) {
            e.printStackTrace();

            Log.v("", e.toString());
            HttpTrace.handleHttpTraceInfor(mUrlString, "IOException", e.toString());

            doResultCallback(null, TaskResult.ERROR);
        }

    }

    private void setCookie() {
        JSESSIONID = "JSESSIONID=" + conn.getHeaderField("jsessionid");
    }

}
