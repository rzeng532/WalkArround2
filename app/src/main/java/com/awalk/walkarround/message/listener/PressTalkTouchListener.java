package com.awalk.walkarround.message.listener;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.awalk.walkarround.R;
import com.awalk.walkarround.util.Logger;

import java.io.File;

/**
 * 语音面板控制
 * Date: 2015-03-23
 *
 * @author mss
 */
public class PressTalkTouchListener implements View.OnTouchListener, IMTalkVoiceManager.RecordCallback {

    private Logger logger = Logger.getLogger(PressTalkTouchListener.class.getSimpleName());

    private final static int VOICE_TOO_SHORT = 1;
    private final static int VOICE_TOO_LONG = 2;
    private final static int VOICE_CANCELED = 3;
    private final static int VOICE_ERROR = 4;
    private final static int VOICE_TIP = 5;
    private final static int VOICE_TIMER_MSG = 6;

    private final static int VOICE_START_RECORD_THREAD = 7;

    public final static int MAX_VOICE_DURATION = 60;
    private final static int VOICE_COUNTDOWN_TIME = 10;

    private Context mContext;
    private IMTalkVoiceManager mIMTalkVoiceManager;
    private VoiceManager mVoiceManager;
    private TextView mDurationView;
    private ImageView mTouchView;
    private TextView mVoiceTextHint;
    private ProgressBar mVoiceProgressBar;
    private int mDefaultVoiceIcon = R.drawable.public_btn_enterbar_voicebtn;
    private int mPressVoicesIcon = R.drawable.public_btn_enterbar_voicebtn2;
    private int mProgressBarBg = R.drawable.progress_voice_duration;

    private float mTouchDownStartPointY;
    private long mTouchDownTime = 0l;
    private int mVoiceDuration = 0;
    private boolean isCancel = false;
    private boolean isRun = true;

    public PressTalkTouchListener(Context context, VoiceManager voiceManger) {
        mContext = context;
        mIMTalkVoiceManager = new IMTalkVoiceManager(mContext);
        mVoiceManager = voiceManger;
    }

    public void setAudioStatusIcon(int defaultAudioId, int pressAudioId, int progressbarId){
        mDefaultVoiceIcon = defaultAudioId;
        mPressVoicesIcon = pressAudioId;
        mProgressBarBg = progressbarId;
        if (mVoiceProgressBar != null) {
            mVoiceProgressBar.setProgressDrawable(mContext.getResources()
                    .getDrawable(mProgressBarBg));
        }
        mTouchView.setImageResource(mDefaultVoiceIcon);
    }

    /**
     * 设置进度条展示
     *
     * @param voiceProgressBar
     */
    public void setVoiceProgressBar(ProgressBar voiceProgressBar, TextView voiceTextHint) {
        mVoiceProgressBar = voiceProgressBar;
        if (mVoiceProgressBar != null) {
            mVoiceProgressBar.setProgressDrawable(mContext.getResources().getDrawable(
                    mProgressBarBg));
        }
        mVoiceTextHint = voiceTextHint;
    }

    /**
     * 设置时间显示
     *
     * @param durationView
     */
    public void setVoiceDuration(TextView durationView) {
        mDurationView = durationView;
    }

