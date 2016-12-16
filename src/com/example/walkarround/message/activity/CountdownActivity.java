/**
 * Copyright (C) 2014-2016 CMCC All rights reserved
 */
package com.example.walkarround.message.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.walkarround.R;
import com.example.walkarround.base.view.DialogFactory;
import com.example.walkarround.base.view.PhotoView;
import com.example.walkarround.base.view.RoundProgressBar;
import com.example.walkarround.main.model.ContactInfo;
import com.example.walkarround.message.manager.ContactsManager;
import com.example.walkarround.message.manager.WalkArroundMsgManager;
import com.example.walkarround.message.util.MessageConstant;
import com.example.walkarround.message.util.MessageUtil;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.util.Logger;

import java.text.SimpleDateFormat;
import java.util.*;


/**
 * TODO: description
 * Date: 2016-07-29
 *
 * @author Administrator
 */
public class CountdownActivity extends Activity implements View.OnClickListener {

    private Logger logger = Logger.getLogger(CountdownActivity.class.getSimpleName());

    private TextView mTvDescription;
    private TextView mTvComplete;
    private TextView mTvCountdownTime;
    private PhotoView mPvPortrait;
    private ImageView mIvCountdown;
    private RoundProgressBar timeProgress;

    private ContactInfo mFriend = null;

    private final int COUNTDOWN_TOTOL_TIME = 10 * 60;
    //private final int COUNTDOWN_TOTOL_TIME = 1 * 60;
    private final int MUSIC_START_TIME = 30; //it means last 30 seconds.
    private int mCurTime = 0;
    private Timer timer;
    //Input parameter for this actvity to display UI elements.
    public static final String PARAMS_FRIEND_OBJ_ID = "friend_obj_id";

    //For playing complete music.
    private MediaPlayer mMediaPlayer;

    private Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 1:
                    logger.d("curTime add: " + mCurTime);
                    mCurTime++;
                    if(mCurTime > COUNTDOWN_TOTOL_TIME) {
                        logger.d("Set time as 0 ");
                        timeProgress.setProgress(100);
                        setTvCountdownTimeUI(0);
                        timer.cancel();
                        jump2EvaluatePage();
                        CountdownActivity.this.finish();
                    } else {
                        timeProgress.setProgress(( 100 * mCurTime / COUNTDOWN_TOTOL_TIME));
                        setTvCountdownTimeUI(COUNTDOWN_TOTOL_TIME - mCurTime);
                        if(COUNTDOWN_TOTOL_TIME - mCurTime == MUSIC_START_TIME) {
                            if (mMediaPlayer == null) {
                                createWalkArroundCompleteMusic();
                            }
                        }
                    }

                    break;
                default:
                    break;
            }
        }
    };

    TimerTask task = new TimerTask(){
        public void run() {
            Message message = new Message();
            message.what = 1;
            logger.d("task run and dispatch msg: " + message.what);
            handler.sendMessage(message);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);

        initData();

        initView();

        if(mFriend != null) {
            List<String> recipient = new ArrayList<>();
            recipient.add(mFriend.getObjectId());
            long msgThreadId = WalkArroundMsgManager.getInstance(getApplicationContext()).getConversationId(MessageConstant.ChatType.CHAT_TYPE_ONE2ONE,
                    recipient);
            if(msgThreadId >= 0) {
                WalkArroundMsgManager.getInstance(getApplicationContext()).updateConversationStatus(msgThreadId, MessageUtil.WalkArroundState.STATE_WALK);
                //TODO: update speed data id state to server.
            }
        }

        DialogFactory.getWalkRuleDialog(this, new DialogFactory.ConfirmDialogClickListener() {
            @Override
            public void onConfirmDialogConfirmClick() {
                timer = new Timer(true);
                timer.schedule(task,1000, 1000);
            }
        }).show();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mMediaPlayer != null) {
            logger.d("onPause: stop & reset media player.");
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void initView() {
        mTvDescription = (TextView)findViewById(R.id.tv_walk_description);
        mTvComplete = (TextView)findViewById(R.id.tv_complete_walk);
        mTvComplete.setOnClickListener(this);
        mPvPortrait = (PhotoView)findViewById(R.id.pv_countdown);

        //Init countdown time text.
        mTvCountdownTime = (TextView)findViewById(R.id.tv_countdown_time);
        setTvCountdownTimeUI(COUNTDOWN_TOTOL_TIME);

        //mIvCountdown = (ImageView)findViewById(R.id.iv_countdown);
        timeProgress = (RoundProgressBar) findViewById(R.id.iv_countdown);
        timeProgress.setProgress(0);

        if(mFriend != null) {
            String friendName = mFriend.getUsername();
            mTvDescription.setText(getString(R.string.countdown_walk_with_who, friendName));

            mPvPortrait.setBaseData(friendName, mFriend.getPortrait().getUrl(), null,
                    R.drawable.contact_default_profile);
        }
    }

    private void initData() {
        Intent intent = getIntent();
        if(intent != null) {
            String friendId = intent.getStringExtra(PARAMS_FRIEND_OBJ_ID);

            if(!TextUtils.isEmpty(friendId)) {
                mFriend = ContactsManager.getInstance(this).getContactByUsrObjId(friendId);
            }
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.tv_complete_walk:
                if(timer != null) {
                    timer.cancel();
                    timer = null;
                }

                //We need a popup here
                jump2EvaluatePage();

                this.finish();
                break;
            default:
                break;
        }
    }

    private void setTvCountdownTimeUI(int timeSec) {
        if(mTvCountdownTime != null && timeSec > 30l) {
            SimpleDateFormat sdf = new SimpleDateFormat( "mm:ss");
            String time = sdf.format(new Date((timeSec * 1000L)));
            mTvCountdownTime.setText(time);
        } else if(mTvCountdownTime != null && timeSec >= 0l) {
            SimpleDateFormat sdf = new SimpleDateFormat( "ss");
            String time = sdf.format(new Date((timeSec * 1000L)));
            mTvCountdownTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 50);
            mTvCountdownTime.setTextColor(getResources().getColor(R.color.cor_red));
            mTvCountdownTime.setText(time);
        }
    }

    /**
     * 创建本地MP3播放器
     *
     * @return
     */
    public void createWalkArroundCompleteMusic() {

        handler.post(new Runnable() {
            @Override
            public void run() {
                mMediaPlayer = MediaPlayer.create(CountdownActivity.this, R.raw.walk_arround_end_music);
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        if(mediaPlayer != null) {
                            mediaPlayer.reset();
                        }
                    }
                });
                mMediaPlayer.start();
            }
        });
    }

    private void jump2EvaluatePage() {
        //Start Evaluate activity
        if(mFriend != null) {
            Intent intent = new Intent(CountdownActivity.this, EvaluateActivity.class);
            intent.putExtra(EvaluateActivity.PARAMS_FRIEND_OBJ_ID, mFriend.getObjectId());
            startActivity(intent);
        }
        ProfileManager.getInstance().setCurUsrDateState(MessageUtil.WalkArroundState.STATE_WALK);
    }
}
