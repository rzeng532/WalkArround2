package com.example.walkarround.message.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.*;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.example.walkarround.Location.activity.LocationActivity;
import com.example.walkarround.R;
import com.example.walkarround.base.WalkArroundApp;
import com.example.walkarround.base.view.DialogFactory;
import com.example.walkarround.base.view.EmojiPanelView;
import com.example.walkarround.base.view.EmojiPanelView.EmojiListener;
import com.example.walkarround.base.view.PhotoView;
import com.example.walkarround.handmark.PullToRefreshBase;
import com.example.walkarround.handmark.PullToRefreshBase.OnRefreshListener2;
import com.example.walkarround.handmark.PullToRefreshListView;
import com.example.walkarround.main.model.ContactInfo;
import com.example.walkarround.main.task.TaskUtil;
import com.example.walkarround.main.task.UpdateSpeedDateColorTask;
import com.example.walkarround.message.adapter.MessageDetailListAdapter;
import com.example.walkarround.message.adapter.PopupListAdapter;
import com.example.walkarround.message.adapter.PopupListAdapter.PopupListItemListener;
import com.example.walkarround.message.listener.MessageItemListener;
import com.example.walkarround.message.listener.PressTalkTouchListener;
import com.example.walkarround.message.listener.PressTalkTouchListener.VoiceManager;
import com.example.walkarround.message.manager.ContactsManager;
import com.example.walkarround.message.manager.WalkArroundMsgManager;
import com.example.walkarround.message.model.ChatMsgAndSMSReturn;
import com.example.walkarround.message.model.ChatMsgBaseInfo;
import com.example.walkarround.message.model.MessageRecipientInfo;
import com.example.walkarround.message.receiver.AlarmReceiver;
import com.example.walkarround.message.task.LoadMessageTask;
import com.example.walkarround.message.task.LoadMessageTask.MessageLoadListener;
import com.example.walkarround.message.task.LoadSearchResultMessageTask;
import com.example.walkarround.message.util.EmojiParser;
import com.example.walkarround.message.util.MessageConstant;
import com.example.walkarround.message.util.MessageConstant.ChatType;
import com.example.walkarround.message.util.MessageConstant.MessageSendReceive;
import com.example.walkarround.message.util.MessageConstant.MessageState;
import com.example.walkarround.message.util.MessageConstant.MessageType;
import com.example.walkarround.message.util.MessageUtil;
import com.example.walkarround.message.util.MsgBroadcastConstants;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.CommonUtils;
import com.example.walkarround.util.Logger;
import com.example.walkarround.util.http.HttpTaskBase;
import com.example.walkarround.util.http.HttpUtil;
import com.example.walkarround.util.http.ThreadPoolManager;
import com.example.walkarround.util.image.ImageBrowserActivity;
import com.example.walkarround.util.image.ImageChooseActivity;
import com.example.walkarround.util.network.NetWorkManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import static com.example.walkarround.base.view.EmojiPanelView.EMOJI_ITEM_TYPE_DEL_BTN;
import static com.example.walkarround.message.activity.ChatAssistToolsView.ToolsViewOnClick;

