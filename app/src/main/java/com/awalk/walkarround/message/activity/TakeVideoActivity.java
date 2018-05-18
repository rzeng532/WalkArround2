package com.awalk.walkarround.message.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.awalk.walkarround.R;
import com.awalk.walkarround.message.activity.VideoRelativeLayout.OnFinishListener;

import java.io.File;

public class TakeVideoActivity extends Activity implements View.OnTouchListener, OnFinishListener, OnClickListener {

    public static final String INTENT_VIDEO_PATH = "filename";
    public static final String INTENT_VIDEO_LENGTH = "videoLength";
    private VideoRelativeLayout mMovieView;
    private Button mTakeVideoButton;
    private TextView mUpCouldCancelTxtView;
    private TextView mRecordingTimeTxtView;
    private TextView mPressToTakeVideoTxtView;
    private float mStartY;// 按下录像按钮开始的Y坐标
    private long mStartTime;// 按下录像按钮的时间点
    private boolean mIsRecording;// 是否正在录像
    private boolean mIsShowCancel;// 是否出现了可以松开就取消了的画面
    private ImageView mSureCancelView;
    private final int MINTIME = 2000;// 需要最短多少录制时间
    private int mCancelDistance;

    private final int TAKEVIDEO_DISPLAY_TOAST = 1;
    private final int TAKEVIDEO_START_RECORD = 2;
    private final int TAKEVIDEO_START_RECORD_DELAY = 300;
    private final int TAKEVIDEO_TOAST_DELYAY_TIME = 300;
    private final String TAKEVIDEO_START_POSTIION_Y = "takevideo_start_y";

