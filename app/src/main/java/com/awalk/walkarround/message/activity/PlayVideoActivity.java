package com.awalk.walkarround.message.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import com.awalk.walkarround.R;
import com.awalk.walkarround.base.view.RoundProgressBar;
import com.awalk.walkarround.message.manager.WalkArroundMsgManager;
import com.awalk.walkarround.message.model.ChatMsgBaseInfo;
import com.awalk.walkarround.message.util.MessageConstant.MessageState;
import com.awalk.walkarround.message.util.MsgBroadcastConstants;
import com.awalk.walkarround.util.image.ImageLoaderManager;

/**
 * 播放视频
 */
public class PlayVideoActivity extends Activity {
    public static final String INTENT_THUMB_PATH = "thumb";
    public static final String INTENT_THUMB_URL_PATH = "thumbUrl";
    public static final String INTENT_MESSAGE_ID = "messageId";
    public static final String INTENT_BURN_FLAG = "isBurnAfter";
    public static final String INTENT_IS_COLLECT_MSG = "isCollectMsg";

    private ImageView mVideoThumbView;
    private RoundProgressBar mProgressBar;
    private VideoPlayLayout mMyPlayVideoLayout;
    private String mThumbPath;
    private String mThumbUrlPath;
    private long mMessageId = 0;
    private boolean isBurnAfter = false;
    private boolean isCollectMsg = false;

    /* 图片、视频等上传/下载监听 */
    private BroadcastReceiver mMessageDownReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            long messageId = intent.getLongExtra(MsgBroadcastConstants.BC_VAR_DOWN_PRG_ID, 0);
            if (messageId != mMessageId) {
                return;
            }
            boolean collectMsg = intent.getBooleanExtra(MsgBroadcastConstants.BC_VAR_IS_COLLECT_MSG, false);
            if (collectMsg != isCollectMsg) {
                return;
            }
            String action = intent.getAction();
            if (MsgBroadcastConstants.ACTION_DOWNLOADING_FILE_CHANGE.equals(action)
                    || MsgBroadcastConstants.ACTION_FILE_TRANSFER_PROGRESS.equals(action)) {
                // 长传/下载进度变化了
                long end = intent.getLongExtra(MsgBroadcastConstants.BC_VAR_TRANSFER_PRG_END, 0);
                long total = intent.getLongExtra(MsgBroadcastConstants.BC_VAR_TRANSFER_PRG_TOTAL, 0);
                if (end >= total) {
                    mVideoThumbView.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.GONE);
                    mMyPlayVideoLayout.setVisibility(View.VISIBLE);
                    String path = intent.getStringExtra(MsgBroadcastConstants.BC_VAR_MSG_CONTENT);
                    mMyPlayVideoLayout.setVideoPathAndThumb(path, mThumbPath, mThumbUrlPath);
                } else {
                    int percent = (int) (100 * end / total);
                    mProgressBar.setProgress(percent);
                }
            } else if (MsgBroadcastConstants.ACTION_DOWNLOADING_FILE_FAIL.equals(action)) {
                isBurnAfter = false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChatMsgBaseInfo message = initIntentData(getIntent());
        if (message == null) {
            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_playvideo);

        mVideoThumbView = (ImageView) findViewById(R.id.video_thumb_iv);
        mProgressBar = (RoundProgressBar) findViewById(R.id.progress_rpb);
        mMyPlayVideoLayout = (VideoPlayLayout) findViewById(R.id.myrelativelayout);
        message.updateFilePercent();
        int videoDownStatus = message.getDownStatus();
        if (videoDownStatus == ChatMsgBaseInfo.LOADED) {
            mMyPlayVideoLayout.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            // 可以播放
            mMyPlayVideoLayout.setVideoPathAndThumb(message.getFilepath(), mThumbPath, mThumbUrlPath);
        } else if (videoDownStatus == ChatMsgBaseInfo.WAIT_DOWNLOAD) {
            // 下载
            ImageLoaderManager.displayImage(mThumbPath, mThumbUrlPath, R.drawable.default_image, mVideoThumbView);
            if (message.getMsgState() == MessageState.MSG_STATE_RECEIVING) {
                // 已经开始下载 do nothing
            } else if (!TextUtils.isEmpty(message.getFileUrlPath())) {
                WalkArroundMsgManager.getInstance(getApplicationContext()).acceptFile(message, isCollectMsg);
            } else {
                Toast.makeText(this, R.string.msg_pic_deleted_notices, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            // 监听下载文件
            IntentFilter filter = new IntentFilter();
            filter.addAction(MsgBroadcastConstants.ACTION_DOWNLOADING_FILE_CHANGE);
            filter.addAction(MsgBroadcastConstants.ACTION_FILE_TRANSFER_PROGRESS);
            filter.addAction(MsgBroadcastConstants.ACTION_DOWNLOADING_FILE_FAIL);
            registerReceiver(mMessageDownReceiver, filter);
            mMyPlayVideoLayout.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void finish() {
        if (isBurnAfter) {
            Intent intent = new Intent();
            intent.putExtra(INTENT_MESSAGE_ID, mMessageId);
            setResult(RESULT_OK, intent);
        }
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMyPlayVideoLayout != null) {
            mMyPlayVideoLayout.releaseVideo();
        }
        try {
            unregisterReceiver(mMessageDownReceiver);
        } catch (Exception e) {
        }
    }

    private ChatMsgBaseInfo initIntentData(Intent intent) {
        mThumbPath = intent.getStringExtra(INTENT_THUMB_PATH);
        mThumbUrlPath = intent.getStringExtra(INTENT_THUMB_URL_PATH);
        mMessageId = intent.getLongExtra(INTENT_MESSAGE_ID, 0);
        isBurnAfter = intent.getBooleanExtra(INTENT_BURN_FLAG, false);
        isCollectMsg = intent.getBooleanExtra(INTENT_IS_COLLECT_MSG, false);
        ChatMsgBaseInfo message;

        message = WalkArroundMsgManager.getInstance(getApplicationContext()).getMessageById(mMessageId);

        return message;
    }
}
