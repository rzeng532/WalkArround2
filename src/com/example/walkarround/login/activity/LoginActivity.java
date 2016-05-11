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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.avos.avoscloud.AVException;
import com.example.walkarround.R;
import com.example.walkarround.base.view.DialogFactory;
import com.example.walkarround.login.manager.LoginManager;
import com.example.walkarround.main.activity.AppMainActivity;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.AsyncTaskListener;
import com.example.walkarround.util.CommonUtils;
import com.example.walkarround.util.Logger;
import com.example.walkarround.util.network.NetWorkManager;

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
    private TextView tvForgotPwd = null;
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

            LoginManager.getInstance().setCurrentUser();

            msg.what = LOGIN_OK;
            mLoginHandler.sendMessageDelayed(msg, HANDLER_MSG_DELAY);
        }

        @Override
        public void onFailed(AVException e) {
            loginLogger.d("Get SMS code failed, " + e.getMessage());
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

        //Get current user
        String strOrigUser = LoginManager.getInstance().getCurrentUserName();
        if (!TextUtils.isEmpty(strOrigUser)) {
            edUserName.setText(strOrigUser);
            edPassWord.requestFocus();
        }

        btnLogin = (Button) findViewById(R.id.signin_loginin);
        btnLogin.setOnClickListener(this);
        tvForgotPwd = (TextView) findViewById(R.id.signin_forgot_password);
        tvForgotPwd.setOnClickListener(this);
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
                LoginManager.getInstance().doLogin(strUsername, strPwd, mLoginListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (v.getId() == R.id.signin_forgot_password) {
            //TODO: add forgot password step.
        } else if (v.getId() == R.id.back_rl) {
            this.finish();
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
