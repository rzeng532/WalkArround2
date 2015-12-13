package com.example.walkarround;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.avos.avoscloud.AVUser;
import com.example.walkarround.login.activity.LoginActivity;
import com.example.walkarround.login.manager.LoginManager;
import com.example.walkarround.myself.activity.MyselfActivity;

public class MyActivity extends Activity {

    private static final String TAG = MyActivity.class.getSimpleName();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isLogined = LoginManager.getInstance().isLogined();
        if (isLogined) {
            startActivity(new Intent(this, MyselfActivity.class));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish();
    }
}