public class BuildMessageActivity extends Activity implements OnClickListener, ToolsViewOnClick,
        MessageItemListener, MessageLoadListener, VoiceManager, PopupListItemListener, EmojiListener,
        OnRefreshListener2<ListView>, SensorEventListener, LoadSearchResultMessageTask.SearchMessageLoadListener,
        DialogFactory.ConfirmDialogClickListener {

    /* 收信人是否可编辑 */
    public static final String INTENT_RECEIVER_EDITABLE = "receiverEditable";
    /* 消息类型：一对一、一对多、群聊 */
    public static final String INTENT_CONVERSATION_TYPE = "conversationType";
    public static final String INTENT_CONVERSATION_RECEIVER = "conversationReceiver";
    public static final String INTENT_CONVERSATION_DISPLAY_NAME = "conversationDisplayName";
    public static final String INTENT_CONVERSATION_THREAD_ID = "conversationThreadId";
    public static final String INTENT_CONVERSATION_GROUP_ID = "conversationGroupId";
    public static final String INTENT_CONVERSATION_ID = "ConversationId";
    public static final String INTENT_FAST_CONTACTS = "fastContacts";
    public static final String INTENT_LOCATION_MESSAGE_ID = "messageId";
    public static final String INTENT_LOCATION_MESSAGE_FROM_TYPE = "messageFromType";

    public static final int CONVERSATION_DEFAULT_THREAD_ID = -2;

    private static final Logger logger = Logger.getLogger(BuildMessageActivity.class.getSimpleName());

    public static final int MAX_INPUT_LENGTH = 450;

    /* 添加收信人 */
    private static final int REQUEST_CODE_SELECT_CONTACTS_WITH_NUM = 1;
    /* 发送联系人名片 */
    private static final int REQUEST_CODE_SELECT_FOR_SEND_VCARD = 2;
    /* 发送视频 */
    private static final int REQUEST_CODE_TAKE_VIDEO = 3;
    /* 发送位置信息 */
    private static final int REQUEST_CODE_MAP = 4;
    /* 发送图片 */
    private static final int REQUEST_CODE_PICTURE_CHOOSE = 5;
    /* 转发 */
    private static final int REQUEST_CODE_FORWARD_MSG = 6;
    /* 下载/浏览图片 */
    private static final int REQUEST_CODE_PREVIEW_IMAGE = 7;
    /* 聊天详情 */
    public static final int REQUEST_CODE_CHAT_DETAIL = 8;
    /* 打开相机拍摄图片 */
    private static final int REQUEST_CODE_CAMERA = 9;
    /* 查看视频 */
    private static final int REQUEST_CODE_PREVIEW_VIDEO = 10;
    /* 查看发送的地图 */
    private static final int REQUEST_CODE_SHOW_LOCATION = 11;

    /* 当前编辑状态：默认、已经有输入、语音、更多、表情 */
    private static final int MESSAGE_EDIT_STATE_DEFAULT = 0;
    private static final int MESSAGE_EDIT_STATE_HAS_INPUT = 1;
    private static final int MESSAGE_EDIT_STATE_VOICE = 2;
    private static final int MESSAGE_EDIT_STATE_MORE_OPERATE = 3;
    private static final int MESSAGE_EDIT_STATE_MORE_EMOJI = 4;
    private static final int MESSAGE_EDIT_STATE_BURN_AFTER = 5;
    private static final int MESSAGE_EDIT_STATE_BURN_HAS_INPUT = 6;
    private static final int MESSAGE_EDIT_STATE_BURN_VOICE = 7;

    /* 通话类型：普通通话、语音通话、视频通话 */
    private static final int CALL_TYPE_NORMAL = 0;
    private static final int CALL_TYPE_VOICE = 1;
    private static final int CALL_TYPE_VIDEO = 2;
    /* 更多操作：查看联系人、查看友圈、加入黑名单 */
    private static final int MORE_TYPE_LOOK_CONTACT = 0;
    private static final int MORE_TYPE_LOOK_DYNAMIC = 1;
    private static final int MORE_TYPE_TO_BLACK = 2;

    /* 消息来源：RCS(IM)消息、SMS消息、定时消息 */
    public static final int MSG_FROM_TYPE_RCS = 1;
    public static final int MSG_FROM_TYPE_SMS = 2;

    /* 发送方发送阅后即焚后消息消息间隔 */
    public static final int MSG_BURN_AFTER_READ_DURATION = 5000;

    /* 收信人信息 */
    private MessageRecipientInfo mRecipientInfo = new MessageRecipientInfo();

    /* 通知会话列表刷新*/
    public static final String ACTION_NOTIFY_CONVERSATION_REFRESH = "notify_conversation_refresh";
    public static final String CONVERSATION_REFRESH_ID = "conversation_refresh_id";

    /* 收信人是否可编辑 */
    protected boolean isReceiverEditable = true;

    protected boolean isActivityOnForground = false;

    /* 编辑联系人 */
    private EditText mReceiverEditView;
    /* 进入双方距离界面按钮 */
    ImageView mImvDistance;

    /* 底部消息编辑区域 */
    private View mMessageBottomView;
    /* 消息编辑框 */
    protected EditText mSendMessageEditView;
    /* 定时发送或者阅后即焚标记 */
    private TextView mTimeSendView;
    /* 左侧按钮 */
    private ImageView mBottomLeftView;
    /* 发送和语音区域 */
    private View mBottomRightView;

    /* 更多操作：发送联系人、位置、图片等 */
    private ChatAssistToolsView mMoreToolsPanel;
    /* 表情面板 */
    private EmojiPanelView mEmojiPanel;
    /* 语音面板 */
    private View mVoicePanel;
    private PressTalkTouchListener mVoiceListener;
    /* 时间选择面板 */
    private View mTimeSelectPanel;
    /* 批量操作面板：删除、收藏等 */
    private View mSelectModelPanel;

    /* 当前编辑状态 */
    private int mCurrentMessageEditState;

    /* 消息展示列表 */
    private PullToRefreshListView mMessageListView;
    private MessageDetailListAdapter mMessageDetailAdapter;

    /* 整个页面遮盖View(透明)，防止连续点击 */
    private View mMaskView;

    /* 拨打电话/更多PopupWindow */
    private PopupWindow mPopupWindow;
    /* PopupWindow的展示内容 */
    private PopupListAdapter mPopupWindowAdapter;
    /* 音频播放 */
    private MediaPlayer mAudioMediaPlayer;
    /* 传感器监听，控制语音播放模式 */
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private AudioManager mAudioManager;
    /* 当前正在播放的音频消息 */
    private ChatMsgBaseInfo mCurrentPlayAudioMsg;

    /* 回复走走请求对话框 */
    private Dialog mWalkReplyDialog;

    /*消息状态更新*/
    private HashMap<Long, Integer> mMessageStatus = new HashMap<Long, Integer>();

    /* 一对一会话时接收者Number */
    public static String sCurrentReceiverNum = null;
    private String mOnPauseSavedReceiverNum = null;
    /* 网络是否可用 */
    private boolean isNetworkAvailable = true;

    /* 是否黑名单聊天人 */
    private boolean isBlackContact = false;

    /* 利用相机刚拍图片 */
    private String mPhotoImagePath;

    private PopupWindow mBurnMsgTypeChoosePopup;

    private Dialog mStart2walkDialog = null;

    /*搜索结果定位*/
    private boolean isEnablePullDownLoad = false;
    private long mSearchMsgId = -1;
    private int mSearchMsgFromType = MSG_FROM_TYPE_RCS;

    private static final int UI_WHAT_SCROLL = 1;
    private Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UI_WHAT_SCROLL:
                    if (mMessageListView != null) {
                        mMessageListView.getRefreshableView().smoothScrollToPositionFromTop(msg.arg1, 0);
                        if (msg.arg2 > 0) {
                            Message newMsg = Message.obtain();
                            newMsg.what = UI_WHAT_SCROLL;
                            newMsg.arg1 = msg.arg1;
                            newMsg.arg2 = 0;
                            mUiHandler.sendMessageDelayed(newMsg, 100);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /* 消息状态监听 */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (isEnablePullDownLoad && !action.equals(AlarmReceiver.ACTION_TIME_MSG_SEND)) {
                return;
            }
            long threadId = intent.getLongExtra(MsgBroadcastConstants.BC_VAR_THREAD_ID, CONVERSATION_DEFAULT_THREAD_ID);
            if (threadId != mRecipientInfo.getThreadId()) {
                return;
            }

            long messageId = intent.getLongExtra(MsgBroadcastConstants.BC_VAR_MSG_ID, 0);
            if (action.equals(MsgBroadcastConstants.ACTION_MESSAGE_STATUS_CHANGED)) {
                // 消息状态变化
                int status = intent.getIntExtra(MsgBroadcastConstants.BC_VAR_MSG_STATUS, 0);
                messageStateChanged(messageId, status);
            } else if (action.equals(MsgBroadcastConstants.ACTION_MESSAGE_NEW_RECEIVED)) {
                // 新到一对一消息
                if (!isBlackContact) {
                    int count = intent.getIntExtra(MsgBroadcastConstants.BC_VAR_MSG_COUNT, 0);
                    long[] idList = intent.getLongArrayExtra(MsgBroadcastConstants.BC_VAR_MSG_ID_LIST);
                    onReceiveMsg(messageId, count, idList);
                }
            } else if (action.equals(AlarmReceiver.ACTION_TIME_MSG_SEND)) {
                // 定时短信
//                if (!isEnablePullDownLoad) {
//                    updateLastSendMessageToList(messageId, MSG_FROM_TYPE_RCS, false);
//                }
//                long deleteMsgId = intent.getLongExtra(AlarmReceiver.INTENT_TIME_MSG_ID, -1);
//                mMessageDetailAdapter.deleteTimeMessageById(deleteMsgId);
//                mMessageDetailAdapter.notifyDataSetChanged();
            } else if (action.equals(MsgBroadcastConstants.ACTION_CONTACT_COMPOSING_INFO)) {
                // 某个人正在输入（一对一消息）
            }
        }
    };

    private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isAvailable = NetWorkManager.getInstance(context).isNetworkAvailable();
            if (isAvailable != isNetworkAvailable) {
                isNetworkAvailable = isAvailable;
                setToolsViewEnable();
                if (isAvailable) {
                    findViewById(R.id.network_status_notice_tv).setVisibility(View.GONE);
                } else if (mReceiverEditView == null || !mReceiverEditView.hasFocus()) {
                    // 网络不可用
                    findViewById(R.id.network_status_notice_tv).setVisibility(View.VISIBLE);
                }
            }
        }
    };

    private HttpTaskBase.onResultListener mUpdateSpeedDateColorListener = new HttpTaskBase.onResultListener() {
        @Override
        public void onPreTask(String requestCode) {

        }

        @Override
        public void onResult(Object object, HttpTaskBase.TaskResult resultCode, String requestCode, String threadId) {
            if (HttpTaskBase.TaskResult.SUCCEESS == resultCode) {
                logger.d("mUpdateSpeedDateColorListener success");
            } else if (HttpTaskBase.TaskResult.FAILED == resultCode) {
                logger.d("mUpdateSpeedDateColorListener failed");
            }
        }

        @Override
        public void onProgress(int progress, String requestCode) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_message);
        // 初始化数据
        if (!initData(savedInstanceState, getIntent())) {
            // 数据不合法
            finish();
            return;
        }

        // 初始化头部
        if (isReceiverEditable) {
//            PullToRefreshListView msgListView = (PullToRefreshListView) findViewById(R.id.message_list_xlv);
//            msgListView.setMode(PullToRefreshBase.Mode.DISABLED);
//            initHeadReceiverView();
//            getAllContacts();
        } else {
            // 监听新消息及消息状态变化
            IntentFilter commandFilter = new IntentFilter();
            commandFilter.addAction(MsgBroadcastConstants.ACTION_MESSAGE_STATUS_CHANGED);
            commandFilter.addAction(MsgBroadcastConstants.ACTION_MESSAGE_NEW_RECEIVED);
            commandFilter.addAction(AlarmReceiver.ACTION_TIME_MSG_SEND);
            // 群聊通知消息
            commandFilter.addAction(MsgBroadcastConstants.ACTION_GROUP_MESSAGE_NEW_RECEIVED);
            commandFilter.addAction(MsgBroadcastConstants.ACTION_GROUP_INFO_CHANGED);
            commandFilter.addAction(MsgBroadcastConstants.ACTION_CONTACT_COMPOSING_INFO);
            registerReceiver(mMessageReceiver, commandFilter);

            // 初始化头部
            initMessageDetailHeader();
            // 加载消息
            loadMessageList();
            initSensor();
        }
        // 初始化消息编辑区域
        initMessageEditArea();

        mMaskView = findViewById(R.id.build_message_mask_v);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!isSameConversation(intent)) {
            // 非同一个会话, 设置会话对应的未读消息数为0
            setThreadToRead();
        }
        saveDraftMessage();
        // 初始化数据
        if (!initData(null, intent)) {
            // 数据不合法
            finish();
            return;
        }
        try {
            unregisterReceiver(mMessageReceiver);
        } catch (Exception e) {
        }
        mMessageStatus.clear();

        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }

        if (mMessageDetailAdapter != null) {
            mMessageDetailAdapter.dismissDialog();
            mMessageListView.setMode(PullToRefreshBase.Mode.DISABLED);
            mMessageDetailAdapter.setMessageInfo(null);
            onSelectModeChange(false);
            mMessageDetailAdapter.notifyDataSetChanged();
            mMessageDetailAdapter = null;
        }
        // 初始化头部
        if (isReceiverEditable) {
//            initHeadReceiverView();
//            getAllContacts();
//            releaseSensor();
//            mSendMessageEditView.setText("");
        } else {
            IntentFilter commandFilter = new IntentFilter();
            commandFilter.addAction(MsgBroadcastConstants.ACTION_MESSAGE_STATUS_CHANGED);
            commandFilter.addAction(MsgBroadcastConstants.ACTION_MESSAGE_NEW_RECEIVED);
            commandFilter.addAction(AlarmReceiver.ACTION_TIME_MSG_SEND);
            // 群聊通知消息
            commandFilter.addAction(MsgBroadcastConstants.ACTION_GROUP_MESSAGE_NEW_RECEIVED);
            commandFilter.addAction(MsgBroadcastConstants.ACTION_GROUP_INFO_CHANGED);
            commandFilter.addAction(MsgBroadcastConstants.ACTION_CONTACT_COMPOSING_INFO);
            getBaseContext().registerReceiver(mMessageReceiver, commandFilter);
            initMessageDetailHeader();
            // 加载消息
            if (mMessageListView != null) {
                PullToRefreshBase.Mode mode = isEnablePullDownLoad ? PullToRefreshBase.Mode.BOTH
                        : PullToRefreshBase.Mode.PULL_FROM_START;
                mMessageListView.setMode(mode);
            }
            loadMessageList();
            initSensor();
            setToolsViewEnable();
            // 显示草稿消息
            ChatMsgBaseInfo draftMessage = WalkArroundMsgManager.getInstance(this).getDraftMessage(mRecipientInfo.getThreadId());
            if (draftMessage != null && !TextUtils.isEmpty(draftMessage.getData())) {
                mSendMessageEditView.setText(EmojiParser.getInstance(this).addSmileySpans(draftMessage.getData()));
                mSendMessageEditView.setSelection(draftMessage.getData().length());
            } else {
                mSendMessageEditView.setText("");
            }
        }
    }

    /**
     * 是否同一个会话
     *
     * @return
     */
    private boolean isSameConversation(Intent intent) {
        boolean isReceiverEditable = intent.getBooleanExtra(INTENT_RECEIVER_EDITABLE, true);
        if (isReceiverEditable) {
            return false;
        }
        long threadId = intent.getLongExtra(INTENT_CONVERSATION_THREAD_ID, CONVERSATION_DEFAULT_THREAD_ID);
        if (threadId == mRecipientInfo.getThreadId()) {
            return true;
        }
        if (threadId == CONVERSATION_DEFAULT_THREAD_ID) {
            int chatType = intent.getIntExtra(INTENT_CONVERSATION_TYPE, ChatType.CHAT_TYPE_ONE2ONE);
            List<String> recipient = null;
            if (chatType == ChatType.CHAT_TYPE_ONE2ONE) {
                String receiver = intent.getStringExtra(INTENT_CONVERSATION_RECEIVER);
                recipient = new ArrayList<String>();
                recipient.add(receiver);
            } else {
                recipient = (List<String>) intent.getSerializableExtra(INTENT_CONVERSATION_RECEIVER);
            }
            threadId = WalkArroundMsgManager.getInstance(this).getConversationId(ChatType.CHAT_TYPE_ONE2ONE,
                    recipient);
            return threadId == mRecipientInfo.getThreadId();
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(INTENT_RECEIVER_EDITABLE, isReceiverEditable);
        outState.putInt(INTENT_CONVERSATION_TYPE, mRecipientInfo.getConversationType());
        outState.putLong(INTENT_CONVERSATION_THREAD_ID, mRecipientInfo.getThreadId());
        outState.putSerializable(INTENT_CONVERSATION_RECEIVER, (Serializable) mRecipientInfo.getRecipientList());
        if (mRecipientInfo.getConversationType() == ChatType.CHAT_TYPE_ONE2ONE) {
        } else {
            outState.putString(INTENT_CONVERSATION_GROUP_ID, mRecipientInfo.getGroupId());
            outState.putString(INTENT_CONVERSATION_ID, mRecipientInfo.getConversationId());
        }
        outState.putString(INTENT_CONVERSATION_DISPLAY_NAME, mRecipientInfo.getDisplayName());
    }

    /**
     * 通知会话列表页刷新会话消息
     */
    private void notifyConversationRefresh(long threadId) {
        if (threadId > 0) {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(ACTION_NOTIFY_CONVERSATION_REFRESH);
            broadcastIntent.putExtra(CONVERSATION_REFRESH_ID, threadId);
            this.sendBroadcast(broadcastIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sCurrentReceiverNum = null;
        try {
            unregisterReceiver(mMessageReceiver);
            //unRegisterContactReceiver();
        } catch (Exception e) {
        }
        if (mAudioMediaPlayer != null) {
            try {
                if (mAudioMediaPlayer.isPlaying()) {
                    mAudioMediaPlayer.stop();
                }
                mAudioMediaPlayer.release();
                mAudioMediaPlayer = null;
            } catch (Exception e) {
            }
        }
        releaseSensor();
        // 设置会话对应的未读消息数为0
        setThreadToRead();
        mUiHandler.removeMessages(UI_WHAT_SCROLL);
    }

    /**
     * 设置会话对应的未读消息数为0
     */
    private void setThreadToRead() {
        if (mRecipientInfo.getThreadId() > 0) {
            List<Long> threadIdList = new ArrayList<Long>();
            threadIdList.add(mRecipientInfo.getThreadId());
            WalkArroundMsgManager.getInstance(this).batchSetMsgRead(threadIdList);
            notifyConversationRefresh(mRecipientInfo.getThreadId());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        isActivityOnForground = true;

        if (mMaskView.getVisibility() == View.VISIBLE) {
            mMaskView.setVisibility(View.GONE);
        }
        if (mMessageDetailAdapter != null) {
            mMessageDetailAdapter.clearDisplayName();
            mMessageDetailAdapter.notifyDataSetChanged();
        }
        sCurrentReceiverNum = mOnPauseSavedReceiverNum;
        // 取消通知消息
        MessageUtil.cancelNotification(getApplicationContext(), mRecipientInfo.getRecipientList().get(0), MessageConstant.ChatType.CHAT_TYPE_ONE2ONE);
        boolean oldNetworkStatus = isNetworkAvailable;
        isNetworkAvailable = NetWorkManager.getInstance(this).isNetworkAvailable();
        if (oldNetworkStatus != isNetworkAvailable) {
            int visibility = isNetworkAvailable ? View.GONE : View.VISIBLE;
            if (mReceiverEditView != null && mReceiverEditView.hasFocus()) {
                visibility = View.GONE;
            }
            findViewById(R.id.network_status_notice_tv).setVisibility(visibility);
            setToolsViewEnable();
        }
        IntentFilter networkFilter = new IntentFilter();
        networkFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkReceiver, networkFilter);

        if (mSensorManager != null) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        isActivityOnForground = false;

        mOnPauseSavedReceiverNum = sCurrentReceiverNum;
        sCurrentReceiverNum = null;
        if (mAudioMediaPlayer != null && mAudioMediaPlayer.isPlaying()) {
            mAudioMediaPlayer.stop();
            mAudioMediaPlayer.reset();
            if (mCurrentPlayAudioMsg != null) {
                mCurrentPlayAudioMsg.setDownStatus(ChatMsgBaseInfo.LOADED);
            }
        }
        try {
            unregisterReceiver(mNetworkReceiver);
        } catch (Exception e) {
        }

        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        if (mAudioManager != null) {
            mAudioManager.setMode(AudioManager.MODE_NORMAL);
        }

        if (!isReceiverEditable) {
            saveDraftMessage();
        }

        if (mMessageListView != null) {
            mMessageListView.getRefreshableView().setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
        }
    }

    @Override
    public void onBackPressed() {
        saveDraftMessage();
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_MAP) {
            if (resultCode != RESULT_OK) {
                return;
            }

            double latitude = data.getDoubleExtra(LocationActivity.LATITUDE, 0);
            double longitude = data.getDoubleExtra(LocationActivity.LONGITUDE, 0);
            String address = data.getStringExtra(LocationActivity.ADDRESS);
            String imagePath = data.getStringExtra(LocationActivity.IMAGE_PATH);

            long messageId = sendLocationInfo(latitude, longitude, address, imagePath);
            transferToDetailView(messageId, true);
            switchBottomPanelView(false);
        } else if (requestCode == REQUEST_CODE_PICTURE_CHOOSE) {
            // 选择了要发送的图片
            if (resultCode != RESULT_OK) {
                return;
            }
            @SuppressWarnings("unchecked")
            HashSet<String> fullSizedMap = (HashSet<String>) data
                    .getSerializableExtra(ImageBrowserActivity.INTENT_IMAGE_FULLSIZED);
            ArrayList<String> pathList = data.getExtras()
                    .getStringArrayList(ImageBrowserActivity.INTENT_CHOSE_PATHLIST);
            boolean isBurnAfter = mCurrentMessageEditState == MESSAGE_EDIT_STATE_BURN_AFTER;
            onSelectedPictures(pathList, fullSizedMap, isBurnAfter);
            switchBottomPanelView(false);
        } else if (requestCode == REQUEST_CODE_TAKE_VIDEO) {
//            if (resultCode != RESULT_OK) {
//                return;
//            }
//            final String filename = data.getStringExtra(TakeVideoActivity.INTENT_VIDEO_PATH);
//            final int videoLength = data.getIntExtra(TakeVideoActivity.INTENT_VIDEO_LENGTH, 0);
//            final boolean isBurn = mCurrentMessageEditState == MESSAGE_EDIT_STATE_BURN_AFTER;
//            new Handler().post(new Runnable() {
//                @Override
//                public void run() {
//                    long messageId = WalkArroundMsgManager.getInstance(getApplicationContext()).sendVideoFile(mRecipientInfo, filename, videoLength,
//                            isBurn, 0, true);
//                    transferToDetailView(messageId, false);
//                    switchBottomPanelView(false);
//                }
//            });
        } else if (requestCode == REQUEST_CODE_FORWARD_MSG) {
            // 转发，选择了联系人
//            if (resultCode != RESULT_OK) {
//                if (mSelectModelPanel == null || mSelectModelPanel.getVisibility() == View.GONE) {
//                    // 单条消息操作
//                    mMessageDetailAdapter.clearSelectedMsgList();
//                }
//                return;
//            }
//            @SuppressWarnings("unchecked")
//            List<ContactInfo> selectedContacts = (List<ContactInfo>) data
//                    .getSerializableExtra(SelectContactActivity.RESULT_CONTACTS_LIST);
//            forwardSelectedMsg(selectedContacts);
        } else if (requestCode == REQUEST_CODE_CHAT_DETAIL) {
//            // 从聊天详情返回到当前界面
//            if (resultCode == ChatDetailActivity.RESULT_CODE_CHAT_LOG_CLEARED) {
//                finish();
//                return;
//            }
//            // 是否黑名单
//            if (mRecipientInfo.getConversationType() == ChatType.CHAT_TYPE_ONE2ONE) {
//                isBlackContact = NewContactManager.getInstance(getApplicationContext()).isBlackNumber(mRecipientInfo.getRecipientList().get(0));
//                // 更新联系人信息
//                updateContactInfo();
//                setToolsViewEnable();
//            } else {
//                isBlackContact = false;
//            }
//            if (mRecipientInfo.getConversationType() == ChatType.CHAT_TYPE_ONE2ONE) {
//                // 更新会话消息thread ID(从联系人名片点击发送消息，置顶取消置顶后thread id可能会变化)
//                long thread = MessageManager.getService().getConversationId(mRecipientInfo.getConversationType(),
//                        mRecipientInfo.getRecipientList());
//                if (thread != mRecipientInfo.getThreadId()) {
//                    notifyConversationRefresh(mRecipientInfo.getThreadId());
//                }
//                mRecipientInfo.setThreadId(thread);
//            }
//            if (resultCode != RESULT_OK) {
//                return;
//            }
//            ArrayList<String> recipientList = (ArrayList<String>) data
//                    .getSerializableExtra(INTENT_CONVERSATION_RECEIVER);
//            mRecipientInfo.setRecipientList(recipientList);
//            String groupName = data.getStringExtra(INTENT_CONVERSATION_DISPLAY_NAME);
//            mRecipientInfo.setDisplayName(groupName);
//            if (mMessageDetailAdapter != null) {
//                boolean isShowNickName = MessageGroupManager.isShowNickName(this, mRecipientInfo.getGroupId());
//                if (mMessageDetailAdapter.isShowNickName() != isShowNickName) {
//                    mMessageDetailAdapter.setShowNickName(isShowNickName);
//                    mMessageDetailAdapter.notifyDataSetChanged();
//                }
//            }
//            TextView receiverName = (TextView) findViewById(R.id.message_title_name_tv);
//            receiverName.setText(groupName + getString(R.string.group_subject, mRecipientInfo.getRecipientList().size()));
        } else if (requestCode == REQUEST_CODE_PREVIEW_IMAGE) {
            if (resultCode != RESULT_OK) {
                mMessageDetailAdapter.notifyDataSetChanged();
                return;
            }
            long msgId = data.getLongExtra(ImageViewerActivity.MESSAGE_ID, -1);
            mMessageDetailAdapter.deleteRcsMessage(msgId);
            mMessageDetailAdapter.notifyDataSetChanged();
        } else if (requestCode == REQUEST_CODE_CAMERA) {
            if (resultCode != RESULT_OK || TextUtils.isEmpty(mPhotoImagePath)) {
                return;
            }
            Intent intent = new Intent(this, ImageBrowserActivity.class);
            ArrayList<String> originFilePath = new ArrayList<String>();
            originFilePath.add(mPhotoImagePath);
            intent.putExtra(ImageBrowserActivity.INTENT_ORIGIN_PATHLIST, originFilePath);
            intent.putExtra(ImageBrowserActivity.INTENT_CHOSE_PATHLIST, originFilePath);
            intent.putExtra(ImageBrowserActivity.INTENT_IMAGE_FROM_TYPE, ImageBrowserActivity._TYPE_FROM_CAMERA);
            intent.putExtra(ImageBrowserActivity.INTENT_IMAGE_MAX_NUM, 1);
            startActivityForResult(intent, REQUEST_CODE_PICTURE_CHOOSE);
            mPhotoImagePath = null;
        } else if (requestCode == REQUEST_CODE_PREVIEW_VIDEO) {
            // 浏览视频
            if (resultCode != RESULT_OK) {
                return;
            }
            long msgId = data.getLongExtra(PlayVideoActivity.INTENT_MESSAGE_ID, -1);
            mMessageDetailAdapter.deleteRcsMessage(msgId);
            mMessageDetailAdapter.notifyDataSetChanged();
        } else if (requestCode == REQUEST_CODE_SHOW_LOCATION) {
            //There are two results from show location activity: normal finish & goto another
            if (resultCode == RESULT_CANCELED) {
                //normal finish
                return;
            } else if (resultCode == RESULT_FIRST_USER) {
                //Send a agreement IM message
                sendAgreement2WalkArround();
            } else if (resultCode == RESULT_OK) {
                //Start location activity to select another place.
                Intent intent = new Intent(this, LocationActivity.class);
                startActivityForResult(intent, REQUEST_CODE_MAP);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mTimeSelectPanel != null && mTimeSelectPanel.getVisibility() == View.VISIBLE) {
                // 隐藏设置时间View
                mTimeSelectPanel.setVisibility(View.GONE);
                return true;
            }
//            if (mSearchContactView != null && mSearchContactView.getVisibility() == View.VISIBLE) {
//                // 隐藏搜索联系人View
//                showSearchContact(false);
//                return true;
//            }
            if (mSelectModelPanel != null && mSelectModelPanel.getVisibility() == View.VISIBLE) {
                // 取消选择模式
                if (mMessageListView != null) {
                    mMessageListView.getRefreshableView().setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
                }
                mMessageDetailAdapter.setInSelectMode(false);
                mMessageDetailAdapter.notifyDataSetChanged();
                return true;
            }

            if (mPopupWindow != null && mPopupWindow.isShowing()) {
                mPopupWindow.dismiss();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private BroadcastReceiver mContactReceiver = null;
    private boolean isContactRegister = false;
//    private void registerContactReceiver(){
//        if (!isContactRegister && mContactReceiver == null){
//            mContactReceiver = new BroadcastReceiver() {
//                public void onReceive(Context context, Intent intent) {
//                    mSearchContactAdapter.setAllContactList(NewContactManager.getInstance(getApplicationContext()).getAllContacts());
//                }
//            };
//            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mContactReceiver,new IntentFilter(NewContactManager.Action.ACTION_CONTACT_INIT_DONE));
//            isContactRegister = true;
//        }
//    }
//
//    private void unRegisterContactReceiver(){
//        if (isContactRegister && mContactReceiver != null){
//            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mContactReceiver);
//            mContactReceiver = null;
//            isContactRegister = false;
//        }
//    }
    /**
     * 快速联系人
     *
     * @param isVisible
     *            是否可见
     */
//    private void showFastContact(boolean isVisible) {
//        if (isVisible && mFastContactGridView.getCount() > 0) {
//            mFastContactGridView.setVisibility(View.VISIBLE);
//        } else {
//            mFastContactGridView.setVisibility(View.GONE);
//        }
//    }

    /**
     * 检索联系人结果列表
     *
     * @param isVisible
     */
//    private void showSearchContact(boolean isVisible) {
//        if (isVisible && mSearchContactAdapter.getCount() > 0) {
//            mSearchContactView.setVisibility(View.VISIBLE);
//            mMessageBottomView.setVisibility(View.GONE);
//            showFastContact(false);
//        } else {
//            mSearchContactView.setVisibility(View.GONE);
//            mMessageBottomView.setVisibility(View.VISIBLE);
//            showFastContact(true);
//        }
//    }

    /**
     * 加载通话消息
     */
    private void loadMessageList() {
        if (!isEnablePullDownLoad) {
            loadUpMessageList();
        } else {
            loadDownMessageList();
        }
    }

    /**
     * 向上加载更多
     */
    private void loadUpMessageList() {
        if (mMessageListView != null) {
            mMessageListView.setTag(true);
        }
        String threadId = String.valueOf(mRecipientInfo.getThreadId());
        String number = null;
        if (mRecipientInfo.getConversationType() == ChatType.CHAT_TYPE_ONE2ONE) {
            number = mRecipientInfo.getRecipientList().get(0);
        }
        String lastImMsgId = Integer.toString(0);
        String lastSmsMsgId = Integer.toString(0);
        if (mMessageDetailAdapter != null) {
            lastImMsgId = Long.toString(mMessageDetailAdapter.getLastChatId());
            lastSmsMsgId = Long.toString(mMessageDetailAdapter.getLastSmsId());
        }
        new LoadMessageTask(this, this).execute(threadId, number, lastImMsgId, lastSmsMsgId);
    }

    /**
     * 向下加载更多
     */
    private void loadDownMessageList() {
        if (mMessageListView != null) {
            mMessageListView.setTag(true);
        }
        String threadId = String.valueOf(mRecipientInfo.getThreadId());
        String number = null;
        if (mRecipientInfo.getConversationType() == ChatType.CHAT_TYPE_ONE2ONE) {
            number = mRecipientInfo.getRecipientList().get(0);
        }
        if (mMessageDetailAdapter != null) {
            String lastImMsgId = Long.toString(mMessageDetailAdapter.getBottomChatId());
            String lastSmsMsgId = Long.toString(mMessageDetailAdapter.getBottomSmsId());
            new LoadSearchResultMessageTask(this, this).execute(threadId, number, lastImMsgId, lastSmsMsgId, "");
        } else {
            new LoadSearchResultMessageTask(this, this).execute(threadId, number, Long.toString(mSearchMsgId),
                    Long.toString(mSearchMsgFromType));
        }
    }

    /**
     * 初始化数据
     */
    @SuppressWarnings("unchecked")
    private boolean initData(Bundle savedInstanceState, Intent intent) {
        if (savedInstanceState != null) {
            isReceiverEditable = savedInstanceState.getBoolean(INTENT_RECEIVER_EDITABLE, true);
            int conversationType = savedInstanceState.getInt(INTENT_CONVERSATION_TYPE, ChatType.CHAT_TYPE_ONE2ONE);
            mRecipientInfo.setConversationType(conversationType);
            long threadId = savedInstanceState.getLong(INTENT_CONVERSATION_THREAD_ID, CONVERSATION_DEFAULT_THREAD_ID);
            mRecipientInfo.setThreadId(threadId);
            if (conversationType == ChatType.CHAT_TYPE_ONE2ONE) {
                List<String> recipient = (List<String>) savedInstanceState
                        .getSerializable(INTENT_CONVERSATION_RECEIVER);
                mRecipientInfo.setRecipientList(recipient);
                if (recipient.size() == 1) {
                    sCurrentReceiverNum = recipient.get(0);
                }
            }

            String displayName = savedInstanceState.getString(INTENT_CONVERSATION_DISPLAY_NAME);
            mRecipientInfo.setDisplayName(displayName);
        } else if (intent != null) {
            isReceiverEditable = intent.getBooleanExtra(INTENT_RECEIVER_EDITABLE, true);
            int conversationType = intent.getIntExtra(INTENT_CONVERSATION_TYPE, ChatType.CHAT_TYPE_ONE2ONE);
            mRecipientInfo.setConversationType(conversationType);

            if (conversationType == ChatType.CHAT_TYPE_ONE2ONE) {
                String receiver = intent.getStringExtra(INTENT_CONVERSATION_RECEIVER);
                if (TextUtils.isEmpty(receiver) && !isReceiverEditable) {
                    return false;
                }
                if (!TextUtils.isEmpty(receiver)) {
                    List<String> recipient = new ArrayList<String>();
                    recipient.add(receiver);
                    mRecipientInfo.setRecipientList(recipient);
                }
                sCurrentReceiverNum = receiver;
            }

            long threadId = intent.getLongExtra(INTENT_CONVERSATION_THREAD_ID, CONVERSATION_DEFAULT_THREAD_ID);
            if (conversationType == ChatType.CHAT_TYPE_ONE2ONE && threadId <= 0) {
                threadId = WalkArroundMsgManager.getInstance(getApplicationContext()).getConversationId(conversationType,
                        mRecipientInfo.getRecipientList());
            }

            WalkArroundMsgManager.getInstance(getApplicationContext()).setMsgReadByThreadId(threadId);
            mRecipientInfo.setThreadId(threadId);
            String displayName = intent.getStringExtra(INTENT_CONVERSATION_DISPLAY_NAME);
            mRecipientInfo.setDisplayName(displayName);
            mSearchMsgId = intent.getLongExtra(INTENT_LOCATION_MESSAGE_ID, -1);
            mSearchMsgFromType = intent.getIntExtra(INTENT_LOCATION_MESSAGE_FROM_TYPE, MSG_FROM_TYPE_RCS);
            isEnablePullDownLoad = mSearchMsgId > 0;
        }
        // 是否黑名单
        isBlackContact = false;

        mOnPauseSavedReceiverNum = sCurrentReceiverNum;
        return true;
    }

    /**
     * 初始化底部消息编辑区域
     */
    private void initMessageEditArea() {
        mMessageBottomView = findViewById(R.id.message_bottom_layout);
        mTimeSendView = (TextView) findViewById(R.id.special_message_ahead_tv);
        mTimeSendView.setOnClickListener(this);
        // 左侧语音等按钮
        mBottomLeftView = (ImageView) findViewById(R.id.left_change_iv);
        mBottomLeftView.setOnClickListener(this);
        setToolsViewEnable();

        //mMessageBottomView.findViewById(R.id.emoji_iv).setOnClickListener(this);

        // 右侧发送、更多、表情等按钮
        mBottomRightView = findViewById(R.id.edit_message_right_rl);
        //View rightBtn = mBottomRightView.findViewById(R.id.right_change_iv);
        //rightBtn.setOnClickListener(this);
        //mBottomRightView.setTag(R.id.right_change_iv, rightBtn);
        View sendBtn = mBottomRightView.findViewById(R.id.send_message_tv);
        sendBtn.setOnClickListener(this);
        // Check if there is recipients or not.
        if (mRecipientInfo.getRecipientList() == null || mRecipientInfo.getRecipientList().size() <= 0) {
            sendBtn.setEnabled(false);
        }
        mBottomRightView.setTag(R.id.send_message_tv, sendBtn);

        // 语音
        mVoicePanel = mMessageBottomView.findViewById(R.id.press_to_speak_ll);
        /* 语音消息进度条 */
        ProgressBar voiceProgress = (ProgressBar) findViewById(R.id.msg_progress_voice);
        mVoiceListener = new PressTalkTouchListener(this, this);
        mVoiceListener.setVoiceProgressBar(voiceProgress, (TextView) mVoicePanel.findViewById(R.id.press_to_speak_tv));
        ImageView touchView = (ImageView) mVoicePanel.findViewById(R.id.press_to_speak_iv);
        mVoiceListener.setVoiceDuration((TextView) mVoicePanel.findViewById(R.id.press_to_speak_hint_tv));
        mVoiceListener.setTouchView(touchView);
        touchView.setOnTouchListener(mVoiceListener);

        mSendMessageEditView = (EditText) findViewById(R.id.message_edit_et);
        mSendMessageEditView.addTextChangedListener(new MsgEditTextWatcher(this));
        mSendMessageEditView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_DEL && keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        && mSendMessageEditView.getTag(R.id.message_edit_et) == null) {
                    // 删除键，删除阅后即焚/定时发送
                    int selectionStart = mSendMessageEditView.getSelectionStart();
                    int selectionEnd = mSendMessageEditView.getSelectionEnd();
                    if (mTimeSendView.getVisibility() == View.VISIBLE && selectionStart == 0 && selectionEnd == 0) {
                        mTimeSendView.setText(null);
                        mTimeSendView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        mTimeSendView.setVisibility(View.GONE);
                        if (mSendMessageEditView.getText().toString().trim().length() > 0) {
                            mCurrentMessageEditState = MESSAGE_EDIT_STATE_HAS_INPUT;
                        } else {
                            mCurrentMessageEditState = MESSAGE_EDIT_STATE_DEFAULT;
                        }
                        switchBottomPanelView(mCurrentMessageEditState);
                        return true;
                    }
                }
                mSendMessageEditView.setTag(R.id.message_edit_et, null);
                return false;
            }
        });
        mSendMessageEditView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
//                if (mEmojiPanel != null) {
//                    mEmojiPanel.setVisibility(View.GONE);
//                    ImageView emjio = (ImageView) mMessageBottomView.findViewById(R.id.emoji_iv);
//                    emjio.setSelected(false);
//                }
                switchBottomPanelView();
            }
        });

        if (mRecipientInfo.getThreadId() > 0) {
            // 显示草稿消息
            ChatMsgBaseInfo draftMessage = WalkArroundMsgManager.getInstance(getApplicationContext()).getDraftMessage(mRecipientInfo.getThreadId());
            if (draftMessage != null && !TextUtils.isEmpty(draftMessage.getData())) {
                mSendMessageEditView.setText(EmojiParser.getInstance(this).addSmileySpans(draftMessage.getData()));
                mSendMessageEditView.setSelection(draftMessage.getData().length());
            }
        }

        findViewById(R.id.tv_select_position).setOnClickListener(this);

        /*
         * if(!NetworkUtil.isNetworkAvailable(getBaseContext())){
         * mSendMessageEditView.setHint(R.string.msg_edit_hint_no_network); if(mCurrentMessageEditState !=
         * MESSAGE_EDIT_STATE_VOICE){ mBottomLeftView.setEnabled(false);
         * mBottomLeftView.setImageResource(R.drawable.public_btn_enterbar_voice_disable); } }else{
         * mSendMessageEditView.setHint(R.string.message_edittext_hint); mBottomLeftView.setEnabled(true); }
         */
    }

    @Override
    public void onDownMessageLoaded(ChatMsgAndSMSReturn result) {
        if (mMessageListView != null) {
            mMessageListView.onRefreshComplete();
        }
        if (result == null) {
            logger.e("onDownMessageLoaded no result");
            if (mMessageListView != null) {
                mMessageListView.setTag(false);
            }
            return;
        }
        if (result.getChatMessages().size() == 0) {
            // 没有更多了
            if (mMessageListView != null) {
                mMessageListView.setTag(false);
                isEnablePullDownLoad = false;
                if (mMessageListView.getMode() == PullToRefreshBase.Mode.BOTH) {
                    mMessageListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                } else {
                    mMessageListView.setMode(PullToRefreshBase.Mode.DISABLED);
                }
            }
            return;
        }
        int searchMsgPos = -1;
        if (mMessageDetailAdapter == null) {
            mMessageDetailAdapter = new MessageDetailListAdapter(this, this);
            //boolean isGroupChat = mRecipientInfo.getConversationType() == ChatType.CHAT_TYPE_GROUP;
            mMessageDetailAdapter.setGroupChat(false);

            mMessageListView = (PullToRefreshListView) findViewById(R.id.message_list_xlv);
            initListPullRefreshView(mMessageListView);
            mMessageListView.setAdapter(mMessageDetailAdapter);
            mMessageListView.setOnRefreshListener(this);
            PullToRefreshBase.Mode mode = isEnablePullDownLoad ? PullToRefreshBase.Mode.BOTH : PullToRefreshBase.Mode.PULL_FROM_END;
            mMessageListView.setMode(mode);
            updateMessageStatus(result.getChatMessages(), mMessageStatus);
            long lastChatId = 0;
            long lastSmsId = 0;
            for (int i = result.getChatMessages().size() - 1; i >= 0; i--) {
                ChatMsgBaseInfo chatMessage = result.getChatMessages().get(i);
                lastChatId = chatMessage.getMsgId();
            }
            searchMsgPos = result.getSearchMsgPos();
            if (searchMsgPos < 0) {
                searchMsgPos = result.getChatMessages().size();
            }
            mMessageDetailAdapter.setLastMessagesId(lastChatId, lastSmsId);
        }
        mMessageDetailAdapter.addDownMessageInfo(result.getChatMessages());
        mMessageDetailAdapter.notifyDataSetChanged();
        if (searchMsgPos != -1) {
            mMessageListView.setSelection(searchMsgPos + 1);
        }
        mMessageDetailAdapter.setBottomMessagesId(result.getLastChatId(), result.getLastSmsId());
        mMessageListView.setTag(false);
    }

    @Override
    public void onConfirmDialogConfirmClick() {
        //TODO:
        if(mStart2walkDialog != null) {
            mStart2walkDialog.dismiss();
            mStart2walkDialog = null;
            logger.d("Start 2 walkarround.");
        }
    }

