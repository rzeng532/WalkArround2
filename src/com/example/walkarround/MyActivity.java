package com.example.walkarround;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.walkarround.login.activity.LoginActivity;
import com.example.walkarround.login.activity.NickNameActivity;

public class MyActivity extends Activity {

    private static final String TAG = MyActivity.class.getSimpleName();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
