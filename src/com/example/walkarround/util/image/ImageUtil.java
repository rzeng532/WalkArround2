package com.example.walkarround.util.image;

import android.content.Context;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import com.example.walkarround.base.WalkArroundApp;

import java.io.*;

/**
 * Image tools
 *
 * @author wei.chen
 */
public class ImageUtil {

    /**
     * 缩放图片
     *
     * @param bitmap
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        int bmpWidth = 0, bmpHeight = 0;
        if (bitmap == null || (bmpWidth = bitmap.getWidth()) <= 0 || (bmpHeight = bitmap.getHeight()) <= 0) {
            return null;
        }

        Matrix matrix = new Matrix();
        matrix.postScale((float) newWidth / bmpWidth, (float) newHeight / bmpHeight);
        try {
            return Bitmap.createBitmap(bitmap, 0, 0, bmpWidth, bmpHeight, matrix, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Bitmap zoomBitmapToSquare(Bitmap bitmap) throws OutOfMemoryError {
        if (bitmap == null) {
            return null;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        if (w > h) {
            return zoomBitmap(bitmap, h, h);
        } else {
            return zoomBitmap(bitmap, w, w);
        }
    }

    /**
     * 圆形图
     *
     * @param oldBitmap
     * @return
     */
    public static Bitmap toOvalBitmap(Bitmap oldBitmap) throws OutOfMemoryError {
        if (oldBitmap == null) {
            return null;
        }
        Bitmap bitmap = ImageUtil.zoomBitmapToSquare(oldBitmap);
        final int ratio = 2;

        Bitmap output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getWidth(), Config.ARGB_4444);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, bitmap.getWidth() / ratio, bitmap.getHeight() / ratio, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rectF, paint);
        return output;
    }

    private static final int MAX_IMAGE_HEIGHT = 768;
    private static final int MAX_IMAGE_WIDTH = 1024;
    private static final int MAX_IMAGE_SIZE = 200 * 1024; // max 500k

    public static Bitmap zipPicture(Context context, Uri picUri) {
        if (picUri == null) {
            return null;
        }

        final UriImage uriImage = new UriImage(context, picUri);

        int width = uriImage.getWidth();
        int height = uriImage.getHeight();

        Bitmap pic = null;
        // Log.d("XXX", "^^^^^^^^^^^^^^^^^^Original picture size: " + size/1024 + " K, "+ "width: " + width +" height: "
        // + height);
        if (width > MAX_IMAGE_WIDTH || height > MAX_IMAGE_HEIGHT || uriImage.getSizeOfImage() > MAX_IMAGE_SIZE) {
            final byte[] result = uriImage.getResizedImageData(MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT, MAX_IMAGE_SIZE);
            if (result == null) {
                Log.e("XXX", "Fail to zip picture, the original size is " + width + " * " + height);
                return null;
            }
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inPurgeable = true;
            // option.inSampleSize = 4; //too aggressive: the bitmap will be uploaded to server, not the the thumbnail
            pic = BitmapFactory.decodeByteArray(result, 0, result.length, option);
            // Log.d("XXX", "^^^^^^^^^^^^^^^^^^Zipped picture width: " + pic.getWidth() +" height: "+ pic.getHeight());
        } else {
            try {
                pic = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(picUri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return pic;
    }

    // 根据指定的图像路径和大小来获取缩略图
    public static Bitmap getThumbPicture(Context context, Uri picUri) {
        if (picUri == null) {
            return null;
        }

        final UriImage uriImage = new UriImage(context, picUri);

        int width = uriImage.getWidth();
        int height = uriImage.getHeight();

        Bitmap pic = null;
        // Log.d("XXX", "^^^^^^^^^^^^^^^^^^Original picture size: " + size/1024 + " K, "+ "width: " + width +" height: "
        // + height);
        final byte[] result = uriImage.getResizedImageData(MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT, MAX_IMAGE_SIZE);
        if (result == null) {
            Log.e("XXX", "Fail to zip picture, the original size is " + width + " * " + height);
            return null;
        }
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inPurgeable = true;
        option.inSampleSize = 2;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        do {
            baos.reset();
            option.inSampleSize++; // too aggressive: the bitmap will be uploaded to server, not the the thumbnail
            pic = BitmapFactory.decodeByteArray(result, 0, result.length, option);
            pic.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        } while (baos.toByteArray().length / 1024 > 10);

        // Log.d("XXX", "^^^^^^^^^^^^^^^^^^Zipped picture width: " + pic.getWidth() +" height: "+ pic.getHeight());
        return pic;
    }

    public static Bitmap getThumbPicture(Context context, Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Bitmap pic = null;
        // Log.d("XXX", "^^^^^^^^^^^^^^^^^^Original picture size: " + size/1024 + " K, "+ "width: " + width +" height: "
        // + height);
        final byte[] result = bitmapToBytes(bitmap);

        if (result == null) {
            Log.e("XXX", "Fail to zip picture, the original size is " + width + " * " + height);
            return null;
        }
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inPurgeable = true;
        option.inSampleSize = 2;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        do {
            baos.reset();
            option.inSampleSize++; // too aggressive: the bitmap will be uploaded to server, not the the thumbnail
            pic = BitmapFactory.decodeByteArray(result, 0, result.length, option);
            pic.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        } while (baos.toByteArray().length / 1024 > 10);

        // Log.d("XXX", "^^^^^^^^^^^^^^^^^^Zipped picture width: " + pic.getWidth() +" height: "+ pic.getHeight());
        return pic;
    }

    public static byte[] bitmapToBytes(Bitmap bm) {
        if (bm == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap bytesToBitmap(byte[] b) {
        if (b != null && b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    public static boolean saveBitmapToFile(String path, String bitName, Bitmap mBitmap) {
        File f = new File(path + bitName);
        try {
            if (!f.exists()) {
                boolean createNewFile = f.createNewFile();
                if (!createNewFile) {
                    return false;
                }
            }

            FileOutputStream fOut = new FileOutputStream(f);

            if (null != mBitmap) {
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            }
            fOut.flush();
            fOut.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Bitmap rotatePicture(Bitmap sourcePic, int degree) {
        if (sourcePic == null) {
            return null;
        }

        Bitmap rotatedBitmap = sourcePic;

        if (degree != 0) {
            //boolean mutable = sourcePic.isMutable();
            // Log.e("XXX", "Picture is mutable? " + mutable);
            Matrix matrix = new Matrix();
            matrix.postRotate(degree);
            rotatedBitmap = Bitmap.createBitmap(sourcePic, 0, 0, sourcePic.getWidth(), sourcePic.getHeight(), matrix,
                    false);
            sourcePic.recycle();
            sourcePic = null; // release ASAP
        }
        return rotatedBitmap;
    }

    /**
     * 根据指定的图像路径和大小来获取缩略图 此方法有两点好处： 1. 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
     * 第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。 2. 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使 用这个工具生成的图像不会被拉伸。
     *
     * @param imagePath 图像的路径
     * @param width     指定输出图像的宽度
     * @param height    指定输出图像的高度
     * @return 生成的缩略图
     */
    public static Bitmap getImageThumbnail(String imagePath, int width, int height) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高，注意此处的bitmap为null
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        options.inJustDecodeBounds = false; // 设为 false
        // 计算缩放比
        int h = options.outHeight;
        int w = options.outWidth;
        int beWidth = w / width;
        int beHeight = h / height;
        int be = 1;
        if (beWidth < beHeight) {
            be = beWidth;
        } else {
            be = beHeight;
        }
        if (be <= 0) {
            be = 1;
        }
        options.inSampleSize = be;
        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    public static void saveMyBitmap(String fileName, Bitmap mBitmap) {
        File f = new File(fileName);
        try {
            f.createNewFile();
        } catch (IOException e) {
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
            if (null != fOut) {
                fOut.flush();
                fOut.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static String compressImage(String newFilePath, String imagePath) {
        String newImagePath;
        try {
            File f = new File(imagePath);
            if (f.length() > CompressPicUtil.MAX_IMAGE_SIZE) {
                try {
                    CompressPicUtil.compressImage(WalkArroundApp.getInstance(), imagePath, newFilePath, 70);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                File newF = new File(newFilePath);
                if (newF.exists() && newF.length() < f.length()) {
                    newImagePath = newFilePath;
                } else {
                    newImagePath = imagePath;
                }

            } else {
                newImagePath = imagePath;
            }
            return newImagePath;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 图片去色,返回灰度图片
     *
     * @param bmpOriginal 传入的图片
     * @return 去色后的图片
     */
    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public static Bitmap convertViewToBitmap(View view) {
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();

        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    /**
     * 将图片的角度置为0
     * 
     * @Title: setPictureDegreeZero
     * @param path
     * @return void
     * @date 2012-12-10 上午10:54:46
     */
    public static void writePictureDegreeZero(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            // 修正图片的旋转角度，设置其不旋转。这里也可以设置其旋转的角度，可以传值过去，
            // 例如旋转90度，传值ExifInterface.ORIENTATION_ROTATE_90，需要将这个值转换为String类型的
            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, "no");
            exifInterface.saveAttributes();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // 获取图片需要旋转的角度（到0度）
    // -1是非法值
    public static int getDegreeForRotateToZero(String path) {
        int degree = 0;

        if (TextUtils.isEmpty(path)) {
            return -1;
        }
        File file = new File(path);
        if (!file.exists()) {
            return -1;
        }

        try {
            ExifInterface exifInterface = new ExifInterface(path);

            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = -90;
                break;
            default:
                degree = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return degree;
    }

    // 旋转图片（以某个角度）
    public static void rotatePictureWithDegree(String picPath, float degree) {
        if (TextUtils.isEmpty(picPath)) {
            return;
        }
        File file = new File(picPath);
        if (!file.exists()) {
            return;
        }

        Bitmap newBitmap = null;
        int picWidth = 0;
        int picHeight = 0;
        Bitmap oldBitmap = BitmapFactory.decodeFile(picPath);

        if(oldBitmap != null) {
            picWidth = oldBitmap.getWidth();
            picHeight = oldBitmap.getHeight();
        }

        Matrix matrix = new Matrix();
        matrix.setRotate(degree);
        newBitmap = Bitmap.createBitmap(oldBitmap, 0, 0, picWidth, picHeight, matrix, true);

        // 3.保存Bitmap
        try {
            FileOutputStream fos = null;
            fos = new FileOutputStream(file);
            if (null != fos) {
                String path = picPath.toUpperCase();
                if (path.endsWith("WEBP")) {
                    newBitmap.compress(Bitmap.CompressFormat.WEBP, 100, fos);
                } else if (path.endsWith("PNG")) {
                    newBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                } else {
                    newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                }

                fos.flush();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (oldBitmap != null && !oldBitmap.isRecycled()) {
                oldBitmap.recycle();
                oldBitmap = null;
            }
            if (newBitmap != null && !newBitmap.isRecycled()) {
                newBitmap.recycle();
                newBitmap = null;
            }
        }
    }
}
