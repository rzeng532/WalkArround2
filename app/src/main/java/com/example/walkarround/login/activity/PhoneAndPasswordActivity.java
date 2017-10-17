package com.example.walkarround.login.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.example.walkarround.R;
import com.example.walkarround.base.view.DialogFactory;
import com.example.walkarround.login.manager.LoginManager;
import com.example.walkarround.util.AppConstant;
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
    private Dialog mLoadingDialog;
    private final int ERROR = 1;

    public static final int RESULT_OK = 0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ERROR) {

                dismissLoadingDialog();

                String tip = (String) msg.obj;
                String oldTip = getString(R.string.register_user_exist);
                if (tip.equals(oldTip)) {
                    tip = getString(R.string.register_user_exist);
                }
                Toast.makeText(getApplicationContext(), tip, Toast.LENGTH_SHORT).show();
            }
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
        if(mTelView != null && mTelView.getText().length() > 0
                && mPassView != null && mPassView.getText().length() > 0) {
            btnNext.setVisibility(View.VISIBLE);
        } else {
            btnNext.setVisibility(View.GONE);
        }
    }

    AsyncTaskListener mAccountManagerListener = new AsyncTaskListener() {
        @Override
        public void onSuccess(Object data) {
            dismissLoadingDialog();
            //Start check SMS code activity
            Intent intent = new Intent(PhoneAndPasswordActivity.this, CheckSMSCodeActivity.class);
            startActivityForResult(intent, CHECKSMSCODE);
        }

        @Override
        public void onFailed(AVException e) {
            //dismissDialog();
            AVAnalytics.onEvent(PhoneAndPasswordActivity.this, AppConstant.ANA_EVENT_GEN_SMS, AppConstant.ANA_TAG_RET_FAIL);
            Message msg = Message.obtain();
            msg.what = ERROR;
            msg.obj = LoginManager.getInstance().getErrStringViaErrorCode(getApplicationContext(), e != null ? e.getCode() : R.string.err_unknow);
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

    private void initView() {

        //Title
        View title = findViewById(R.id.title);
        title.findViewById(R.id.back_rl).setOnClickListener(this);
        title.findViewById(R.id.more_rl).setVisibility(View.GONE);
        ((TextView)(title.findViewById(R.id.display_name))).setText(R.string.register_create_account);

        mTelView = (EditText) findViewById(R.id.account_edit);
        mTelView.addTextChangedListener(mContentWatcher);
        mPassView = (EditText) findViewById(R.id.password_edit);
        mPassView.addTextChangedListener(mContentWatcher);
        btnNext = (Button) findViewById(R.id.btn_nextstep);
        btnNext.setOnClickListener(this);

        //Init next button state.
        setNextButtonVisibleOrNot();
        myLogger.d("Init view complete.");
    }

    @Override
    public void onBackPressed() {
        setResult(CommonUtils.ACTIVITY_FINISH_BACK);
        finish();
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

            AVAnalytics.onEvent(this, AppConstant.ANA_EVENT_GEN_SMS);

            LoginManager.getInstance().setPhoneNum(mStrPhoneNum);
            LoginManager.getInstance().setPassword(pass);
            LoginManager.getInstance().doRegister(mAccountManagerListener);
            mLoadingDialog = DialogFactory.getLoadingDialog(this, false, null);
            mLoadingDialog.show();

            //Following code are test code.
            //Intent intent = new Intent(PhoneAndPasswordActivity.this, CheckSMSCodeActivity.class);
            //startActivityForResult(intent, CHECKSMSCODE);
        } else if (v.getId() == R.id.back_rl) {
            setResult(CommonUtils.ACTIVITY_FINISH_BACK);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Get result from check SMS code activity.
        if (requestCode == CHECKSMSCODE) {
            myLogger.d("onActivityResult, CHECKSMSCODE.");

//            if (resultCode == CheckSMSCodeActivity.GIVEUP) {
//                setResult(RESULT_BACK);
//                finish();
//            } else
            if (resultCode == CheckSMSCodeActivity.FINISHTHIS) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    private void dismissLoadingDialog() {
        if(!PhoneAndPasswordActivity.this.isFinishing()
                && !PhoneAndPasswordActivity.this.isDestroyed()
                && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }
}
