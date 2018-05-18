/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.awalk.walkarround.base;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap.Config;
import android.os.Environment;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.im.v2.AVIMMessageManager;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.awalk.walkarround.Location.manager.LocationManager;
import com.awalk.walkarround.R;
import com.awalk.walkarround.message.handler.WrDefaultMsgHandler;
import com.awalk.walkarround.message.handler.WrTypedMsgHandler;
import com.awalk.walkarround.message.manager.ContactsManager;
import com.awalk.walkarround.message.manager.WalkArroundMsgManager;
import com.awalk.walkarround.util.AppConstant;
import com.awalk.walkarround.util.Logger;
import com.awalk.walkarround.util.network.NetWorkManager;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.tencent.bugly.Bugly;

/**
 * TODO: description
 * Date: 2015-11-26
 *
 * @author Richard
 */
public class WalkArroundApp extends Application {
    private static WalkArroundApp mWorkArroundApp = null;
    private static Logger logger = Logger.getLogger(WalkArroundApp.class.getSimpleName());
    public static String MTC_DATA_PATH = null;

    @Override
    public void onCreate() {
        super.onCreate();

        mWorkArroundApp = this;

        MTC_DATA_PATH = getDataDir(this) + AppConstant.APP_DATA_ROOT_PATH;
        //TODO: make dir for above path.

        //Init lean cloud & image loader manager
        try {
            AVOSCloud.setDebugLogEnabled(true);
            AVOSCloud.initialize(this, AppConstant.LEANCLOUD_APP_ID, AppConstant.LEANCLOUD_APP_KEY);
            AVAnalytics.enableCrashReport(this, true);
            AVIMMessageManager.registerMessageHandler(AVIMTypedMessage.class, WrTypedMsgHandler.getMsgHandlerInstance(this));
            AVIMMessageManager.registerDefaultMessageHandler(new WrDefaultMsgHandler());
            initImageLoader(AppConstant.MAX_IMAGE_LOADER_CACHE_SIZE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Init location manager.
        LocationManager.getInstance(getApplicationContext());
        NetWorkManager.getInstance(getApplicationContext());

        //Send prior sending message as FAIL state.
        WalkArroundMsgManager.getInstance(getApplicationContext()).setAllSendingMsgStatusFail();

        //Init contacts
        ContactsManager.getInstance(getApplicationContext());

        //Init bugly, the 3rd parameter should set to false on release version.
        Bugly.init(getApplicationContext(), AppConstant.BUGLY_APP_ID, true);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        NetWorkManager.getInstance(mWorkArroundApp).onDestroy(mWorkArroundApp);
    }

    public static WalkArroundApp getInstance() {
        return mWorkArroundApp;
    }

    private void initImageLoader(int cacheSize) throws Exception {
        logger.d("initImageLoader. Size: " + cacheSize);
        try {
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                    .threadPriority(Thread.NORM_PRIORITY - 2)
                    .denyCacheImageMultipleSizesInMemory()
                    .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                    .diskCacheSize(cacheSize)
                    .tasksProcessingOrder(QueueProcessingType.LIFO)
                    .defaultDisplayImageOptions(
                            new DisplayImageOptions.Builder()
                                    // =============下面三项先暂时配置ic_lanucher
                                    .showImageOnLoading(R.drawable.default_image)
                                    // 加载时候显示
                                    .showImageForEmptyUri(R.drawable.default_image)
                                    // 地址为空显示
                                    .showImageOnFail(R.drawable.default_image)
                                    // 加载失败显示
                                    .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                                    .bitmapConfig(Config.RGB_565).build()).build();

            /* 图片组件 */
            ImageLoader.getInstance().init(config);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    public static String getDataDir(Context context) {
        String state = Environment.getExternalStorageState();
        String dir = null;
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            dir = context.getFilesDir().getAbsolutePath();
        }
        return dir;
    }

}
