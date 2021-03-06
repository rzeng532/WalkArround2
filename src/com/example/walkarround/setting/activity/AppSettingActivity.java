/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.example.walkarround.setting.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import com.example.walkarround.R;
import com.example.walkarround.base.view.DialogFactory;
import com.example.walkarround.login.activity.LoginOrRegActivity;
import com.example.walkarround.login.activity.UpdatePswActivity;
import com.example.walkarround.setting.manager.SettingManager;
import com.example.walkarround.util.Logger;

/**
 * TODO: description
 * Date: 2015-12-07
 *
 * @author Administrator
 */
public class AppSettingActivity extends Activity implements View.OnClickListener{

    private static final Logger logger = Logger.getLogger(AppSettingActivity.class.getSimpleName());
    //private CheckSwitchButton csbNewMsgNotifyReceive;
    private TextView tvResetPasswordApp;
    private TextView tvAboutApp;
    private TextView tvUpdate;
    //    private TextView tvFeedback;
    private TextView tvLogout;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                default:
                    break;
            }
        }
    };

    private void initView() {
        //Title
        View title = findViewById(R.id.title);
        title.findViewById(R.id.back_rl).setOnClickListener(this);
        title.findViewById(R.id.more_rl).setVisibility(View.GONE);
        ((TextView)(title.findViewById(R.id.display_name))).setText(R.string.setting_title);

        tvResetPasswordApp = (TextView) findViewById(R.id.tv_reset_password);
        tvResetPasswordApp.setOnClickListener(this);

        tvAboutApp = (TextView) findViewById(R.id.tv_about_app);
        tvAboutApp.setOnClickListener(this);

        tvLogout = (TextView) findViewById(R.id.tv_logout);
        tvLogout.setOnClickListener(this);

        tvUpdate = (TextView) findViewById(R.id.tv_usr_protocol);
        tvUpdate.setOnClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_rl://返回
                finish();
                break;

            case R.id.tv_reset_password: //修改密码
                doResetPassword();
                break;

            case R.id.tv_about_app://关于APP
                checkAboutApp();
                break;

            case R.id.tv_usr_protocol:
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
                        try {
                            //TODO: logout step
                            SettingManager.getInstance().doLogout();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //finish();

                        Intent intent = new Intent(AppSettingActivity.this, LoginOrRegActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        AppSettingActivity.this.finish();
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
        startActivity(new Intent(this, UpdatePswActivity.class));
    }
}
