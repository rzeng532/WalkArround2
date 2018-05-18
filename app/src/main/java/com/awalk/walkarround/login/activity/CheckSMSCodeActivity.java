/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.awalk.walkarround.login.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.awalk.walkarround.R;
import com.awalk.walkarround.assistant.AssistantHelper;
import com.awalk.walkarround.base.view.DialogFactory;
import com.awalk.walkarround.login.manager.LoginManager;
import com.awalk.walkarround.util.AppConstant;
import com.awalk.walkarround.util.AsyncTaskListener;
import com.awalk.walkarround.util.Logger;

import java.util.Timer;
import java.util.TimerTask;


/**
 * TODO: description
 * Date: 2015-11-26
 *
 * @author Richard
 */
public class CheckSMSCodeActivity extends Activity implements View.OnClickListener, DialogFactory.NoticeDialogClickListener {

    private String mStrPhoneNum;
    private String mStrNickName;
    private TextView mTipView;
    private TextView mTipStandard;
    private Button mNextButton;
    private EditText mEvCodeView;
    private Timer mTimer;
    private int mTime;
    private Dialog mLoadingDialog;
    private final int TIME = 1;
    private final int REGET = 2;
    private final int ERROR = 3;
    private final int REGISTERSUCCESS = 4;
    private Logger myLogger;
    private TimerTask mTimerTask;

    public static final int GIVEUP = 1;
    public static final int FINISHTHIS = 3;

