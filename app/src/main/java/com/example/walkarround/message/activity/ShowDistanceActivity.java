/**
 * Copyright (C) 2014-2016 CMCC All rights reserved
 */
package com.example.walkarround.message.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import com.avos.avoscloud.AVException;
import com.example.walkarround.Location.manager.LocationManager;
import com.example.walkarround.Location.model.GeoData;
import com.example.walkarround.R;
import com.example.walkarround.base.WalkArroundApp;
import com.example.walkarround.base.view.DialogFactory;
import com.example.walkarround.base.view.PortraitView;
import com.example.walkarround.base.view.RippleView;
import com.example.walkarround.base.view.photoview.RoundImageView;
import com.example.walkarround.main.model.ContactInfo;
import com.example.walkarround.main.parser.WalkArroundJsonResultParser;
import com.example.walkarround.base.task.TaskUtil;
import com.example.walkarround.message.manager.ContactsManager;
import com.example.walkarround.message.manager.WalkArroundMsgManager;
import com.example.walkarround.message.model.ChatMsgBaseInfo;
import com.example.walkarround.message.model.MessageSessionBaseModel;
import com.example.walkarround.message.task.QueryUsrCoordinateTask;
import com.example.walkarround.message.util.MessageUtil;
import com.example.walkarround.message.util.MsgBroadcastConstants;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.myself.model.MyDynamicInfo;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.AsyncTaskListener;
import com.example.walkarround.util.CommonUtils;
import com.example.walkarround.util.Logger;
import com.example.walkarround.util.http.HttpTaskBase;
import com.example.walkarround.util.http.HttpUtil;
import com.example.walkarround.util.http.ThreadPoolManager;

/**
 * TODO: description
 * Date: 2016-07-20
 *
 * @author Administrator
 */
public class ShowDistanceActivity extends Activity implements View.OnClickListener {

    private Logger logger = Logger.getLogger(ShowDistanceActivity.class.getSimpleName());
    private RippleView mSearchingView;
    private View mFulfillView;
    private RelativeLayout mRlSearchArea;
    private RelativeLayout mLlDistance;
    private PortraitView mPvFriend;
    private Dialog mWalkRequestDialog;
    private TextView mTvPleaseClickPortrait;
    private TextView mTvTitle;
    private RoundImageView mIvTextBg;
    private TextView mTvXiuYiXiu;
    private String mStrFriendId;
    private GeoData mFriendGeoData;
    private int mPriorDistance = -1;

    private String mDistanceBeyonded;
    private String mDistanceWithin;

    public static final String PARAMS_THREAD_ID = "thread_id";

    private final int MSG_FRIEND_REPLY_OK = 1;
    private final int MSG_FRIEND_REPLY_NEXT_TIME = 2;
    private final int MSG_FRIEND_REQ_START_2_WALK = 3;
    private final int MSG_UPDATE_DISTANCE = 4;
    private final int MSG_DISPLAY_SEARCHING = 5;

    //Display friend portrait if distance between you and friend less than 100m.
    private final int DISTANCE_2_DISPLAY_FRIEND_PORTRAIT = 500;

    /* 回复走走请求对话框 */
    private Dialog mWalkReplyDialog;

    private Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FRIEND_REPLY_OK:
                    logger.d("ShowDistance: friend reply ok");
                    if(mWalkRequestDialog != null) {
                        mWalkRequestDialog.dismiss();
                        mWalkRequestDialog = null;
                    }

