package com.awalk.walkarround.retrofit;

import com.awalk.walkarround.retrofit.model.CommonHttpResult;
import com.awalk.walkarround.retrofit.trace.HttpTrace;

import org.json.JSONObject;

import java.net.SocketTimeoutException;

import io.reactivex.observers.DisposableObserver;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

/**
 * Author: zhou
 * Email: zhougaofeng@chinamobile.com
 * Time: 2017/8/31
 * Description:
 */
public abstract class ZCommonObserver<T> extends DisposableObserver<T> {

    protected String mUrl;

    public ZCommonObserver(String url) {
        mUrl = url;
    }

    /**
     * real success with model data  从服务区获取成功处理逻辑
     *
     * @param t
     */
    protected abstract void onSuccess(T t, String resultMessage);

    /**
     * 失败处理逻辑，可以选择overrider
     */
    protected void onFailed(String code, String message) {
    }

    @Override
    public void onNext(T t) {
        if (null == t) {
            onFailed(CommonHttpResult.HTTP_ERROR, "");
        } else {
            if (t instanceof CommonHttpResult) {
                CommonHttpResult body = (CommonHttpResult) t;
                // 取得消息code值
                String resultCode = body.getCode();
                String resultMessage = body.getMessage();

                if (String.valueOf(CommonHttpResult.SESSION_EXPIRE).equals(resultCode)) {
                    onFailed(resultCode, resultMessage);
                    HttpTrace.handleHttpTraceInfor(mUrl, resultCode, resultMessage);
                } else if (String.valueOf(CommonHttpResult.HTTP_SUCCESS).equals(resultCode)) {
                    onSuccess(t, resultMessage);
                } else {
                    resultMessage = body.getMessage();
                    onFailed(resultCode, resultMessage);
                    HttpTrace.handleHttpTraceInfor(mUrl, resultCode, resultMessage);
                }
            } else {
                onSuccess(t, "");
            }
        }
    }

    @Override
    public void onError(Throwable e) {
        if (e instanceof HttpException) {
            ResponseBody responseBody = ((HttpException) e).response().errorBody();
            String errorString = getErrorMessage(responseBody);
            onFailed(CommonHttpResult.HTTP_FAILED, errorString);
        } else if (e instanceof SocketTimeoutException) {
            onFailed(CommonHttpResult.HTTP_TIMEOUT, "");
        } else {
            onFailed(CommonHttpResult.HTTP_NET_ERROR, "");
        }
    }

    private String getErrorMessage(ResponseBody responseBody) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody.string());
            return jsonObject.getString("message");
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public void onComplete() {
        dispose();
    }

}
