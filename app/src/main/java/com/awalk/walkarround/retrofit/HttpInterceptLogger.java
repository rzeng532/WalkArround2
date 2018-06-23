package com.awalk.walkarround.retrofit;

import com.awalk.walkarround.util.Logger;

/**
 * Created by 15766_000 on 2017/8/4.
 */
public class HttpInterceptLogger implements HttpLoggingInterceptor.Logger {
    private static Logger mLogger = Logger.getLogger(HttpInterceptLogger.class.getSimpleName());
    @Override
    public void log(String message) {
        mLogger.i(message);
    }
}