    public void setTouchView(ImageView touchView) {
        mTouchView = touchView;
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case VOICE_TOO_SHORT:
                    Toast.makeText(mContext, R.string.msg_msg_voice_record_duration_too_short,
                            Toast.LENGTH_SHORT).show();
                    break;
                case VOICE_CANCELED:
                    Toast.makeText(mContext, R.string.msg_msg_voice_cancel_send, Toast.LENGTH_SHORT).show();
                    break;
                case VOICE_ERROR:
                    Toast.makeText(mContext, R.string.msg_msg_voice_file_error, Toast.LENGTH_SHORT).show();
                    break;
                case VOICE_START_RECORD_THREAD:
                    mTouchDownTime = System.currentTimeMillis();
                    mIMTalkVoiceManager.startRecording();
                    break;
                default:
                    break;
            }
        }
    };

    private Handler mTimerHandler = new Handler() {

        public void handleMessage(Message msg) {
            if (!isRun) {
                return;
            }
            mVoiceDuration++;
            if (mVoiceProgressBar != null) {
                mVoiceProgressBar.incrementProgressBy(1);
            }
            if (MAX_VOICE_DURATION - mVoiceDuration <= 0) {
                mTimerHandler.removeMessages(VOICE_TIMER_MSG);
                mVoiceDuration = 0;
                mIMTalkVoiceManager.closeVoicePanel(PressTalkTouchListener.this, MAX_VOICE_DURATION);
            } else {
                if (MAX_VOICE_DURATION - mVoiceDuration < VOICE_COUNTDOWN_TIME) {
                    mDurationView.setText(mContext.getString(
                            R.string.msg_msg_voice_time_remaining, (MAX_VOICE_DURATION - mVoiceDuration)));
                } else {
                    mDurationView.setText(IMTalkVoiceManager
                            .voiceFormatText(mVoiceDuration));
                }
                mTimerHandler.removeMessages(VOICE_TIMER_MSG);
                mTimerHandler.sendEmptyMessageDelayed(VOICE_TIMER_MSG, 1000);
            }
        }
    };

    /**
     * 按住说话时长
     *
     * @return
     */
    private boolean checkPressDuration() {
        long actionUpTime = System.currentTimeMillis();
        long duration = actionUpTime - mTouchDownTime;
        if (duration < 500) {
            handler.sendEmptyMessage(VOICE_TOO_SHORT);
            return false;
        }
        return true;
    }

    @Override
    public boolean onTouch(final View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isCancel = false;
                isRun = true;
                mVoiceDuration = 0;
                mTouchDownStartPointY = event.getY();
                mTouchDownTime = System.currentTimeMillis();
                if (mVoiceProgressBar != null) {
                    mVoiceProgressBar.setVisibility(View.VISIBLE);
                    mVoiceProgressBar.setProgress(mVoiceDuration);
                }
                if (mVoiceTextHint != null) {
                    mVoiceTextHint.setText(R.string.msg_msg_voice_do_cancel_send_1);
                }
                isRun = mIMTalkVoiceManager.showVoicePanel();
                if (isRun) {
                    mTimerHandler.sendEmptyMessageDelayed(VOICE_TIMER_MSG, 1000);
                    handler.sendEmptyMessageDelayed(VOICE_START_RECORD_THREAD, 300);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isRun) {
                    break;
                }
                if (Math.abs(event.getY() - mTouchDownStartPointY) > 100f) {
                    isCancel = true;
                    mTouchView.setImageResource(R.drawable.public_btn_enterbar_voicebtndelete);
                    if (mVoiceTextHint != null) {
                        mVoiceTextHint.setText(R.string.msg_msg_voice_cancel_send);
                    }
                } else {
                    isCancel = false;
                    if (MAX_VOICE_DURATION - mVoiceDuration > VOICE_COUNTDOWN_TIME)
                        mTouchView.setImageResource(mPressVoicesIcon);
                    if (mVoiceTextHint != null) {
                        mVoiceTextHint.setText(R.string.msg_msg_voice_do_cancel_send_1);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isRun) {
                    break;
                }
                if (isCancel) {
                    mTimerHandler.removeMessages(VOICE_TIMER_MSG);
                    handler.removeMessages(VOICE_START_RECORD_THREAD);
                    handler.sendEmptyMessage(VOICE_CANCELED);
                    mIMTalkVoiceManager.closeVoicePanel(null, 0);
                    restoreToDefaultStatus();
                    break;
                }
                mTimerHandler.removeMessages(VOICE_TIMER_MSG);
                handler.removeMessages(VOICE_START_RECORD_THREAD);
                // 发送语音消息
                if (checkPressDuration()) {
                    mIMTalkVoiceManager.closeVoicePanel(this, mVoiceDuration);
                } else {
                    mIMTalkVoiceManager.closeVoicePanel(null, 0);
                }
                restoreToDefaultStatus();
                break;
            case MotionEvent.ACTION_CANCEL:
                mTimerHandler.removeMessages(VOICE_TIMER_MSG);
                handler.removeMessages(VOICE_START_RECORD_THREAD);
                mIMTalkVoiceManager.closeVoicePanel(null, 0);
                restoreToDefaultStatus();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onRecordFileCreateOK(String filePath, int audioLength, int actualLen) {
        File file = new File(filePath);
        if (isCancel) {
            handler.sendEmptyMessage(VOICE_CANCELED);
            if (file.exists()) {
                file.delete();
            }
        } else if (file.length() < 10) {
            handler.sendEmptyMessage(VOICE_ERROR);
        } else if (mVoiceManager != null) {
            if (audioLength <= 0) {
                audioLength = actualLen;
            }
            mVoiceManager.sendVoice(filePath, audioLength);
        }
        isRun = false;
        restoreToDefaultStatus();
    }

    private void restoreToDefaultStatus() {
        mTouchView.setImageResource(mDefaultVoiceIcon);
        mDurationView.setText(R.string.msg_msg_voice_press_speak);
        mVoiceTextHint.setText(null);
        if (mVoiceProgressBar != null) {
            mVoiceProgressBar.setVisibility(View.GONE);
        }
    }

    public interface VoiceManager {
        /**
         * 发送语音
         *
         * @param audioFilePath
         * @param recordTime
         */
        public void sendVoice(String audioFilePath, int recordTime);
    }

}
