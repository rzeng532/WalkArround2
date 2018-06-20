package com.awalk.walkarround.retrofit;

import com.awalk.walkarround.util.AppConstant;
import com.awalk.walkarround.util.http.HttpUtil;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * RetrofitManager
 * <p>
 * Created by mashanshan on 2017/6/6.
 */
public class RetrofitManager {

    public static final long READ_TIME_OUT = 10;
    public static final long CONNECT_TIME_OUT = 10;

    private static RetrofitManager instance;

    protected HttpLoggingInterceptor mHttpLoggingInterceptor;

    protected HttpHeaderInterceptor mHttpHeaderInterceptor;
    private Retrofit mRetrofit;

    protected RetrofitManager() {
        mHttpLoggingInterceptor = new HttpLoggingInterceptor();
        mHttpHeaderInterceptor = new HttpHeaderInterceptor();
        addHeader(HttpUtil.HTTP_REQ_HEADER_LC_ID, AppConstant.LEANCLOUD_APP_ID);
        addHeader(HttpUtil.HTTP_REQ_HEADER_LC_KEY, AppConstant.LEANCLOUD_APP_KEY);
        addHeader(HttpUtil.HTTP_REQ_HEADER_CONTENT_TYPE, HttpUtil.HTTP_REQ_HEADER_CONTENT_TYPE_JSON);
        addHeader("Cookie", "");
    }

    public static RetrofitManager getInstance() {
        if (instance == null) {
            syncInit();
        }
        return instance;
    }

    private static synchronized void syncInit() {
        if (instance == null) {
            instance = new RetrofitManager();
        }
    }

    protected OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder()
                .addNetworkInterceptor(new CommonHttpLoggingInterceptor())
                .addInterceptor(mHttpHeaderInterceptor)
                .addInterceptor(mHttpLoggingInterceptor)
                //time out
                .readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
                .connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS)
                .build();
    }

    /**
     * @param key
     * @param value
     */
    public void addHeader(String key, String value) {
        mHttpHeaderInterceptor.addHeader(key, value);
    }

    public final <T> T getServices(Class<T> service, String baseUrl) {
        //httpClient request config
        OkHttpClient client = getOkHttpClient();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();
        return retrofit.create(service);
    }

    public final ApiService getServices() {
        if (mRetrofit == null) {
            //httpClient request config
            OkHttpClient client = getOkHttpClient();
            mRetrofit = new Retrofit.Builder()
                    .baseUrl(HttpUtil.SERVER_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(client)
                    .build();
        }
        return mRetrofit.create(ApiService.class);
    }

}
