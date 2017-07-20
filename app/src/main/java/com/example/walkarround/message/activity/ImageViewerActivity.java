package com.example.walkarround.message.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.example.walkarround.R;
import com.example.walkarround.base.view.RoundProgressBar;
import com.example.walkarround.base.view.gifview.GifView;
import com.example.walkarround.base.view.gifview.GifView.GifImageType;
import com.example.walkarround.base.view.photoview.PhotoView;
import com.example.walkarround.base.view.photoview.PhotoViewAttacher;
import com.example.walkarround.message.manager.WalkArroundMsgManager;
import com.example.walkarround.message.model.ChatMessageInfo;
import com.example.walkarround.message.model.ChatMsgBaseInfo;
import com.example.walkarround.message.util.MessageUtil;
import com.example.walkarround.message.util.MsgBroadcastConstants;
import com.example.walkarround.util.Logger;
import com.example.walkarround.util.image.ImageLoaderManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.example.walkarround.message.util.MessageConstant.MessageState;

public class ImageViewerActivity extends Activity implements OnClickListener {
    public static final String MESSAGE_ID = "messageId";
    public static final String MESSAGE_IS_COLLECT_MSG = "isCollectMsg";
    public static final String MESSAGE_ORIGIN_FROM_TYPE = "messageFromType";
    private Logger logger = Logger.getLogger("ImageViewerActivity");
    private PhotoView photoView;
    private RoundProgressBar picProgress;
    private ChatMsgBaseInfo mMessageInfo = new ChatMessageInfo();
    private boolean isCollectMsg = false;
    private int mOriginMsgFromType = -1;

