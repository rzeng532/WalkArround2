package com.awalk.walkarround.retrofit.trace;

import android.content.Context;

import java.util.Map;

/**
 */
public class HttpTraceInfo {
    private String url;
    private String code;
    private String message;
    private Map<String, String> params;
    private String time;// 字符串类型，格式为（YYYY-MM-DD HH:MM:SS

    private HttpTraceInfo(Builder builder) {
        url = builder.url;
        code = builder.code;
        message = builder.message;
        params = builder.params;
        time = builder.time;
    }

    /**
     *
     */
    public static final class Builder {

        private String url;
        private String code;
        private String message;
        private Map<String, String> params;
        private String time;

        /**
         *
         * @param url
         * @return
         */
        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        /**
         *
         * @param code
         * @return
         */
        public Builder setCode(String code) {
            this.code = code;
            return this;
        }

        /**
         *
         * @param message
         * @return
         */
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        /**
         *
         * @param params
         * @return
         */
        public Builder setParams(Map<String, String> params) {
            this.params = params;
            return this;
        }

        /**
         *
         * @param time
         * @return
         */
        public Builder setTime(String time) {
            this.time = time;
            return this;
        }

        /**
         *
         * @param context
         */
        public Builder(Context context) {
            time = System.currentTimeMillis() + "";
        }

        /**
         *
         */
        public Builder() {
            time = System.currentTimeMillis() + "";
        }

        /**
         * 
         * @return
         */
        public HttpTraceInfo builder() {
            return new HttpTraceInfo(this);
        }
    }

}
