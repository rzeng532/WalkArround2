package com.awalk.walkarround.message.activity;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.*;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.*;
import com.awalk.walkarround.R;
import com.awalk.walkarround.base.WalkArroundApp;
import com.awalk.walkarround.util.AppConstant;
import com.awalk.walkarround.util.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class VideoRelativeLayout extends RelativeLayout implements OnErrorListener {
    private static final Logger logger = Logger.getLogger(VideoRelativeLayout.class.getSimpleName());
    private Context mContext;
    private ImageView mTopView, mSureCancelView;
    private RelativeLayout mBottomLayout;
    private FrameLayout mFrameLayout;
    private int mRecordWidth;
    private int mRecordHeight;
    private int mRecordMaxTime;
    private SurfaceView mSurfaceView;
    private ProgressBar mProgressBar;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private int mBestWidth;
    private int mBestHeight;
    private File mVecordFile;
    private MediaRecorder mMediaRecorder;
    private int mTimeCount;// 时间计数
    private Timer mTimer;
    private final int PROGRESSRATE = 300;// 进度条变化频率，毫秒为单位
    private Button mTakeVideoButton;
    private TextView mUpCouldCancelTxtView, mRecordingTimeTxtView, mPressToTakeVideoTxtView;
    private OnFinishListener mFinishListener;
    private boolean mIsReleased;
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                mProgressBar.setProgress(mTimeCount);
                timeViewChange();
            } else if (msg.what == 1) {
                mFinishListener.onFinish();
            } else if (msg.what == 2) {
                mProgressBar.setProgress(0);
            }
        }

    };

    public interface OnFinishListener {
        public void onFinish();
    }

    public VideoRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoRelativeLayout(Context context) {
        this(context, null);
    }

    public VideoRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyRelativeRecordLayout, defStyle, 0);
        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.MyRelativeRecordLayout_recordWidth) {
                mRecordWidth = a.getInteger(R.styleable.MyRelativeRecordLayout_recordWidth, 320);// 默认320
            } else if (attr == R.styleable.MyRelativeRecordLayout_recordHeight) {
                mRecordHeight = a.getInteger(R.styleable.MyRelativeRecordLayout_recordHeight, 240);// 默认240
            } else if (attr == R.styleable.MyRelativeRecordLayout_record_maxtime) {
                mRecordMaxTime = a.getInteger(R.styleable.MyRelativeRecordLayout_record_maxtime, 10);// 默认为10
            }
        }
        a.recycle();

        LayoutInflater.from(context).inflate(R.layout.message_movie_relativelayout, this);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTakeVideoButton = (Button) findViewById(R.id.take_video_button);
        mUpCouldCancelTxtView = (TextView) findViewById(R.id.recording_if_cencel_txt);
        mRecordingTimeTxtView = (TextView) findViewById(R.id.recording_time_txt);
        mPressToTakeVideoTxtView = (TextView) findViewById(R.id.press_video_txt);
        mTopView = (ImageView) findViewById(R.id.topView);
        mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);
        mFrameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        mSureCancelView = (ImageView) findViewById(R.id.cancel_button);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceView.setZOrderOnTop(true);
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(new CustomCallBack());
        mProgressBar.setMax(mRecordMaxTime * 1000);
    }

    private class CustomCallBack implements Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                initCamera();
            } catch (Exception e) {
                logger.e("CustomCallBack surfaceCreated Exception:" + e.getMessage());
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            freeCameraResource();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.e("FT", "surfaceChanged");
        }

    }

    /**
     * 创建摄像头
     */
    private void initCamera() throws IOException {
        if (mCamera != null) {
            freeCameraResource();
        }

        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            logger.e("initCamera Exception:" + e.getMessage());
            freeCameraResource();
        }

        updateCameraParameters();
        mCamera.setPreviewDisplay(mSurfaceHolder);
        mCamera.startPreview();
        Log.e("FT", "initCamera");
    }

    /**
     * 配置摄像头参数
     */
    private void updateCameraParameters() {
        if (mCamera != null) {
            Camera.Parameters p = mCamera.getParameters();

            long time = new Date().getTime();
            p.setGpsTimestamp(time);

            Size previewSize = findBestPreviewSize(p);
            p.setPreviewSize(previewSize.width, previewSize.height);
            // p.setPictureSize(previewSize.width, previewSize.height);

            if (mContext.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                mCamera.setDisplayOrientation(90);
                p.setRotation(90);
            }

            if (p.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (p.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            mCamera.setParameters(p);
        }
    }

    /**
     * 找到最合适的摄像头分辨率，防止预览变形
     * 
     * @param parameters
     *            摄像头参数
     * @return
     */
    private Size findBestPreviewSize(Camera.Parameters parameters) {
        // 系统支持的现有分辨率
        String previewSizeValueString = null;
        previewSizeValueString = parameters.get("preview-size-values");

        if (previewSizeValueString == null) {
            previewSizeValueString = parameters.get("preview-size-value");
        }

        if (previewSizeValueString == null) {// 有些手机例如m9获取不到支持的预览大小 就直接返回屏幕大小
            return mCamera.new Size(getScreenWH().widthPixels, getScreenWH().heightPixels);
        }
        float bestX = 0;
        float bestY = 0;

        float tmpRadio = 0;
        float viewRadio = 0;
        if (mRecordWidth != 0 && mRecordHeight != 0) {
            viewRadio = Math.min((float) mRecordWidth, (float) mRecordHeight)
                    / Math.max((float) mRecordWidth, (float) mRecordHeight);
        }

        String COMMA_PATTERN[] = previewSizeValueString.split(",");
        for (String prewsizeString : COMMA_PATTERN) {
            prewsizeString = prewsizeString.trim();

            int dimPosition = prewsizeString.indexOf('x');
            if (dimPosition == -1) {
                continue;
            }
            float newX = 0;
            float newY = 0;

            try {
                newX = Float.parseFloat(prewsizeString.substring(0, dimPosition));
                newY = Float.parseFloat(prewsizeString.substring(dimPosition + 1));
            } catch (NumberFormatException e) {
                continue;
            }

            float radio = Math.min(newX, newY) / Math.max(newX, newY);
            if (tmpRadio == 0) {
                tmpRadio = radio;
                bestX = newX;
                bestY = newY;
            } else if (tmpRadio != 0
                    && (Math.abs(radio - viewRadio) <= Math.abs(tmpRadio - viewRadio))
                    && (Math.abs(newX * newY - mRecordWidth * mRecordHeight) <= Math.abs(bestX * bestY - mRecordWidth
                            * mRecordHeight))) {
                tmpRadio = radio;
                bestX = newX;
                bestY = newY;
            }
        }
        if (bestX > 0 && bestY > 0) {
            mBestWidth = (int) bestX;
            mBestHeight = (int) bestY;
            return mCamera.new Size((int) bestX, (int) bestY);
        }
        return null;
    }

    /**
     * 释放摄像头资源
     */
    private void freeCameraResource() {
        if (mCamera != null) {
            mCamera.lock();
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 创建视频文件
     */
    private void createRecordDir() {
        File sampleDir = new File(WalkArroundApp.MTC_DATA_PATH + AppConstant.VIDEO_FILE_PATH);
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }
        try {
            mVecordFile = File.createTempFile("recording", ".mp4", sampleDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initRecord() throws IOException {
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.reset();
        if (mCamera != null) {
            mCamera.unlock();
            mMediaRecorder.setCamera(mCamera);
        }
        mMediaRecorder.setOnErrorListener(this);
        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        mMediaRecorder.setVideoSource(VideoSource.CAMERA);// 视频源
        mMediaRecorder.setAudioSource(AudioSource.MIC);// 音频源
        mMediaRecorder.setOutputFormat(OutputFormat.MPEG_4);// 视频输出格式
        mMediaRecorder.setAudioEncoder(AudioEncoder.AAC);// 音频格式
        CamcorderProfile profile;
        try {
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_CIF);
            mMediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
        } catch (Exception e) {
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
            mMediaRecorder.setVideoSize(mBestWidth, mBestHeight);
        }
        mMediaRecorder.setVideoEncodingBitRate(1 * 512 * 1024);// 设置帧频率，然后就清晰了
        mMediaRecorder.setOrientationHint(90);// 输出旋转90度，保持竖屏录制
        if (MediaRecorder.VideoEncoder.H264 == profile.videoCodec) {// 设置录制的视频编码
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        } else {
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        }
        mMediaRecorder.setOutputFile(mVecordFile.getAbsolutePath());
        mMediaRecorder.prepare();
        try {
            mMediaRecorder.start();
        } catch (IllegalStateException e) {
            logger.e("initRecord IllegalStateException:" + e.getMessage());
        } catch (RuntimeException e) {
            logger.e("initRecord RuntimeException:" + e.getMessage());
        } catch (Exception e) {
            logger.e("initRecord Exception:" + e.getMessage());
        }
    }

    /**
     * 开始录制视频
     */
    public void record(OnFinishListener finishListener) {
        mFinishListener = finishListener;
        createRecordDir();
        try {
            initRecord();
        } catch (IOException e) {
            logger.e("record Exception:" + e.getMessage());
        }

        mTimeCount = mRecordMaxTime * 1000;// 时间计数器重新赋值
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                mTimeCount = mTimeCount - PROGRESSRATE;
                if (mTimeCount <= 0) {
                    stop();
                    handler.sendEmptyMessage(1);
                }
                handler.sendEmptyMessage(0);
            }
        }, 0, PROGRESSRATE);
    }

    /**
     * 时间控件变化
     */
    private void timeViewChange() {
        int lastlooseTime = (int) ((mTimeCount + PROGRESSRATE) / 1000);// 上次的所剩时间
        int looseTime = (int) (mTimeCount / 1000);// 现在的所剩时间
        if (looseTime != lastlooseTime) {
            String time = "";
            if (looseTime < 10) {
                time = "0:0" + looseTime + "'";
            } else {
                time = "0:" + looseTime + "'";
            }
            mRecordingTimeTxtView.setText(time);
        }
    }

    /**
     * 停止拍摄
     */
    public void stop() {
        mIsReleased = true;
        stopRecord();
        releaseRecord();
        freeCameraResource();
    }

    /**
     * 停止拍摄，恢复如初，但不释放资源
     */
    public void cancelRecord() {
        if (mTimer != null){
            mTimer.cancel();
        }
        if(mCamera != null) {
            mCamera.lock();
        }

        stopRecord();
        releaseRecord();
    }

    /**
     * 停止录制
     */
    private void stopRecord() {
        handler.sendEmptyMessage(2);
        // mProgressBar.setProgress(0);
        if (mTimer != null)
            mTimer.cancel();
        if (mMediaRecorder != null) {
            // 设置后不会崩溃
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            try {
                mMediaRecorder.stop();
            } catch (IllegalStateException e) {
                logger.e("stopRecord IllegalStateException:" + e.getMessage());
            } catch (RuntimeException e) {
                logger.e("stopRecord RuntimeException:" + e.getMessage());
            } catch (Exception e) {
                logger.e("stopRecord Exception:" + e.getMessage());
            }
        }
    }

    /**
     * 释放资源
     */
    private void releaseRecord() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.release();
            } catch (IllegalStateException e) {
                logger.e("releaseRecord IllegalStateException:" + e.getMessage());
            } catch (Exception e) {
                logger.e("releaseRecord Exception:" + e.getMessage());
            }
        }
        mMediaRecorder = null;
    }

    /**
     * 删除录像带 mp4文件
     */
    public void deleteTheFile() {
        if (mVecordFile != null && mVecordFile.exists()) {
            mVecordFile.delete();
        }
    }

    protected DisplayMetrics getScreenWH() {
        return this.getResources().getDisplayMetrics();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int thisLayoutHeight = MeasureSpec.getSize(heightMeasureSpec);
        int thisLayoutWidth = MeasureSpec.getSize(widthMeasureSpec);
        setTopLayoutSize(thisLayoutWidth, thisLayoutHeight);
        setFrameLayoutSize(thisLayoutWidth, thisLayoutHeight);
    }

    /**
     * 设置toplayout的尺寸
     */
    private void setTopLayoutSize(int relativeLayoutWidth, int relativeLayoutHeight) {
        android.view.ViewGroup.LayoutParams lp = mTopView.getLayoutParams();
        lp.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = relativeLayoutHeight - relativeLayoutWidth * 3 / 4 - mBottomLayout.getMeasuredHeight();
        mTopView.setLayoutParams(lp);
    }

    /**
     * 设置frameLayout的尺寸，主要是surfaceView高度的控制，这里高度设置为宽度的1.5倍，1是为了符合摄像头的分辨率比例，2是为了后期直接裁剪掉一半的高度。
     */
    private void setFrameLayoutSize(int relativeLayoutWidth, int relativeLayoutHeight) {
        android.view.ViewGroup.LayoutParams lp = mFrameLayout.getLayoutParams();
        lp.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = relativeLayoutWidth * 3 / 2;
        mFrameLayout.setLayoutParams(lp);
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        logger.e("onError:what = " + what + ", extra=" + extra);
//        try {
//            if (mr != null)
//                mr.reset();
//        } catch (IllegalStateException e) {
//            logger.e("releaseRecord IllegalStateException:" + e.getMessage());
//        } catch (Exception e) {
//            logger.e("releaseRecord Exception:" + e.getMessage());
//        }
    }

    public Button getmTakeVideoButton() {
        return mTakeVideoButton;
    }

    public TextView getmUpCouldCancelTxtView() {
        return mUpCouldCancelTxtView;
    }

    public TextView getmRecordingTimeTxtView() {
        return mRecordingTimeTxtView;
    }

    public TextView getmPressToTakeVideoTxtView() {
        return mPressToTakeVideoTxtView;
    }

    public ImageView getmSureCancelView() {
        return mSureCancelView;
    }

    public void removeHandlerMessageAndCallback() {
        handler.removeCallbacksAndMessages(null);
    }
    
    public boolean getReleasedStatue(){
        return mIsReleased;
    }

    public File getVideoFile() {
        return mVecordFile;
    }

    /**
     * 返回拍摄的时间长度
     * 
     * @return 单位：秒
     */
    public int getRecordTime() {
        return (int) ((mRecordMaxTime * 1000.0 - mTimeCount) / 1000);
    }
}
