/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.example.walkarround.util.image;

import android.graphics.Bitmap;
import com.example.walkarround.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

/**
 * TODO: description
 * Date: 2015-12-08
 *
 * @author Administrator
 */
public class ImageOptionsManager {

    /**
     * 联系人头像图片option
     */
    private DisplayImageOptions headImageOptions;
    /**
     * 公众号默认头像option
     */
    private DisplayImageOptions publicImageOptions;
    /**
     * 默认图片option
     */
    private DisplayImageOptions defaultImageLoadOptions;
    /**
     * 空图片option
     */
    private DisplayImageOptions emptyImageLoadOptions;

    // 内部类实现懒汉式
    private static class SingletonHolder {
        // 单例变量
        private static ImageOptionsManager instance = new ImageOptionsManager();
    }

    // 私有化的构造方法，保证外部的类不能通过构造器来实例化
    private ImageOptionsManager() {
        super();
    }

    // 获取单例对象实例
    public static ImageOptionsManager getInstance() {
        return SingletonHolder.instance;
    }

    public DisplayImageOptions getUserHead() {
        if (null == headImageOptions) {
            headImageOptions = new DisplayImageOptions.Builder().showImageOnFail(R.drawable.default_profile_portrait)
                    .showImageForEmptyUri(R.drawable.default_profile_portrait).cacheInMemory(true).cacheOnDisk(true)
                    .displayer(new RoundedBitmapDisplayer(360))
                    .considerExifParams(true)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565).build();
        }
        return headImageOptions;
    }

    public DisplayImageOptions getPublicdefault() {
        if (null == publicImageOptions) {
            publicImageOptions = new DisplayImageOptions.Builder()

                    .showImageOnFail(R.drawable.default_profile_portrait_grey)
                    .showImageForEmptyUri(R.drawable.default_profile_portrait_grey).cacheInMemory(true).cacheOnDisk(true)
                    .considerExifParams(true)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565).build();
        }
        return publicImageOptions;
    }

    public DisplayImageOptions getLoadDefaultOptions() {
        if (null == defaultImageLoadOptions) {
            defaultImageLoadOptions = new DisplayImageOptions.Builder().showImageOnFail(R.drawable.default_image)
                    .showImageForEmptyUri(R.drawable.default_image).cacheInMemory(true).cacheOnDisk(true)
                    .considerExifParams(true)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565).build();
        }
        return defaultImageLoadOptions;
    }

    public DisplayImageOptions getEmptyOptions() {
        if (null == emptyImageLoadOptions) {
            emptyImageLoadOptions = new DisplayImageOptions.Builder()
                    .showImageOnFail(R.drawable.empty_bg)
                    .cacheInMemory(true).cacheOnDisk(true)
                    .considerExifParams(true)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                    .bitmapConfig(Bitmap.Config.RGB_565).build();
        }
        return emptyImageLoadOptions;
    }
}
