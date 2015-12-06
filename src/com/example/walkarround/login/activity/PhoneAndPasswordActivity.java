package com.example.walkarround.login.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.example.walkarround.util.CommonUtils;
import com.example.walkarround.util.Logger;

/**
 * Created by Richard on 2015/11/25.
 * <p/>
 * User can input account and password on this activity.
 * And if server return SUCCESS, account will get a SMS check code and can do next step.
 */
public class PhoneAndPasswordActivity extends Activity implements View.OnClickListener {

    private final int CHECKSMSCODE = 1;
    private Logger myLogger = null;
    private String mStrPhoneNum;
    private String pass;
    private EditText mTelView;
    private EditText mPassView;
    private Button btnNext;
    private TextView mTitleView;
    private Dialog mLoadingDialog;
    private final int ERROR = 1;

    public static final int RESULT_OK = 0;
    public static final int RESULT_BACK = 1;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ERROR) {
                String tip = (String) msg.obj;
                String oldTip = getString(R.string.register_user_exist);
                if (tip.equals(oldTip)) {
                    tip = getString(R.string.register_user_exist);
                }
                Toast.makeText(getApplicationContext(), tip, Toast.LENGTH_SHORT).show();
            }
        }
    };

    AsyncTaskListener mAccountManagerListener = new AsyncTaskListener() {
        @Override
        public void onSuccess() {
            //dismissDialog();
            //Start check SMS code activity
            Intent intent = new Intent(PhoneAndPasswordActivity.this, CheckSMSCodeActivity.class);
            startActivityForResult(intent, CHECKSMSCODE);
        }

        @Override
        public void onFailed(AVException e) {
            //dismissDialog();
            Message msg = Message.obtain();
            msg.what = ERROR;
            msg.obj = LoginManager.getInstance().getErrStringViaErrorCode(getApplicationContext(), e.getCode());
            mHandler.sendMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myLogger = Logger.getLogger(PhoneAndPasswordActivity.class.getSimpleName());

        setContentView(R.layout.activity_login_input_account);
        initView();
    }

    private void initView() {
        mTelView = (EditText) findViewById(R.id.account_edit);
        mPassView = (EditText) findViewById(R.id.password_edit);
        btnNext = (Button) findViewById(R.id.btn_nextstep);
        mTitleView = (TextView) findViewById(R.id.title_name);
        mTitleView.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        myLogger.d("Init view complete.");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_nextstep) {
            myLogger.d("Onclick next button.");

            mStrPhoneNum = mTelView.getText().toString();
            pass = mPassView.getText().toString();
            if (mStrPhoneNum == null || mStrPhoneNum.equals("")) {
                Toast.makeText(this, R.string.login_tel_cannot_be_null, Toast.LENGTH_SHORT).show();
                return;
            } else if (!CommonUtils.validatePhoneNum(mStrPhoneNum)) {
                Toast.makeText(this, R.string.login_tel_are_illegal, Toast.LENGTH_SHORT).show();
                return;
            }
            if (pass == null || pass.equals("")) {
                Toast.makeText(this, R.string.login_password_cannot_be_null, Toast.LENGTH_SHORT).show();
                return;
            }
            //TODO: do register step.
            LoginManager.getInstance().setPhoneNum(mStrPhoneNum);
            LoginManager.getInstance().setPassword(pass);
            LoginManager.getInstance().doRegister(mAccountManagerListener);
            //showDialog(getString(R.string.register_do_signup));
        } else if (v.getId() == R.id.title_name) {
            setResult(RESULT_BACK);
            finish();
        }
    }

    private void showDialog(String content) {
        if (mLoadingDialog == null) {
            mLoadingDialog = DialogFactory.getLoadingDialog(this, content, true, null);
            mLoadingDialog.show();
        }
    }

    private void dismissDialog() {
        if (!this.isFinishing() && mLoadingDialog != null) {
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Get result from check SMS code activity.
        if (requestCode == CHECKSMSCODE) {
            myLogger.d("onActivityResult, CHECKSMSCODE.");

            if (resultCode == CheckSMSCodeActivity.GIVEUP) {
                setResult(RESULT_BACK);
                finish();
            } else if (resultCode == CheckSMSCodeActivity.FINISHTHIS) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }
}
