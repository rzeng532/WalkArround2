/**
 * Copyright (C) 2014-2015 CMCC All rights reserved
 */
package com.example.walkarround.base;

import android.app.Application;
import com.avos.avoscloud.AVOSCloud;
import com.example.walkarround.util.AppConstant;

/**
 * TODO: description
 * Date: 2015-11-26
 *
 * @author Administrator
 */
public class WalkArroundApp extends Application {
    private static WalkArroundApp mWorkArroundApp = null;

    @Override
    public void onCreate() {
        super.onCreate();

        mWorkArroundApp = this;

        //Init lean cloud
        AVOSCloud.setDebugLogEnabled(true);
        AVOSCloud.initialize(this, AppConstant.LEANCLOUD_APP_ID, AppConstant.LEANCLOUD_APP_KEY);
    }

    public static WalkArroundApp getAppInstance() {
        return mWorkArroundApp;
    }
}
