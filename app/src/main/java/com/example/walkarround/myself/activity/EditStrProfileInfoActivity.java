/**
 * Copyright (C) 2014-2015 CMCC All rights reserved
 */
package com.example.walkarround.myself.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.example.walkarround.R;
import com.example.walkarround.base.view.DialogFactory;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.myself.model.MyProfileInfo;
import com.example.walkarround.myself.util.ProfileUtil;
import com.example.walkarround.util.AsyncTaskListener;

/**
 * TODO: description
 * Date: 2015-12-11
 *
 * @author Richard
 */
public class EditStrProfileInfoActivity extends Activity implements View.OnClickListener {

    private TextView mTvTitle;
    private TextView mTvUpdate;
    private EditText mEtInput;
    private MyProfileInfo myProfileInfo;
    private int mEditType = -1;

    private final int UPDATE_OK = 0;
    private final int UPDATE_FAIL = 0;

    private Dialog mLoadingDialog;

    private Handler mUpdateProfileHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_OK) {
                dismissDialog();
                Toast.makeText(getApplicationContext(), getString(R.string.profile_infor_update_ok), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else if (msg.what == UPDATE_FAIL) {
                dismissDialog();
                Toast.makeText(getApplicationContext(), getString(R.string.profile_infor_update_fail), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private AsyncTaskListener updateProfileListener = new AsyncTaskListener() {
        @Override
        public void onSuccess(Object data) {

            //Update local profile information.
            if(mEditType == ProfileUtil.REG_TYPE_USER_NAME) {
                ProfileManager.getInstance().getMyProfile().setUsrName(mEtInput.getText().toString());
            } else if(mEditType == ProfileUtil.REG_TYPE_SIGNATURE) {
                ProfileManager.getInstance().getMyProfile().setSignature(mEtInput.getText().toString());
            }

            Message msg = Message.obtain();
            msg.what = UPDATE_OK;
            mUpdateProfileHandler.sendMessageDelayed(msg, 0);
        }

        @Override
        public void onFailed(AVException e) {
            Message msg = Message.obtain();
            msg.what = UPDATE_FAIL;
            mUpdateProfileHandler.sendMessageDelayed(msg, 0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_str_profile_infor);
        initView();
        initData();
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

    public void initView() {
        //Title
        //Back key
        ((View) findViewById(R.id.title)).findViewById(R.id.back_rl).setOnClickListener(this);
        //Title
        mTvTitle = (TextView)(findViewById(R.id.title).findViewById(R.id.display_name));
        //Right key
        mTvUpdate = (TextView)(findViewById(R.id.title).findViewById(R.id.right_tx));
        mTvUpdate.setText(R.string.profile_infor_update);
        mTvUpdate.setOnClickListener(this);
        (findViewById(R.id.title).findViewById(R.id.more_iv)).setVisibility(View.GONE);

        mEtInput = (EditText) findViewById(R.id.input);
    }

    /*
     * Init profile information via current user and set tile / edit text.
     */
    public void initData() {
        myProfileInfo = ProfileManager.getInstance().getMyProfile();
        if (myProfileInfo != null) {
            Intent intent = getIntent();
            if (intent != null) {
                mEditType = intent.getIntExtra(ProfileUtil.EDIT_ACTIVITY_START_TYPE, -1); //-1 means unvalid value
            }

            setData(myProfileInfo, mEditType);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_rl://back
                //popup to indicate user exit or not if there is data.
                //TODO:
                finish();
                break;
            case R.id.right_tx://send
                updateData(mEditType);
                break;
            default:
                break;
        }
    }

    /*
     * Set local title & data via input parameter - edit type.
     */
    private void setData(MyProfileInfo profileInfo, int type) {
        switch (type) {
            case ProfileUtil.REG_TYPE_USER_NAME:
                mTvTitle.setText(getString(R.string.profile_infor_nick_name));
                if (!TextUtils.isEmpty(profileInfo.getUsrName())) {
                    mEtInput.setText(profileInfo.getUsrName());
                }
                break;

            case ProfileUtil.REG_TYPE_PORTRAIT:
                break;
            case ProfileUtil.REG_TYPE_GENDER:

                break;
            case ProfileUtil.REG_TYPE_MOBILE:
                //It looks like we can't change mobile looks like that
                break;
            case ProfileUtil.REG_TYPE_BIRTH_DAY:

                break;
            case ProfileUtil.REG_TYPE_SIGNATURE:
                mTvTitle.setText(getString(R.string.profile_infor_signature));
                if (!TextUtils.isEmpty(profileInfo.getSignature())) {
                    mEtInput.setText(profileInfo.getSignature());
                }
                break;
            default:

                break;
        }

        mEtInput.setSelection(mEtInput.getText().length());
    }

    /*
     * Update data to server side.
     */
    private void updateData(int editType) {
        String data = mEtInput.getText().toString();

        if (TextUtils.isEmpty(data)) {
            //Toast.makeText()
            return;
        }
        showDialog();
        switch (editType) {
            case ProfileUtil.REG_TYPE_USER_NAME:
                ProfileManager.getInstance().updateUsername(data, updateProfileListener);
                break;

            case ProfileUtil.REG_TYPE_SIGNATURE:
                ProfileManager.getInstance().updateSignature(data, updateProfileListener);
                break;

            default:

                break;
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
}