    private Handler mTakeVideoHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TAKEVIDEO_DISPLAY_TOAST:
                    showToastShort(R.string.msg_video_too_short);
                    break;
                case TAKEVIDEO_START_RECORD:
                    float posY = Float.valueOf((String)msg.obj);
                    startTakeVideo(posY);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_takevideo);
        initView();
        calculateDistance();

    }

    /**
     * 初始化view
     */
    private void initView() {
        mMovieView = (VideoRelativeLayout) findViewById(R.id.movie_view);
        mTakeVideoButton = mMovieView.getmTakeVideoButton();
        mUpCouldCancelTxtView = mMovieView.getmUpCouldCancelTxtView();
        mRecordingTimeTxtView = mMovieView.getmRecordingTimeTxtView();
        mPressToTakeVideoTxtView = mMovieView.getmPressToTakeVideoTxtView();
        mSureCancelView = mMovieView.getmSureCancelView();
        mTakeVideoButton.setOnTouchListener(this);
        mSureCancelView.setOnClickListener(this);
    }

    private void calculateDistance() {
        final float scale = getResources().getDisplayMetrics().density;
        mCancelDistance = ((int) (scale * 80)) * -1;
    }

    private void startTakeVideo(float positionY) {
        if (!mIsRecording) {
            mIsRecording = true;
            mStartY = positionY;
            mStartTime = System.currentTimeMillis();
            mMovieView.record(this);
            fromCouldCancelToNormal();
            return;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Message msg = mTakeVideoHandler.obtainMessage();
                msg.what = TAKEVIDEO_START_RECORD;
                msg.obj = "" + event.getY();
                mTakeVideoHandler.sendMessageDelayed(msg, TAKEVIDEO_START_RECORD_DELAY);
                break;
            case MotionEvent.ACTION_MOVE:
                float endY = event.getY();
                if (mIsRecording && (endY - mStartY < mCancelDistance) && !mIsShowCancel) {
                    showCouldCancel();
                    mIsShowCancel = true;
                } else if (mIsRecording && mIsShowCancel && endY - mStartY > mCancelDistance) {
                    fromCouldCancelToNormal();
                    mIsShowCancel = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                mTakeVideoHandler.removeMessages(TAKEVIDEO_START_RECORD);
                if (mIsRecording && mIsShowCancel) {
                    // 需要停止并删除
                    stopVideoAndDelete();
                } else if (mIsRecording && !mIsShowCancel && (System.currentTimeMillis() - mStartTime <= MINTIME)) {
                    // 太短了提示后删除,toast延迟300毫秒显示防止重复点击
                    mTakeVideoHandler.removeMessages(TAKEVIDEO_DISPLAY_TOAST);
                    mTakeVideoHandler.sendEmptyMessageDelayed(TAKEVIDEO_DISPLAY_TOAST, TAKEVIDEO_TOAST_DELYAY_TIME);
                    stopVideoAndDelete();
                } else if (mIsRecording && !mIsShowCancel && (System.currentTimeMillis() - mStartTime > MINTIME)) {
                    if (!mMovieView.getReleasedStatue()) {
                        saveVideo();
                        // 结束的事情
                    }
                }
                mIsShowCancel = false;
                mIsRecording = false;
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.cancel_button) {
            if (mIsRecording && (System.currentTimeMillis() - mStartTime > MINTIME)) {
                saveVideo();
            } else if (mIsRecording && (System.currentTimeMillis() - mStartTime <= MINTIME)) {
                // 太短了提示后删除
                showToastShort(R.string.msg_video_too_short);
                stopVideoAndDelete();
                finish();
            } else if (!mIsRecording) {
                finish();
            }
        }
    }

    /**
     * 将界面显示为松开就取消
     */
    private void showCouldCancel() {
        mPressToTakeVideoTxtView.setVisibility(View.GONE);
        mRecordingTimeTxtView.setVisibility(View.VISIBLE);
        mUpCouldCancelTxtView.setVisibility(View.VISIBLE);
        mTakeVideoButton.setVisibility(View.VISIBLE);
        mTakeVideoButton.setBackgroundResource(R.drawable.message_btn_enterbar_videobtn_delete);
        mUpCouldCancelTxtView.setText(R.string.msg_loose_to_giveup);
    }

    /**
     * 将界面显示为上滑可取消
     */
    private void fromCouldCancelToNormal() {
        mPressToTakeVideoTxtView.setVisibility(View.GONE);
        mRecordingTimeTxtView.setVisibility(View.VISIBLE);
        mUpCouldCancelTxtView.setVisibility(View.VISIBLE);
        mTakeVideoButton.setVisibility(View.VISIBLE);
        mTakeVideoButton.setBackgroundResource(R.drawable.btn_takevideo);
        mUpCouldCancelTxtView.setText(R.string.msg_up_to_giveup);
    }

    /**
     * 保存录像并结束
     */
    private void saveVideo() {
        mMovieView.stop();
        mIsRecording = false;
        mIsShowCancel = false;
        onFinish();
    }

    /**
     * 取消录像并删除
     */
    private void stopVideoAndDelete() {
        mMovieView.cancelRecord();
        mMovieView.deleteTheFile();
        mIsRecording = false;
        mIsShowCancel = false;
        viewToInit();
    }

    /**
     * 界面恢复为最初的状态
     */
    private void viewToInit() {
        mPressToTakeVideoTxtView.setVisibility(View.VISIBLE);
        mRecordingTimeTxtView.setVisibility(View.GONE);
        mUpCouldCancelTxtView.setVisibility(View.GONE);
        mTakeVideoButton.setVisibility(View.VISIBLE);
        mTakeVideoButton.setBackgroundResource(R.drawable.btn_takevideo);
        mUpCouldCancelTxtView.setText(R.string.msg_up_to_giveup);
    }

    @Override
    public void onFinish() {
        mIsRecording = false;
        showToastShort(R.string.msg_video_record_success);
        getIntent().putExtra(INTENT_VIDEO_PATH, getVideoFile().getAbsolutePath());
        getIntent().putExtra(INTENT_VIDEO_LENGTH, getRecordTime());
        setResult(RESULT_OK, getIntent());
        this.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTakeVideoHandler.removeMessages(TAKEVIDEO_START_RECORD);
        mTakeVideoHandler.removeMessages(TAKEVIDEO_DISPLAY_TOAST);
        if (mIsRecording) {
            // 需要停止并删除
            stopVideoAndDelete();
        }
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeHandlerMessageAndCallback();
    }

    /**
     * 移除正在完成的异步的回调消息
     */
    private void removeHandlerMessageAndCallback() {
        mMovieView.removeHandlerMessageAndCallback();
    }

    public File getVideoFile() {
        return mMovieView.getVideoFile();
    }

    public int getRecordTime() {
        return mMovieView.getRecordTime();
    }

    public void showToastShort(int message) {
        Toast.makeText(TakeVideoActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
