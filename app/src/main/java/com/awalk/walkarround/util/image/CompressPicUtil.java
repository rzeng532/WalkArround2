package com.awalk.walkarround.util.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import com.awalk.walkarround.base.WalkArroundApp;
import com.awalk.walkarround.message.util.MessageUtil;
import com.awalk.walkarround.util.AppConstant;

import java.io.*;

public class CompressPicUtil {
    private static final int MAX_IMAGE_HEIGHT = 1920;
    private static final int MAX_IMAGE_WIDTH = 1080;
    public static final int MAX_IMAGE_SIZE = 500 * 1024; // max 500k

    /**
     * 压缩图片
     *
     * @param srcPath
     * @param maxSendSize
     * @return
     */
    public static String compressImage(String srcPath, int maxSendSize) {
        if (MessageUtil.isGifFile(srcPath)) {
            return srcPath;
        }
        int fileSize;
        try {
            FileInputStream fis = new FileInputStream(srcPath);
            fileSize = fis.available() / 1024; // Kb
            fis.close();
        } catch (IOException e) {
            return srcPath;
        }
        if (fileSize <= maxSendSize) {
            return srcPath;
        }
        try {
            String imageType = getFileType(srcPath);
            File folder = new File(WalkArroundApp.MTC_DATA_PATH + AppConstant.LOCATION_PIC_PATH);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            String compressPath = WalkArroundApp.MTC_DATA_PATH + AppConstant.LOCATION_PIC_PATH
                    + System.currentTimeMillis() + imageType;
            compressImage(WalkArroundApp.getInstance(), srcPath, compressPath, 70);
            return compressPath;
        } catch (Exception e) {
        }
        return srcPath;
    }

    /**
     * 获取图片格式
     *
     * @param file
     * @return
     */
    public static String getFileType(String file) {
        String[] array = file.split("\\.");
        return "." + array[array.length - 1].toLowerCase();
    }

    public static void compressImage(Context context, String filePath, String newFilePath, int q)
            throws FileNotFoundException {

        int degree = ImageUtil.readPictureDegree(filePath);

        Bitmap bm = zipPicture(context, Uri.fromFile(new File(filePath)), degree);

        if (degree != 0) {
            bm = ImageUtil.rotatePicture(bm, degree);
        }

        File outputFile = new File(newFilePath);

        FileOutputStream out = new FileOutputStream(outputFile);

        bm.compress(Bitmap.CompressFormat.JPEG, q, out);

        if (out != null) {
            try {
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        bm.recycle();
    }

    public static Bitmap zipPicture(Context context, Uri picUri, int degree) {
        if (picUri == null) {
            return null;
        }

        final UriImage uriImage = new UriImage(context, picUri);
        Bitmap pic = null;
        int maxWidth = MAX_IMAGE_WIDTH;
        int maxHeight = MAX_IMAGE_HEIGHT;
        if (degree / 90 % 2 != 0) {
            int tempSwitch = maxWidth;
            maxWidth = maxHeight;
            maxHeight = tempSwitch;
        }
        final byte[] result = uriImage.getResizedImageData(maxWidth, maxHeight, MAX_IMAGE_SIZE);
        if (result == null) {
            // Log.e("XXX", "Fail to zip picture, the original size is " + width+" * "+ height);
            return null;
        }
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inPurgeable = true;
        // option.inSampleSize = 4; //too aggressive: the bitmap will be uploaded to server, not the the thumbnail
        pic = BitmapFactory.decodeByteArray(result, 0, result.length, option);
        return pic;
    }
}
