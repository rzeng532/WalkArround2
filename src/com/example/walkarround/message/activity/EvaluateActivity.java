/**
 * Copyright (C) 2014-2016 CMCC All rights reserved
 */
package com.example.walkarround.message.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.walkarround.EntranceActivity;
import com.example.walkarround.R;
import com.example.walkarround.base.view.DialogFactory;
import com.example.walkarround.base.view.PhotoView;
import com.example.walkarround.main.model.ContactInfo;
import com.example.walkarround.main.parser.WalkArroundJsonResultParser;
import com.example.walkarround.main.task.AddFriendTask;
import com.example.walkarround.main.task.QuerySpeedDateIdTask;
import com.example.walkarround.main.task.TaskUtil;
import com.example.walkarround.message.manager.ContactsManager;
import com.example.walkarround.message.manager.WalkArroundMsgManager;
import com.example.walkarround.message.task.EndSpeedDateTask;
import com.example.walkarround.message.task.EvaluateFriendTask;
import com.example.walkarround.message.util.MessageConstant;
import com.example.walkarround.message.util.MessageUtil;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.Logger;
import com.example.walkarround.util.http.HttpTaskBase;
import com.example.walkarround.util.http.HttpUtil;
import com.example.walkarround.util.http.ThreadPoolManager;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: description
 * Date: 2016-08-12
 *
 * @author Administrator
 */
public class EvaluateActivity extends Activity implements View.OnClickListener, RatingBar.OnRatingBarChangeListener {

    Logger myLogger = Logger.getLogger(EvaluateActivity.class.getSimpleName());

    public static final String PARAMS_FRIEND_OBJ_ID = "friend_obj_id";

    private TextView mTvDescription;
    private TextView mTvComplete;

    private RatingBar mRbHonest;
    private RatingBar mRbConversationStyle;
    private RatingBar mRbAppearance;
    private RatingBar mRbTemperament;

    private ContactInfo mFriend = null;
    private PhotoView mPvPortrait;

    private Dialog mLoadingDialog;
    private long mThreadId = -1l;

    private HttpTaskBase.onResultListener mEvaluateFriendTaskListener = new HttpTaskBase.onResultListener() {
        @Override
        public void onPreTask(String requestCode) {

        }

        @Override
        public void onResult(Object object, HttpTaskBase.TaskResult resultCode, String requestCode, String threadId) {
            myLogger.d("EvaluateFriend done." + (String)object);
            if (HttpTaskBase.TaskResult.SUCCEESS == resultCode && requestCode.equalsIgnoreCase(HttpUtil.HTTP_FUNC_EVALUATE_EACH) && mFriend != null) {
                myLogger.d("EvaluateFriend success, next step is add friend");

                //Get colorIndex
                List<String> recipient = new ArrayList<>();
                recipient.add(mFriend.getObjectId());
                long msgThreadId = mThreadId;
                if (msgThreadId >= 0) {
                    int colorIndex = WalkArroundMsgManager.getInstance(getApplicationContext()).getConversationColorIndex(msgThreadId);
                    WalkArroundMsgManager.getInstance(getApplicationContext()).updateConversationStatus(msgThreadId, MessageUtil.WalkArroundState.STATE_END);

                    myLogger.d("AddFriendTask, color index is: " + colorIndex);

                    ThreadPoolManager.getPoolManager().addAsyncTask(new AddFriendTask(getApplicationContext(),
                            mAddFriendTaskListener,
                            HttpUtil.HTTP_FUNC_ADD_FRIEND,
                            HttpUtil.HTTP_TASK_ADD_FRIEND,
                            AddFriendTask.getParams(ProfileManager.getInstance().getCurUsrObjId(), (mFriend != null) ? mFriend.getObjectId() : null, "" + colorIndex),
                            TaskUtil.getTaskHeader()));
                } else {
                    mUIHandler.sendEmptyMessage(MSG_EVALUATE_FAILED);
                }
            } else if (HttpTaskBase.TaskResult.SUCCEESS != resultCode && requestCode.equalsIgnoreCase(HttpUtil.HTTP_FUNC_EVALUATE_EACH)) {
                myLogger.d("EvaluateFriend failed.");
                mUIHandler.sendEmptyMessage(MSG_EVALUATE_FAILED);
            }
        }

        @Override
        public void onProgress(int progress, String requestCode) {

        }
    };