//    @Override
//    public void onDownMessageLoaded(ChatMsgAndSMSReturn result) {
//        if (mMessageListView != null) {
//            mMessageListView.onRefreshComplete();
//        }
//        if (result == null) {
//            logger.e("onDownMessageLoaded no result");
//            if (mMessageListView != null) {
//                mMessageListView.setTag(false);
//            }
//            return;
//        }
//        if (result.getChatMessages().size() == 0) {
//            // 没有更多了
//            if (mMessageListView != null) {
//                mMessageListView.setTag(false);
//                isEnablePullDownLoad = false;
//                if (mMessageListView.getMode() == PullToRefreshBase.Mode.BOTH) {
//                    mMessageListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
//                } else {
//                    mMessageListView.setMode(PullToRefreshBase.Mode.DISABLED);
//                }
//            }
//            return;
//        }
//        int searchMsgPos = -1;
//        if (mMessageDetailAdapter == null) {
//            mMessageDetailAdapter = new MessageDetailListAdapter(this, this);
//            //boolean isGroupChat = mRecipientInfo.getConversationType() == ChatType.CHAT_TYPE_GROUP;
//            mMessageDetailAdapter.setGroupChat(false);
//
//            mMessageListView = (PullToRefreshListView) findViewById(R.id.message_list_xlv);
//            initListPullRefreshView(mMessageListView);
//            mMessageListView.setAdapter(mMessageDetailAdapter);
//            mMessageListView.setOnRefreshListener(this);
//            PullToRefreshBase.Mode mode = isEnablePullDownLoad ? PullToRefreshBase.Mode.BOTH : PullToRefreshBase.Mode.PULL_FROM_END;
//            mMessageListView.setMode(mode);
//            updateMessageStatus(result.getChatMessages(), mMessageStatus);
//            long lastChatId = 0;
//            long lastSmsId = 0;
//            for (int i = result.getChatMessages().size() - 1; i >= 0; i--) {
//                ChatMsgBaseInfo chatMessage = result.getChatMessages().get(i);
//                lastChatId = chatMessage.getMsgId();
//            }
//            searchMsgPos = result.getSearchMsgPos();
//            if (searchMsgPos < 0) {
//                searchMsgPos = result.getChatMessages().size();
//            }
//            mMessageDetailAdapter.setLastMessagesId(lastChatId, lastSmsId);
//        }
//        mMessageDetailAdapter.addDownMessageInfo(result.getChatMessages());
//        mMessageDetailAdapter.notifyDataSetChanged();
//        if (searchMsgPos != -1) {
//            mMessageListView.setSelection(searchMsgPos + 1);
//        }
//        mMessageDetailAdapter.setBottomMessagesId(result.getLastChatId(), result.getLastSmsId());
//        mMessageListView.setTag(false);
//    }


    /**
     * 消息输入框文字变化监听
     */
    private class MsgEditTextWatcher implements TextWatcher {

        private Context mContext;
        private int mAddTextStart;
        private int mAddTextCount;
        private String mBeforeText;

        MsgEditTextWatcher(Context context) {
            mContext = context;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            mAddTextStart = start;
            mAddTextCount = after;
            mBeforeText = charSequence.toString();
            mSendMessageEditView.setTag(R.id.message_edit_et, mSendMessageEditView.getSelectionStart());
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            mSendMessageEditView.setTag(R.id.message_edit_et, null);
            String input = editable.toString();
            if (EmojiParser.getInstance(mContext).getSmileySpansLength(input) > MAX_INPUT_LENGTH) {
                // 文字超出限定长度
                Toast.makeText(mContext, getString(R.string.msg_max_input_length), Toast.LENGTH_SHORT).show();
                mSendMessageEditView.setText(EmojiParser.getInstance(mContext).getSmileySpans(input, MAX_INPUT_LENGTH));
                mSendMessageEditView.setSelection(mSendMessageEditView.getText().length());
                return;
            } else if (mAddTextCount > 0 && !input.equals(mBeforeText)
                    && EmojiParser.getInstance(mContext).hasSmileySpans(input.substring(mAddTextStart, mAddTextStart + mAddTextCount))) {
                // 避免粘贴字符串表情没有转换
                int selection = mSendMessageEditView.getSelectionStart();
                mSendMessageEditView.setText(EmojiParser.getInstance(mContext).addSmileySpans(input));
                mSendMessageEditView.setSelection(selection);
                return;
            }
            String inputText = input.trim();
//            if (mCurrentMessageEditState == MESSAGE_EDIT_STATE_MORE_EMOJI) {
//                if (mTimeSendView.getVisibility() == View.VISIBLE
//                        || inputText.length() > 0) {
//                    ((TextView) mBottomRightView.getTag(R.id.send_message_tv)).setVisibility(View.VISIBLE);
//                    //((ImageView) mBottomRightView.getTag(R.id.right_change_iv)).setVisibility(View.INVISIBLE);
//                } else {
//                    ((TextView) mBottomRightView.getTag(R.id.send_message_tv)).setVisibility(View.GONE);
//                    //((ImageView) mBottomRightView.getTag(R.id.right_change_iv)).setVisibility(View.VISIBLE);
//                }
//                return;
//            }
            if (!TextUtils.isEmpty(inputText)
                    && (mCurrentMessageEditState == MESSAGE_EDIT_STATE_HAS_INPUT
                    || mCurrentMessageEditState == MESSAGE_EDIT_STATE_BURN_HAS_INPUT)) {
                return;
            }
            switchBottomPanelView();
        }
    }

    /**
     * 切换到信息详细列表
     */
    private void transferToDetailView(long lastMessageId, int msgFrom, boolean isDelayScroll) {
        if (!isReceiverEditable) {
            // 已经是详细通话页面
            if (isEnablePullDownLoad) {
                isEnablePullDownLoad = false;
                // 搜索结果页，取消搜索结果
                mMessageListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                mMessageDetailAdapter.setMessageInfo(null);
                mMessageDetailAdapter.notifyDataSetChanged();
                loadUpMessageList();
            } else {
                updateLastSendMessageToList(lastMessageId, msgFrom, isDelayScroll);
            }
            return;
        }

        /*
        isReceiverEditable = false;
        View editableContactsHeader = findViewById(R.id.build_message_title_layout);
        if (editableContactsHeader != null) {
            editableContactsHeader.setVisibility(View.GONE);
        }
        initMessageDetailHeader();

        long threadId = WalkArroundMsgManager.getInstance(getApplicationContext()).getConversationId(mRecipientInfo.getConversationType(),
                mRecipientInfo.getRecipientList());
        mRecipientInfo.setThreadId(threadId);
        isBlackContact = false;
        if (mRecipientInfo.getConversationType() == ChatType.CHAT_TYPE_ONE2ONE) {
            sCurrentReceiverNum = mRecipientInfo.getRecipientList().get(0);
            // 是否黑名单
            isBlackContact = false;
        }
        mOnPauseSavedReceiverNum = sCurrentReceiverNum;
        // 加载消息
        loadMessageList();

        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(MsgBroadcastConstants.ACTION_MESSAGE_STATUS_CHANGED);
        commandFilter.addAction(MsgBroadcastConstants.ACTION_MESSAGE_NEW_RECEIVED);
        commandFilter.addAction(AlarmReceiver.ACTION_TIME_MSG_SEND);
        registerReceiver(mMessageReceiver, commandFilter);

        // 初始化传感器
        initSensor();
        */
    }

    /**
     * 切换到信息详细列表
     */
    private void transferToDetailView(long lastMessageId, boolean isDelayScroll) {
        transferToDetailView(lastMessageId, MSG_FROM_TYPE_RCS, isDelayScroll);
    }

    /**
     * 消息状态变化
     *
     * @param messageId
     * @param status
     */
    private void messageStateChanged(long messageId, int status) {
        if (mMessageDetailAdapter == null) {
            mMessageStatus.put(messageId, status);
            return;
        }
        mMessageDetailAdapter.updateMessageStatus(messageId, status);
        mMessageDetailAdapter.notifyDataSetChanged();
    }

    /**
     * 收到新消息
     *
     * @param topMsgId
     * @param msgCount
     * @param idList
     */
    private void onReceiveMsg(long topMsgId, int msgCount, long[] idList) {
        if (isEnablePullDownLoad) {
            isEnablePullDownLoad = false;
            // 搜索结果页，取消搜索结果
            mMessageListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            mMessageDetailAdapter.setMessageInfo(null);
            mMessageDetailAdapter.notifyDataSetChanged();
            loadDownMessageList();
        } else if (msgCount > WalkArroundMsgManager.PAGE_COUNT) {
            // 收到大于15条消息
            mMessageDetailAdapter.setMessageInfo(null);
            mMessageDetailAdapter.notifyDataSetChanged();
            loadDownMessageList();
        } else if (msgCount > 1) {
            // 收到多条消息
            for (long id : idList) {
                updateLastSendMessageToList(id, MSG_FROM_TYPE_RCS, false);
            }
        } else {
            //收到一条消息
            updateLastSendMessageToList(topMsgId, MSG_FROM_TYPE_RCS, false);
        }
    }

    /**
     * 将消息添加到信息列表尾部
     *
     * @param messageId
     */
    private void updateLastSendMessageToList(long messageId, int msgFrom, boolean isDelayScroll) {
        // 若mMessageDetailAdapter=null，则说明当前发送的消息为第一条消息，应初始化界面。
        if (mMessageDetailAdapter == null) {
            return;
        }
        ChatMsgBaseInfo message = WalkArroundMsgManager.getInstance(getApplicationContext()).getMessageById(messageId);
        if (message == null) {
            logger.e("can't find message. lastMessageId = " + messageId);
            return;
        }
        long lastSmsMsgId = mMessageDetailAdapter.getLastSmsId();
        long lastRcsMsgId = mMessageDetailAdapter.getLastChatId();
        if (msgFrom == MSG_FROM_TYPE_RCS) {
            if (lastRcsMsgId == 0) {
                mMessageDetailAdapter.setLastMessagesId(message.getMsgId(), lastSmsMsgId);
            }
        } else {
            if (lastSmsMsgId == 0) {
                mMessageDetailAdapter.setLastMessagesId(lastRcsMsgId, message.getMsgId());
            }
        }
        if ((message.getSendReceive() == MessageSendReceive.MSG_RECEIVE)
                && (mMessageListView.getRefreshableView().getLastVisiblePosition() < (mMessageDetailAdapter.getCount() - 1))) {
            // 正在浏览历史消息，不滚动
            if (mMessageListView.getRefreshableView().getTranscriptMode() != AbsListView.TRANSCRIPT_MODE_DISABLED) {
                mMessageListView.getRefreshableView().setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
            }
        } else {
            if (mMessageListView.getRefreshableView().getTranscriptMode() != AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL) {
                mMessageListView.getRefreshableView().setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            }
        }
        mMessageDetailAdapter.addMessage(message);
        if (mMessageListView.getRefreshableView().getLastVisiblePosition() >= mMessageDetailAdapter.getCount() - 1) {
            Message msg = Message.obtain();
            msg.what = UI_WHAT_SCROLL;
            msg.arg1 = mMessageDetailAdapter.getCount();
            if (message.getMsgType() == MessageType.MSG_TYPE_IMAGE) {
                msg.arg2 = 1;
            }
            if (isDelayScroll) {
                mUiHandler.sendMessageDelayed(msg, 200);
            } else {
                mUiHandler.sendMessage(msg);
            }
        }
        mMessageDetailAdapter.notifyDataSetChanged();
        updateHeaderAreaOnRecMsg(message);
    }

    /**
     * 初始化加载更多View
     */
    private void initListPullRefreshView(PullToRefreshListView listView) {
        PullToRefreshBase.Mode mode = listView.getMode();
        listView.setMode(PullToRefreshBase.Mode.BOTH);
        listView.getLoadingLayoutProxy(true, false).setPullLabel(getString(R.string.pull_down_load_more));
        listView.getLoadingLayoutProxy(false, true).setPullLabel(getString(R.string.pull_up_load_more));
        listView.getLoadingLayoutProxy(true, true).setReleaseLabel(getString(R.string.release_to_load));
        listView.getLoadingLayoutProxy(true, true).setRefreshingLabel(getString(R.string.loading));
        listView.setMode(mode);
        listView.getLoadingLayoutProxy(true, true).setLoadingDrawable(getResources().getDrawable(R.drawable.public_loading));
    }

    /**
     * 更新联系人信息
     */
    private void updateContactInfo() {
        if (mRecipientInfo.getConversationType() != ChatType.CHAT_TYPE_ONE2ONE) {
            return;
        }
        View detailHeaderView = findViewById(R.id.message_detail_title_layout);
        PhotoView photoView = (PhotoView) detailHeaderView.findViewById(R.id.message_title_profile_pv);
        String receiverNameStr = mRecipientInfo.getDisplayName();
        String receiverNumStr = mRecipientInfo.getRecipientList().get(0);
        ContactInfo contact = ContactsManager.getInstance(getApplicationContext()).getContactByUsrObjId(receiverNumStr);
        if (contact != null) {
            photoView.setBaseData(contact.getUsername(), contact.getPortrait().getUrl(), null,
                    R.drawable.contact_default_profile);
            mRecipientInfo.setDisplayName(contact.getUsername());
            receiverNameStr = contact.getUsername();
        }

        TextView receiverName = (TextView) detailHeaderView.findViewById(R.id.message_title_name_tv);
        if (TextUtils.isEmpty(receiverNameStr)) {
            receiverName.setText(receiverNumStr);
        } else {
            receiverName.setText(receiverNameStr);
        }
    }

    /**
     * 初始化收信人头部
     */
    private void initMessageDetailHeader() {

        //Title
        View detailHeaderView = findViewById(R.id.message_header_layout);
        //Left, back
        detailHeaderView.findViewById(R.id.back_iv).setOnClickListener(this);
        //Miffle, portrait
        PhotoView photoView = (PhotoView) detailHeaderView.findViewById(R.id.message_title_profile_pv);
        //Right, more
        View moreView = detailHeaderView.findViewById(R.id.message_title_more_iv);
        moreView.setVisibility(View.GONE);
        //moreView.setOnClickListener(this);

        //Get color and set image view.
        logger.d("thread id is: " + mRecipientInfo.getThreadId());
        int color = WalkArroundMsgManager.getInstance(getApplicationContext()).getConversationColor(mRecipientInfo.getThreadId());
        logger.d("color is: " + color);
        mImvDistance = (ImageView) detailHeaderView.findViewById(R.id.iv_show_distance);
        mImvDistance.setOnClickListener(this);
        if (color == -1) {
            mImvDistance.setVisibility(View.GONE);
        } else {
            mImvDistance.setImageResource(color);
            mImvDistance.setVisibility(View.VISIBLE);
        }

        int conversationType = mRecipientInfo.getConversationType();
        String receiverNameStr = mRecipientInfo.getDisplayName();
        String receiverNumStr = null;
        if (conversationType == ChatType.CHAT_TYPE_ONE2ONE) {
            ContactInfo contact = ContactsManager.getInstance(getApplicationContext()).getContactByUsrObjId(mRecipientInfo.getRecipientList().get(0));

            if (contact != null) {
                photoView.setBaseData(contact.getUsername(), contact.getPortrait().getUrl(), null,
                        R.drawable.contact_default_profile);
                mRecipientInfo.setDisplayName(contact.getUsername());
                receiverNameStr = contact.getUsername();
                receiverNumStr = contact.getMobilePhoneNumber();
            }

            //moreView.setVisibility(View.VISIBLE);
        }

        TextView receiverName = (TextView) detailHeaderView.findViewById(R.id.message_title_name_tv);
        if (TextUtils.isEmpty(receiverNameStr)) {
            receiverName.setText(receiverNumStr);
        } else {
            receiverName.setText(receiverNameStr);
        }
    }

    private void switchBottomPanelView() {
        switchBottomPanelView(true);
    }

    /**
     * 切换短信编辑区域
     */
    private void switchBottomPanelView(boolean isEnableListScroll) {
        int oldState = mCurrentMessageEditState;
        if (mTimeSendView.getVisibility() == View.VISIBLE) {
            if (TextUtils.isEmpty(mTimeSendView.getText())) {
                // 阅后即焚
                mCurrentMessageEditState = mSendMessageEditView.getText().toString().trim().length() > 0 ? MESSAGE_EDIT_STATE_BURN_HAS_INPUT
                        : MESSAGE_EDIT_STATE_BURN_AFTER;
            } else {
                // 定时发送
                mCurrentMessageEditState = TextUtils.isEmpty(mSendMessageEditView.getText()) ? MESSAGE_EDIT_STATE_DEFAULT
                        : MESSAGE_EDIT_STATE_HAS_INPUT;
            }
        } else if (mSendMessageEditView.getText().toString().trim().length() > 0) {
            int emojiVisibility = mEmojiPanel == null ? View.GONE : mEmojiPanel.getVisibility();
            mCurrentMessageEditState = emojiVisibility == View.VISIBLE ?
                    MESSAGE_EDIT_STATE_MORE_EMOJI : MESSAGE_EDIT_STATE_HAS_INPUT;
        } else {
            int emojiVisibility = mEmojiPanel == null ? View.GONE : mEmojiPanel.getVisibility();
            mCurrentMessageEditState = emojiVisibility == View.VISIBLE ?
                    MESSAGE_EDIT_STATE_MORE_EMOJI : MESSAGE_EDIT_STATE_DEFAULT;
        }
        if (oldState != mCurrentMessageEditState) {
            switchBottomPanelView(mCurrentMessageEditState, isEnableListScroll);
        }
    }

    /**
     * 切换底部编辑状态
     *
     * @param currentState 切换到的状态
     */
    private void switchBottomPanelView(int currentState) {
        switchBottomPanelView(currentState, true);
    }

    /**
     * 切换底部编辑状态
     *
     * @param currentState       切换到的状态
     * @param isEnableListScroll ListView是否滚动
     */
    private void switchBottomPanelView(int currentState, boolean isEnableListScroll) {
        if (mMessageListView != null) {
            int mode = mMessageListView.getRefreshableView().getTranscriptMode();
            int destMode = isEnableListScroll ? AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL
                    : AbsListView.TRANSCRIPT_MODE_DISABLED;
            if (mode != destMode) {
                mMessageListView.getRefreshableView().setTranscriptMode(destMode);
            }
        }
        mSendMessageEditView.requestFocus();
        int leftViewResId = R.drawable.message_btn_voice;
        /*
         * if(!NetworkUtil.isNetworkAvailable(getBaseContext())){ leftViewResId =
         * R.drawable.public_btn_enterbar_voice_disable; }
         */
        int rightViewResId = R.drawable.message_btn_open_more;
        int emojiVisibility = View.GONE;
        int toolsPanelVisibility = View.GONE;
        int voicePanelVisibility = View.GONE;
        boolean canSend = false;
        //ImageView emjio = (ImageView) mMessageBottomView.findViewById(R.id.emoji_iv);
        switch (currentState) {
            case MESSAGE_EDIT_STATE_DEFAULT:
                mSendMessageEditView.setHint("");
                mBottomRightView.findViewById(R.id.send_message_tv).setVisibility(View.VISIBLE);
                //emjio.setImageResource(R.drawable.message_btn_smile);
                if (mTimeSendView.getVisibility() == View.VISIBLE) {
                    rightViewResId = R.drawable.message_btn_close;
                }
                break;
            case MESSAGE_EDIT_STATE_HAS_INPUT:
                //emjio.setImageResource(R.drawable.message_btn_smile);
                if (mTimeSendView.getVisibility() == View.VISIBLE) {
                    rightViewResId = R.drawable.message_btn_close;
                }
                canSend = true;
                break;

            case MESSAGE_EDIT_STATE_VOICE:
                mVoiceListener.setAudioStatusIcon(R.drawable.public_btn_enterbar_voicebtn,
                        R.drawable.public_btn_enterbar_voicebtn2, R.drawable.progress_voice_duration);
                //emjio.setImageResource(R.drawable.message_btn_smile);
                hideSoftInput();
                mBottomRightView.findViewById(R.id.send_message_tv).setVisibility(View.GONE);
                voicePanelVisibility = View.VISIBLE;
                leftViewResId = R.drawable.message_btn_keybroad;
                if (mTimeSendView.getVisibility() == View.VISIBLE) {
                    rightViewResId = R.drawable.message_btn_close;
                }
                break;
            default:
                break;
        }

        mBottomLeftView.setImageResource(leftViewResId);

        mVoicePanel.setVisibility(voicePanelVisibility);
        voicePanelVisibility = voicePanelVisibility == View.VISIBLE ? View.GONE : View.VISIBLE;
        mMessageBottomView.findViewById(R.id.message_edit_ll).setVisibility(voicePanelVisibility);
        voicePanelVisibility = currentState == MESSAGE_EDIT_STATE_BURN_HAS_INPUT ? View.GONE : voicePanelVisibility;
        //emjio.setVisibility(voicePanelVisibility);

        if (mMoreToolsPanel != null) {
            mMoreToolsPanel.setVisibility(toolsPanelVisibility);
        }

        if (mEmojiPanel != null) {
            mEmojiPanel.setVisibility(emojiVisibility);
        }
    }

    /**
     * 设置发送联系人、图片等是否可用
     */
    private void setToolsViewEnable() {
        if (mMoreToolsPanel == null) {
            if (mRecipientInfo.getRecipientList() == null || mRecipientInfo.getRecipientList().size() == 0) {
                mBottomLeftView.setEnabled(false);
            } else {
                mBottomLeftView.setEnabled(isNetworkAvailable);
            }
            return;
        }
        if (mRecipientInfo.getRecipientList() == null || mRecipientInfo.getRecipientList().size() == 0) {
            mMoreToolsPanel.setPictureEnable(false);
            mMoreToolsPanel.setLocationEnable(false);
            //mMoreToolsPanel.setVideoEnable(false);
            mBottomLeftView.setEnabled(false);
        } else {
            boolean isEnable = isNetworkAvailable;
            mMoreToolsPanel.setLocationEnable(isEnable);
            //mMoreToolsPanel.setVideoEnable(isEnable);
            mMoreToolsPanel.setPictureEnable(isEnable);
            mBottomLeftView.setEnabled(isEnable);
        }
    }

    /**
     * 发送消息
     */
    public void onSend() {
        String msg = mSendMessageEditView.getText().toString();
        if (TextUtils.isEmpty(msg)) {
            return;
        }

        mSendMessageEditView.setText("");
        mTimeSendView.setVisibility(View.GONE);
        long lastMessageId = WalkArroundMsgManager.getInstance(getApplicationContext()).sendTextMsg(mRecipientInfo, msg, null);
        transferToDetailView(lastMessageId, false);
        int emojiVisibility = mEmojiPanel == null ? View.GONE : mEmojiPanel.getVisibility();
        mCurrentMessageEditState = emojiVisibility == View.VISIBLE ?
                MESSAGE_EDIT_STATE_MORE_EMOJI : MESSAGE_EDIT_STATE_DEFAULT;
        switchBottomPanelView(mCurrentMessageEditState);
    }

    /*
     * Send agreement
     */
    private void sendAgreement2WalkArround() {
        long threadId = mRecipientInfo.getThreadId();
        int colorIndex = MessageUtil.getFriendColorIndex(threadId);
        logger.d("send agreement, the color index is " + colorIndex);
        String extraInfor = MessageUtil.EXTRA_AGREEMENT_2_WALKARROUND +
                MessageUtil.EXTRA_INFOR_SPLIT +
                colorIndex;
        logger.d("send agreement, the extra is: " + extraInfor);
        long messageId = WalkArroundMsgManager.getInstance(getApplicationContext()).sendTextMsg(mRecipientInfo,
                getString(R.string.agree_2_walkarround_postfix) +
                getString(MessageUtil.getFriendColorDescription(colorIndex)),
                extraInfor);

        //Update conversation state & color
        WalkArroundMsgManager.getInstance(getApplicationContext()).updateConversationStatusAndColor(threadId, MessageUtil.WalkArroundState.STATE_WALK, colorIndex);

        //Update color to Server
        ThreadPoolManager.getPoolManager().addAsyncTask(new UpdateSpeedDateColorTask(getApplicationContext(),
                mUpdateSpeedDateColorListener,
                HttpUtil.HTTP_FUNC_UPDATE_SPEEDDATE_COLOR,
                HttpUtil.HTTP_TASK_UPDATE_SPEEDDATE_COLOR,
                UpdateSpeedDateColorTask.getParams(ProfileManager.getInstance().getSpeedDateId(), String.valueOf(colorIndex)),
                TaskUtil.getTaskHeader()));

        transferToDetailView(messageId, false);
        logger.d("Send agreement result: " + messageId);
    }

    /**
     * 保存草稿
     */
    private void saveDraftMessage() {
        String saveText = null;
        if (mTimeSendView.getVisibility() != View.VISIBLE) {
            // 不收藏阅后即焚定时消息
            saveText = mSendMessageEditView.getText().toString();
        }
        WalkArroundMsgManager.getInstance(getApplicationContext()).updateDraftMessage(mRecipientInfo, saveText);
    }

    protected void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSendMessageEditView.getWindowToken(), 0);
    }

    protected void showSoftInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
    }

    @Override
    public void onPhrasebookSelected(String selectedStr) {
        // 常用语
        if (selectedStr == null || selectedStr.length() == 0) {
            return;
        }
        int cursorPos = mSendMessageEditView.getSelectionStart();
        Editable editable = mSendMessageEditView.getEditableText();
        editable.insert(cursorPos, selectedStr);
        mSendMessageEditView.setSelection(cursorPos + selectedStr.length());
        mCurrentMessageEditState = MESSAGE_EDIT_STATE_HAS_INPUT;
        switchBottomPanelView(mCurrentMessageEditState);
    }

    @Override
    public void onPictureClick() {
        Intent intent = new Intent();
        intent.setClass(this, ImageChooseActivity.class);
        intent.putExtra(ImageChooseActivity.IS_FULL_SIZE_OPTION, true);
        intent.putExtra(ImageChooseActivity.IMAGE_CHOOSE_TYPE, ImageChooseActivity.FROM_MESSAGE_CODE);
        startActivityForResult(intent, REQUEST_CODE_PICTURE_CHOOSE);
    }

    /**
     * 打开相机
     */
    public void onCameraClick() {
        // 打开照相机进行拍照
//        mPhotoImagePath = MessageUtil.createCameraTakePicFile();
//        if (TextUtils.isEmpty(mPhotoImagePath)) {
//            Toast.makeText(this, R.string.open_camera_fail, Toast.LENGTH_LONG).show();
//            return;
//        }
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        Uri fileUri = Uri.fromFile(new File(mPhotoImagePath));
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
//        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    @Override
    public void onVideoClick() {
//        Intent intent = new Intent(this, TakeVideoActivity.class);
//        startActivityForResult(intent, REQUEST_CODE_TAKE_VIDEO);
    }

    @Override
    public void onLocation() {
        mMaskView.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, LocationActivity.class);
        startActivityForResult(intent, REQUEST_CODE_MAP);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_iv:
                // 返回
                saveDraftMessage();
                finish();
                break;
            case R.id.send_message_tv:
                // 发送
                onSend();
                break;
//        case R.id.build_message_receiver_rl:
//            // 编辑联系人状态
//            changeToReceiverEditState();
//            break;
            case R.id.emoji_iv:
                // 表情/阅后即焚图片按钮
//            if (mCurrentMessageEditState == MESSAGE_EDIT_STATE_BURN_AFTER) {
//                view.setSelected(false);
//                hideSoftInput();
//                // 阅后即焚图片按钮
//                if (mBurnMsgTypeChoosePopup == null) {
//                    mBurnMsgTypeChoosePopup = createPopupWindow();
//                }
//                mBurnMsgTypeChoosePopup.showAtLocation(view, Gravity.NO_GRAVITY, 0, 0);
//            } else {
                // 表情
//                if (view.isSelected()) {
//                    if (mEmojiPanel != null) {
//                        mEmojiPanel.setVisibility(View.GONE);
//                    }
//                    showSoftInput();
//                    switchBottomPanelView();
//                } else {
//                    hideSoftInput();
//                    mCurrentMessageEditState = MESSAGE_EDIT_STATE_MORE_EMOJI;
//                    switchBottomPanelView(mCurrentMessageEditState);
//                }
//                view.setSelected(!view.isSelected());
                //}
                break;
            case R.id.msg_popup_menu_layout:
                mBurnMsgTypeChoosePopup.dismiss();
                break;
            case R.id.left_change_iv:
                // 消息编辑左侧按钮
                if (mCurrentMessageEditState == MESSAGE_EDIT_STATE_VOICE
                        || mCurrentMessageEditState == MESSAGE_EDIT_STATE_BURN_VOICE) {
                    showSoftInput();
                    switchBottomPanelView();
                } else if (mCurrentMessageEditState == MESSAGE_EDIT_STATE_BURN_AFTER) {
                    mCurrentMessageEditState = MESSAGE_EDIT_STATE_BURN_VOICE;
                    switchBottomPanelView(mCurrentMessageEditState);
                } else {
                    mCurrentMessageEditState = MESSAGE_EDIT_STATE_VOICE;
                    switchBottomPanelView(mCurrentMessageEditState);
                }
                break;
//        case R.id.right_change_iv:
//            // 消息编辑右侧按钮
//            if (mCurrentMessageEditState == MESSAGE_EDIT_STATE_VOICE
//                    && mTimeSendView.getVisibility() == View.VISIBLE) {
//                switchBottomPanelView();
//            } else if (mCurrentMessageEditState == MESSAGE_EDIT_STATE_MORE_OPERATE) {
//                if (mEmojiPanel != null) {
//                    mEmojiPanel.setVisibility(View.GONE);
//                }
//                switchBottomPanelView();
//            } else if (mCurrentMessageEditState == MESSAGE_EDIT_STATE_MORE_EMOJI
//                    || mCurrentMessageEditState == MESSAGE_EDIT_STATE_VOICE) {
//                mCurrentMessageEditState = MESSAGE_EDIT_STATE_MORE_OPERATE;
//                switchBottomPanelView(mCurrentMessageEditState);
//            } else if (mCurrentMessageEditState == MESSAGE_EDIT_STATE_BURN_AFTER
//                    || mCurrentMessageEditState == MESSAGE_EDIT_STATE_BURN_VOICE) {
//                mTimeSendView.setText(null);
//                mTimeSendView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
//                mTimeSendView.setVisibility(View.GONE);
//                mCurrentMessageEditState = MESSAGE_EDIT_STATE_DEFAULT;
//                switchBottomPanelView(mCurrentMessageEditState);
//            } else if (mCurrentMessageEditState == MESSAGE_EDIT_STATE_DEFAULT) {
//                if (mTimeSendView.getVisibility() == View.VISIBLE) {
//                    mSendMessageEditView.setText("");
//                    mTimeSendView.setText("");
//                    mTimeSendView.setVisibility(View.GONE);
//                    switchBottomPanelView(mCurrentMessageEditState);
//                } else {
//                    mCurrentMessageEditState = MESSAGE_EDIT_STATE_MORE_OPERATE;
//                    switchBottomPanelView(mCurrentMessageEditState);
//                }
//            }
//            break;
            case R.id.message_title_more_iv:
                //case R.id.message_profile_group_iv:
                // 更多
                // titleContactsMore(view);
                //showChatDetail();
                break;
            case R.id.select_all_rl:
                // 全选或取消全选
                if (view.getTag() == null || (Boolean) view.getTag()) {
                    // 全选
                    mMessageDetailAdapter.setSelectForAll(true);
                } else {
                    mMessageDetailAdapter.setSelectForAll(false);
                }
                break;
//        case R.id.msg_mark_collect_tv:
//            // 收藏
//            List<ChatMsgBaseInfo> selectMsg = mMessageDetailAdapter.getSelectedMsgList();
//            int resultCode = FavoriteMessageManager.setMessageFavorite(this, mRecipientInfo, selectMsg);
//            if (resultCode == FavoriteMessageManager.COLLECT_SUCCESS_CODE) {
//                Toast.makeText(this, R.string.collect_success, Toast.LENGTH_SHORT).show();
//            } else if (resultCode == FavoriteMessageManager.COLLECT_FAIL_REASON_BURN) {
//                Toast.makeText(this, R.string.connot_collect_msg_notice1, Toast.LENGTH_SHORT).show();
//            } else if (resultCode == FavoriteMessageManager.COLLECT_FAIL_REASON_BURN_UNLOAD) {
//                Toast.makeText(this, R.string.connot_collect_msg_notice2, Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, R.string.part_msg_collect_fail, Toast.LENGTH_SHORT).show();
//            }
//            mMessageDetailAdapter.setInSelectMode(false);
//            mMessageDetailAdapter.notifyDataSetChanged();
//            break;
            case R.id.msg_detail_delete_tv:
                // 删除
                List<ChatMsgBaseInfo> delMsgList = mMessageDetailAdapter.getSelectedMsgList();
                WalkArroundMsgManager.getInstance(getApplicationContext()).deleteMessages(this, delMsgList);
                if (!WalkArroundMsgManager.getInstance(getApplicationContext()).isConversationExist(mRecipientInfo.getThreadId())) {
                    mRecipientInfo.setThreadId(-1);
                }
                mMessageDetailAdapter.deleteSelectedMessage();
                mMessageDetailAdapter.setInSelectMode(false);
                mMessageDetailAdapter.notifyDataSetChanged();
                break;
            case R.id.msg_mark_foward_tv:
                // 转发
//            Intent forwardIntent = new Intent(this, SelectContactActivity.class);
//            forwardIntent.putExtra(SelectContactActivity.SELECT_TYPE,
//                    SelectContactActivity.TYPE_SELECT_CONTACTS_WITH_NUM);
//            forwardIntent.putExtra(SelectContactActivity.SELECTION_TYPE, SelectContactActivity.SINGLE_SELECTION);
//            forwardIntent.putExtra(SelectContactActivity.INTENT_CONFIRM_SELECTION, true);
////            forwardIntent.putExtra(SelectContactActivity.INTENT_DISABLE_NOT_RCS_NUMBER, true);
//            startActivityForResult(forwardIntent, REQUEST_CODE_FORWARD_MSG);
                break;
            case R.id.msg_mark_copy_tv:
                // 拷贝
                List<ChatMsgBaseInfo> msgList = mMessageDetailAdapter.getSelectedMsgList();
                StringBuilder text = new StringBuilder();
                for (ChatMsgBaseInfo message : msgList) {
                    text.append(message.getData());
                    text.append("\n");
                }
                ClipData clip = ClipData.newPlainText("message", text.toString());
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(clip);
                mMessageDetailAdapter.setInSelectMode(false);
                mMessageDetailAdapter.notifyDataSetChanged();
                break;
            case R.id.special_message_ahead_tv:
                if (!TextUtils.isEmpty(mTimeSendView.getText())) {
                    //onSetTimeOnClick();
                    hideSoftInput();
                }
                break;
            case R.id.tv_select_position:
                Intent intent = new Intent(this, LocationActivity.class);
                startActivityForResult(intent, REQUEST_CODE_MAP);
                break;
            case R.id.iv_show_distance:
                Intent intentShowDistance = new Intent(BuildMessageActivity.this, ShowDistanceActivity.class);
                intentShowDistance.putExtra(ShowDistanceActivity.PARAMS_THREAD_ID, mRecipientInfo.getThreadId());
                startActivity(intentShowDistance);
                break;
            default:
                break;
        }
    }

