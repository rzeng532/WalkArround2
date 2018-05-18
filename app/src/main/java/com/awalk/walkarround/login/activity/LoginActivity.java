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
import com.awalk.walkarround.base.view.DialogFactory;
import com.awalk.walkarround.login.manager.LoginManager;
import com.awalk.walkarround.main.activity.AppMainActivity;
import com.awalk.walkarround.myself.manager.ProfileManager;
import com.awalk.walkarround.util.AppConstant;
import com.awalk.walkarround.util.AsyncTaskListener;
import com.awalk.walkarround.util.CommonUtils;
import com.awalk.walkarround.util.Logger;
import com.awalk.walkarround.util.network.NetWorkManager;

/**
 * A class for login operations
 * Date: 2015-12-02
 *
 * @author Richard
 */
public class LoginActivity extends Activity implements View.OnClickListener {

    private EditText edUserName = null;
    private EditText edPassWord = null;
    private Button btnLogin = null;
    //private Button btnRegister = null;
    //private TextView tvForgotPwd = null;
    private Dialog mLoadingDialog;
    private Logger loginLogger;

    private final int LOGIN_OK = 0;
    private final int LOGIN_FAIL = 1;
    private final int HANDLER_MSG_DELAY = 2000; //1 second

    private Handler mLoginHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == LOGIN_OK) {
                //Goto target page.
                dismissDialog();
                Toast.makeText(getApplicationContext(), getString(R.string.login_do_login_success), Toast.LENGTH_SHORT).show();
                startMainActivity();
                setResult(AppConstant.ACTIVITY_RETURN_CODE_OK);
                setResult(CommonUtils.ACTIVITY_FINISH_NORMAL_FINISH);
                finish();
            } else if (msg.what == LOGIN_FAIL) {
                dismissDialog();
                Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
            }
        }
    };

    AsyncTaskListener mLoginListener = new AsyncTaskListener() {
        @Override
        public void onSuccess(Object data) {
            Message msg = Message.obtain();
            loginLogger.d("Do login success.");

            ProfileManager.getInstance().getMyProfile();
            LoginManager.getInstance().setCurrentUser();

            msg.what = LOGIN_OK;
            mLoginHandler.sendMessageDelayed(msg, HANDLER_MSG_DELAY);
        }

        @Override
        public void onFailed(AVException e) {
            loginLogger.d("Get SMS code failed, " + e.getMessage());
            AVAnalytics.onEvent(LoginActivity.this, AppConstant.ANA_EVENT_LOGIN, AppConstant.ANA_TAG_RET_FAIL);
            Message msg = Message.obtain();
            msg.what = LOGIN_FAIL;
            msg.obj = LoginManager.getInstance().getErrStringViaErrorCode(getApplicationContext(), e.getCode());
            mLoginHandler.sendMessageDelayed(msg, HANDLER_MSG_DELAY);
        }
    };

    private TextWatcher mContentWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            setNextButtonVisibleOrNot();
        }
    };

    private void setNextButtonVisibleOrNot() {
        if(edUserName != null && edUserName.getText().length() > 0
                && edPassWord != null && edPassWord.getText().length() > 0) {
            btnLogin.setVisibility(View.VISIBLE);
        } else {
            btnLogin.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_do_login);
        findView();
        setNextButtonVisibleOrNot();
        loginLogger = Logger.getLogger(LoginActivity.class.getSimpleName());
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

    private void findView() {

        //Title
        View title = findViewById(R.id.title);
        title.findViewById(R.id.back_rl).setOnClickListener(this);
        title.findViewById(R.id.more_rl).setVisibility(View.GONE);
        ((TextView)(title.findViewById(R.id.display_name))).setText(R.string.login_dologin);

        //Init UI elements
        edUserName = (EditText) findViewById(R.id.signin_input_username);
        edUserName.addTextChangedListener(mContentWatcher);
        edPassWord = (EditText) findViewById(R.id.signin_input_password);
        edPassWord.addTextChangedListener(mContentWatcher);

        //Button init step should before setText step. Since set text will trigger button visible or not.
        btnLogin = (Button) findViewById(R.id.signin_loginin);
        btnLogin.setOnClickListener(this);

        //Get current user
        String strOrigUser = LoginManager.getInstance().getCurrentUserName();
        if (!TextUtils.isEmpty(strOrigUser)) {
            edUserName.setText(strOrigUser);
            edPassWord.requestFocus();
        }

//        tvForgotPwd = (TextView) findViewById(R.id.signin_forgot_password);
//        tvForgotPwd.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.signin_loginin) {
            //Start login process
            if (!isLoginParamsValid()) {
                return;
            }
            if(!NetWorkManager.getInstance(getApplicationContext()).isNetworkAvailable()) {
                Toast.makeText(getApplicationContext(), getString(R.string.err_network_unavailable), Toast.LENGTH_SHORT).show();
                return;
            }

            showDialog();
            try {
                String strUsername = ((EditText) findViewById(R.id.signin_input_username)).getText().toString();
                String strPwd = ((EditText) findViewById(R.id.signin_input_password)).getText().toString();
                AVAnalytics.onEvent(this, AppConstant.ANA_EVENT_LOGIN);
                LoginManager.getInstance().doLogin(strUsername, strPwd, mLoginListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        else if (v.getId() == R.id.signin_forgot_password) {
//            //TODO: add forgot password step.
//        }
        else if (v.getId() == R.id.back_rl) {
            this.finish();
            onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        startActivity(new Intent(LoginActivity.this, LoginOrRegActivity.class));
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

    /*
     * Check if login parameters if valid or not.
     * return false if information is invalide, otherwise it will return true.
     */
    private boolean isLoginParamsValid() {
        String strUsername = ((EditText) findViewById(R.id.signin_input_username)).getText().toString();
        String strPwd = ((EditText) findViewById(R.id.signin_input_password)).getText().toString();

        if (TextUtils.isEmpty(strUsername) || TextUtils.isEmpty(strPwd)) {
            // TODO: add a popup to indicate user?
            Toast.makeText(this, getString(R.string.err_login_infor_cannot_empty),
                    Toast.LENGTH_LONG).show();
            return false;
        }

        if (!CommonUtils.checkUserName(strUsername)) {
            Toast.makeText(this, getString(R.string.err_login_infor_is_illegal),
                    Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void updateUserData() {

    }

    private void startMainActivity() {
        startActivity(new Intent(LoginActivity.this, AppMainActivity.class));
    }
}
