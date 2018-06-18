package com.awalk.walkarround;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.awalk.walkarround.main.activity.AppMainActivity;
import com.awalk.walkarround.myself.manager.ProfileManager;
import com.awalk.walkarround.myself.task.OnlineStateTask;
import com.awalk.walkarround.util.AppConstant;

public class EntranceActivity extends Activity {

    private static final String TAG = EntranceActivity.class.getSimpleName();
    private final int REQ_CODE_LOGIN = 0;
    private final int REQ_CODE_MAIN = 1;
    private final int REQ_CODE_SPLASH = 2;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isLogined = ProfileManager.getInstance().getCurAccountLoginState();
        if(isLogined) {
            Intent intent = new Intent(EntranceActivity.this, AppMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intent, REQ_CODE_LOGIN);
        } else {
            startActivityForResult(new Intent(this, SplashActivity.class), REQ_CODE_SPLASH);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        OnlineStateTask.getInstance(getApplicationContext()).startTask();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //退出
        if ((Intent.FLAG_ACTIVITY_CLEAR_TOP & intent.getFlags()) != 0) {
            finishEntranceActivity();
        }

        int targetActivity = intent.getIntExtra(AppConstant.KEY_START_TARGET_ACTIVITY, AppConstant.START_INVALID_VALUE);
        switch (targetActivity) {
            case AppConstant.START_LOGIN_ACTIVITY:
                Intent target = new Intent(getApplicationContext(), EntranceActivity.class);
                startActivity(target);
                break;
            case AppConstant.START_MAIN_ACTIVITY:
                startActivityForResult(new Intent(this, AppMainActivity.class), REQ_CODE_LOGIN);
                break;
            default:
                finish();
                break;
        }

        return;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQ_CODE_LOGIN:
                if (resultCode != AppConstant.ACTIVITY_RETURN_CODE_OK) {
                    //finishEntranceActivity();
                } else {
                    OnlineStateTask.getInstance(getApplicationContext()).startTask();
                }
                break;
            case REQ_CODE_MAIN:
                finishEntranceActivity();
                break;
            case REQ_CODE_SPLASH:

                break;
            default:
                break;
        }
    }

    private void finishEntranceActivity() {
        OnlineStateTask.getInstance(getApplicationContext()).stopTask();

        //Get latest profile data while
        ProfileManager.onDestroy();
        finish();
    }
}