//    public void choseContactsCountChange(List<MessageFastContact> choseContactsList) {
//        // 收信人个数变化了
//        int choseCount = choseContactsList.size();
//
//        // 选择的人数
//        String choseCountStr = null;
//        if (choseCount > 1) {
//            choseCountStr = getString(R.string.message_contact_count, choseCount);
//        }
//        TextView receiverCount = (TextView) findViewById(R.id.build_message_chose_count);
//        receiverCount.setTag(choseCount);
//        receiverCount.setText(choseCountStr);
//
//        // 选择的人显示
//        HashMap<String, Boolean> choseContactsMap = new HashMap<String, Boolean>();
//        StringBuilder displayReceiver = new StringBuilder();
//        for (int i = 0; i < choseCount; i++) {
//            MessageFastContact contact = choseContactsList.get(i);
//            if (TextUtils.isEmpty(contact.getName())) {
//                displayReceiver.append(contact.getAddress());
//            } else {
//                displayReceiver.append(contact.getName());
//            }
//
//            displayReceiver.append("，");
//            choseContactsMap.put(contact.getAddress(), true);
//        }
//        TextView receiverNames = (TextView) findViewById(R.id.build_message_chose_names);
//        if (displayReceiver.length() > 0) {
//            displayReceiver.deleteCharAt(displayReceiver.length() - 1);
//        } else {
//            displayReceiver.append(getString(R.string.message_receiver_hint));
//        }
//        receiverNames.setText(displayReceiver.toString());
//        mRecipientInfo.setDisplayName(receiverNames.getText().toString());
//
//        // 收信人信息
//        int type = ChatType.CHAT_TYPE_ONE2ONE;
//        mRecipientInfo.setConversationType(type);
//        if (choseCount > 0) {
//            String[] receivers = new String[choseCount];
//            choseContactsMap.keySet().toArray(receivers);
//            mRecipientInfo.setRecipientList(Arrays.asList(receivers));
//        } else {
//            mRecipientInfo.setRecipientList(null);
//        }
//        // 底部部分功能（发送联系人、图片等）是否可用
//        setToolsViewEnable();
//
//        // 发送按钮
//        boolean isEnableSend = choseCount > 0;
//        TextView sendView = (TextView) mBottomRightView.getTag(R.id.send_message_tv);
//        sendView.setEnabled(isEnableSend);
//    }

    /**
     * 切换到联系人可以编辑状态
     */
