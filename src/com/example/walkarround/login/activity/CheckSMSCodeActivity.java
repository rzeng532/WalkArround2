/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.example.walkarround.login.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.example.walkarround.R;
import com.example.walkarround.base.view.DialogFactory;
import com.example.walkarround.login.manager.LoginManager;
import com.example.walkarround.util.AsyncTaskListener;
import com.example.walkarround.util.Logger;

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
    private Button mFinishButton;
    private EditText mCodeView;
    private TextView mBackView;
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
                mFinishButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_gray_nextstep));
                mFinishButton.setOnClickListener(null);
            } else if (msg.what == ERROR) {
                String tip = (String) msg.obj;
                Toast.makeText(CheckSMSCodeActivity.this, tip, Toast.LENGTH_SHORT).show();
            } else if (msg.what == REGISTERSUCCESS) {
                //TODO: set register account to local
                LoginManager.getInstance().setCurrentUser();
                //TODO: start login activity or goto main activity?
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
        mFinishButton = (Button) findViewById(R.id.btn_nextstep);
        mTipView = (TextView) findViewById(R.id.tip_txtView);
        mTipStandard = (TextView) findViewById(R.id.tip_standard);
        mCodeView = (EditText) findViewById(R.id.checkcode);
        mBackView = (TextView) findViewById(R.id.title_name);
        mBackView.setOnClickListener(this);
        mFinishButton.setOnClickListener(this);
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
        mFinishButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_nextstep));
        mFinishButton.setOnClickListener(this);
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
            String code = mCodeView.getText().toString();
            if (TextUtils.isEmpty(code)) {
                Toast.makeText(this, getString(R.string.login_hint_please_input_SMS_code), Toast.LENGTH_SHORT).show();
                return;
            }
            showDialog();
            //TODO: create account via SMS code.
            LoginManager.getInstance().createAccountWithCode(code, mGetSMSCodeListener);
        } else if (v.getId() == R.id.tip_standard) {
            startTimerTask();
        } else if (v.getId() == R.id.title_name) {
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
