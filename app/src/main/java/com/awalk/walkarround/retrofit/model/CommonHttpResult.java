package com.awalk.walkarround.retrofit.model;

import java.io.Serializable;

/**
 *
 */
public class CommonHttpResult<D> implements Serializable {

    public static final String HTTP_SUCCESS = "1000000";// 请求完成，数据解析完成
    public static final String HTTP_FAILED = "failed";// 请求失败
    public static final String HTTP_TIMEOUT = "timeout";// 请求超时
    public static final String HTTP_ERROR = "error";// 请求完成，数据解析失败
    public final static String HTTP_NET_ERROR = "net error";
    public static final String SESSION_EXPIRE = "5101001"; // session 过期

    private Result result;

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public class Result {
        private String code;
        private D result;
        private D results;
        private String message;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
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
