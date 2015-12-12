/**
 * Copyright (C) 2014-2015 CMCC All rights reserved
 */
package com.example.walkarround.myself.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.example.walkarround.R;
import com.example.walkarround.base.view.PortraitView;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.myself.model.MyProfileInfo;
import com.example.walkarround.myself.util.ProfileUtil;
import com.example.walkarround.util.AppSharedPreference;
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

    private Handler mUpdateProfileHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_OK) {
                Toast.makeText(getApplicationContext(), getString(R.string.profile_infor_update_ok), Toast.LENGTH_SHORT).show();
                finish();
            } else if (msg.what == UPDATE_FAIL) {
                Toast.makeText(getApplicationContext(), getString(R.string.profile_infor_update_fail), Toast.LENGTH_SHORT).show();
            }
        }
    };


    private AsyncTaskListener updateProfileListener = new AsyncTaskListener() {
        @Override
        public void onSuccess() {
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

    public void initView() {
        mTvTitle = (TextView) findViewById(R.id.back);
        mTvTitle.setOnClickListener(this);

        mTvUpdate = (TextView) findViewById(R.id.send);
        mTvUpdate.setOnClickListener(this);

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
            case R.id.back://back
                //popup to indicate user exit or not if there is data.
                //TODO:
                finish();
                break;
            case R.id.send://send
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
    }

    /*
     * Update data to server side.
     */
    private void updateData(int editType) {
        String data = mEtInput.getText().toString();

        if(TextUtils.isEmpty(data)) {
            //Toast.makeText()
            return;
        }

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
}