//    private void changeToReceiverEditState() {
//        mFastContactGridView.setVisibility(View.VISIBLE);
//        mSearchContactView.setVisibility(View.GONE);
//        findViewById(R.id.build_message_receiver_rl).setVisibility(View.GONE);
//        findViewById(R.id.build_message_reciever_sv).setVisibility(View.VISIBLE);
//        switchBottomPanelView();
//        mReceiverEditView.requestFocus();
//    }

    /**
     * 选择好时间
     *
     * @param pickedTime 选择的时间
     */
    private void onSelectedTime(String pickedTime) {
        mTimeSelectPanel.setVisibility(View.GONE);
        mTimeSendView.setText(pickedTime);
        mTimeSendView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.public_icon_enterbar_clock, 0, 0, 0);
        mTimeSendView.setVisibility(View.VISIBLE);
        mCurrentMessageEditState = MESSAGE_EDIT_STATE_DEFAULT;
        switchBottomPanelView(mCurrentMessageEditState);
        showSoftInput();
    }

    /**
     * 选择了发送的图片
     *
     * @param picPathList  选择的所有图片路径
     * @param fullSizedMap 原图发送图片的路径
     */
    private void onSelectedPictures(ArrayList<String> picPathList, HashSet<String> fullSizedMap, boolean isBurn) {
        for (int i = 0; i < picPathList.size(); i++) {
            File picFile = new File(picPathList.get(i));
            if (!picFile.exists()) {
                continue;
            }
            int fileSize;
            try {
                FileInputStream fis = new FileInputStream(picFile); // b
                fileSize = fis.available() / 1024; // Kb
                fis.close();
            } catch (IOException e) {
                logger.e("onSelectedPictures ,calc file size IOException: " + e.getMessage());
                continue;
            }
            if (fileSize < MessageConstant.MSG_IMAGE_MAXIMUM_PICTURE_SIZE) {
                int maxSendSize = MessageConstant.MSG_IMAGE_MINIMUM_COMPRESSING_SIZE;
                if (fullSizedMap != null && fullSizedMap.contains(picPathList.get(i))) {
                    // 不需要压缩
                    maxSendSize = fileSize;
                }
                long messageId = WalkArroundMsgManager.getInstance(getApplicationContext()).sendImageFiles(mRecipientInfo, picPathList.get(i), isBurn,
                        0, maxSendSize);
                transferToDetailView(messageId, true);
            } else {
                Toast.makeText(getBaseContext(), R.string.file_broken_please_send_other_file, Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    public void messageItemOnClick(View clickedItemView, ChatMsgBaseInfo clickedMessage) {

        if (clickedMessage == null) {
            return;
        }

        // 点击了消息
        switch (clickedMessage.getMsgType()) {
            case MessageType.MSG_TYPE_TEXT:
                // 纯文本消息;
                onPlainTextClick(clickedMessage);
                break;
            case MessageType.MSG_TYPE_AUDIO:
                // 语音消息;
                if (clickedMessage.isBurnAfterMsg()) {
                    // 阅后即焚
                    onPlainTextClick(clickedMessage);
                } else {
                    onStartAudio(clickedMessage);
                }
                break;
            case MessageType.MSG_TYPE_VIDEO:
                // 视频;
                downVideoOrPlay(clickedMessage);
                break;
            case MessageType.MSG_TYPE_IMAGE:
                // 图片消息;
                onStartPicture(clickedMessage);
                break;
            case MessageType.MSG_TYPE_MAP:
                // 定位消息
                onStartMapActivity(clickedMessage);
                break;
            case MessageType.MSG_TYPE_NOTIFICATION:
                onHandleNotifyMsg(clickedMessage);
                break;
            default:
                break;
        }
    }

    @Override
    public void messageResend(ChatMsgBaseInfo clickedMessage) {
        // 发送失败,重发
        clickedMessage.setMsgState(MessageState.MSG_STATE_SEND_ING);
        clickedMessage.setGroupId(mRecipientInfo.getGroupId());
        mMessageDetailAdapter.notifyDataSetChanged();
        WalkArroundMsgManager.getInstance(getApplicationContext()).resendMessage(clickedMessage);
    }

    @Override
    public void onSelectModeChange(boolean isSelectMode) {
        // 选择模式
        if (mSelectModelPanel == null) {
            ViewStub selectModeView = (ViewStub) findViewById(R.id.message_batch_operation_vs);
            selectModeView.inflate();
            mSelectModelPanel = findViewById(R.id.message_batch_panel_ll);
            // 全选
            mSelectModelPanel.findViewById(R.id.select_all_rl).setOnClickListener(this);
            mSelectModelPanel.findViewById(R.id.msg_mark_collect_tv).setOnClickListener(this);
            mSelectModelPanel.findViewById(R.id.msg_detail_delete_tv).setOnClickListener(this);
            mSelectModelPanel.findViewById(R.id.msg_mark_foward_tv).setOnClickListener(this);
            mSelectModelPanel.findViewById(R.id.msg_mark_copy_tv).setOnClickListener(this);
        }
        if (mMessageListView != null) {
            mMessageListView.getRefreshableView().setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
        }
        if (isSelectMode) {
            hideSoftInput();
            switchBottomPanelView(MESSAGE_EDIT_STATE_DEFAULT, false);
            mSelectModelPanel.setVisibility(View.VISIBLE);
        } else {
            mSelectModelPanel.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSelectCountChange(int selectCount, int oldCount) {
        if (selectCount == oldCount) {
            return;
        }
        int totalCount = mMessageDetailAdapter.getCount();
        if (oldCount == totalCount && selectCount < totalCount) {
            // 显示全选
            TextView selectBtn = (TextView) mSelectModelPanel.findViewById(R.id.select_all_tv);
            selectBtn.setText(R.string.message_mark_select_all);
            mSelectModelPanel.findViewById(R.id.select_all_rl).setTag(true);
        } else if (selectCount == totalCount && oldCount < totalCount) {
            // 显示取消
            TextView selectBtn = (TextView) mSelectModelPanel.findViewById(R.id.select_all_tv);
            selectBtn.setText(R.string.message_mark_clear_all);
            mSelectModelPanel.findViewById(R.id.select_all_rl).setTag(false);
        }
        if (oldCount == 0 && selectCount > 0) {
            mSelectModelPanel.findViewById(R.id.msg_mark_collect_tv).setEnabled(true);
            mSelectModelPanel.findViewById(R.id.msg_detail_delete_tv).setEnabled(true);
            mSelectModelPanel.findViewById(R.id.msg_mark_foward_tv).setEnabled(true);
            mSelectModelPanel.findViewById(R.id.msg_mark_copy_tv).setEnabled(true);
        } else if (selectCount == 0) {
            mSelectModelPanel.findViewById(R.id.msg_mark_collect_tv).setEnabled(false);
            mSelectModelPanel.findViewById(R.id.msg_detail_delete_tv).setEnabled(false);
            mSelectModelPanel.findViewById(R.id.msg_mark_foward_tv).setEnabled(false);
            mSelectModelPanel.findViewById(R.id.msg_mark_copy_tv).setEnabled(false);
        }
    }

    @Override
    public void onMsgCollect(ChatMsgBaseInfo clickedMessage) {
        // 收藏
//        List<ChatMsgBaseInfo> selectMsg = new ArrayList<ChatMsgBaseInfo>();
//        selectMsg.add(clickedMessage);
//        int resultCode = FavoriteMessageManager.setMessageFavorite(this, mRecipientInfo, selectMsg);
//        if (resultCode == FavoriteMessageManager.COLLECT_SUCCESS_CODE) {
//            Toast.makeText(this, R.string.collect_success, Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(this, R.string.collect_fail, Toast.LENGTH_SHORT).show();
//        }
//        mMessageDetailAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMsgDelete(ChatMsgBaseInfo clickedMessage) {
        // 删除
        List<ChatMsgBaseInfo> msgList = new ArrayList<ChatMsgBaseInfo>();
        msgList.add(clickedMessage);
        WalkArroundMsgManager.getInstance(getApplicationContext()).deleteMessages(this, msgList);
        if (!WalkArroundMsgManager.getInstance(getApplicationContext()).isConversationExist(mRecipientInfo.getThreadId())) {
            mRecipientInfo.setThreadId(-1);
        }
        mMessageDetailAdapter.deleteMessage(clickedMessage);
        mMessageDetailAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMsgForward(ChatMsgBaseInfo clickedMessage) {
//        // 转发
//        Intent forwardIntent = new Intent(this, SelectContactActivity.class);
//        forwardIntent.putExtra(SelectContactActivity.SELECT_TYPE, SelectContactActivity.TYPE_SELECT_CONTACTS_WITH_NUM);
//        forwardIntent.putExtra(SelectContactActivity.SELECTION_TYPE, SelectContactActivity.SINGLE_SELECTION);
//        forwardIntent.putExtra(SelectContactActivity.INTENT_CONFIRM_SELECTION, true);
////        forwardIntent.putExtra(SelectContactActivity.INTENT_DISABLE_NOT_RCS_NUMBER, true);
//        startActivityForResult(forwardIntent, REQUEST_CODE_FORWARD_MSG);
    }

    @Override
    public void onMessageLoaded(ChatMsgAndSMSReturn result) {
        // 加载完通信信息
        if (mMessageDetailAdapter == null) {
            mMessageDetailAdapter = new MessageDetailListAdapter(this, this);
            boolean isGroupChat = mRecipientInfo.getConversationType() == ChatType.CHAT_TYPE_GROUP;
            mMessageDetailAdapter.setGroupChat(isGroupChat);

            mMessageListView = (PullToRefreshListView) findViewById(R.id.message_list_xlv);
            initListPullRefreshView(mMessageListView);
            mMessageListView.setAdapter(mMessageDetailAdapter);
            mMessageListView.setOnRefreshListener(this);
            mMessageListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            if (result != null) {
                updateMessageStatus(result.getChatMessages(), mMessageStatus);
            }
        }
        if (mMessageListView != null) {
            mMessageListView.onRefreshComplete();
        }
        if (result == null) {
            logger.e("onMessageLoaded no result");
            mMessageListView.setTag(false);
            return;
        }
        if (result.getChatMessages().size() == 0 && mMessageListView != null) {
            // 没有更多了
            mMessageListView.setTag(false);
            if (mMessageListView.getMode() == PullToRefreshBase.Mode.BOTH) {
                mMessageListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
            } else {
                mMessageListView.setMode(PullToRefreshBase.Mode.DISABLED);
            }
            return;
        }

        mMessageDetailAdapter.addUpMessageInfo(result.getChatMessages());
        mMessageDetailAdapter.notifyDataSetChanged();
        int selectionPos = result.getChatMessages().size() - 1;
        if (mMessageDetailAdapter.getCount() == result.getChatMessages().size()) {
            selectionPos = mMessageDetailAdapter.getCount() + 1;
        }

        if (mMessageListView != null) {
            mMessageListView.setSelection(selectionPos);
            mMessageListView.setTag(false);
        }
        mMessageDetailAdapter.setLastMessagesId(result.getLastChatId(), result.getLastSmsId());
    }


    /**
     * 更新消息状态
     *
     * @param messageInfoList
     * @param messageStatus
     */
    public void updateMessageStatus(List<ChatMsgBaseInfo> messageInfoList, HashMap<Long, Integer> messageStatus) {
        if (messageInfoList == null || messageStatus == null || messageStatus.size() == 0) {
            return;
        }
        int messageCount = messageInfoList.size();
        int updateCount = 0;
        for (int i = messageCount - 1; i >= 0; i--) {
            ChatMsgBaseInfo message = messageInfoList.get(i);
            long messageId = message.getMsgId();
            if (messageStatus.containsKey(messageId)) {
                message.setMsgState(messageStatus.get(messageId));
                updateCount++;
                messageStatus.remove(messageId);
                if (updateCount == messageStatus.size()) {
                    break;
                }
            }
        }
    }

    @Override
    public void sendVoice(String audioFilePath, int recordTime) {
        // 发送音频
        long messageId = WalkArroundMsgManager.getInstance(getApplicationContext()).sendAudioFile(mRecipientInfo, audioFilePath, recordTime, false,
                0, true);
        transferToDetailView(messageId, false);
//        if (isBurn) {
//            mCurrentMessageEditState = MESSAGE_EDIT_STATE_BURN_AFTER;
//            switchBottomPanelView(mCurrentMessageEditState);
//        }
    }

    /**
     * 发送位置信息
     *
     * @param dLat
     * @param dLng
     * @param strAddr
     * @return
     */
    public long sendLocationInfo(double dLat, double dLng, String strAddr, String imagePath) {
        return WalkArroundMsgManager.getInstance(getApplicationContext()).sendLocation(mRecipientInfo, dLat, dLng, strAddr, imagePath);
    }

    /**
     * 点击文本消息.
     */
    private void onPlainTextClick(ChatMsgBaseInfo cmiMsg) {

    }

    /**
     * Start vcard activity to check details.
     */
//    private void onStartVcardActivity(ChatMsgBaseInfo cmiMsg) {
//        Intent cardIntent = new Intent(this, ContactCardActivity.class);
//        ContactInfo contactInfo;
//        try {
//            contactInfo = restoreContacts(cmiMsg.getData()).get(0);
//            cardIntent.putExtra(ContactCardActivity.INTENT_CONTACT_CARD_INFO, contactInfo);
//            cardIntent.putExtra(ContactCardActivity.INTENT_SHOW_ADD_TO_CONTACT,
//                    cmiMsg.getSendReceive() == MessageSendReceive.MSG_RECEIVE);
//            startActivity(cardIntent);
//        } catch (Exception e) {
//            // 解析文档异常
//            logger.e(e.getMessage());
//        }
//    }

    /**
     * @param message
     * @方法名：onStartPicture
     * @描述：打开浏览图片的Activity
     * @输出：void
     * @作者：史卓君
     */
    private void onStartPicture(ChatMsgBaseInfo message) {
        Intent intent = new Intent(this, ImageViewerActivity.class);
        intent.putExtra(ImageViewerActivity.MESSAGE_ID, message.getMsgId());
        if (message.getDownStatus() == ChatMsgBaseInfo.LOADED) {
            startActivity(intent);
        } else {
            startActivityForResult(intent, REQUEST_CODE_PREVIEW_IMAGE);
        }
    }

    /**
     * 播放音频消息
     *
     * @param message
     */
    private void onStartAudio(ChatMsgBaseInfo message) {
        if (mAudioMediaPlayer == null) {
            mAudioMediaPlayer = new MediaPlayer();
            mAudioMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    muteAudioFocus(false);
                    mediaPlayer.reset();
                    if (mCurrentPlayAudioMsg != null) {
                        mCurrentPlayAudioMsg.setDownStatus(ChatMsgBaseInfo.LOADED);
                        mCurrentPlayAudioMsg = null;
                        if (mMessageListView.getRefreshableView().getTranscriptMode() != AbsListView.TRANSCRIPT_MODE_DISABLED) {
                            mMessageListView.getRefreshableView().setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
                        }
                        mMessageDetailAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        if (mMessageListView.getRefreshableView().getTranscriptMode() != AbsListView.TRANSCRIPT_MODE_DISABLED) {
            mMessageListView.getRefreshableView().setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
        }
        if (mCurrentPlayAudioMsg != null && mAudioMediaPlayer.isPlaying()) {
            // 停止当前播放的语音
            mAudioMediaPlayer.stop();
            mAudioMediaPlayer.reset();
            mCurrentPlayAudioMsg.setDownStatus(ChatMsgBaseInfo.LOADED);
        }
        if (mCurrentPlayAudioMsg != null && mCurrentPlayAudioMsg.getMsgId() == message.getMsgId()
                && mCurrentPlayAudioMsg.getMsgType() == message.getMsgType()
                && mCurrentPlayAudioMsg.isTimeSendMsg() == message.isTimeSendMsg()) {
            // 同一个消息,只停止播放即可
            muteAudioFocus(false);
            mCurrentPlayAudioMsg = null;
            mMessageDetailAdapter.notifyDataSetChanged();
            return;
        }
        mCurrentPlayAudioMsg = message;
        String audioPath = message.getFilepath();
        try {
            muteAudioFocus(true);
            mAudioMediaPlayer.setDataSource(audioPath);
            mAudioMediaPlayer.prepare();
            mAudioMediaPlayer.start();
            mCurrentPlayAudioMsg.setDownStatus(ChatMsgBaseInfo.DOWNLOADING);
        } catch (Exception e) {
            logger.e("onStartAudio() paly audio exception: " + e.getMessage());
        }
        if (!message.getIsRead()) {
            // 未读消息
            int count = WalkArroundMsgManager.getInstance(getApplicationContext()).setMessageRead(message.getMsgId());
            if (count > 0) {
                message.setIsRead(true);
            }
        }
        mMessageDetailAdapter.notifyDataSetChanged();
    }

    /**
     * 禁止或恢复背景音乐
     *
     * @param bMute
     * @return
     */
    public boolean muteAudioFocus(boolean bMute) {
        boolean bool = false;
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        if (bMute) {
            int result = mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            bool = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        } else {
            int result = mAudioManager.abandonAudioFocus(null);
            bool = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }
        return bool;
    }

    /**
     * 点击了视频消息
     *
     * @param message
     */
    private void downVideoOrPlay(ChatMsgBaseInfo message) {
        Intent intent = new Intent(this, PlayVideoActivity.class);
        intent.putExtra(PlayVideoActivity.INTENT_THUMB_PATH, message.getThumbpath());
        intent.putExtra(PlayVideoActivity.INTENT_THUMB_URL_PATH, message.getThumbUrlPath());
        intent.putExtra(PlayVideoActivity.INTENT_MESSAGE_ID, message.getMsgId());
        intent.putExtra(PlayVideoActivity.INTENT_BURN_FLAG, message.isBurnAfterMsg());
        intent.putExtra(PlayVideoActivity.INTENT_IS_COLLECT_MSG, false);
        if (message.isBurnAfterMsg()) {
            startActivityForResult(intent, REQUEST_CODE_PREVIEW_VIDEO);
        } else {
            startActivity(intent);
        }
    }

    /*
     * Start map activity
     */
    void onStartMapActivity(ChatMsgBaseInfo selectChat) {
        mMaskView.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, ShowLocationActivity.class);
        intent.putExtra(LocationActivity.LATITUDE, selectChat.getLatitude());
        intent.putExtra(LocationActivity.LONGITUDE, selectChat.getLongitude());
        intent.putExtra(LocationActivity.ADDRESS, selectChat.getLocationLabel());
        intent.putExtra(LocationActivity.SENDER_OR_RECEIVER, selectChat.getSendReceive());
        intent.putExtra(LocationActivity.THREAD_ID, selectChat.getMsgThreadId());
        startActivityForResult(intent, REQUEST_CODE_SHOW_LOCATION);
    }

    private void onHandleNotifyMsg(ChatMsgBaseInfo chat) {
        if(chat != null) {
            if(chat.getSendReceive() == MessageSendReceive.MSG_RECEIVE && !TextUtils.isEmpty(chat.getExtraInfo())) {
                String[] extraArray = chat.getExtraInfo().split(MessageUtil.EXTRA_INFOR_SPLIT);
                if(extraArray != null && extraArray.length >= 2) {
                    if(extraArray[1].equalsIgnoreCase(MessageUtil.EXTRA_START_2_WALK_REQUEST)) {
                        long msgTime = chat.getTime();
                        long curTime = System.currentTimeMillis();
                        //Validate time is : current time - msg time <= 60s
                        if(curTime - msgTime > 60 * 1000) {
                            Toast.makeText(this, R.string.msg_walk_req_time_out, Toast.LENGTH_SHORT).show();
                        } else {
                            if(mWalkReplyDialog == null) {
                                createWalkReplyDialog();
                                mWalkReplyDialog.show();
                            }
                        }
                    } else if(extraArray[1].equalsIgnoreCase(MessageUtil.EXTRA_START_2_WALK_REPLY_OK)) {
                        long msgTime = chat.getTime();
                        long curTime = System.currentTimeMillis();
                        //Valide time is : current time - msg time <= 60s
                        if(curTime - msgTime > 60 * 1000) {
                            Toast.makeText(this, R.string.msg_walk_req_time_out, Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intentShowDistance = new Intent(BuildMessageActivity.this, CountdownActivity.class);
                            intentShowDistance.putExtra(CountdownActivity.PARAMS_FRIEND_OBJ_ID, mRecipientInfo.getRecipientList().get(0));
                            startActivity(intentShowDistance);
                            BuildMessageActivity.this.finish();
                        }
                    }
                }
            }
        }
    }

    /**
     * 头部右侧更多操作
     *
     * @param anchor 显示的相对位置view
     */
    private void titleContactsMore(View anchor) {
        if (mPopupWindow == null) {
            initPopupView();
        } else if (mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
            return;
        }
        if (mPopupWindowAdapter.getPopupType() != PopupListAdapter.TYPE_MESSAGE_MORE) {
            String[] moreArray = getResources().getStringArray(R.array.message_time_more_menu);
            mPopupWindowAdapter.setDisplayStrList(PopupListAdapter.TYPE_MESSAGE_MORE, Arrays.asList(moreArray));
            mPopupWindowAdapter.notifyDataSetChanged();
        }

        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        int xPosition = location[0] + anchor.getWidth() / 2 - mPopupWindow.getWidth() + CommonUtils.dip2px(this, 10);
        int yPosition = location[1] + anchor.getHeight() - CommonUtils.dip2px(this, 15);
        mPopupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPosition, yPosition);
    }

    /**
     * 创建阅后即焚类型选择
     *
     * @return
     */
//    private PopupWindow createPopupWindow() {
//        View popupContentView = View.inflate(this, R.layout.message_burn_msg_type_view, null);
//        popupContentView.setOnClickListener(this);
//        popupContentView.findViewById(R.id.msg_popup_menu_layout).setOnClickListener(this);
//        popupContentView.findViewById(R.id.burn_msg_type_picture_ll).setOnClickListener(this);
//        popupContentView.findViewById(R.id.burn_msg_type_camera_ll).setOnClickListener(this);
//        popupContentView.findViewById(R.id.burn_msg_type_video_ll).setOnClickListener(this);
//        PopupWindow popupWindow = new PopupWindow(popupContentView, ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT);
//        popupWindow.setBackgroundDrawable(new BitmapDrawable());
//        popupWindow.setOutsideTouchable(true);
//        popupWindow.setFocusable(true);
//        return popupWindow;
//    }

    /**
     * 初始化PopupWindow
     */
    @SuppressWarnings("deprecation")
    private void initPopupView() {
        View popupContentView = View.inflate(this, R.layout.popup_window_view, null);
        ListView popupList = (ListView) popupContentView.findViewById(R.id.popup_list_lv);
        mPopupWindowAdapter = new PopupListAdapter(this, this);
        popupList.setAdapter(mPopupWindowAdapter);
        mPopupWindow = new PopupWindow(popupContentView, getResources().getDimensionPixelSize(
                R.dimen.popup_widow_big_width), ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);
    }

    @Override
    public void popupListItemOnClick(int type, int position) {
        mPopupWindow.dismiss();
        if (type == PopupListAdapter.TYPE_MESSAGE_CALL) {
            //TODO:
//            switch (position) {
//            case CALL_TYPE_NORMAL:
//                // 普通通话
//                CallRelatedUtil.normalCall(this, mRecipientInfo.getRecipientList().get(0));
//                break;
//            case CALL_TYPE_VOICE:
//                // 网络通话
//                ContactInfo contactInfo = new ContactInfo();
//                contactInfo.setFirstName(mRecipientInfo.getDisplayName());
//                contactInfo.setPhoneNumList(mRecipientInfo.getRecipientList());
//                CallRelatedUtil.networkCall(this, contactInfo);
//                break;
//            case CALL_TYPE_VIDEO:
//                // 视频通话
//                ContactInfo contact = new ContactInfo();
//                contact.setFirstName(mRecipientInfo.getDisplayName());
//                contact.setPhoneNumList(mRecipientInfo.getRecipientList());
//                CallRelatedUtil.videoCall(this, contact);
//                break;
//            default:
//                break;
//            }
        } else if (type == PopupListAdapter.TYPE_MESSAGE_MORE) {
            switch (position) {
                case MORE_TYPE_LOOK_CONTACT:
                    //TODO
//                // 查看联系人详情
//                Intent contactDetailIntent = new Intent(this, NewContactsDetailActivity.class);
//                String phone = CommonUtil.getPhoneNum(mRecipientInfo.getRecipientList().get(0));
//                contactDetailIntent.putExtra(NewContactsDetailActivity.INTENT_DATA_PHONE_NUM, phone);
////                contactDetailIntent.putExtra(NewContactsDetailActivity.TYPE_CHOOSE_CARD,
////                        NewContactsDetailActivity.VIEWPAGER_INFORMATION_ITEM);
//                startActivity(contactDetailIntent);
                    break;
                case MORE_TYPE_LOOK_DYNAMIC:
//                // 查看友圈
//                Intent intent = new Intent(this, NewContactsDetailActivity.class);
//                String number = CommonUtil.getPhoneNum(mRecipientInfo.getRecipientList().get(0));
//                intent.putExtra(NewContactsDetailActivity.INTENT_DATA_PHONE_NUM, number);
////                intent.putExtra(NewContactsDetailActivity.TYPE_CHOOSE_CARD,
////                        NewContactsDetailActivity.VIEWPAGER_SOCIAL_INFO_ITEM);
//                startActivity(intent);
                    break;
                case MORE_TYPE_TO_BLACK:
                    // 加入黑名单
//                if (NewContactManager.getInstance(getApplicationContext()).addBlackNumber(mRecipientInfo.getRecipientList().get(0)) == 0) {
//                    Toast.makeText(this, R.string.add_black_success, Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(this, R.string.add_black_failure, Toast.LENGTH_SHORT).show();
//                }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void emojiClick(CharSequence emojiChar) {
        int index = mSendMessageEditView.getSelectionStart();
        if (EMOJI_ITEM_TYPE_DEL_BTN.equals(emojiChar)) {
            // 删除按钮
            if (index > 0) {
                mSendMessageEditView.onKeyDown(KeyEvent.KEYCODE_DEL, new KeyEvent(KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_DEL));
            } else if (mTimeSendView.getVisibility() == View.VISIBLE) {
                mTimeSendView.setText(null);
                mTimeSendView.setVisibility(View.GONE);
            }
        } else if (EmojiParser.getInstance(this).getSmileySpansLength(mSendMessageEditView.getText()) < MAX_INPUT_LENGTH) {
            Editable editable = mSendMessageEditView.getEditableText();
            editable.insert(index, emojiChar);
            Selection.setSelection(editable, index + emojiChar.length());
        } else {
            Toast.makeText(BuildMessageActivity.this, getString(R.string.msg_max_input_length), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        // 从上面加载更多
        if (mMessageListView.getTag() != null && (Boolean) mMessageListView.getTag()) {
            // 加载中
            return;
        }
        mMessageListView.getRefreshableView().setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
        // 加载更多
        loadUpMessageList();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        // 从下面加载更多
        if (mMessageListView.getTag() != null && (Boolean) mMessageListView.getTag()) {
            // 加载中
            return;
        }
        mMessageListView.getRefreshableView().setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
        loadDownMessageList();
    }

    /**
     * 转发消息
     *
     * @param selectedContacts
     */
//    private void forwardSelectedMsg(List<ContactInfo> selectedContacts) {
//        MessageRecipientInfo recipientInfo = new MessageRecipientInfo();
//        recipientInfo.setConversationType(ChatType.CHAT_TYPE_ONE2ONE);
//        List<String> recipient = new ArrayList<String>();
//        recipient.add(selectedContacts.get(0).getPhoneNumList().get(0));
//        recipientInfo.setRecipientList(recipient);
//        long threadId = MessageManager.getService().getConversationId(recipientInfo.getConversationType(), recipient);
//        if (threadId < 0) {
//            threadId = MessageManager.getService().createConversationId(recipientInfo.getConversationType(), recipient);
//        }
//        recipientInfo.setThreadId(threadId);
//        List<ChatMsgBaseInfo> msgList = mMessageDetailAdapter.getSelectedMsgList();
//        boolean hasAudioOrBurnMsg = false;
//        boolean hasUnLoadMsg = false;
//        int successCount = 0;
//        for (ChatMsgBaseInfo message : msgList) {
//            int messageType = message.getMsgType();
//            if (message.isBurnAfterMsg()) {
//                // 阅后即焚不能转发
//                hasAudioOrBurnMsg = true;
//                continue;
//            }
//            String filePath = copyFileToMsgFolder(messageType, message.getFilepath());
//            switch (messageType) {
//            case MessageType.MSG_TYPE_TEXT:
//            case MessageType.MSG_TYPE_SMS:
//                MessageManager.getService().sendPlainText(recipientInfo, message.getData());
//                successCount++;
//                break;
//            case MessageType.MSG_TYPE_CONTACT:
//                MessageManager.getService().sendVCardInfo(recipientInfo, message.getData(), message.getExtraInfo());
//                successCount++;
//                break;
//            case MessageType.MSG_TYPE_AUDIO:
//                // 语音消息不可转发
//                hasAudioOrBurnMsg = true;
//                break;
//            case MessageType.MSG_TYPE_VIDEO:
//                if (message.getSendReceive() == MessageSendReceive.MSG_RECEIVE
//                        && message.getDownStatus() != ChatMsgBaseInfo.LOADED
//                        && filePath == null) {
//                    // 消息还未下载，没有源文件
//                    hasUnLoadMsg = true;
//                } else {
//                    MessageManager.getService().sendVideoFile(recipientInfo, filePath,
//                            message.getDuration(), false);
//                    successCount++;
//                }
//                break;
//            case MessageType.MSG_TYPE_IMAGE:
//                if (message.getSendReceive() == MessageSendReceive.MSG_RECEIVE
//                        && message.getDownStatus() != ChatMsgBaseInfo.LOADED
//                        && filePath == null) {
//                    // 消息还未下载，没有源文件
//                    hasUnLoadMsg = true;
//                } else {
//                    MessageManager.getService().sendImageFiles(recipientInfo, filePath, 100);
//                    successCount++;
//                }
//                break;
//            case MessageType.MSG_TYPE_MAP:
//                MessageManager.getService().sendLocation(recipientInfo, message.getLatitude(), message.getLongitude(),
//                        message.getLocationLabel(), filePath);
//                successCount++;
//                break;
//            default:
//                break;
//            }
//        }
//        if (hasAudioOrBurnMsg) {
//            if (hasUnLoadMsg) {
//                Toast.makeText(this, R.string.connot_forward_msg_notice2, Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, R.string.connot_forward_msg_notice1, Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            if (hasUnLoadMsg) {
//                Toast.makeText(this, R.string.connot_forward_msg_notice2, Toast.LENGTH_SHORT).show();
//            }
//        }
//        if (successCount == 0) {
//            return;
//        }
//        Intent intent = new Intent(this, BuildMessageActivity.class);
//        intent.putExtra(INTENT_RECEIVER_EDITABLE, isReceiverEditable);
//        intent.putExtra(INTENT_CONVERSATION_TYPE, ChatType.CHAT_TYPE_ONE2ONE);
//        String displayName = selectedContacts.get(0).getFirstName();
//        intent.putExtra(INTENT_CONVERSATION_DISPLAY_NAME, displayName);
//        intent.putExtra(INTENT_CONVERSATION_RECEIVER, recipient.get(0));
//        intent.putExtra(INTENT_CONVERSATION_THREAD_ID, threadId);
//        startActivity(intent);
//        mMessageDetailAdapter.setMessageInfo(null);
//        mMessageDetailAdapter.notifyDataSetChanged();
//    }

    /**
     * 拷贝文件
     *
     * @param msgType
     * @param filePath
     * @return
     */
    public static String copyFileToMsgFolder(int msgType, String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        File sourceFile = new File(filePath);
        if (!sourceFile.exists()) {
            return null;
        }
        String subPath = null;
        switch (msgType) {
            case MessageType.MSG_TYPE_MAP:
                subPath = AppConstant.LOCATION_PIC_PATH;
                break;
            case MessageType.MSG_TYPE_IMAGE:
                subPath = AppConstant.CAMERA_TAKE_PIC_PATH;
                break;
            case MessageType.MSG_TYPE_VIDEO:
                subPath = AppConstant.VIDEO_FILE_PATH;
                break;
            default:
                break;
        }
        if (subPath == null) {
            return null;
        }
        StringBuilder fileName = new StringBuilder();
        fileName.append(WalkArroundApp.MTC_DATA_PATH);
        fileName.append(subPath);
        File folder = new File(fileName.toString());
        if (!folder.exists()) {
            folder.mkdirs();
        }
        fileName.append(System.currentTimeMillis());
        int dot = filePath.lastIndexOf('.');
        if ((dot > -1) && (dot < (filePath.length() - 1))) {
            fileName.append(".");
            fileName.append(filePath.substring(dot + 1));
        }
        String newFilePath = fileName.toString();

        File collectFile = new File(newFilePath);
//        try {
//            FavoriteMessageManager.copyFile(sourceFile, collectFile);
//        } catch (IOException e) {
//            return null;
//        }
        return newFilePath;
    }

    /**
     * 跳转到聊天详情页面
     */
    public void showChatDetail() {
        Intent intent = new Intent(this, ChatDetailActivity.class);
        intent.putExtra(INTENT_CONVERSATION_GROUP_ID, mRecipientInfo.getGroupId());
        intent.putExtra(INTENT_CONVERSATION_TYPE, mRecipientInfo.getConversationType());
        intent.putExtra(INTENT_CONVERSATION_THREAD_ID, mRecipientInfo.getThreadId());
        intent.putExtra(INTENT_CONVERSATION_DISPLAY_NAME, mRecipientInfo.getDisplayName());
        intent.putExtra(INTENT_CONVERSATION_GROUP_ID, mRecipientInfo.getGroupId());
        intent.putExtra(INTENT_CONVERSATION_RECEIVER, (Serializable) mRecipientInfo.getRecipientList());
        startActivityForResult(intent, REQUEST_CODE_CHAT_DETAIL);
    }

    /**
     * 初始化传感器
     */
    private void initSensor() {
        if (mSensorManager != null) {
            return;
        }
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * 取消传感器监听
     */
    private void releaseSensor() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
            mSensorManager = null;
            // 恢复正常模式
            if (mAudioManager != null) {
                mAudioManager.setMode(AudioManager.MODE_NORMAL);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        if (mAudioMediaPlayer == null || mAudioManager.isWiredHeadsetOn()) {
            // 只有在语音播放时有效
            return;
        }
        float range = sensorEvent.values[0];
        if (Math.abs(range - mSensor.getMaximumRange()) < 0.01) {
            // 正常模式
            mAudioManager.setMode(AudioManager.MODE_NORMAL);
        } else {
            // 听筒模式
            mAudioManager.setMode(AudioManager.MODE_IN_CALL);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void updateHeaderAreaOnRecMsg(ChatMsgBaseInfo msg) {
        if (msg.getMsgType() == MessageType.MSG_TYPE_NOTIFICATION) {
            logger.d("Get notification msg with extra infor.");
            if(!TextUtils.isEmpty(msg.getExtraInfo())) {
                String[] extraArray = msg.getExtraInfo().split(MessageUtil.EXTRA_INFOR_SPLIT);
                if(extraArray != null && extraArray.length >= 2 && !TextUtils.isEmpty(extraArray[0])) {
                    if(extraArray[1].equalsIgnoreCase(MessageUtil.EXTRA_START_2_WALK_REQUEST)) {
                        //Show dialog directly if activity on foreground
                        if(mWalkReplyDialog == null && isActivityOnForground) {
                            createWalkReplyDialog();
                            mWalkReplyDialog.show();
                        }
                    } else if(extraArray[1].equalsIgnoreCase(MessageUtil.EXTRA_START_2_WALK_REPLY_OK)) {

                    } else if(extraArray[1].equalsIgnoreCase(MessageUtil.EXTRA_START_2_WALK_REPLY_NEXT_TIME)) {

                    } else if(extraArray[0].equalsIgnoreCase(MessageUtil.EXTRA_AGREEMENT_2_WALKARROUND)) {
                        //Agree to walk, build msg UI add distance button.
                        int color = MessageUtil.getFriendColor(Integer.parseInt(extraArray[1]));
                        logger.d("EXTRA_AGREEMENT_2_WALKARROUND with color : " + color);
                        if (color > 0) {
                            mImvDistance.setVisibility(View.VISIBLE);
                            mImvDistance.setImageResource(color);
                        }
                    }
                }
            }
        }
    }

    /*
     * Create a dialog for user to select.
     */
    private void createWalkReplyDialog() {
        mWalkReplyDialog = DialogFactory.getStart2WalkReplyDialog(this, mRecipientInfo.getRecipientList().get(0), new DialogFactory.NoticeDialogCancelClickListener() {
            @Override
            public void onNoticeDialogCancelClick() {
                String extraInfor = MessageUtil.EXTRA_START_2_WALKARROUND +
                        MessageUtil.EXTRA_INFOR_SPLIT +
                        MessageUtil.EXTRA_START_2_WALK_REPLY_NEXT_TIME;
                WalkArroundMsgManager.getInstance(getApplicationContext()).sendTextMsg(mRecipientInfo.getRecipientList().get(0),
                        getString(R.string.agree_2_walk_face_2_face_req), extraInfor);

                mWalkReplyDialog.dismiss();
                mWalkReplyDialog = null;
            }

            @Override
            public void onNoticeDialogConfirmClick(boolean isChecked, Object value) {
                String extraInfor = MessageUtil.EXTRA_START_2_WALKARROUND +
                        MessageUtil.EXTRA_INFOR_SPLIT +
                        MessageUtil.EXTRA_START_2_WALK_REPLY_OK;
                WalkArroundMsgManager.getInstance(getApplicationContext()).sendTextMsg(mRecipientInfo.getRecipientList().get(0),
                        getString(R.string.agree_2_walk_face_2_face_req), extraInfor);

                mWalkReplyDialog.dismiss();
                mWalkReplyDialog = null;

                Intent intent = new Intent(BuildMessageActivity.this, CountdownActivity.class);
                intent.putExtra(CountdownActivity.PARAMS_FRIEND_OBJ_ID, mRecipientInfo.getRecipientList().get(0));
                BuildMessageActivity.this.startActivity(intent);
            }
        });
    }
}
