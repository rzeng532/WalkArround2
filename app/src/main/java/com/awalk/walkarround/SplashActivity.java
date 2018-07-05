package com.awalk.walkarround;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.awalk.walkarround.login.activity.LoginOrRegActivity;
import com.awalk.walkarround.login.manager.LoginManager;
import com.awalk.walkarround.main.activity.AppMainActivity;

public class SplashActivity extends Activity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private final int SPLASH_STAY_TIME = 2000; //ms

    private Handler mSplashHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    boolean isLogined = LoginManager.getInstance().isLogined();
                    if (isLogined) {
                        Intent intent = new Intent(SplashActivity.this, AppMainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        startActivity(new Intent(SplashActivity.this, LoginOrRegActivity.class));
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

        setContentView(R.layout.activity_splash);

        mSplashHandler.sendEmptyMessageDelayed(0, SPLASH_STAY_TIME);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSplashHandler.removeCallbacksAndMessages(null);
    }
}
