/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.example.walkarround.base;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import com.avos.avoscloud.AVOSCloud;
import com.example.walkarround.R;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.Logger;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

/**
 * TODO: description
 * Date: 2015-11-26
 *
 * @author Richard
 */
public class WalkArroundApp extends Application {
    private static WalkArroundApp mWorkArroundApp = null;
    private static Logger logger = Logger.getLogger(WalkArroundApp.class.getSimpleName());
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

}
