package com.example.walkarround;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;
import com.example.walkarround.login.activity.LoginOrRegActivity;
import com.example.walkarround.login.manager.LoginManager;
import com.example.walkarround.main.activity.AppMainActivity;

public class SplashActivity extends Activity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    private final int REQ_CODE_LOGIN = 0;
    private final int REQ_CODE_MAIN = 1;

    private final int SPLASH_STAY_TIME = 2000; //ms

    Handler mSplashHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    boolean isLogined = LoginManager.getInstance().isLogined();
                    if (isLogined) {
                        Intent intent = new Intent(SplashActivity.this, AppMainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivityForResult(intent, REQ_CODE_LOGIN);
                    } else {
                        startActivityForResult(new Intent(SplashActivity.this, LoginOrRegActivity.class), REQ_CODE_MAIN);
                    }
                    SplashActivity.this.finish();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_splash);

        mSplashHandler.sendEmptyMessageDelayed(0, 2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