    private final int COUNTDOWN_TIME = 120;
    private final int HANDLER_MSG_DELAY = 1000; //1 second

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TIME) {
                mTipView.setText(mTime + getString(R.string.common_second));
            } else if (msg.what == REGET) {
                mTipView.setVisibility(View.GONE);
                mTipStandard.setText(getString(R.string.register_reget_check_code));
                mTipStandard.setTextColor(getResources().getColor(R.color.fontcor6));
                mTipStandard.setOnClickListener(CheckSMSCodeActivity.this);
                mNextButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_gray_nextstep));
                mNextButton.setOnClickListener(null);
            } else if (msg.what == ERROR) {
                String tip = (String) msg.obj;
                Toast.makeText(CheckSMSCodeActivity.this, tip, Toast.LENGTH_SHORT).show();
            } else if (msg.what == REGISTERSUCCESS) {
                //TODO: set register account to local
                LoginManager.getInstance().setCurrentUser();
                //start login activity
                Intent intent = new Intent(CheckSMSCodeActivity.this, LoginActivity.class);
                startActivity(intent);
                setResult(FINISHTHIS, getIntent());
                finish();
            }
        }
    };

    AsyncTaskListener mGetSMSCodeListener = new AsyncTaskListener() {
        @Override
        public void onSuccess(Object data) {
            // show guide for new user
            AssistantHelper.getInstance().forkRegisterState();
            Toast.makeText(getApplicationContext(), getString(R.string.register_signup_success), Toast.LENGTH_SHORT).show();
            Message msg = Message.obtain();
            myLogger.d("Get SMS code success.");
            dismissDialog();
            msg.what = REGISTERSUCCESS;
            mHandler.sendMessageDelayed(msg, HANDLER_MSG_DELAY);
        }

        @Override
        public void onFailed(AVException e) {
            dismissDialog();
            AVAnalytics.onEvent(CheckSMSCodeActivity.this, AppConstant.ANA_EVENT_REGISTER, AppConstant.ANA_TAG_RET_FAIL);
            myLogger.d("Get SMS code failed, " + e.getMessage());
            Message msg = Message.obtain();
            msg.what = ERROR;
            msg.obj = LoginManager.getInstance().getErrStringViaErrorCode(getApplicationContext(), e.getCode());
            mHandler.sendMessageDelayed(msg, HANDLER_MSG_DELAY);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myLogger = Logger.getLogger(CheckSMSCodeActivity.class.getSimpleName());

        setContentView(R.layout.activity_login_input_checkcode);
        getIntentData();
        initView();
        startTimerTask();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AVAnalytics.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AVAnalytics.onPause(this);
    }

    private void getIntentData() {
        //Get data from login manager
        mStrPhoneNum = LoginManager.getInstance().getPhoneNum();
        if (TextUtils.isEmpty(mStrPhoneNum)) {
            finish();
        }

        mStrNickName = LoginManager.getInstance().getUserName();
        if (TextUtils.isEmpty(mStrNickName)) {
            mStrNickName = mStrPhoneNum;
        }
    }

    private void initView() {

        //Tip
        if(!TextUtils.isEmpty(mStrPhoneNum)) {
            TextView tip = (TextView)findViewById(R.id.tip_sms_already_sent);
            tip.setText(getString(R.string.register_sms_already_sent, mStrPhoneNum));
        }

        //Title
        View title = findViewById(R.id.title);
        title.findViewById(R.id.back_rl).setOnClickListener(this);
        title.findViewById(R.id.more_rl).setVisibility(View.GONE);
        ((TextView)(title.findViewById(R.id.display_name))).setText(R.string.register_create_account);

        mNextButton = (Button) findViewById(R.id.btn_nextstep);
        mTipView = (TextView) findViewById(R.id.tip_txtView);
        mTipStandard = (TextView) findViewById(R.id.tip_standard);
        mEvCodeView = (EditText) findViewById(R.id.checkcode);
        mEvCodeView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                setNextButtonVisibleOrNot();
            }
        });
        mNextButton.setOnClickListener(this);

        setNextButtonVisibleOrNot();
    }

    private void setNextButtonVisibleOrNot() {
        if(mEvCodeView != null && mEvCodeView.getText().length() > 0) {
            mNextButton.setVisibility(View.VISIBLE);
        } else {
            mNextButton.setVisibility(View.GONE);
        }
    }

    private void startTimerTask() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        newTimerTask();
        mTimer = new Timer();
        mTime = COUNTDOWN_TIME;
        mTipView.setText(mTime + getString(R.string.common_second));
        mTipView.setVisibility(View.VISIBLE);
        mTipStandard.setOnClickListener(null);
        mTipStandard.setTextColor(getResources().getColor(R.color.fontcor4));
        mTipStandard.setText(getString(R.string.register_time_next_reget));
        mNextButton.setOnClickListener(this);
        mTimer.schedule(mTimerTask, 0, 1000);
    }

    private void newTimerTask() {
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (mTime <= 0) {
                    mTimer.cancel();
                    Message msg = Message.obtain();
                    msg.what = REGET;
                    mHandler.sendMessage(msg);
                    return;
                }
                mTime--;
                Message msg = Message.obtain();
                msg.what = TIME;
                mHandler.sendMessage(msg);
            }
        };
    }

    @Override
    public void onBackPressed() {
        Dialog noticeDialog = DialogFactory.getNoticeDialog(this,
                getResources().getString(R.string.register_sure_give_up_register), this, null);
        noticeDialog.show();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_nextstep) {
            String code = mEvCodeView.getText().toString();
            if (TextUtils.isEmpty(code)) {
                Toast.makeText(this, getString(R.string.login_hint_please_input_SMS_code), Toast.LENGTH_SHORT).show();
                return;
            }
            showDialog();
            //TODO: create account via SMS code.
            AVAnalytics.onEvent(CheckSMSCodeActivity.this, AppConstant.ANA_EVENT_REGISTER);
            LoginManager.getInstance().createAccountWithCode(code, mGetSMSCodeListener);
        } else if (v.getId() == R.id.tip_standard) {
            startTimerTask();
        } else if (v.getId() == R.id.back_rl) {
            Dialog noticeDialog = DialogFactory.getNoticeDialog(this,
                    getResources().getString(R.string.register_sure_give_up_register), this, null);
            noticeDialog.show();
        }
    }

    private void showDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = DialogFactory.getLoadingDialog(this, getString(R.string.common_please_wait_for_a_moment),
                    true, null);
            mLoadingDialog.show();
        }
    }

    private void dismissDialog() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }
    }

    @Override
    public void onNoticeDialogConfirmClick(boolean isChecked, Object value) {
        setResult(GIVEUP, getIntent());
        finish();
    }
}