    private HttpTaskBase.onResultListener mAddFriendTaskListener = new HttpTaskBase.onResultListener() {
        @Override
        public void onPreTask(String requestCode) {

        }

        @Override
        public void onResult(Object object, HttpTaskBase.TaskResult resultCode, String requestCode, String threadId) {
            //Task success.
            if (HttpTaskBase.TaskResult.SUCCEESS == resultCode && requestCode.equalsIgnoreCase(HttpUtil.HTTP_FUNC_ADD_FRIEND)) {

                //End speed date
                String speedDateId = ProfileManager.getInstance().getSpeedDateId();
                if (!TextUtils.isEmpty(speedDateId)) {
                    ThreadPoolManager.getPoolManager().addAsyncTask(new EndSpeedDateTask(getApplicationContext(),
                            mEndSpeedDateTaskListener,
                            HttpUtil.HTTP_FUNC_END_SPEED_DATE,
                            HttpUtil.HTTP_TASK_END_SPEED_DATE,
                            EndSpeedDateTask.getParams(speedDateId),
                            TaskUtil.getTaskHeader()));
                }
            } else if (HttpTaskBase.TaskResult.SUCCEESS != resultCode && requestCode.equalsIgnoreCase(HttpUtil.HTTP_FUNC_ADD_FRIEND)) {
                mUIHandler.sendEmptyMessage(MSG_EVALUATE_FAILED);
            }
        }

        @Override
        public void onProgress(int progress, String requestCode) {

        }
    };

    private HttpTaskBase.onResultListener mEndSpeedDateTaskListener = new HttpTaskBase.onResultListener() {
        @Override
        public void onPreTask(String requestCode) {

        }

        @Override
        public void onResult(Object object, HttpTaskBase.TaskResult resultCode, String requestCode, String threadId) {
            //Task success.
            if (HttpTaskBase.TaskResult.SUCCEESS == resultCode && requestCode.equalsIgnoreCase(HttpUtil.HTTP_FUNC_END_SPEED_DATE)) {
                mUIHandler.sendEmptyMessage(MSG_EVALUATE_SUCCESS);
            } else if (HttpTaskBase.TaskResult.SUCCEESS != resultCode && requestCode.equalsIgnoreCase(HttpUtil.HTTP_FUNC_END_SPEED_DATE)) {
                mUIHandler.sendEmptyMessage(MSG_EVALUATE_FAILED);
            }
        }

        @Override
        public void onProgress(int progress, String requestCode) {

        }
    };

    private HttpTaskBase.onResultListener mGetSpeedIdTaskListener = new HttpTaskBase.onResultListener() {
        @Override
        public void onPreTask(String requestCode) {

        }

        @Override
        public void onResult(Object object, HttpTaskBase.TaskResult resultCode, String requestCode, String threadId) {
            //Task success.
            if (HttpTaskBase.TaskResult.SUCCEESS == resultCode && requestCode.equalsIgnoreCase(HttpUtil.HTTP_FUNC_QUERY_SPEED_DATE)) {
                //Get status & Get TO user.
                String strSpeedDateId = WalkArroundJsonResultParser.parseRequireCode((String) object, HttpUtil.HTTP_RESPONSE_KEY_OBJECT_ID);
                myLogger.d("Query speed date id response success: " + strSpeedDateId);

                //Send impression value to server
                ThreadPoolManager.getPoolManager().addAsyncTask(new EvaluateFriendTask(getApplicationContext(),
                        mEvaluateFriendTaskListener,
                        HttpUtil.HTTP_FUNC_EVALUATE_EACH,
                        HttpUtil.HTTP_TASK_EVALUATION_EACH,
                        EvaluateFriendTask.getParams(ProfileManager.getInstance().getCurUsrObjId(), (int) (mRbHonest.getRating()),
                                (int) (mRbConversationStyle.getRating()), (int) (mRbAppearance.getRating()),
                                (int) (mRbTemperament.getRating()), strSpeedDateId),
                        TaskUtil.getTaskHeader()));
            } else {
                myLogger.d("Query speed date id failed");
                mUIHandler.sendEmptyMessage(MSG_EVALUATE_FAILED);
            }
        }

        @Override
        public void onProgress(int progress, String requestCode) {

        }
    };

