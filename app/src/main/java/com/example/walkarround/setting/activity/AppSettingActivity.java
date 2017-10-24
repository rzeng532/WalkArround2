/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.example.walkarround.setting.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.avos.avoscloud.AVAnalytics;
import com.example.walkarround.R;
import com.example.walkarround.base.view.DialogFactory;
import com.example.walkarround.login.activity.LoginOrRegActivity;
import com.example.walkarround.login.activity.UpdatePswActivity;
import com.example.walkarround.setting.manager.SettingManager;
import com.example.walkarround.util.Logger;
import com.example.walkarround.util.UniversalWebView;

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
    private TextView tvProtocol;
    //private TextView tvFeedback;
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

//        tvFeedback = (TextView) findViewById(R.id.tv_feedback);
//        tvFeedback.setOnClickListener(this);

        tvLogout = (TextView) findViewById(R.id.tv_logout);
        tvLogout.setOnClickListener(this);

        tvProtocol = (TextView) findViewById(R.id.tv_usr_protocol);
        tvProtocol.setOnClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
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
                checkUserProtocol();
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

    private void doLogout() {
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

    private void checkUserProtocol() {
        Intent intent = new Intent(this, UniversalWebView.class);
        intent.putExtra("URL", "http://mp.weixin.qq.com/s/Ex96oOUTGHRrdR5Z9OVPBQ");
        intent.putExtra("TITLE", getString(R.string.setting_user_protocol));

        startActivity(intent);
    }

    private void checkAboutApp() {
        Intent intent = new Intent(this, UniversalWebView.class);
        intent.putExtra("URL", "http://mp.weixin.qq.com/s/ucauwQqXa2FHSee8lA6U9Q");
        intent.putExtra("TITLE", getString(R.string.setting_about_app));

        startActivity(intent);
    }

    private void doResetPassword() {
        startActivity(new Intent(this, UpdatePswActivity.class));
    }
}
