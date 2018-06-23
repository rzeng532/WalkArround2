package com.awalk.walkarround.retrofit.model;

import java.io.Serializable;

/**
 *
 */
public class CommonHttpResult<D> implements Serializable {

    public static final int HTTP_SUCCESS = 200;// 请求完成，数据解析完成
    public static final int HTTP_FAILED = 10001;// 请求失败
    public static final int HTTP_TIMEOUT = 10002;// 请求超时
    public static final int HTTP_ERROR = 10003;// 请求完成，数据解析失败
    public final static int HTTP_NET_ERROR = 10004;
    public static final int SESSION_EXPIRE = 10005; // session 过期

    private Result result;

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public class Result {
        private int code;
        private D result;
        private D results;
        private String message;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public D getData() {
            return result == null ? results : result;
        }

        public void setData(D data) {
            this.result = data;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public D getResults() {
            return results;
        }

        public void setResults(D results) {
            this.results = results;
        }

        public D getResult() {
            return result;
        }

        public void setResult(D result) {
            this.result = result;
        }
    }

}
