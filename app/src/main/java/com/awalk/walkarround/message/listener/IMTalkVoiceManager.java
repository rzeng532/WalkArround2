package com.awalk.walkarround.message.listener;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;
import com.awalk.walkarround.R;
import com.awalk.walkarround.base.WalkArroundApp;
import com.awalk.walkarround.util.AppConstant;
import com.awalk.walkarround.util.Logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class IMTalkVoiceManager {
    public static String FILE_SUFFIX = ".amr";// ".wav";
    private final Logger logger = Logger.getLogger("IMTalkVoiceManager");

    private static SimpleDateFormat mFormat = new SimpleDateFormat("yyyMMddHHmmssSSS");
    private Context mContext;
    private boolean isRun = false;
    private String mFilePath;
    private MediaRecorder mMediaRecorder;

    public IMTalkVoiceManager(Context context) {
        mContext = context;
        try {
            File dir = new File(WalkArroundApp.MTC_DATA_PATH + AppConstant.AUDIO_FILE_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 准备录音
     *
     * @return
     */
    public synchronized boolean showVoicePanel() {
        if (isRun) {
            cancelRecord();
            return false;
        }
        return true;
    }

    /**
     * 开始录音
     *
     * @return
     */
    public synchronized boolean startRecording() {
        String path = WalkArroundApp.MTC_DATA_PATH + AppConstant.AUDIO_FILE_PATH + mFormat.format(new Date()) + FILE_SUFFIX;
        try {
            initAudioOutPath(path);
        } catch (Exception e) {
            logger.e("startRecord Exception:" + e.getMessage());
            Toast.makeText(mContext, R.string.msg_msg_voice_create_file_error, Toast.LENGTH_LONG).show();
            return false;
        }
        try {
            startRecord();
        } catch (Exception e) {
            logger.e("startRecord mRecordThread.startRecord():" + e.getMessage());
            return false;
        }

        return true;
    }

    public static String voiceFormatText(int time) {
        String sec = String.valueOf(time % 60);
        String min = String.valueOf(time / 60);
        return min + ":" + sec + "'";
    }

    /**
     * 停止录音
     *
     * @param cb
     * @param audioLength
     */
    public void closeVoicePanel(RecordCallback cb, int audioLength) {
        if (cb != null) {
            logger.d("CALLBACK SENT!!! filePath" + mFilePath);
            stopRecord();
            cb.onRecordFileCreateOK(mFilePath, audioLength, getAudioTime(mContext, mFilePath));
        } else {
            cancelRecord();
        }
    }

    public interface RecordCallback {
        void onRecordFileCreateOK(String filePath, int audioLength, int actualLen);
    }

    /**
     * 初始化录音
     *
     * @param path
     * @throws IOException
     */
    private void initAudioOutPath(String path) throws IOException {
        mFilePath = path;
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setOnErrorListener(null);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        // Create the new file.
        file.createNewFile();
        mMediaRecorder.setOutputFile(file.getAbsolutePath());
    }

    /**
     * 开始录音
     */
    private void startRecord() {
        if (isRun) {
            return;
        }
        isRun = true;
        logger.d("RECORDING STARTED!!!");
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (Exception e) {
            logger.d("RECORDING STARTED Exception:" + e.getMessage());
        }
    }

    /**
     * 停止录音
     */
    private void stopRecord() {
        if (!isRun) {
            return;
        }
        logger.d("RECORDING PAUSED!!!");
        isRun = false;
        if (mMediaRecorder != null) {
            mMediaRecorder.setOnErrorListener(null);
            try {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mMediaRecorder.release();
            } catch (Exception e) {
                logger.d("MediaRecorder STOP error!!!" + e.getMessage());
            }

            logger.d("RECORDING STOPPED!!!");
        }
    }

    /**
     * 取消录音
     */
    private void cancelRecord() {
        if (!isRun) {
            return;
        }
        stopRecord();
        if (!TextUtils.isEmpty(mFilePath)) {
            File file = new File(mFilePath);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 获取录音时长
     *
     * @param mContext
     * @param audioPath
     * @return
     */
    private int getAudioTime(Context mContext, String audioPath) {
        int duration = 0;
        MediaPlayer mp = MediaPlayer.create(mContext, Uri.parse(audioPath));
        if (mp != null) {
            duration = mp.getDuration() / 1000;
            if (duration == 0) {
                duration = 1;
            }
        } else {
            duration = 0;
        }
        return duration;
    }

}