                    Intent intentShowDistance = new Intent(ShowDistanceActivity.this, CountdownActivity.class);
                    intentShowDistance.putExtra(CountdownActivity.PARAMS_FRIEND_OBJ_ID, mStrFriendId);
                    startActivity(intentShowDistance);
                    ShowDistanceActivity.this.finish();
                    break;
                case MSG_FRIEND_REPLY_NEXT_TIME:
                    logger.d("ShowDistance: friend reply next time.");
                    if(mWalkRequestDialog != null && mWalkRequestDialog.isShowing()) {
                        mWalkRequestDialog.dismiss();
                        mWalkRequestDialog = null;
                    }
                    Toast.makeText(ShowDistanceActivity.this, getString(R.string.msg_walk_reply_receiver_next_time), Toast.LENGTH_SHORT).show();
                    ShowDistanceActivity.this.finish();
                    break;
                case MSG_FRIEND_REQ_START_2_WALK:
                    //TODO: send agreement and goto countdown UI directly?
                    logger.d("ShowDistance: recejve MSG_FRIEND_REQ_START_2_WALK");
                    if(mWalkRequestDialog != null && mWalkRequestDialog.isShowing()) {
                        logger.d("ShowDistance: dismiss old dialog.");
                        mWalkRequestDialog.dismiss();
                        mWalkRequestDialog = null;
                    }
                    createWalkReplyDialog();
                    mWalkReplyDialog.show();
                    break;
                case MSG_UPDATE_DISTANCE:
                    int distance = msg.arg1;
                    logger.d("ShowDistance:update distance: " + distance);
                    if(DISTANCE_2_DISPLAY_FRIEND_PORTRAIT >= distance) {
                        mSearchingView.setInitRadiusByPortraitWidth(mIvTextBg);
                        mSearchingView.setVisibility(View.VISIBLE);
                        mSearchingView.start();
                        mTvTitle.setText(distance + getResources().getString(R.string.common_distance_unit_meter));
                        mPvFriend.setVisibility(View.VISIBLE);
                        mTvPleaseClickPortrait.setText(mDistanceWithin);
                        mTvXiuYiXiu.setVisibility(View.VISIBLE);
                        mIvTextBg.setVisibility(View.VISIBLE);
                    } else {
                        mSearchingView.stop();
                        mTvTitle.setText(R.string.main_title);
                        mSearchingView.setVisibility(View.GONE);
                        mPvFriend.setVisibility(View.GONE);
                        mTvPleaseClickPortrait.setText(mDistanceBeyonded);
                        //mTvPleaseClickPortrait.setVisibility(View.GONE);
                        mTvXiuYiXiu.setVisibility(View.GONE);
                        mIvTextBg.setVisibility(View.GONE);
                    }
                    break;
                case MSG_DISPLAY_SEARCHING:
                    mSearchingView.start();
                    break;
                default:
                    break;
            }
        }
    };

    /* 消息状态监听 */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            long messageId = intent.getLongExtra(MsgBroadcastConstants.BC_VAR_MSG_ID, 0);
            if (action.equals(MsgBroadcastConstants.ACTION_MESSAGE_NEW_RECEIVED)) {
                // 新到一对一消息
                ChatMsgBaseInfo message = WalkArroundMsgManager.getInstance(getApplicationContext()).getMessageById(messageId);
                if(message != null && mStrFriendId != null && mStrFriendId.equalsIgnoreCase(message.getContact())) {
                    if(!TextUtils.isEmpty(message.getExtraInfo())) {
                        String[] extraArray = message.getExtraInfo().split(MessageUtil.EXTRA_INFOR_SPLIT);
                        if(extraArray != null && extraArray.length >= 2 && !TextUtils.isEmpty(extraArray[0])) {
                            if(extraArray[1].equalsIgnoreCase(MessageUtil.EXTRA_START_2_WALK_REPLY_OK)) {
                                //Friend send agreement.
                                mUiHandler.sendEmptyMessage(MSG_FRIEND_REPLY_OK);
                            } else if(extraArray[1].equalsIgnoreCase(MessageUtil.EXTRA_START_2_WALK_REPLY_NEXT_TIME)) {
                                //Friend refuse your request this time.
                                mUiHandler.sendEmptyMessage(MSG_FRIEND_REPLY_NEXT_TIME);
                            } else if(extraArray[1].equalsIgnoreCase(MessageUtil.EXTRA_START_2_WALK_REQUEST)) {
                                //Friend send a start to walk request at the same time.
                                mUiHandler.sendEmptyMessage(MSG_FRIEND_REQ_START_2_WALK);
                            }
                        }
                    }
                }
            }
        }
    };

    private HttpTaskBase.onResultListener mQueryFriendCoordinateTaskListener = new HttpTaskBase.onResultListener() {
        @Override
        public void onPreTask(String requestCode) {
            logger.d("QueryFriendDynData onPreTask.");
        }

        @Override
        public void onResult(Object object, HttpTaskBase.TaskResult resultCode, String requestCode, String threadId) {
            logger.d("QueryFriendDynData done.");
            if (HttpTaskBase.TaskResult.SUCCEESS == resultCode && requestCode.equalsIgnoreCase(HttpUtil.HTTP_FUNC_QUERY_USR_COORDINATE)) {
                logger.d("QueryFriendDynData success, DATA is: " + (String)object);

                //Parse object
                mFriendGeoData = WalkArroundJsonResultParser.parseUserCoordinate((String)object);

                updateDistanceBetweenFriends();

                mUiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        start2GetFriendCoordinate();
                    }
                }, AppConstant.MAP_CONTINUE_LOC_INTERVAL /* Interval is the same as continue location*/);
            }
        }

        @Override
        public void onProgress(int progress, String requestCode) {

        }
    };

    AsyncTaskListener mWalkArroundLocListener = new AsyncTaskListener() {
        @Override
        public void onSuccess(Object data) {
            GeoData geoData = LocationManager.getInstance(getApplicationContext()).getCurrentLoc();
            try{
                ProfileManager.getInstance().updateDynamicData(new MyDynamicInfo(geoData, true, 1), null);
            } catch (Exception e) {
                logger.d(" ------ ShowDistanceActivity continue locate exception: ");
                e.printStackTrace();
            }

            updateDistanceBetweenFriends();
            logger.d("ShowDistanceActivity continue locate. The geo data is: " + geoData.getAddrInfor());
        }

        @Override
        public void onFailed(AVException e) {
            //TODO:
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_show_distance);

        logger.d("onCreate");

        initView();

        initData();

        initRegisterforNewMsg();

        //Start to update current user location information.
        LocationManager.getInstance(WalkArroundApp.getInstance())
                .start2ContinueLocate(AppConstant.KEY_MAP_ASYNC_LISTERNER_CONTINUE_LOC_DURING_WALK
                                        , mWalkArroundLocListener);

        //Start to update friend location information.
        start2GetFriendCoordinate();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mSearchingView.getVisibility() == View.VISIBLE) {
            mUiHandler.sendEmptyMessageDelayed(MSG_DISPLAY_SEARCHING, 500);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mSearchingView.isStarting()) {
            mSearchingView.stop();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        mPriorDistance = -1;
        LocationManager.getInstance(WalkArroundApp.getInstance()).stopContinueLocate();

        try {
            unregisterReceiver(mMessageReceiver);
            mUiHandler.removeCallbacksAndMessages(null);
        } catch (Exception e) {
        }
    }

    private void initView() {
        //Title
        View title = findViewById(R.id.title);
        ImageView back = (ImageView)(title.findViewById(R.id.back_iv));
        back.setImageResource(R.drawable.back_white);
        title.findViewById(R.id.back_rl).setOnClickListener(this);
        title.findViewById(R.id.more_rl).setVisibility(View.GONE);
        View line = (View)(title.findViewById(R.id.line));
        line.setVisibility(View.GONE);
        mTvTitle = (TextView)(title.findViewById(R.id.display_name));
        mTvTitle.setTextColor(getResources().getColor(R.color.fontcor3));
        mTvTitle.setText(R.string.main_title);

        mLlDistance = (RelativeLayout)findViewById(R.id.ll_distance);
        mTvXiuYiXiu = (TextView)findViewById(R.id.tv_xiyixiu);
        mIvTextBg = (RoundImageView)findViewById(R.id.iv_tv_backgroup);
        mSearchingView = (RippleView) findViewById(R.id.searchingView);
        //mFulfillView = (View) findViewById(R.id.iv_tv_backgroup);
        mRlSearchArea = (RelativeLayout) findViewById(R.id.rlSearching);
        mPvFriend = (PortraitView) findViewById(R.id.pv_friend_portrait);
        mPvFriend.setOnClickListener(this);
        mTvPleaseClickPortrait = (TextView)findViewById(R.id.tv_click_portrait);

        //Disable those UI element until APP get friend's coordinate.
        mSearchingView.stop();
        mSearchingView.setVisibility(View.GONE);
        mPvFriend.setVisibility(View.GONE);
        mTvXiuYiXiu.setVisibility(View.GONE);
        mIvTextBg.setVisibility(View.GONE);
        //mTvPleaseClickPortrait.setVisibility(View.GONE);

    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            //Get conversation color, default is color index 1.
            long threadId = intent.getLongExtra(PARAMS_THREAD_ID, 0l);
            MessageSessionBaseModel conversation = WalkArroundMsgManager.getInstance(getApplicationContext()).getSessionByThreadId(threadId);
            int colorIndex = conversation.colorIndex;
            int color = MessageUtil.getFriendColor(colorIndex);
            //Init background color
            if (color >= 0) {
                //Activity body color
                mLlDistance.setBackgroundColor(getResources().getColor(color));

                //Title color
                RelativeLayout rlTitle = (RelativeLayout) (findViewById(R.id.title));
                if (rlTitle != null) {
                    rlTitle.setBackgroundColor(getResources().getColor(color));
                }

                logger.d("init  color index is :" + colorIndex);
            }

            //Set searching circle color
            mSearchingView.setInitColor(R.color.bgcor15);

            //Init portrait
            mStrFriendId = conversation.getContact();
            ContactInfo usr = ContactsManager.getInstance(this.getApplicationContext()).getContactByUsrObjId(mStrFriendId);
            if (usr != null) {
                mPvFriend.setBaseData(usr.getUsername(), usr.getPortrait().getUrl(),
                        usr.getUsername().substring(0, 1), -1);

                //Init bottom indication text
                String friendName = usr.getUsername();
                if(friendName.length() > AppConstant.SHORTNAME_LEN) {
                    friendName = friendName.substring(0, AppConstant.SHORTNAME_LEN) + "...";
                }
                mDistanceWithin = getString(R.string.walk_rule_please_click_portrait, friendName);
                mDistanceBeyonded = getString(R.string.mapping_hint_find_friend_cor, getString(MessageUtil.getFriendColorDescription(colorIndex)),friendName);

                mTvPleaseClickPortrait.setText(mDistanceBeyonded);
            }

            mIvTextBg.setBackgroundColor(getResources().getColor(MessageUtil.getFriendColor(colorIndex)));
        }
    }

    private void initRegisterforNewMsg() {
        // 监听新消息及消息状态变化
        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(MsgBroadcastConstants.ACTION_MESSAGE_NEW_RECEIVED);

        registerReceiver(mMessageReceiver, commandFilter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_rl:
                finish();
                break;
            case R.id.pv_friend_portrait:
                mWalkRequestDialog = DialogFactory.getStart2WalkDialog(this, mStrFriendId, new DialogFactory.ConfirmDialogClickListener() {

                    @Override
                    public void onConfirmDialogConfirmClick() {
                        //Send walk invitation to friend.
                        String extraInfor = MessageUtil.EXTRA_START_2_WALKARROUND +
                                MessageUtil.EXTRA_INFOR_SPLIT +
                                MessageUtil.EXTRA_START_2_WALK_REQUEST;

                        WalkArroundMsgManager.getInstance(getApplicationContext()).sendTextMsg(mStrFriendId, getString(R.string.agree_2_walk_face_2_face_req), extraInfor);
                    }
                });
                mWalkRequestDialog.show();
                break;
            default:
                break;
        }
    }

    /*
     * True: visible
     * False: gone
     */
    private void setPortraitViewVisible(int visibility) {
        if(mPvFriend != null) {
            mPvFriend.setVisibility(visibility);
        }

        if(mTvPleaseClickPortrait != null) {
            mTvPleaseClickPortrait.setVisibility(visibility);
        }
    }

    private void createWalkReplyDialog() {
        mWalkReplyDialog = DialogFactory.getStart2WalkReplyDialog(this, mStrFriendId, new DialogFactory.NoticeDialogCancelClickListener() {
            @Override
            public void onNoticeDialogCancelClick() {
                String extraInfor = MessageUtil.EXTRA_START_2_WALKARROUND +
                        MessageUtil.EXTRA_INFOR_SPLIT +
                        MessageUtil.EXTRA_START_2_WALK_REPLY_NEXT_TIME;
                WalkArroundMsgManager.getInstance(getApplicationContext()).sendTextMsg(mStrFriendId,
                        getString(R.string.agree_2_walk_face_2_face_req), extraInfor);

                if(mWalkReplyDialog != null) {
                    mWalkReplyDialog.dismiss();
                    mWalkReplyDialog = null;
                }
            }

            @Override
            public void onNoticeDialogConfirmClick(boolean isChecked, Object value) {
                String extraInfor = MessageUtil.EXTRA_START_2_WALKARROUND +
                        MessageUtil.EXTRA_INFOR_SPLIT +
                        MessageUtil.EXTRA_START_2_WALK_REPLY_OK;
                WalkArroundMsgManager.getInstance(getApplicationContext()).sendTextMsg(mStrFriendId,
                        getString(R.string.agree_2_walk_face_2_face_req), extraInfor);

                if(mWalkReplyDialog != null) {
                    mWalkReplyDialog.dismiss();
                    mWalkReplyDialog = null;
                }


                Intent intent = new Intent(ShowDistanceActivity.this, CountdownActivity.class);
                intent.putExtra(CountdownActivity.PARAMS_FRIEND_OBJ_ID, mStrFriendId);
                ShowDistanceActivity.this.startActivity(intent);
            }
        });
    }

    private void start2GetFriendCoordinate() {
        //Start mRealCountdownTask to get friend dynamic data, like distance, online or not...
        ThreadPoolManager.getPoolManager().addAsyncTask(new QueryUsrCoordinateTask(getApplicationContext(),
                mQueryFriendCoordinateTaskListener,
                HttpUtil.HTTP_FUNC_QUERY_USR_COORDINATE,
                HttpUtil.HTTP_TASK_QUERY_USR_COORDINATE,
                QueryUsrCoordinateTask.getParams(mStrFriendId),
                TaskUtil.getTaskHeader()));
    }

    private void updateDistanceBetweenFriends() {
        if(mFriendGeoData != null) {
            GeoData myGeo = ProfileManager.getInstance().getMyProfile().getLocation();
            if(myGeo != null) {
                int distance = (int)CommonUtils.getDistance(myGeo.getLatitude(), myGeo.getLongitude(),
                        mFriendGeoData.getLatitude(), mFriendGeoData.getLongitude());

                if(mPriorDistance != -1) {
                    if(mPriorDistance == distance) {
                        return;
                    }
                }

                mPriorDistance = distance;
                mUiHandler.removeMessages(MSG_UPDATE_DISTANCE);
                Message msg = mUiHandler.obtainMessage();
                msg.what = MSG_UPDATE_DISTANCE;
                msg.arg1 = distance;
                mUiHandler.sendMessageDelayed(msg, 500);
            }
        }
    }
}
