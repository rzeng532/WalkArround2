/**
 * Copyright (C) 2014-2015 CMCC All rights reserved
 */
package com.example.walkarround.myself.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.example.walkarround.R;
import com.example.walkarround.base.view.PortraitView;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.myself.model.MyProfileInfo;
import com.example.walkarround.myself.util.ProfileUtil;
import com.example.walkarround.util.AppSharedPreference;

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

    public void initData() {
        myProfileInfo = ProfileManager.getInstance().getMyProfile();
        if (myProfileInfo != null) {
            Bundle bundle = getIntent().getExtras();
        }
    }

    @Override
    public void onClick(View v) {

    }

    private void setData(MyProfileInfo profileInfo, int type) {
        switch (type) {
            case ProfileUtil.REG_TYPE_USER_NAME:

            break;
            case ProfileUtil.REG_TYPE_PORTRAIT:

                break;
            case ProfileUtil.REG_TYPE_GENDER:

                break;
            case ProfileUtil.REG_TYPE_MOBILE:

                break;
            case ProfileUtil.REG_TYPE_BIRTH_DAY:

                break;
            case ProfileUtil.REG_TYPE_SIGNATURE:

                break;
            default:

                break;
        }
    }
}
