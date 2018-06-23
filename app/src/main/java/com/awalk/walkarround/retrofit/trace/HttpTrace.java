package com.awalk.walkarround.retrofit.trace;

import com.alibaba.fastjson.JSONObject;
import com.awalk.walkarround.base.WalkArroundApp;
import com.awalk.walkarround.retrofit.model.CommonHttpResult;
import com.awalk.walkarround.util.AppConstant;
import com.tencent.bugly.crashreport.CrashReport;

public class HttpTrace {
    /**
     * 上报网络异常
     *
     * @param httpTraceInfo
     */
    public static synchronized void reportHttpEvent(HttpTraceInfo httpTraceInfo) {
        try {
            // 上报后的Crash会显示HttpTrace标签
            CrashReport.setUserSceneTag(WalkArroundApp.getInstance(), AppConstant.LOG_OUTPUT ? 77555 : 77554);
            String msg = JSONObject.toJSONString(httpTraceInfo);
            CrashReport.postCatchedException(new Exception(msg));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public static void handleHttpTraceInfor(String url, int code, String msg) {
        try {
            HttpTraceInfo.Builder builder = new HttpTraceInfo.Builder();
            HttpTraceInfo httpTraceInfo = builder
                    .setCode(Integer.toString(code))
                    .setUrl(url)
                    .setMessage(msg)
                    .builder();

            HttpTrace.reportHttpEvent(httpTraceInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void handleHttpTraceInfor(String url, String code, String msg) {
        try {
            HttpTraceInfo.Builder builder = new HttpTraceInfo.Builder();
            HttpTraceInfo httpTraceInfo = builder
                    .setCode(code)
                    .setUrl(url)
                    .setMessage(msg)
                    .builder();

            HttpTrace.reportHttpEvent(httpTraceInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 上报网络异常
     *
     * @param result
     */
    public static void reportHttpEvent(String url, CommonHttpResult<?> result) {
        if (CommonHttpResult.HTTP_SUCCESS != result.getResult().getCode()) {
            HttpTraceInfo.Builder builder = new HttpTraceInfo.Builder();
            HttpTraceInfo httpTraceInfo = builder
                    .setCode(Integer.toString(result.getResult().getCode()))
                    .setUrl(url)
                    .setMessage(result.getResult().getMessage())
                    .builder();
            HttpTrace.reportHttpEvent(httpTraceInfo);
        }
    }
}
