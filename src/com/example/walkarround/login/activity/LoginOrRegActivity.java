/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.example.walkarround.login.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.example.walkarround.R;
import com.example.walkarround.main.activity.AppMainActivity;
import com.example.walkarround.util.CommonUtils;
import com.example.walkarround.util.Logger;

/**
 * A class for login operations
 * Date: 2015-12-02
 *
 * @author Richard
 */
public class LoginOrRegActivity extends Activity implements View.OnClickListener {

    private Button btnLogin = null;
    private Button btnRegister = null;

    private Logger loginLogger;

    //Request code
    private final int REQUEST_CODE_NEXT_PAGE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_or_register);
        findView();
        loginLogger = Logger.getLogger(LoginOrRegActivity.class.getSimpleName());
    }

    private void findView() {

        btnRegister = (Button) findViewById(R.id.goto_register);
        btnRegister.setOnClickListener(this);

        btnLogin = (Button) findViewById(R.id.goto_login);
        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.goto_login) {
            startActivityForResult(new Intent(LoginOrRegActivity.this, LoginActivity.class), REQUEST_CODE_NEXT_PAGE);
        } else if (v.getId() == R.id.goto_register) {
            startActivityForResult(new Intent(LoginOrRegActivity.this, NickNameActivity.class), REQUEST_CODE_NEXT_PAGE);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_NEXT_PAGE) {
            if (resultCode == CommonUtils.ACTIVITY_FINISH_NORMAL_FINISH) {
                this.finish();
            }
        }
    }

    private void startMainActivity() {
        startActivity(new Intent(LoginOrRegActivity.this, AppMainActivity.class));
    }
}
