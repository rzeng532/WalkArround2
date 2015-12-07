/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.example.walkarround.setting.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.example.walkarround.R;
import com.example.walkarround.base.view.DialogFactory;
import com.example.walkarround.util.Logger;

/**
 * TODO: description
 * Date: 2015-12-07
 *
 * @author Administrator
 */
public class AppSettingActivity extends Activity implements View.OnClickListener{

    private static final Logger logger = Logger.getLogger(AppSettingActivity.class.getSimpleName());
    private TextView titleName;
    private TextView tvLastBackupTime;
    private TextView tvContactBackup;
    private TextView tvContactResotre;
    private TextView tvBlackList;
    //private CheckSwitchButton csbNewMsgNotifyReceive;
    private TextView tvResetPasswordApp;
    private TextView tvAboutApp;
    private TextView tvUpdate;
    //    private TextView tvFeedback;
    private TextView tvLogout;
    private String masterPhoneNum;
    private int maxProgress;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                default:
                    break;
            }
        }
    };

    private BroadcastReceiver mContactReceiver = null;
    private boolean isContactRegister = false;

    private void assignViews() {
        titleName = (TextView) findViewById(R.id.title_name);
        //csbNewMsgNotifyReceive = (CheckSwitchButton) findViewById(R.id.csb_new_msg_notify_receive);
        tvResetPasswordApp = (TextView) findViewById(R.id.tv_reset_password);
        tvAboutApp = (TextView) findViewById(R.id.tv_about_app);
        tvLogout = (TextView) findViewById(R.id.tv_logout);
        tvUpdate = (TextView) findViewById(R.id.tv_update);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        assignViews();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_name://返回
                finish();
                break;

            case R.id.tv_reset_password: //修改密码
                doResetPassword();
                break;

            case R.id.tv_about_app://关于APP
                checkAboutApp();
                break;

            case R.id.tv_update:
                checkAppVersion();
                break;

            case R.id.tv_logout://退出登录
                doLogout();
                break;

            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public void doLogout() {
        Dialog noticeDialog = DialogFactory.getNoticeDialog(this,
                R.string.setting_logout_or_not, new DialogFactory.NoticeDialogClickListener() {

                    @Override
                    public void onNoticeDialogConfirmClick(boolean isChecked, Object value) {
                        finish();
                        try {
                            //TODO: logout step

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, null);
        noticeDialog.show();
    }

    public void checkAppVersion() {

    }

    public void doUpgradeApp() {

    }

    public void checkAboutApp() {

    }

    public void doResetPassword() {

    }
}