    private static final int MSG_EVALUATE_SUCCESS = 1;
    private static final int MSG_EVALUATE_FAILED = 2;

    private Handler mUIHandler = new Handler() {
        public void handleMessage(Message msg) {

            dismissCircleDialog();
            switch (msg.what) {
                case MSG_EVALUATE_SUCCESS:
                    //Send I agree to walk arround.
                    //Use RESULT_FIRST_USER as agreement for prior activity.
                    //setResult(RESULT_FIRST_USER);
                    ProfileManager.getInstance().setCurUsrDateState(MessageUtil.WalkArroundState.STATE_END);
                    Toast.makeText(EvaluateActivity.this, R.string.evaluate_send_impression2server_suc, Toast.LENGTH_LONG).show();
                    dismissCircleDialog();
                    Intent target = new Intent(getApplicationContext(), EntranceActivity.class);
                    target.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    target.putExtra(AppConstant.KEY_START_TARGET_ACTIVITY, AppConstant.START_MAIN_ACTIVITY);
                    startActivity(target);
                    EvaluateActivity.this.finish();
                    break;
                case MSG_EVALUATE_FAILED:
                    dismissCircleDialog();

                    //TODO: We should indicate user details by different server response.Like this friend already be evaluated, time out...
                    Toast.makeText(EvaluateActivity.this, R.string.evaluate_send_impression2server_fail, Toast.LENGTH_LONG).show();

                    EvaluateActivity.this.finish();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluate);

        initData();

        initView();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initData() {
        //Get data from intent, like friend objId, speed data id.
        Intent intent = getIntent();
        if (intent != null) {
            String friendId = intent.getStringExtra(PARAMS_FRIEND_OBJ_ID);

            if (!TextUtils.isEmpty(friendId)) {
                mFriend = ContactsManager.getInstance(this).getContactByUsrObjId(friendId);
            }
        }

        if(mFriend != null) {
            List<String> recipient = new ArrayList<>();
            recipient.add(mFriend.getObjectId());
            mThreadId = WalkArroundMsgManager.getInstance(getApplicationContext()).getConversationId(MessageConstant.ChatType.CHAT_TYPE_ONE2ONE,
                    recipient);
            if(mThreadId > -1l) {
                WalkArroundMsgManager.getInstance(getApplicationContext()).updateConversationStatus(mThreadId, MessageUtil.WalkArroundState.STATE_IMPRESSION);
            }
        }
    }

    private void initView() {
        mTvDescription = (TextView) findViewById(R.id.tv_walk_description);
        mTvComplete = (TextView) findViewById(R.id.tv_complete_walk);
        mTvComplete.setOnClickListener(this);

        mPvPortrait = (PhotoView) findViewById(R.id.pv_evaluate);

        mRbHonest = (RatingBar) findViewById(R.id.rating_honest);
        mRbHonest.setOnRatingBarChangeListener(this);

        mRbConversationStyle = (RatingBar) findViewById(R.id.rating_style_of_conversation);
        mRbConversationStyle.setOnRatingBarChangeListener(this);

        mRbAppearance = (RatingBar) findViewById(R.id.rating_appearance);
        mRbAppearance.setOnRatingBarChangeListener(this);

        mRbTemperament = (RatingBar) findViewById(R.id.rating_temperament);
        mRbTemperament.setOnRatingBarChangeListener(this);

        if (mFriend != null) {
            String friendName = mFriend.getUsername();
            mTvDescription.setText(getString(R.string.countdown_walk_with_who, friendName));

            mPvPortrait.setBaseData(friendName, mFriend.getPortrait().getUrl(), null,
                    R.drawable.contact_default_profile);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_complete_walk) {
            if (mRbHonest.getRating() > 0.0f
                    && mRbConversationStyle.getRating() > 0.0f
                    && mRbAppearance.getRating() > 0.0f
                    && mRbTemperament.getRating() > 0.0f) {
                //Get speed data id -> evaluate friend -> finish;
                showCircleDialog();
                String speedDateId = ProfileManager.getInstance().getSpeedDateId();
                if (!TextUtils.isEmpty(speedDateId)) {
                    ThreadPoolManager.getPoolManager().addAsyncTask(new EvaluateFriendTask(getApplicationContext(),
                            mEvaluateFriendTaskListener,
                            HttpUtil.HTTP_FUNC_EVALUATE_EACH,
                            HttpUtil.HTTP_TASK_EVALUATION_EACH,
                            EvaluateFriendTask.getParams(ProfileManager.getInstance().getCurUsrObjId(), (int) (mRbHonest.getRating()),
                                    (int) (mRbConversationStyle.getRating()), (int) (mRbAppearance.getRating()),
                                    (int) (mRbTemperament.getRating()), speedDateId),
                            TaskUtil.getTaskHeader()));
                } else {
                    mUIHandler.sendEmptyMessage(MSG_EVALUATE_FAILED);
                }
            } else {
                //Indicate user to evaluate
                Toast.makeText(this, getResources().getString(R.string.evaluate_please_rating), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

        checkRatingData();

        switch (ratingBar.getId()) {
            case R.id.rating_honest:
                myLogger.d("onRatingChanged rating_honest, value is " + rating);
                break;
            case R.id.rating_style_of_conversation:
                myLogger.d("onRatingChanged rating_style_of_conversation, value is " + rating);

                break;
            case R.id.rating_appearance:
                myLogger.d("onRatingChanged rating_appearance, value is " + rating);

                break;
            case R.id.rating_temperament:
                myLogger.d("onRatingChanged rating_temperament, value is " + rating);

                break;
            default:
                return;
        }
    }

    /*
     * Check Rating state.
     * If user complete rating, button background is red. Otherwise, background is transparent.
     */
    private void checkRatingData() {
        //User complete rating.
        if (mRbHonest.getRating() > 0.0f
                && mRbConversationStyle.getRating() > 0.0f
                && mRbAppearance.getRating() > 0.0f
                && mRbTemperament.getRating() > 0.0f) {
            mTvComplete.setClickable(true);
            GradientDrawable backGround = (GradientDrawable) mTvComplete.getBackground();
            backGround.setColor(getResources().getColor(R.color.red_button));
        } else {
            mTvComplete.setClickable(false);
            GradientDrawable backGround = (GradientDrawable) mTvComplete.getBackground();
            backGround.setColor(getResources().getColor(R.color.transparent));
        }
    }

    private void showCircleDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = DialogFactory.getLoadingDialog(this, false, null);
        }
        myLogger.d("Show dialog.");
        mLoadingDialog.show();
    }

    private void dismissCircleDialog() {
        myLogger.d("Dismiss dialog.");
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    private void getSpeedDataId() {
        String userObjId = ProfileManager.getInstance().getCurUsrObjId();

        if (TextUtils.isEmpty(userObjId)) {
            return;
        }

        ThreadPoolManager.getPoolManager().addAsyncTask(new QuerySpeedDateIdTask(getApplicationContext(),
                mGetSpeedIdTaskListener,
                HttpUtil.HTTP_FUNC_QUERY_SPEED_DATE,
                HttpUtil.HTTP_TASK_QUERY_SPEED_DATE,
                QuerySpeedDateIdTask.getParams(userObjId),
                TaskUtil.getTaskHeader()));
    }
}
