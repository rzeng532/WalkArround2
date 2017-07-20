package com.example.walkarround.message.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import com.example.walkarround.R;
import com.example.walkarround.base.view.DialogFactory;
import com.example.walkarround.message.manager.WalkArroundMsgManager;
import com.example.walkarround.message.util.MessageConstant.ChatType;
import com.example.walkarround.message.util.MsgBroadcastConstants;
import com.example.walkarround.util.CheckSwitchButton;
import com.example.walkarround.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatDetailActivity extends Activity implements View.OnClickListener, CheckSwitchButton.OnCheckedChangeListener,
        DialogFactory.NoticeDialogClickListener {

    public static final int RESULT_CODE_CHAT_LOG_CLEARED = 22;

    private static Logger logger = Logger.getLogger(ChatDetailActivity.class.getSimpleName());

    private static final int REQUEST_CODE_SET_MY_ALIAS = 111;
    private static final int REQUEST_CODE_SET_GROUP_NAME = 122;

    private static final int MAX_GROUP_NAME_LENGTH = 16;
    private static final int MAX_ALIAS_NAME_LENGTH = 32;
    /* 添加收信人 */
    private static final int REQUEST_CODE_SELECT_CONTACTS_WITH_NUM = 1;

    /* 收信人信息 */
    private int mOldConversationType;
    private int mConversationType;
    private long mCurrentThreadId;
    private String mCurrentConversationId;
    private String mCurrentGroupId;
    private String mCurrentGroupName;
    private String mMyAlias;

    private ArrayList<String> mRecipientList = new ArrayList<String>();

    private boolean isDeleteMode = false;
    private boolean isNotifyMe = false;
    private boolean isChairmanOfGroup = false;

    private View mExtraViewPanel;

    private CheckSwitchButton swAddBlack;
    private CheckSwitchButton swNotifyMessage;
    private TextView tvSetGroupName;
    private TextView tvSetMyAlias;
    private HashMap<String, String> mNickNameMap = new HashMap<String, String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);
        initData(getIntent());
        initView();
        IntentFilter notifyFilter = new IntentFilter();
        notifyFilter.addAction(MsgBroadcastConstants.ACTION_GROUP_INFO_CHANGED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mConversationType == ChatType.CHAT_TYPE_ONE2ONE) {
            View rlViewMoments = findViewById(R.id.rl_msg_detail_view_moments);
            rlViewMoments.setVisibility(View.VISIBLE);
            //swAddBlack.setChecked(isBlack(mRecipientList.get(0)));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        findViewById(R.id.back_tv).setOnClickListener(this);
        // 成员
        ListView gvRecipient = (ListView) findViewById(R.id.msg_chat_recipient_lv);
        View extraView = initViewFromGroupType(mConversationType);
        gvRecipient.addFooterView(extraView);

        //gvRecipient.setAdapter(mAdapter);
    }

    /**
     * 根据聊天类型设置UI
     *
     * @param groupType
     */
    private View initViewFromGroupType(int groupType) {
        if (mExtraViewPanel == null) {
            mExtraViewPanel = View.inflate(this, R.layout.chat_detail_extra_info, null);
        }
        View llMenuOne2One = mExtraViewPanel.findViewById(R.id.msg_chat_detail_menu_one2one);
        switch (groupType) {
            case ChatType.CHAT_TYPE_ONE2ONE:
                if (llMenuOne2One == null) {
                    ViewStub view = (ViewStub) mExtraViewPanel.findViewById(R.id.one_to_one_chat_vs);
                    view.inflate();
                    llMenuOne2One = mExtraViewPanel.findViewById(R.id.msg_chat_detail_menu_one2one);
                } else {
                    llMenuOne2One.setVisibility(View.VISIBLE);
                }

                CheckSwitchButton swSetOne2OneTop = (CheckSwitchButton) llMenuOne2One.findViewById(R.id.sw_set_one2one_top);
                swSetOne2OneTop.setOnCheckedChangeListener(this);
                swSetOne2OneTop.setChecked(isTopMsgByThreadId(mCurrentThreadId));
                View rlViewMoments = llMenuOne2One.findViewById(R.id.rl_msg_detail_view_moments);
                rlViewMoments.setOnClickListener(this);
                View rlClearChatLogs = llMenuOne2One.findViewById(R.id.rl_msg_detail_clear_chat_logs);
                rlClearChatLogs.setOnClickListener(this);
                //swAddBlack = (CheckSwitchButton) llMenuOne2One.findViewById(R.id.sw_add_black);
                //swAddBlack.setOnCheckedChangeListener(this);
                break;

            default:
                if (llMenuOne2One != null) {
                    llMenuOne2One.setVisibility(View.GONE);
                }

                break;
        }
        return mExtraViewPanel;
    }

    /**
     * 检查对方是否被拉黑
     */
    private boolean isBlack(String recipient) {
        return false;
    }

    /**
     * 检查消息是否置顶
     */
    private boolean isTopMsgByThreadId(long mCurrentThreadId) {
        return WalkArroundMsgManager.getInstance(this).isTopMessage(mCurrentThreadId);
    }

    private void initData(Intent intent) {
        mConversationType = intent.getIntExtra(BuildMessageActivity.INTENT_CONVERSATION_TYPE,
                ChatType.CHAT_TYPE_ONE2ONE);
        mOldConversationType = mConversationType;
        mRecipientList = (ArrayList<String>) intent
                .getSerializableExtra(BuildMessageActivity.INTENT_CONVERSATION_RECEIVER);
        mCurrentThreadId = intent.getLongExtra(BuildMessageActivity.INTENT_CONVERSATION_THREAD_ID,
                BuildMessageActivity.CONVERSATION_DEFAULT_THREAD_ID);
    }

    /**
     * 清空聊天记录
     */
    public void clearChatLog() {
        Dialog noticeDialog = DialogFactory.getNoticeDialog(ChatDetailActivity.this,
                getResources().getString(R.string.clear_chat_log_hint), new DialogFactory.NoticeDialogClickListener() {

                    @Override
                    public void onNoticeDialogConfirmClick(boolean isChecked, Object value) {
                        WalkArroundMsgManager.getInstance(ChatDetailActivity.this).deleteThreadMessages(getBaseContext(), mCurrentThreadId,
                                mRecipientList.get(0));
                        setResult(RESULT_CODE_CHAT_LOG_CLEARED);
                        finish();
                    }
                }, null
        );
        noticeDialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_tv:
                onBackPressed();
                break;
            case R.id.rl_msg_detail_clear_chat_logs:
                clearChatLog();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mOldConversationType == ChatType.CHAT_TYPE_ONE2ONE && mConversationType == ChatType.CHAT_TYPE_GROUP) {
            Intent intent = new Intent(this, BuildMessageActivity.class);
            intent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_RECEIVER, mRecipientList);
            intent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_ID, mCurrentConversationId);
            intent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_GROUP_ID, mCurrentGroupId);
            intent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_THREAD_ID, mCurrentThreadId);
            intent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_TYPE, ChatType.CHAT_TYPE_GROUP);
            intent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_DISPLAY_NAME, mCurrentGroupName);
            intent.putExtra(BuildMessageActivity.INTENT_RECEIVER_EDITABLE, false);
            startActivity(intent);
            setResult(RESULT_CANCELED);
        } else if (mConversationType == ChatType.CHAT_TYPE_ONE2ONE) {
            setResult(RESULT_CANCELED);
        } else {
            Intent intent = new Intent();
            intent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_RECEIVER, mRecipientList);
            intent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_DISPLAY_NAME, mCurrentGroupName);
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sw_set_one2one_top:
                if (isTopMsgByThreadId(mCurrentThreadId) == isChecked) {
                    break;
                }
                if (mCurrentThreadId < 0) {
                    mCurrentThreadId = WalkArroundMsgManager.getInstance(this).createConversationId(mConversationType, mRecipientList);
                    if (mCurrentThreadId < 0 && isChecked) {
                        mCurrentThreadId = WalkArroundMsgManager.getInstance(this).createConversationId(mConversationType, mRecipientList);
                    }
                }
                WalkArroundMsgManager.getInstance(this).setOrCancelTopMessage(this, mCurrentThreadId, mRecipientList.get(0), isChecked);
                long threadId = WalkArroundMsgManager.getInstance(this).getConversationId(mConversationType, mRecipientList);
                if (threadId != mCurrentThreadId) {
                    mCurrentThreadId = threadId;
                }
                break;
            default:
                break;
        }
    }

    /**
     * 设置是否为删除群成员／群发对象模式
     */
    private void setDeleteReceiverMode(boolean isDelete) {
        isDeleteMode = isDelete;
    }

    @Override
    public void onNoticeDialogConfirmClick(boolean isChecked, Object value) {
        // 确定退出群聊
    }
}