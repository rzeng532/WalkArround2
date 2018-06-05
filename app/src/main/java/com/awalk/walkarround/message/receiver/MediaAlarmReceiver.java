package com.awalk.walkarround.message.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;

import com.awalk.walkarround.R;
import com.awalk.walkarround.util.Logger;

/**
 * Created by Richard on 2017/12/13.
 */

public class MediaAlarmReceiver extends BroadcastReceiver {

    public final static String ACTION_START_MEDIA_TASK = "MediaAlarmReceiver_START";
    public final static String ACTION_STOP_MEDIA_TASK = "MediaAlarmReceiver_STOP";

    Logger logger = Logger.getLogger(MediaAlarmReceiver.class.getSimpleName());
    private static MediaPlayer mMediaPlayer;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && ACTION_START_MEDIA_TASK.equalsIgnoreCase(intent.getAction())) {
            logger.d("onReceive ACTION_START_MEDIA_TASK.");

            if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                return;
            }

            mMediaPlayer = MediaPlayer.create(context, R.raw.walk_arround_end_music);
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if (mediaPlayer != null) {
                        mediaPlayer.reset();
                    }
                }
            });
            mMediaPlayer.start();
        } else if (intent != null && ACTION_STOP_MEDIA_TASK.equalsIgnoreCase(intent.getAction())) {
            logger.d("onReceive ACTION_STOP_MEDIA_TASK.");

            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }
    }
}
