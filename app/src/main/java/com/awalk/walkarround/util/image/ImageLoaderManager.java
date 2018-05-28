/**
 * Copyright (C) 2014-2015 All rights reserved
 */
package com.awalk.walkarround.util.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

/**
 * TODO: description
 * Date: 2015-12-08
 *
 * @author Administrator
 */
public class ImageLoaderManager {

    /**
     * 显示本地/网络图片到targetView
     *
     * @param url        本地/网络图片地址
     * @param targetView 目标View
     */
    public static void displayImage(Context context, String url, ImageView targetView) {
        displayImage(context, url, -1, targetView);
    }


    /**
     * 显示url地址的图片到targetView
     *
     * @param url          本地/网络图片地址
     * @param defaultResId 加载失败显示的图片，<0则显示透明
     * @param targetView   目标View
     */
    public static void displayImage(Context context, String url, int defaultResId, ImageView targetView) {
        if (defaultResId > 0) {
            Glide.with(context).load(url)
                    .error(defaultResId)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(targetView);
        } else {
            Glide.with(context).load(url)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(targetView);
        }
    }

    /**
     * 根据原图和变长绘制圆形图片
     *
     * @param source
     * @return
     */
    public static Bitmap createCircleImage(Bitmap source) {
        if (source == null) {
            return null;
        }
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        int min = source.getWidth();
        Bitmap target = Bitmap.createBitmap(min, min, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(target);
        // 绘制圆形
        canvas.drawCircle(min / 2, min / 2, min / 2, paint);
        //使用SRC_IN
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //绘制图片
        canvas.drawBitmap(source, 0, 0, paint);
        return target;
    }
}