    /**
     * 监听图片下载进度的广播
     */
    private BroadcastReceiver mPictureDownReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            if (MsgBroadcastConstants.ACTION_DOWNLOADING_FILE_CHANGE.equals(action)
                    || MsgBroadcastConstants.ACTION_FILE_TRANSFER_PROGRESS.equals(action)) {
                // 长传/下载进度变化了
                // 开始修改进度
                // 长传/下载进度变化了
                long messageId = intent.getLongExtra(MsgBroadcastConstants.BC_VAR_DOWN_PRG_ID, 0);
                if (messageId != mMessageInfo.getMsgId()) {
                    return;
                }
                long end = intent.getLongExtra(MsgBroadcastConstants.BC_VAR_TRANSFER_PRG_END, 0);
                long total = intent.getLongExtra(MsgBroadcastConstants.BC_VAR_TRANSFER_PRG_TOTAL, 0);
                int picDownPercent = (int) (100 * end / total);
                picProgress.setProgress(picDownPercent);
                // 根据图片下载状态显示原图或者缩略图
                int picDownStatus = ChatMessageInfo.DOWNLOADING;
                if (end >= total) {
                    picDownPercent = 100;
                    picDownStatus = ChatMessageInfo.LOADED;
                    String filePath = mMessageInfo.getFilepath();
                    if (TextUtils.isEmpty(filePath)) {
                        filePath = intent.getStringExtra(MsgBroadcastConstants.BC_VAR_TRANSFER_FILE_PATH);
                    }
                    if (MessageUtil.isGifFile(filePath)) {
                        photoView.setVisibility(View.GONE);
                        GifView gifView = (GifView) findViewById(R.id.msg_pic_gifv);
                        gifView.setVisibility(View.VISIBLE);
                        gifView.setGifImageType(GifImageType.COVER);
                        try {
                            gifView.setGifImage(new FileInputStream(filePath));
                        } catch (FileNotFoundException e) {
                            logger.e("ImageViewerActivity initView, FileNotFoundException: " + e.getMessage());
                        }
                    } else {
                        ImageLoaderManager.displayImage(filePath, photoView);
                    }
                    picProgress.setVisibility(View.GONE);
                }
                mMessageInfo.setDownPercent(picDownPercent);
                mMessageInfo.setDownStatus(picDownStatus);
            } else if (MsgBroadcastConstants.ACTION_DOWNLOADING_FILE_FAIL.equals(action)) {
                mMessageInfo.setIsBurnAfter(false);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!initData(getIntent(), savedInstanceState)) {
            finish();
            return;
        }
        setContentView(R.layout.activity_image_viewer);
        initView();
    }

    @Override
    public void finish() {
        if (mMessageInfo != null && mMessageInfo.isBurnAfterMsg()) {
            Intent intent = new Intent();
            intent.putExtra(MESSAGE_ID, mMessageInfo.getMsgId());
            setResult(RESULT_OK, intent);
        }
        super.finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mPictureDownReceiver);
            mPictureDownReceiver = null;
        } catch (Exception e) {
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(MESSAGE_ID, mMessageInfo.getMsgId());
        outState.putBoolean(MESSAGE_IS_COLLECT_MSG, isCollectMsg);
        outState.putInt(MESSAGE_ORIGIN_FROM_TYPE, mOriginMsgFromType);
    }

    /**
     * 初始化数据
     * 
     * @param intent
     * @param savedInstanceState
     * @return
     */
    private boolean initData(Intent intent, Bundle savedInstanceState) {
        long messageId = 0;
        if (savedInstanceState != null) {
            isCollectMsg = savedInstanceState.getBoolean(MESSAGE_IS_COLLECT_MSG);
            mOriginMsgFromType = savedInstanceState.getInt(MESSAGE_ORIGIN_FROM_TYPE, -1);
            messageId = savedInstanceState.getLong(MESSAGE_ID, 0);
        } else if (intent != null) {
            messageId = intent.getLongExtra(MESSAGE_ID, 0);
            isCollectMsg = intent.getBooleanExtra(MESSAGE_IS_COLLECT_MSG, false);
            mOriginMsgFromType = intent.getIntExtra(MESSAGE_ORIGIN_FROM_TYPE, -1);
        }

        mMessageInfo = WalkArroundMsgManager.getInstance(getApplicationContext()).getMessageById(messageId);

        if (mMessageInfo == null || (TextUtils.isEmpty(mMessageInfo.getFilepath())
                && TextUtils.isEmpty(mMessageInfo.getFileUrlPath()))) {
            return false;
        }
        return true;
    }

    private void initView() {
        findViewById(R.id.root_view_layout).setOnClickListener(this);
        photoView = (PhotoView) findViewById(R.id.msg_iv_pic_viewer);
        photoView.setEnabled(true);
        photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {

            @Override
            public void onViewTap(View view, float x, float y) {
                finish();
            }
        });
        picProgress = (RoundProgressBar) findViewById(R.id.msg_progress_picture);
        picProgress.setProgress(mMessageInfo.getDownPercent());
        mMessageInfo.updateFilePercent();
        if (mMessageInfo.getDownStatus() != ChatMsgBaseInfo.LOADED) {
            // 未下载或正在下载
            if (MessageUtil.isGifFile(mMessageInfo.getThumbpath())) {
                photoView.setVisibility(View.GONE);
                GifView gifView = (GifView) findViewById(R.id.msg_pic_gifv);
                gifView.setVisibility(View.VISIBLE);
                gifView.setGifImageType(GifImageType.COVER);
                InputStream inputStream;
                try {
                    inputStream = new FileInputStream(mMessageInfo.getThumbpath());
                    gifView.setGifImage(inputStream);
                } catch (FileNotFoundException e) {
                    logger.e("ImageViewerActivity initView, Thumb FileNotFoundException: " + e.getMessage());
                }
            } else {
                ImageLoaderManager.displayImage(mMessageInfo.getThumbpath(),
                        mMessageInfo.getThumbUrlPath(), photoView);
            }
            if (mMessageInfo.getMsgState() == MessageState.MSG_STATE_RECEIVING) {
                // 已经开始下载 do nothing
                IntentFilter filter = new IntentFilter();
                filter.addAction(MsgBroadcastConstants.ACTION_DOWNLOADING_FILE_CHANGE);
                filter.addAction(MsgBroadcastConstants.ACTION_DOWNLOADING_FILE_FAIL);
                registerReceiver(mPictureDownReceiver, filter);
            } else if (!TextUtils.isEmpty(mMessageInfo.getFileUrlPath())) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(MsgBroadcastConstants.ACTION_DOWNLOADING_FILE_CHANGE);
                filter.addAction(MsgBroadcastConstants.ACTION_DOWNLOADING_FILE_FAIL);
                registerReceiver(mPictureDownReceiver, filter);
                WalkArroundMsgManager.getInstance(getApplicationContext()).acceptFile(mMessageInfo, isCollectMsg);
            } else {
                Toast.makeText(this, R.string.msg_pic_deleted_notices, Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            if (MessageUtil.isGifFile(mMessageInfo.getFilepath())) {
                photoView.setVisibility(View.GONE);
                GifView gifView = (GifView) findViewById(R.id.msg_pic_gifv);
                gifView.setVisibility(View.VISIBLE);
                gifView.setGifImageType(GifImageType.COVER);
                InputStream inputStream;
                try {
                    inputStream = new FileInputStream(mMessageInfo.getFilepath());
                    gifView.setGifImage(inputStream);
                } catch (FileNotFoundException e) {
                    logger.e("ImageViewerActivity initView, FileNotFoundException: " + e.getMessage());
                }
            } else {
                if (new File(mMessageInfo.getFilepath()).exists()) {
                    ImageLoaderManager.displayImage(mMessageInfo.getFilepath(), photoView);
                } else if (mMessageInfo.getFileUrlPath() != null) {
                    ImageLoaderManager.displayImage(mMessageInfo.getFileUrlPath(), photoView);
                } else {
                    Toast.makeText(this, R.string.msg_pic_deleted_notices, Toast.LENGTH_SHORT).show();
                }
            }
            picProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.root_view_layout:
            finish();
            break;
        default:
            break;
        }
    }

}
