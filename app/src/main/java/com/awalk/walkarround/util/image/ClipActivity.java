package com.awalk.walkarround.util.image;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import com.avos.avoscloud.AVException;
import com.awalk.walkarround.R;
import com.awalk.walkarround.base.view.DialogFactory;
import com.awalk.walkarround.myself.manager.ProfileManager;
import com.awalk.walkarround.util.AsyncTaskListener;
import com.awalk.walkarround.util.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;


public class ClipActivity extends FragmentActivity implements View.OnClickListener {
    private static final Logger sMyLogger = Logger.getLogger(ClipActivity.class.getSimpleName());
    private ClipImageView ivPic;
    private Dialog mLoadingDialog;
    private Intent data;

    private final int UPDATE_PORTRAIT_OK = 0;
    private final int UPDATE_PORTRAIT_FAIL = 1;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == UPDATE_PORTRAIT_OK) {
                setResult(RESULT_OK, data);
                dismissDialog();
                finish();
            } else {
                setResult(RESULT_CANCELED, data);
                dismissDialog();
                finish();
            }
        }
    };

    private AsyncTaskListener mUpdateListener = new AsyncTaskListener() {
        @Override
        public void onSuccess(Object data) {
            Message msg = Message.obtain();
            msg.what = UPDATE_PORTRAIT_OK;
            handler.sendMessageDelayed(msg, 1000);
        }

        @Override
        public void onFailed(AVException e) {
            Message msg = Message.obtain();
            msg.what = UPDATE_PORTRAIT_FAIL;
            handler.sendMessageDelayed(msg, 1000);
        }
    };

    private void uploadHead(String headPath) {
        ProfileManager.getInstance().updatePortrait(headPath, mUpdateListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_clip);
        WindowManager wm = (WindowManager)this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        int screenWidth = outMetrics.widthPixels;
        int screenHeight = outMetrics.heightPixels;
        String path = getIntent().getStringExtra("path");
//		setRightText("确定");
        findViewById(R.id.tvConfirm).setOnClickListener(this);
        findViewById(R.id.tvCancel).setOnClickListener(this);
        ivPic = (ClipImageView) findViewById(R.id.ivPic);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        float scaleX = (float) options.outWidth / (float) screenWidth;
        float scaleY = (float) options.outHeight / (float) screenHeight;
        if (scaleX > scaleY && scaleX > 1) {
            options.inSampleSize = (int) Math.ceil(scaleX);
        }
        if (scaleY > scaleX && scaleY > 1) {
            options.inSampleSize = (int) Math.ceil(scaleY);
        }
        options.inJustDecodeBounds = false;
        Bitmap bm = BitmapFactory.decodeFile(path, options);
        ivPic.setImageBitmap(bm);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.tvConfirm) {

            data = new Intent();
            showDialog();
            new Thread() {
                public void run() {
                    long start = System.currentTimeMillis();
                    byte[] bytes = bitmap2BytesJPG(ivPic.clip());
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    String headPath = getExternalCacheDir() + "/portrait.jpg";
                    bmpToJpg(bitmap, new File(headPath));
                    long time = System.currentTimeMillis() - start;
                    sMyLogger.e("toJPEPath:" + time);

                    //handler.sendEmptyMessage(UPDATE_PORTRAIT_OK);
                    uploadHead(headPath);
                }
            }.start();
        } else if (i == R.id.tvCancel) {
            finish();
        } else {
        }
    }

    private void bmpToJpg(Bitmap bmp, File out) {
        FileOutputStream fos = null;
        try {
            if (!out.exists()) {
                out.createNewFile();
            }
            fos = new FileOutputStream(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
        bmp.compress(Bitmap.CompressFormat.JPEG, 80, fos);
    }

    public static byte[] bitmap2BytesJPG(Bitmap bm) {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        if (bm != null) {
            bm.compress(Bitmap.CompressFormat.JPEG, 80, bas);
            return bas.toByteArray();
        }
        return null;
    }

    private void showDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = DialogFactory.getLoadingDialog(this, getString(R.string.common_please_wait_for_a_moment),
                    true, null);
            mLoadingDialog.show();
        }
    }

    private void dismissDialog() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }
    }

}
