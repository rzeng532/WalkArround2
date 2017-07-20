/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.example.walkarround.myself.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import com.example.walkarround.R;
import com.example.walkarround.base.view.PortraitView;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.myself.model.MyProfileInfo;
import com.example.walkarround.setting.activity.AppSettingActivity;
import com.example.walkarround.util.AppSharedPreference;
import com.example.walkarround.util.CommonUtils;

import java.io.File;

/**
 * TODO: description
 * Date: 2015-12-08
 *
 * @author Richard
 */
public class MyselfActivity extends Activity implements View.OnClickListener {

    private TextView mTvTitle = null;
    private TextView mTvSetting = null;

    private View self_info = null;
    private PortraitView mSelfInfoPortrait;
    private TextView mSelfInfoName;
    private TextView mSelfInfoMobile;
    private MyProfileInfo myProfileInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myself);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
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

        mTvSetting = (TextView) findViewById(R.id.tv_setting);
        mTvSetting.setOnClickListener(this);
    }

    private void initData() {
        myProfileInfo = ProfileManager.getInstance().getMyProfile();

        if(!TextUtils.isEmpty(myProfileInfo.getUsrName()) && !TextUtils.isEmpty(myProfileInfo.getMobileNum())) {
            mSelfInfoPortrait.setBaseData(myProfileInfo.getUsrName(), myProfileInfo.getPortraitPath(),
                    myProfileInfo.getUsrName().substring(0, 1), -1);
            mSelfInfoName.setText(myProfileInfo.getUsrName());
            mSelfInfoMobile.setText(myProfileInfo.getMobileNum());
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
        startActivity(new Intent(MyselfActivity.this, DetailInformationActivity.class));
    }
}
