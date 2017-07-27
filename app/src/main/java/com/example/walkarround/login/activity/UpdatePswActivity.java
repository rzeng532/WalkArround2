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
import com.example.walkarround.setting.manager.SettingManager;
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
public class UpdatePswActivity extends Activity implements View.OnClickListener {

    private EditText edOldPsw = null;
    private EditText edNewPsw = null;
    private EditText edNewPswAgain = null;
    private Button btnUpdatePsw = null;
    //private Button btnRegister = null;
    //private TextView tvForgotPwd = null;
    private Dialog mLoadingDialog;
    private Logger updatePswLogger;

    private final int UPDATE_PSW_OK = 0;
    private final int UPDATE_PSW_FAIL = 1;
    private final int HANDLER_MSG_DELAY = 2000; //1 second

    private Handler mUpdatePswHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_PSW_OK) {
                //Goto target page.
                dismissDialog();
                Toast.makeText(getApplicationContext(), getString(R.string.login_do_update_psw_suc), Toast.LENGTH_SHORT).show();
                SettingManager.getInstance().doLogout();
                Intent intent = new Intent(UpdatePswActivity.this, LoginOrRegActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                UpdatePswActivity.this.finish();
            } else if (msg.what == UPDATE_PSW_FAIL) {
                dismissDialog();
                Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
            }
        }
    };

    AsyncTaskListener mUpdatePswListener = new AsyncTaskListener() {
        @Override
        public void onSuccess(Object data) {
            Message msg = Message.obtain();
            updatePswLogger.d("Do login success.");
            msg.what = UPDATE_PSW_OK;
            mUpdatePswHandler.sendMessageDelayed(msg, HANDLER_MSG_DELAY);
        }

        @Override
        public void onFailed(AVException e) {
            updatePswLogger.d("Get SMS code failed, " + e.getMessage());
            Message msg = Message.obtain();
            msg.what = UPDATE_PSW_FAIL;
            msg.obj = LoginManager.getInstance().getErrStringViaErrorCode(getApplicationContext(), e.getCode());
            mUpdatePswHandler.sendMessageDelayed(msg, HANDLER_MSG_DELAY);
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
        if(edOldPsw != null && edOldPsw.getText().length() > 0
                && edNewPsw != null && edNewPsw.getText().length() > 0
                && edNewPswAgain != null && edNewPswAgain.getText().length() > 0) {
            btnUpdatePsw.setVisibility(View.VISIBLE);
        } else {
            btnUpdatePsw.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_update_password);
        findView();
        setNextButtonVisibleOrNot();
        updatePswLogger = Logger.getLogger(UpdatePswActivity.class.getSimpleName());
    }

    private void findView() {

        //Title
        View title = findViewById(R.id.title);
        title.findViewById(R.id.back_rl).setOnClickListener(this);
        title.findViewById(R.id.more_rl).setVisibility(View.GONE);
        ((TextView)(title.findViewById(R.id.display_name))).setText(R.string.setting_modify_password);

        //Init UI elements
        edOldPsw = (EditText) findViewById(R.id.tv_update_psw_input_old);
        edOldPsw.addTextChangedListener(mContentWatcher);
        edNewPsw = (EditText) findViewById(R.id.tv_update_psw_input_new);
        edNewPsw.addTextChangedListener(mContentWatcher);
        edNewPswAgain = (EditText) findViewById(R.id.tv_update_psw_input_new_again);
        edNewPswAgain.addTextChangedListener(mContentWatcher);

        //Button init step should before setText step. Since set text will trigger button visible or not.
        btnUpdatePsw = (Button) findViewById(R.id.btn_update_psw);
        btnUpdatePsw.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_update_psw) {

            if(!edNewPsw.getText().toString().equalsIgnoreCase(edNewPswAgain.getText().toString())) {
                Toast.makeText(getApplicationContext(), getString(R.string.login_please_confirm_new_psw), Toast.LENGTH_SHORT).show();
                return;
            }

            if(!NetWorkManager.getInstance(getApplicationContext()).isNetworkAvailable()) {
                Toast.makeText(getApplicationContext(), getString(R.string.err_network_unavailable), Toast.LENGTH_SHORT).show();
                return;
            }

            showDialog();
            try {
                String newPsw = edNewPsw.getText().toString();
                String oldPsw = edOldPsw.getText().toString();
                LoginManager.getInstance().updatePassword(oldPsw, newPsw, mUpdatePswListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (v.getId() == R.id.back_rl) {
            this.finish();
            onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
        startActivity(new Intent(UpdatePswActivity.this, AppMainActivity.class));
    }
}
