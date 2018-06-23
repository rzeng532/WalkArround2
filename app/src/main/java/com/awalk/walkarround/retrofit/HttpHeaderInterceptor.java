package com.awalk.walkarround.retrofit;

import android.text.TextUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * http interceptor to set header
 */
public class HttpHeaderInterceptor implements Interceptor {

    private HashMap<String, String> mHeaderMap = new HashMap<>();

    public HttpHeaderInterceptor() {
    }

    /**
     * 添加http Header信息
     * @param key
     * @param value
     */
    public void addHeader(String key, String value) {
        if (!TextUtils.isEmpty(key) || !TextUtils.isEmpty(value)) {
            mHeaderMap.put(key, value);
        }
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        //set header
        Request.Builder builder = request.newBuilder();
//        builder.addHeader("Content-Type", "application/json;charset=UTF-8");
        if (mHeaderMap != null && mHeaderMap.size() > 0) {
            for (Map.Entry<String, String> item : mHeaderMap.entrySet()) {
                builder.addHeader(item.getKey(), item.getValue());
            }
        }
        request = builder.build();
        return chain.proceed(request);
    }


}
