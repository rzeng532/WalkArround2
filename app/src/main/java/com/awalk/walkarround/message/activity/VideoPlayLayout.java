package com.awalk.walkarround.message.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.*;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;
import android.widget.*;
import com.awalk.walkarround.R;
import com.awalk.walkarround.util.Logger;
import com.awalk.walkarround.util.image.ImageLoaderManager;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class VideoPlayLayout extends RelativeLayout implements OnClickListener {
    private static final Logger logger = Logger.getLogger(VideoPlayLayout.class.getSimpleName());
    private Context mContext;
    private SurfaceView mSurfaceView;
    private ProgressBar mProgressBar;
    private SurfaceHolder mSurfaceHolder;
    private MediaPlayer mMediaPlayer;
    private FrameLayout mFrameLayout;
    private ImageView mPlayView;
    private ImageView mThumbView;
    private TextView shutBtn;
    private Timer mTimer;
    private boolean mIsCancelDown;
    private String mVideoPath;
    private String mVideoThumb;
    private String mVideoThumbUrl;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // 说明视频播放完了
            if (mMediaPlayer != null) {
                if (!mMediaPlayer.isPlaying()) {
                    mProgressBar.setProgress(mProgressBar.getMax());
                    mSurfaceView.setVisibility(INVISIBLE);
                    if (mTimer != null) {
                        mTimer.cancel();
                    }
                } else {
                    int progress = mMediaPlayer.getCurrentPosition();
                    int duration = mMediaPlayer.getDuration();
                    int toprogress = (int) ((progress * 1.0 / duration) * 100.0);
                    mProgressBar.setProgress(toprogress);
                }
            }
        }

    };

    public VideoPlayLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoPlayLayout(Context context) {
        this(context, null);
    }

    public VideoPlayLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        LayoutInflater.from(mContext).inflate(R.layout.playvideo, this);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mFrameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        mPlayView = (ImageView) findViewById(R.id.playvideo);
        mThumbView = (ImageView) findViewById(R.id.thumbview);
        shutBtn = (TextView) findViewById(R.id.shutbtn);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new CustomCallBack());
        // mSurfaceHolder.setFixedSize(640, 480);
        mSurfaceView.setZOrderOnTop(true);
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mPlayView.setOnClickListener(this);
        shutBtn.setOnClickListener(this);
        mFrameLayout.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mPlayView.getVisibility() == View.GONE) {
                        mIsCancelDown = true;
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mIsCancelDown) {
                        mIsCancelDown = false;
                        mPlayView.setVisibility(View.VISIBLE);
                        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                            mMediaPlayer.pause();
                            if (mTimer != null) {
                                mTimer.cancel();
                            }
                        }
                    }
                    break;
                default:
                    break;
                }
                return false;
            }
        });

    }

    private class CustomCallBack implements Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDisplay(holder);
            if (mVideoPath != null && (mVideoThumb != null || mVideoThumbUrl != null)) {
                initVideo();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.playvideo) {
            File file = new File(mVideoPath);
            if (file.exists() && mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                if (mSurfaceView.getVisibility() != VISIBLE) {
                    mSurfaceView.setVisibility(VISIBLE);
                }
                mMediaPlayer.start();
                mTimer = new Timer();
                mTimer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        mHandler.sendEmptyMessage(0);
                    }
                }, 0, 500);
            }
        } else if (v.getId() == R.id.shutbtn) {
            ((Activity) mContext).finish();
        }
    }

    public void setVideoPathAndThumb(String videoPath, String videoThumb, String videoThumbUrl) {
        mVideoPath = videoPath;
        mVideoThumb = videoThumb;
        mVideoThumbUrl = videoThumbUrl;
        ImageLoaderManager.displayImage(mVideoThumb, mVideoThumbUrl, R.drawable.downvideoerror, mThumbView);
        if (mMediaPlayer != null) {
            initVideo();
        }
    }

    private void initVideo() {
        try {
            mMediaPlayer.setDataSource(mVideoPath);
            mMediaPlayer.prepare();
        } catch (IllegalArgumentException e) {
            logger.e("initVideo IllegalArgumentException:" + e.getMessage());
        } catch (SecurityException e) {
            logger.e("initVideo SecurityException:" + e.getMessage());
        } catch (IllegalStateException e) {
            logger.e("initVideo IllegalStateException:" + e.getMessage());
        } catch (IOException e) {
            logger.e("initVideo IOException:" + e.getMessage());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int thisLayoutHeight = MeasureSpec.getSize(heightMeasureSpec);
        int thisLayoutWidth = MeasureSpec.getSize(widthMeasureSpec);
        setFrameLayoutSize(thisLayoutWidth, thisLayoutHeight);
    }

    private void setFrameLayoutSize(int thisLayoutWidth, int thisLayoutHeight) {
        android.view.ViewGroup.LayoutParams lp = mFrameLayout.getLayoutParams();
        lp.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = thisLayoutWidth * 3 / 2;
        mFrameLayout.setLayoutParams(lp);
    }

    public void releaseVideo() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            if (mTimer != null) {
                mTimer.cancel();
            }
        }
    }

}
