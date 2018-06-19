/**
 * Copyright (C) 2014-2015 All rights reserved
 */
package com.awalk.walkarround.myself.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVAnalytics;
import com.awalk.walkarround.R;
import com.awalk.walkarround.base.view.DialogFactory;
import com.awalk.walkarround.myself.iview.EditStrProfileInfoView;
import com.awalk.walkarround.myself.manager.ProfileManager;
import com.awalk.walkarround.myself.model.MyProfileInfo;
import com.awalk.walkarround.myself.presenter.EditStrProfileInfoPresenter;
import com.awalk.walkarround.myself.util.ProfileUtil;

/**
 * 编辑个人信息
 * Date: 2015-12-11
 *
 * @author Richard
 */
public class EditStrProfileInfoActivity extends Activity implements View.OnClickListener, EditStrProfileInfoView {

    private TextView mTvTitle;
    private TextView mTvUpdate;
    private EditText mEtInput;
    private MyProfileInfo myProfileInfo;
    private int mEditType = -1;

    private Dialog mLoadingDialog;

    private EditStrProfileInfoPresenter mPresenter = new EditStrProfileInfoPresenter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_str_profile_infor);
        initView();
        initData();
        mPresenter.attach(this);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detach();
    }

    public void initView() {
        //Title
        //Back key
        ((View) findViewById(R.id.title)).findViewById(R.id.back_rl).setOnClickListener(this);
        //Title
        mTvTitle = (TextView) (findViewById(R.id.title).findViewById(R.id.display_name));
        //Right key
        mTvUpdate = (TextView) (findViewById(R.id.title).findViewById(R.id.right_tx));
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
                mPresenter.updateUsername(data);
                break;
            case ProfileUtil.REG_TYPE_SIGNATURE:
                mPresenter.updateSignature(data);
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

    @Override
    public void updateUsernameResult(boolean isSuccess) {
        //Update local profile information.
        if (isSuccess) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissDialog();
                    Toast.makeText(getApplicationContext(), getString(R.string.profile_infor_update_ok), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissDialog();
                    Toast.makeText(getApplicationContext(), getString(R.string.profile_infor_update_fail), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void updateSignatureResult(boolean isSuccess) {
        if (isSuccess) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissDialog();
                    Toast.makeText(getApplicationContext(), getString(R.string.profile_infor_update_ok), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissDialog();
                    Toast.makeText(getApplicationContext(), getString(R.string.profile_infor_update_fail), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
