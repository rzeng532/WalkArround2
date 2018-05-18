/**
 * Copyright (C) 2014-2016 CMCC All rights reserved
 */
package com.awalk.walkarround.message.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVAnalytics;
import com.awalk.walkarround.R;
import com.awalk.walkarround.base.view.DialogFactory;
import com.awalk.walkarround.base.view.PhotoView;
import com.awalk.walkarround.base.view.RoundProgressBar;
import com.awalk.walkarround.main.model.ContactInfo;
import com.awalk.walkarround.message.manager.ContactsManager;
import com.awalk.walkarround.message.manager.WalkArroundMsgManager;
import com.awalk.walkarround.message.receiver.MediaAlarmReceiver;
import com.awalk.walkarround.message.util.MessageConstant;
import com.awalk.walkarround.message.util.MessageUtil;
import com.awalk.walkarround.myself.manager.ProfileManager;
import com.awalk.walkarround.util.AppConstant;
import com.awalk.walkarround.util.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


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
    private TextView mTvPreCountdownTime;
    private TextView mTvCountdownHint;
    private PhotoView mPvPortrait;
    private ImageView mIvCountdown;
    private RoundProgressBar timeProgress;
    private RoundProgressBar timeProgress2;
    private RoundProgressBar timeProgress3;

    private LinearLayout mLlPreCountDown;

    private Dialog mRuleDialog;

    private ContactInfo mFriend = null;

    private final int COUNTDOWN_TOTOL_TIME = 10 * 60;
    private final int PREPARE_COUNTDOWN_TOTOL_TIME = 5;
    //private final int COUNTDOWN_TOTOL_TIME = 1 * 60;
    private final int MUSIC_START_TIME = 30; //it means last 30 seconds.
    private int mCurTime = 0;
    private long mCountdownStartTime = 0l;
    private Timer mRealCountdownTimer;
    private Timer mPrepareCountdownTimer;
    //Input parameter for this actvity to display UI elements.
    public static final String PARAMS_FRIEND_OBJ_ID = "friend_obj_id";

    //For playing complete music.
    private final int REAL_COUNTDOWN_MSG = 1;
    private final int PREPARE_COUNTDOWN_MSG = 2;
    private final int RESET_COUNTDOWN_FROM_LOCKSCREEN = 3;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case REAL_COUNTDOWN_MSG:
                    handleRealCountdownMsg();
                    break;
                case PREPARE_COUNTDOWN_MSG:
                    handlePrepareCountdownMsg();
                    break;
                case RESET_COUNTDOWN_FROM_LOCKSCREEN:
                    int resetTime = msg.arg1;
                    setTvCountdownTimeUI(resetTime);
                    handleResetTimer(resetTime);
                    break;
                default:
                    break;
            }
        }
    };

    TimerTask mRealCountdownTask = null;

    TimerTask mPrepareCountdownTask = new TimerTask() {
        public void run() {
            Message message = new Message();
            message.what = PREPARE_COUNTDOWN_MSG;
            logger.d("mPrepareCountdownTask run and dispatch msg: " + message.what);
            handler.sendMessage(message);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);

        initData();

        initView();

        if (mFriend != null) {
            List<String> recipient = new ArrayList<>();
            recipient.add(mFriend.getObjectId());
            long msgThreadId = WalkArroundMsgManager.getInstance(getApplicationContext()).getConversationId(MessageConstant.ChatType.CHAT_TYPE_ONE2ONE,
                    recipient);
            if (msgThreadId >= 0) {
                int oldState = WalkArroundMsgManager.getInstance(getApplicationContext()).getConversationStatus(msgThreadId);

                int mNewThreadState = MessageUtil.WalkArroundState.STATE_IMPRESSION;
                if(oldState == MessageUtil.WalkArroundState.STATE_END
                        || oldState == MessageUtil.WalkArroundState.STATE_END_IMPRESSION) {
                    mNewThreadState = MessageUtil.WalkArroundState.STATE_END_IMPRESSION;
                } else if(oldState == MessageUtil.WalkArroundState.STATE_POP
                        || oldState == MessageUtil.WalkArroundState.STATE_POP_IMPRESSION
                        || oldState == MessageUtil.WalkArroundState.STATE_INIT) {
                    mNewThreadState = MessageUtil.WalkArroundState.STATE_POP_IMPRESSION;
                }

                WalkArroundMsgManager.getInstance(getApplicationContext()).updateConversationStatus(msgThreadId, mNewThreadState);
            }
        }

        mRuleDialog = DialogFactory.getWalkRuleDialog(this, new DialogFactory.ConfirmDialogClickListener() {
            @Override
            public void onConfirmDialogConfirmClick() {
                return;
            }
        });
        ((TextView)mRuleDialog.findViewById(R.id.tv_i_see)).setText(getString(R.string.walk_rule_i_see, PREPARE_COUNTDOWN_TOTOL_TIME));
        mPrepareCountdownTimer = new Timer(true);
        mPrepareCountdownTimer.schedule(mPrepareCountdownTask, 1000, 1000);
        mRuleDialog.setCancelable(false);
        mRuleDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AVAnalytics.onResume(this);

        if (mCountdownStartTime > 0l) {
            long curTime = System.currentTimeMillis();
            int interval = (int) (curTime - mCountdownStartTime) / 1000; //Get interval time (second);

            startCountdownTimer();

            if (interval > 0) {
                Message message = new Message();
                message.what = RESET_COUNTDOWN_FROM_LOCKSCREEN;
                message.arg1 = interval;
                logger.d("RESET_COUNTDOWN_FROM_LOCKSCREEN interval: " + interval);
                handler.sendMessage(message);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        AVAnalytics.onPause(this);

        stopCountdownTimer();
    }

    private void initView() {
        mTvDescription = (TextView) findViewById(R.id.tv_walk_description);
        mTvComplete = (TextView) findViewById(R.id.tv_complete_walk);
        mTvComplete.setOnClickListener(this);
        mPvPortrait = (PhotoView) findViewById(R.id.pv_countdown);

        //Init countdown time text.
        mTvCountdownTime = (TextView) findViewById(R.id.tv_countdown_time);

        mTvPreCountdownTime = (TextView) findViewById(R.id.tv_walk_pre_countdown);
        setTvPrepareCountdownTimeUI(PREPARE_COUNTDOWN_TOTOL_TIME);

        mTvCountdownHint = (TextView) findViewById(R.id.tv_walk_hint);

        //mIvCountdown = (ImageView)findViewById(R.id.iv_countdown);
        timeProgress = (RoundProgressBar) findViewById(R.id.iv_countdown);
        timeProgress2 = (RoundProgressBar) findViewById(R.id.iv_countdown_2);
        timeProgress3 = (RoundProgressBar) findViewById(R.id.iv_countdown_3);
        timeProgress.setProgress(0);

        mLlPreCountDown = (LinearLayout)findViewById(R.id.rl_pre_countdown);

        if (mFriend != null) {
            String friendName = mFriend.getUsername();
            if(friendName.length() > AppConstant.SHORTNAME_LEN) {
                friendName = friendName.substring(0, AppConstant.SHORTNAME_LEN) + "...";
            }
            mTvDescription.setText(getString(R.string.countdown_prepare_walk_with_who, friendName));
            mTvComplete.setVisibility(View.GONE);
            visiableProgressBar();
//            GradientDrawable backGround = (GradientDrawable) mTvComplete.getBackground();
//            backGround.setColor(getResources().getColor(R.color.transparent));
            mPvPortrait.setBaseData(friendName, mFriend.getPortrait().getUrl(), null,
                    mFriend.getPortrait().getId());
        }
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            String friendId = intent.getStringExtra(PARAMS_FRIEND_OBJ_ID);

            if (!TextUtils.isEmpty(friendId)) {
                mFriend = ContactsManager.getInstance(this).getContactByUsrObjId(friendId);
            }
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.tv_complete_walk:
                if (mRealCountdownTimer != null) {
                    mRealCountdownTimer.cancel();
                    mRealCountdownTimer = null;
                }

                //We need a popup here
                jump2EvaluatePage();

                this.finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        return;
        //super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mRealCountdownTimer != null) {
            mRealCountdownTimer.cancel();
            mRealCountdownTimer = null;
        }

        if (mPrepareCountdownTimer != null) {
            mPrepareCountdownTimer.cancel();
            mPrepareCountdownTimer = null;
        }

        //Stop media
        Intent intent = new Intent(this, MediaAlarmReceiver.class);
        intent.setAction(MediaAlarmReceiver.ACTION_STOP_MEDIA_TASK);
        sendBroadcast(intent);
    }

    private void setTvCountdownTimeUI(int timeSec) {
        if (mTvCountdownTime != null && timeSec > 30l) {
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
            String time = sdf.format(new Date((timeSec * 1000L)));

            if (timeSec == (COUNTDOWN_TOTOL_TIME - 1)) {
                //mTvCountdownTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.font_size4_dp));
                mTvCountdownTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.font_size4_dp));
                mTvCountdownTime.setTextColor(getResources().getColor(R.color.fontcor3));
            }

            mTvCountdownTime.setText(time);
        } else if (mTvCountdownTime != null && timeSec <= 30l && timeSec >= 0l) {
            SimpleDateFormat sdf = new SimpleDateFormat("ss");
            String time = sdf.format(new Date((timeSec * 1000L)));

            if (timeSec == 30l) {
                mTvCountdownTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 50);
                mTvCountdownTime.setTextColor(getResources().getColor(R.color.cor_red));
            }

            mTvCountdownTime.setText(time);
        }
    }

    private void setTvPrepareCountdownTimeUI(int timeSec) {
        SimpleDateFormat sdf = new SimpleDateFormat("s");
        String time = sdf.format(new Date((timeSec * 1000L)));

        mTvPreCountdownTime.setText(time);
    }

    private void jump2EvaluatePage() {
        //Start Evaluate activity
        if (mFriend != null) {
            Intent intent = new Intent(CountdownActivity.this, EvaluateActivity.class);
            intent.putExtra(EvaluateActivity.PARAMS_FRIEND_OBJ_ID, mFriend.getObjectId());
            startActivity(intent);
        }
        ProfileManager.getInstance().setCurUsrDateState(MessageUtil.WalkArroundState.STATE_WALK);
    }

    private void handlePrepareCountdownMsg() {

        mCurTime++;
        //setTvPrepareCountdownTimeUI(PREPARE_COUNTDOWN_TOTOL_TIME - mCurTime);

        if (mRuleDialog != null && mCurTime >= (PREPARE_COUNTDOWN_TOTOL_TIME)) {
            //Reset current time second value.
            mCurTime = 0;
            //Mark real countdown start time.
            mCountdownStartTime = System.currentTimeMillis();

            //Reset title
            if (mFriend != null) {
                String friendName = mFriend.getUsername();
                if(friendName.length() > AppConstant.SHORTNAME_LEN) {
                    friendName = friendName.substring(0, AppConstant.SHORTNAME_LEN) + "...";
                }
                mTvDescription.setText(getString(R.string.countdown_walk_with_who, friendName));
            }

            //start real countdown activity.
            startCountdownTimer();

            mRuleDialog.dismiss();
            mRuleDialog = null;

            //Cancel prepare task.
            mPrepareCountdownTimer.cancel();

            //Test code
            startMediaDelayTask();
        } else if(mRuleDialog != null) {
            ((TextView)mRuleDialog.findViewById(R.id.tv_i_see))
                    .setText(getString(R.string.walk_rule_i_see, PREPARE_COUNTDOWN_TOTOL_TIME - mCurTime));
        }
    }

    private void handleRealCountdownMsg() {
        logger.d("curTime add: " + mCurTime);

        if(mCurTime == 0) {
            mTvComplete.setVisibility(View.VISIBLE);
            //visiableProgressBar();
        }

        mCurTime++;

        if (mCurTime > COUNTDOWN_TOTOL_TIME) {
            logger.d("Set time as 0 ");
            timeProgress.setProgress(100);
            setTvCountdownTimeUI(0);
            stopCountdownTimer();
            DialogFactory.getCountDownEndDialog(this, new DialogFactory.ConfirmDialogClickListener() {
                @Override
                public void onConfirmDialogConfirmClick() {
                    jump2EvaluatePage();
                    //CountdownActivity.this.finish();
                }
            }).show();
        } else {
            timeProgress.setProgress((100 * mCurTime / COUNTDOWN_TOTOL_TIME));
            setTvCountdownTimeUI(COUNTDOWN_TOTOL_TIME - mCurTime);
        }
    }

    /**
     * 处理重新设置timer的需求
     *
     * @param resetTime
     */
    private void handleResetTimer(int resetTime) {

        //Check time value
        if (resetTime <= 0) {
            return;
        } else if (resetTime >= COUNTDOWN_TOTOL_TIME) {
            mCurTime = COUNTDOWN_TOTOL_TIME - 1;
        } else {
            mCurTime = resetTime - 1;
        }

        handleRealCountdownMsg();
    }

    private void startCountdownTimer() {
        if(mRealCountdownTimer == null) {
            mRealCountdownTimer = new Timer(true);
            mRealCountdownTask = new TimerTask() {
                public void run() {
                    Message message = new Message();
                    message.what = REAL_COUNTDOWN_MSG;
                    logger.d("mRealCountdownTask run and dispatch msg: " + message.what);
                    handler.sendMessage(message);
                }
            };

            mRealCountdownTimer.schedule(mRealCountdownTask, 1000, 1000);
        }
    }

    private void stopCountdownTimer() {
        if(mRealCountdownTimer != null) {
            mRealCountdownTimer.cancel();
            mRealCountdownTimer = null;

            mRealCountdownTask.cancel();
            mRealCountdownTask = null;
        }
    }

    private void invisiableProgressBar() {
        timeProgress.setVisibility(View.GONE);
        timeProgress2.setVisibility(View.GONE);
        timeProgress3.setVisibility(View.GONE);
        mTvCountdownTime.setVisibility(View.GONE);
        mTvCountdownHint.setVisibility(View.VISIBLE);
        mLlPreCountDown.setVisibility(View.VISIBLE);
    }

    private void visiableProgressBar() {
        timeProgress.setVisibility(View.VISIBLE);
        timeProgress2.setVisibility(View.VISIBLE);
        timeProgress3.setVisibility(View.VISIBLE);
        mTvCountdownTime.setVisibility(View.VISIBLE);
        mTvCountdownHint.setVisibility(View.GONE);
        mLlPreCountDown.setVisibility(View.GONE);
    }

    private void startMediaDelayTask() {
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, MediaAlarmReceiver.class);
        i.setAction(MediaAlarmReceiver.ACTION_START_MEDIA_TASK);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.RTC_WAKEUP
                , System.currentTimeMillis() + (COUNTDOWN_TOTOL_TIME - MUSIC_START_TIME) * 1000
                , pi);
    }
}
