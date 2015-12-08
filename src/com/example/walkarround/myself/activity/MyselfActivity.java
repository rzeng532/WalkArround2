/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.example.walkarround.myself.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import com.example.walkarround.R;
import com.example.walkarround.base.view.PortraitView;
import com.example.walkarround.setting.activity.AppSettingActivity;
import com.example.walkarround.util.AppSharedPreference;

/**
 * TODO: description
 * Date: 2015-12-08
 *
 * @author Richard
 */
public class MyselfActivity extends Activity implements View.OnClickListener {

    private TextView mTvTitle = null;
    private View self_info = null;
    private PortraitView mSelfInfoPortrait;
    private TextView mSelfInfoName;
    private TextView mSelfInfoMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myself);
        initView();
        initData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_name://back
                finish();
                break;
            case R.id.self_info://goto my profile activity
                startMyProfileActivity();
                break;
            case R.id.tv_setting://goto setting
                startSettingActivity();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        mTvTitle = (TextView) findViewById(R.id.title_name);
        mTvTitle.setOnClickListener(this);
        self_info = findViewById(R.id.self_info);
        self_info.setOnClickListener(this);

        mSelfInfoPortrait = (PortraitView) self_info.findViewById(R.id.self_portrait);
        mSelfInfoName = (TextView) self_info.findViewById(R.id.self_info_name);
        mSelfInfoMobile = (TextView) self_info.findViewById(R.id.self_info_mobile);
    }

    private void initData() {
        String userName = AppSharedPreference.getString(AppSharedPreference.ACCOUNT_USERNAME, "");
        String phoneNum = AppSharedPreference.getString(AppSharedPreference.ACCOUNT_PHONE, "");
        String portraitPath = AppSharedPreference.getString(AppSharedPreference.ACCOUNT_PORTRAIT, "");

        if(!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(phoneNum)) {
            mSelfInfoPortrait.setBaseData(userName, portraitPath,
                    userName.substring(0, 1), -1);
            mSelfInfoName.setText(userName);
            mSelfInfoMobile.setText(phoneNum);
        } else {
            mSelfInfoName.setText("");
        }

        /*
        if (masterInfo != null) {
            mSelfInfoPortrait.setBaseData(masterInfo.getFirstName(), masterInfo.getProfilePhoto(),
                    masterInfo.getHeadColorText(), -1);
            mSelfInfoName.setText(masterInfo.getFirstName());
        } else {
            mSelfInfoName.setText("");
        }

        mSelfInfoMobile.setText(NewContactManager.getInstance(getContext()).getMasterPhoneNum());
        */
    }

    private void startSettingActivity() {
        startActivity(new Intent(MyselfActivity.this, AppSettingActivity.class));
    }

    private void startMyProfileActivity() {

    }
}
