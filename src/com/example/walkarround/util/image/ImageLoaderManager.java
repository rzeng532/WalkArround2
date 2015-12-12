/**
 * Copyright (C) 2014-2015 CMCC All rights reserved
 */
package com.example.walkarround.util.image;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;
import com.example.walkarround.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.io.File;

/**
 * TODO: description
 * Date: 2015-12-08
 *
 * @author Administrator
 */
public class ImageLoaderManager {

    public static String URL_PATH_SYMBOL = "http";
    public static String LOCAL_URL_PATH_SYMBOL = "file://";

    /**
     * 本地/网络图片的实际地址
     *
     * @param url 本地/网络图片地址
     * @return
     */
    public static String getImageLoadPath(String url) {
        String newUrl = url;
        if (url != null && url.toLowerCase().startsWith(LOCAL_URL_PATH_SYMBOL)) {
            newUrl = url.substring(LOCAL_URL_PATH_SYMBOL.length());
        }
        return newUrl;
    }

    /**
     * 同步获取图片
     *
     * @param url 图片地址
     * @return
     */
    public static Bitmap getSyncImage(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        String newUrl = url;
        if (!TextUtils.isEmpty(url) && new File(url).exists()) {
            newUrl = LOCAL_URL_PATH_SYMBOL + url;
        }
        return ImageLoader.getInstance().loadImageSync(newUrl);
    }

    /**
     * 缓存下载图片
     *
     * @param url 图片地址
     */
    public static void loadImage(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        ImageLoader.getInstance().loadImage(url, null);
    }

    /**
     * 带回调的缓存图片
     */
    public static void loadImage(String url, ImageLoadingListener imageLoadingListener) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        //本地图片
        if (!url.toLowerCase().startsWith(URL_PATH_SYMBOL)) {
            String newUrl = LOCAL_URL_PATH_SYMBOL + url;
            ImageLoader.getInstance().loadImage(newUrl, imageLoadingListener);
        } else {//网络图片
            ImageLoader.getInstance().loadImage(url, imageLoadingListener);
        }
    }

    /**
     * 删除缓存的图片
     *
     * @param url 缓存的图片的地址
     */
    public static void removeMemoryCache(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        DiskCacheUtils.removeFromCache(url, ImageLoader.getInstance().getDiskCache());
        MemoryCacheUtils.removeFromCache(url, ImageLoader.getInstance().getMemoryCache());
    }

    /**
     * 显示本地/网络图片到targetView
     *
     * @param url        本地/网络图片地址
     * @param targetView 目标View
     */
    public static void displayImage(String url, ImageView targetView) {
        displayImage(url, -1, targetView);
    }

    /**
     * 显示本地/网络图片到targetView
     *
     * @param url        本地/网络图片地址
     * @param targetView 目标View
     * @param listener   图片加载过程监听
     */
    public static void displayImage(String url, int resId, ImageView targetView, ImageLoadingListener listener) {
        if (targetView == null) {
            return;
        }
        DisplayImageOptions displayOptions = getDisplayImageOptions(resId);
        if (!url.toLowerCase().startsWith(URL_PATH_SYMBOL)) {
            String newUrl = LOCAL_URL_PATH_SYMBOL + url;
            ImageLoader.getInstance().displayImage(newUrl, targetView, displayOptions, listener);
        } else {
            ImageLoader.getInstance().displayImage(url, targetView, displayOptions, listener);
        }
    }

    /**
     * 优先加载本地图片，若本地图片为空则显示网络图片
     *
     * @param localFilePath 本地图片地址
     * @param url           网络图片地址
     * @param targetView    目标View
     */
    public static void displayImage(String localFilePath, String url, ImageView targetView) {
        displayImage(localFilePath, url, -1, targetView);
    }

    /**
     * 显示url地址的图片到targetView
     *
     * @param url          本地/网络图片地址
     * @param defaultResId 加载失败显示的图片，<0则显示透明
     * @param targetView   目标View
     */
    public static void displayImage(String url, int defaultResId, ImageView targetView) {
        DisplayImageOptions displayOptions = getDisplayImageOptions(defaultResId);
        String newUrl = url;
        if (!TextUtils.isEmpty(url) && new File(url).exists()) {
            newUrl = LOCAL_URL_PATH_SYMBOL + url;
        }
        if (displayOptions == null) {
            ImageLoader.getInstance().displayImage(newUrl, targetView);
        } else {
            ImageLoader.getInstance().displayImage(newUrl, targetView, displayOptions);
        }
    }

    /**
     * 显示url地址的图片到targetView
     *
     * @param url            本地/网络图片地址
     * @param targetView     目标View
     * @param displayOptions 图片配置
     */
    public static void displayImage(String url, ImageView targetView, DisplayImageOptions displayOptions) {
        String newUrl = url;
        if (!TextUtils.isEmpty(url) && new File(url).exists()) {
            newUrl = LOCAL_URL_PATH_SYMBOL + url;
        }
        if (displayOptions == null) {
            ImageLoader.getInstance().displayImage(newUrl, targetView);
        } else {
            ImageLoader.getInstance().displayImage(newUrl, targetView, displayOptions);
        }
    }

    /**
     * 优先加载本地图片，若本地图片为空则显示网络图片
     *
     * @param localFilePath 本地图片地址
     * @param url           网络图片地址
     * @param defaultResId  加载失败显示的图片，<0则显示透明
     * @param targetView    目标View
     */
    public static void displayImage(String localFilePath, String url, int defaultResId, ImageView targetView) {
        if (targetView == null) {
            return;
        }
        DisplayImageOptions displayOptions = getDisplayImageOptions(defaultResId);
        if (!TextUtils.isEmpty(localFilePath) && new File(localFilePath).exists()) {
            String newUrl = LOCAL_URL_PATH_SYMBOL + localFilePath;
            ImageLoader.getInstance().displayImage(newUrl, targetView, displayOptions);
        } else {
            // 没有本地图片
            ImageLoader.getInstance().displayImage(url, targetView, displayOptions);
        }
    }

    /**
     * 图片显示option配置
     *
     * @param resId 失败时显示的图片资源
     * @return
     */
    public static DisplayImageOptions getDisplayImageOptions(int resId) {
        DisplayImageOptions displayOptions = null;
        if (resId == 0 || resId == R.drawable.default_image) {
            displayOptions = ImageOptionsManager.getInstance().getLoadDefaultOptions();
        } else if (resId == R.drawable.default_profile_portrait) {
            displayOptions = ImageOptionsManager.getInstance().getUserHead();
        } else if (resId == R.drawable.default_profile_portrait_grey) {
            displayOptions = ImageOptionsManager.getInstance().getPublicdefault();
        } else if (resId > 0) {
            displayOptions = new DisplayImageOptions.Builder()
                    .showImageOnFail(resId)
                    .showImageForEmptyUri(resId)
                    .considerExifParams(true)
                    .cacheInMemory(true).cacheOnDisk(true)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                    .bitmapConfig(Bitmap.Config.RGB_565).build();
        } else {
            displayOptions = ImageOptionsManager.getInstance().getEmptyOptions();
        }
        return displayOptions;
    }
}
