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
import com.avos.avoscloud.AVUser;
import com.example.walkarround.R;
import com.example.walkarround.base.view.DialogFactory;
import com.example.walkarround.login.util.LoginConstant;
import com.example.walkarround.login.manager.LoginManager;
import com.example.walkarround.myself.activity.MyselfActivity;
import com.example.walkarround.util.AsyncTaskListener;
import com.example.walkarround.util.CommonUtils;
import com.example.walkarround.util.Logger;

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
    private Button btnRegister = null;
    private TextView tvForgotPwd = null;
    private Dialog mLoadingDialog;
    private Logger loginLogger;

    private final int LOGIN_OK = 0;
    private final int LOGIN_FAIL = 1;
    private final int HANDLER_MSG_DELAY = 1000; //1 second

    private Handler mLoginHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == LOGIN_OK) {
                //Test updating user data.
                updateUserData();
                //Goto target page.
                startMainActivity();
                finish();
            } else if (msg.what == LOGIN_FAIL) {
                Toast.makeText(getApplicationContext(), (String)msg.obj, Toast.LENGTH_SHORT).show();
            }
        }
    };

    AsyncTaskListener mLoginListener = new AsyncTaskListener() {
        @Override
        public void onSuccess() {
            Toast.makeText(getApplicationContext(), getString(R.string.login_do_login_success), Toast.LENGTH_SHORT).show();

            Message msg = Message.obtain();
            loginLogger.d("Do login success.");

            //Test log for verify.
            AVUser currentUser = AVUser.getCurrentUser();
            if(currentUser != null ) {
                loginLogger.d("Do login. User name: " + currentUser.getUsername());
                loginLogger.d("Do login. Mobile number: " + currentUser.getMobilePhoneNumber());
            }

            dismissDialog();
            msg.what = LOGIN_OK;
            mLoginHandler.sendMessageDelayed(msg, HANDLER_MSG_DELAY);
        }

        @Override
        public void onFailed(AVException e) {
            dismissDialog();
            loginLogger.d("Get SMS code failed, " + e.getMessage());
            Message msg = Message.obtain();
            msg.what = LOGIN_FAIL;
            msg.obj = LoginManager.getInstance().getErrStringViaErrorCode(getApplicationContext(), e.getCode());
            mLoginHandler.sendMessageDelayed(msg, HANDLER_MSG_DELAY);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_do_login);
        findView();
        loginLogger = Logger.getLogger(LoginActivity.class.getSimpleName());
    }

    private void findView() {
        setContentView(R.layout.activity_login_do_login);

        //Init UI elements
        edUserName = (EditText) findViewById(R.id.signin_input_username);
        edPassWord = (EditText) findViewById(R.id.signin_input_password);

        //Get current user
        String strOrigUser = LoginManager.getInstance().getCurrentUserName();
        if (!TextUtils.isEmpty(strOrigUser)) {
            edUserName.setText(strOrigUser);
            edPassWord.requestFocus();
        }

        btnLogin = (Button) findViewById(R.id.signin_loginin);
        btnLogin.setOnClickListener(this);
        btnRegister = (Button) findViewById(R.id.signin_register);
        btnRegister.setOnClickListener(this);
        tvForgotPwd = (TextView) findViewById(R.id.signin_forgot_password);
        tvForgotPwd.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.signin_loginin) {
            //Start login process
            if(!isLoginParamsValid()) {
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
        } else if(v.getId() == R.id.signin_register) {
            Intent intent = new Intent(LoginActivity.this, NickNameActivity.class);
            startActivity(intent);
        } else if(v.getId() == R.id.signin_forgot_password) {
            //TODO: add forgot password step.
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
        AVUser user = AVUser.getCurrentUser();

        user.put(LoginConstant.REG_KEY_AGE, 25);
        user.saveInBackground();
    }

    private void startMainActivity() {
        startActivity(new Intent(LoginActivity.this, MyselfActivity.class));
    }
}
