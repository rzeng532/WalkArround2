/**
 * Copyright (C) 2014-2017 CMCC All rights reserved
 */
package com.awalk.walkarround.myself.util;

import com.awalk.walkarround.util.AsyncTaskListener;
import com.awalk.walkarround.util.http.HttpTaskBase;

/**
 * TODO: description
 * Date: 2017-01-20
 *
 * @author Administrator
 */
public class ProfileOnResultListener implements HttpTaskBase.onResultListener {

    private HttpTaskBase.onResultListener mNetOnResultListener;
    private AsyncTaskListener mTaskListener;

    public ProfileOnResultListener(HttpTaskBase.onResultListener netListener, AsyncTaskListener taskListener) {
        mNetOnResultListener = netListener;
        mTaskListener = taskListener;
    }

    @Override
    public void onPreTask(String requestCode) {
        if(mNetOnResultListener == null) {
            return;
        }

        mNetOnResultListener.onPreTask(requestCode);
    }

    @Override
    public void onResult(Object object, HttpTaskBase.TaskResult resultCode, String requestCode, String threadId) {

        if(mNetOnResultListener == null) {
            return;
        }

        mNetOnResultListener.onResult(object, resultCode, requestCode, threadId);

        if(HttpTaskBase.TaskResult.SUCCEESS == resultCode && mTaskListener != null) {
            mTaskListener.onSuccess(object);
        }
    }

    @Override
    public void onProgress(int progress, String requestCode) {
        if(mNetOnResultListener == null) {
            return;
        }

        mNetOnResultListener.onProgress(progress, requestCode);
    }
}
