package com.example.walkarround;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.example.walkarround.login.activity.LoginOrRegActivity;
import com.example.walkarround.login.manager.LoginManager;
import com.example.walkarround.main.activity.AppMainActivity;
import com.example.walkarround.myself.task.OnlineStateTask;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.network.NetWorkManager;

public class EntranceActivity extends Activity {

    private static final String TAG = EntranceActivity.class.getSimpleName();
    private final int REQ_CODE_LOGIN = 0;
    private final int REQ_CODE_MAIN = 1;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isLogined = LoginManager.getInstance().isLogined();
        if (isLogined) {
            startActivityForResult(new Intent(this, AppMainActivity.class), REQ_CODE_LOGIN);
        } else {
            startActivityForResult(new Intent(this, LoginOrRegActivity.class), REQ_CODE_MAIN);
        }

        //finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        OnlineStateTask.getInstance(getApplicationContext()).startTask();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        //退出
        if ((Intent.FLAG_ACTIVITY_CLEAR_TOP & intent.getFlags()) != 0) {
            finish();
        }

        int targetActivity = intent.getIntExtra(AppConstant.KEY_START_TARGET_ACTIVITY, AppConstant.START_INVALID_VALUE);
        switch (targetActivity) {
            case AppConstant.START_LOGIN_ACTIVITY:
                Intent target = new Intent(getApplicationContext(), EntranceActivity.class);
                startActivity(target);
                break;
            default:
                break;
        }

        return;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQ_CODE_LOGIN:
                if (resultCode != AppConstant.ACTIVITY_RETURN_CODE_OK) {
                    finishEntranceActivity();
                } else {
                    OnlineStateTask.getInstance(getApplicationContext()).startTask();
                }
                break;
            case REQ_CODE_MAIN:
                finishEntranceActivity();
                break;
            default:
                break;
        }
    }

    private void finishEntranceActivity() {
        NetWorkManager.getInstance(getApplicationContext()).onDestroy(getApplicationContext());

        OnlineStateTask.getInstance(getApplicationContext()).stopTask();

        finish();
    }
}
