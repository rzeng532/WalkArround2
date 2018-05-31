/**
 * Copyright (C) 2014-2015 All rights reserved
 */
package com.awalk.walkarround.message.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.avos.avoscloud.AVAnalytics;
import com.awalk.walkarround.R;
import com.awalk.walkarround.assistant.AssistantHelper;
import com.awalk.walkarround.base.task.TaskUtil;
import com.awalk.walkarround.base.view.DialogFactory;
import com.awalk.walkarround.main.parser.WalkArroundJsonResultParser;
import com.awalk.walkarround.main.task.QuerySpeedDateIdTask;
import com.awalk.walkarround.message.adapter.BaseConversationListAdapter;
import com.awalk.walkarround.message.adapter.ConversationListAdapter;
import com.awalk.walkarround.message.listener.ConversationItemListener;
import com.awalk.walkarround.message.manager.WalkArroundMsgManager;
import com.awalk.walkarround.message.model.MessageSessionBaseModel;
import com.awalk.walkarround.message.task.AsyncTaskLoadSession;
import com.awalk.walkarround.message.task.AsyncTaskOperation;
import com.awalk.walkarround.message.task.CancelSpeedDateTask;
import com.awalk.walkarround.message.util.MessageConstant;
import com.awalk.walkarround.message.util.MessageConstant.ConversationType;
import com.awalk.walkarround.message.util.MessageUtil;
import com.awalk.walkarround.message.util.MsgBroadcastConstants;
import com.awalk.walkarround.message.util.SessionComparator;
import com.awalk.walkarround.myself.manager.ProfileManager;
import com.awalk.walkarround.util.Logger;
import com.awalk.walkarround.util.http.HttpTaskBase;
import com.awalk.walkarround.util.http.HttpUtil;
import com.awalk.walkarround.util.http.ThreadPoolManager;
import com.awalk.walkarround.util.network.NetWorkManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.awalk.walkarround.util.http.HttpTaskBase.TaskResult;
import static com.awalk.walkarround.util.http.HttpTaskBase.onResultListener;

public class ConversationActivity extends Activity implements ConversationItemListener,
        OnClickListener {

    /* 批操作event */
    private static final int MSG_OPERATION_LOAD_SUCCESS = 5;
    private static final int MSG_OPERATION_NOT_SUCCEED = 101;

    /* cancel speed date event */
    private static final int MSG_CANCEL_SPEED_DATE_OK = 102;
    private static final int MSG_CANCEL_SPEED_DATE_FAIL = 103;

    private static final String MSG_EVENT_EXTRA_LIST = "listData";
    private static final String MSG_OPERATION_KEY_REQUEST = "msg_operation_key_request";

    private Context mContext;

    private static final Logger logger = Logger.getLogger(ConversationActivity.class.getSimpleName());

    /*适配器，用于显示消息的Thread列表*/
    private ListView mConversationListView;
    private ConversationListAdapter mConversationAdapter;

    private ImageView mIvOldFriends;
    private ImageView mIvOldFriendUnread;
    /*没有内容时显示的空页面*/
    private View mNoConversationView;

    private View mNetStatusView;

    /*批操作异步任务*/
    private AsyncTaskOperation mTaskOperation;
    private Dialog mLoadingDialog;
    /* 新建消息/群组PopupWindow */
    // PopupWindow mPopupWindow;
    //private PopupListAdapter mPopupWindowAdapter;

    public static final String CONV_PARAM_OLD_FRIEND = "conv_param_old_friend";
    public static final int CONV_TYPE_CUR_FRIEND = 0;
    public static final int CONV_TYPE_OLD_FRIEND = 1;
    private int mConvType = CONV_TYPE_CUR_FRIEND;
    private boolean mIsThereOldFriend = false;
    private int mOldFriendUnreadCound = 0;

    /**
     * 消息状态监听
     */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MsgBroadcastConstants.ACTION_MESSAGE_NEW_RECEIVED.equals(action)) {
                // 新到消息
                onReceiverRscMsg(context, intent);
            } else if (BuildMessageActivity.ACTION_NOTIFY_CONVERSATION_REFRESH.equals(action)) {
                // 进入了聊天页面，更新item信息
                long threadId = intent.getLongExtra(BuildMessageActivity.CONVERSATION_REFRESH_ID,
                        BuildMessageActivity.CONVERSATION_DEFAULT_THREAD_ID);
                sessionInfoChanged(threadId);
            } else if (MsgBroadcastConstants.ACTION_MESSAGE_STATUS_CHANGED.equals(action)) {
                // 消息状态变化
                onMsgStatusChanged(intent);
            }
        }
    };

    private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isAvailable = NetWorkManager.getInstance(mContext).isNetworkAvailable();
            if (isAvailable) {
                mNetStatusView.setVisibility(View.GONE);
            } else {
                // 网络不可用
                mNetStatusView.setVisibility(View.VISIBLE);
            }
        }
    };

    private HttpTaskBase.onResultListener mGetSpeedIdTaskListener = new HttpTaskBase.onResultListener() {
        @Override
        public void onPreTask(String requestCode) {

        }

        @Override
        public void onResult(Object object, HttpTaskBase.TaskResult resultCode, String requestCode, String threadId) {
            //Task success.
            if (HttpTaskBase.TaskResult.SUCCEESS == resultCode && requestCode.equalsIgnoreCase(HttpUtil.HTTP_FUNC_QUERY_SPEED_DATE)) {
                String strSpeedDateId = WalkArroundJsonResultParser.parseRequireCode((String) object, HttpUtil.HTTP_RESPONSE_KEY_OBJECT_ID);
                if (!TextUtils.isEmpty(strSpeedDateId)) {
                    ProfileManager.getInstance().getMyProfile().setSpeedDateId(strSpeedDateId);
                } else {
                    logger.d("Get speed date id OK but data is EMPTY.");
                }
            } else {
                logger.d("Failed to get speed date id!!!");
            }
        }

        @Override
        public void onProgress(int progress, String requestCode) {

        }
    };


    /**
     * 取消Speed data 在服务器中的状态。
     */
    private HttpTaskBase.onResultListener mCancelSpeedDateTaskListener = new HttpTaskBase.onResultListener() {
        @Override
        public void onPreTask(String requestCode) {
        }

        @Override
        public void onResult(Object object, HttpTaskBase.TaskResult resultCode, String requestCode, String threadId) {
            //Task success.
            if (HttpTaskBase.TaskResult.SUCCEESS == resultCode && requestCode.equalsIgnoreCase(HttpUtil.HTTP_FUNC_CANCEL_SPEED_DATE)) {
                //取消匹配后将自身状态设置为为初始状态
                ProfileManager.getInstance().setCurUsrDateState(MessageUtil.WalkArroundState.STATE_INIT);
                mUIHandler.sendEmptyMessage(MSG_CANCEL_SPEED_DATE_OK);
            } else if (HttpTaskBase.TaskResult.SUCCEESS != resultCode && requestCode.equalsIgnoreCase(HttpUtil.HTTP_FUNC_CANCEL_SPEED_DATE)) {
                mUIHandler.sendEmptyMessage(MSG_CANCEL_SPEED_DATE_FAIL);
                Toast.makeText(ConversationActivity.this, R.string.err_del_speed_date_fail, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onProgress(int progress, String requestCode) {

        }
    };


    /**
     * 收到Rcs信息
     *
     * @param intent
     */
    private void onReceiverRscMsg(Context context, Intent intent) {
        //int msgType = intent.getIntExtra(MsgBroadcastConstants.BC_VAR_MSG_TYPE, 0);
        //String extraInfo = intent.getStringExtra(MsgBroadcastConstants.BC_VAR_MSG_EXTRA);

        long threadId = intent.getLongExtra(MsgBroadcastConstants.BC_VAR_THREAD_ID,
                BuildMessageActivity.CONVERSATION_DEFAULT_THREAD_ID);
        //String contact = intent.getStringExtra(MsgBroadcastConstants.BC_VAR_CONTACT);

        sessionInfoChanged(threadId);
    }


    /**
     * 消息状态变化了
     *
     * @param intent
     */
    private void onMsgStatusChanged(Intent intent) {
        long threadId = intent.getLongExtra(MsgBroadcastConstants.BC_VAR_THREAD_ID, -1);
        sessionInfoChanged(threadId);
    }

    /**
     * 用于更新UI
     */
    private Handler mUIHandler = new Handler() {
        public void handleMessage(Message msg) {
            dismissCircleDialog();
            Bundle data = msg.getData();
            switch (msg.what) {
                case MSG_OPERATION_NOT_SUCCEED:
                    queryConversationList();
                    break;
                case MSG_OPERATION_LOAD_SUCCESS:
                    List<MessageSessionBaseModel> conversationList = (List<MessageSessionBaseModel>) data.getSerializable(MSG_EVENT_EXTRA_LIST);
                    mConversationAdapter.setListData(conversationList);
                    if (mConvType == CONV_TYPE_CUR_FRIEND && mIsThereOldFriend) {
                        mIvOldFriends.setVisibility(View.VISIBLE);
                        if (mOldFriendUnreadCound > 0) {
                            mIvOldFriends.setVisibility(View.VISIBLE);
                            mIvOldFriendUnread.setVisibility(View.VISIBLE);
                        } else {
                            mIvOldFriendUnread.setVisibility(View.GONE);
                        }
                    } else {
                        mIvOldFriends.setVisibility(View.GONE);
                        mIvOldFriendUnread.setVisibility(View.GONE);
                    }

                    mConversationAdapter.notifyDataSetChanged();
                    break;
                case MSG_CANCEL_SPEED_DATE_OK:
                    logger.d("Cancel speed date ok");
                    break;
                case MSG_CANCEL_SPEED_DATE_FAIL:
                    logger.d("Cancel speed date fail");
                    break;
                default:
                    break;
            }
            if (mConversationAdapter.getCount() == ConversationListAdapter.FIXED_ENTRANCES) {
                mConversationListView.addFooterView(mNoConversationView);
            } else {
                mConversationListView.removeFooterView(mNoConversationView);
            }
        }

    };

    /**
     * 异步操作结果callback
     */
    private onResultListener mAsysResultListener = new onResultListener() {
        @Override
        public void onResult(Object object, TaskResult resultCode, String requestCode, String threadId) {
            Bundle dataBundle = new Bundle();
            int what = -1;
            logger.d("onResultListener, onResult, resultCode = " + resultCode);
            if (resultCode == TaskResult.FAILED || resultCode == TaskResult.ERROR) {
                if (!requestCode.equals(MessageConstant.MSG_OPERATION_ADD_BLACKLIST)) {
                    what = MSG_OPERATION_NOT_SUCCEED;
                }
            } else {
                if (requestCode.equals(MessageConstant.MSG_OPERATION_LOAD)) {
                    what = MSG_OPERATION_LOAD_SUCCESS;
                    List<MessageSessionBaseModel> conversationMsgList = (List<MessageSessionBaseModel>) object;

                    Iterator<MessageSessionBaseModel> it = conversationMsgList.iterator();
                    mOldFriendUnreadCound = 0;
                    while (it.hasNext()) {
                        MessageSessionBaseModel item = it.next();

                        if (item.getContact().equals(AssistantHelper.ASSISTANT_OBJ_ID)) {
                            ;
                        } else if (mConvType == CONV_TYPE_OLD_FRIEND
                                && (item.status > MessageUtil.WalkArroundState.STATE_INIT && item.status <= MessageUtil.WalkArroundState.STATE_END_IMPRESSION)) {
                            //Current UI need old friends, so we remove !Old friends.
                            it.remove();
                        } else if (mConvType == CONV_TYPE_CUR_FRIEND
                                && (item.status <= MessageUtil.WalkArroundState.STATE_INIT || item.status > MessageUtil.WalkArroundState.STATE_END_IMPRESSION)) {
                            //Current UI need cur friend, we remove old one
                            mIsThereOldFriend = true;
                            mOldFriendUnreadCound += item.unReadCount;
                            it.remove();
                        }
                    }

                    dataBundle.putSerializable(MSG_EVENT_EXTRA_LIST, (Serializable) conversationMsgList);
                }
            }
            mUIHandler.removeMessages(what);
            Message msg = mUIHandler.obtainMessage(what);
            dataBundle.putString(MSG_OPERATION_KEY_REQUEST, requestCode);
            msg.setData(dataBundle);
            mUIHandler.sendMessageDelayed(msg, 1000);
        }

        @Override
        public void onPreTask(String requestCode) {
        }

        @Override
        public void onProgress(final int progress, String requestCode) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_conversation);
        mContext = getApplicationContext();
        mConvType = getIntent().getIntExtra(CONV_PARAM_OLD_FRIEND, CONV_TYPE_CUR_FRIEND);

        initView();

        //Check 是否有朋友还处于未评价状态
        if (checkIsTherePopImpressionOne()) {
            return;
        }

        //初始化数据
        initData();
        logger.d("onCreate end.");
    }

    public void initView() {
        // 初始化View
        findView();
        // 注册监听
        registerListener();
        showCircleDialog();
    }

    private void initData() {
        //There is user id and there is NO speed date id. We get speed date here and set value to profile.
        if (!TextUtils.isEmpty(ProfileManager.getInstance().getCurUsrObjId())
                && TextUtils.isEmpty(ProfileManager.getInstance().getSpeedDateId())) {
            //Check speed date id
            ThreadPoolManager.getPoolManager().addAsyncTask(new QuerySpeedDateIdTask(getApplicationContext(),
                    mGetSpeedIdTaskListener,
                    HttpUtil.HTTP_FUNC_QUERY_SPEED_DATE,
                    HttpUtil.HTTP_TASK_QUERY_SPEED_DATE,
                    QuerySpeedDateIdTask.getParams(ProfileManager.getInstance().getCurUsrObjId()),
                    TaskUtil.getTaskHeader()));
        }
    }

    private boolean checkIsTherePopImpressionOne() {
        List<MessageSessionBaseModel> listPopImp = WalkArroundMsgManager.getInstance(getApplicationContext())
                .getLocalPopImpressionConv();
        if (listPopImp != null && listPopImp.size() > 0) {
            Intent evaItent = new Intent(this, EvaluateActivity.class);
            evaItent.putExtra(EvaluateActivity.PARAMS_FRIEND_OBJ_ID, listPopImp.get(0).getContact());
            startActivity(evaItent);

            return true;
        }

        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mMessageReceiver) {
            unregisterReceiver(mMessageReceiver);
            mMessageReceiver = null;
        }
        if (null != mNetworkReceiver) {
            unregisterReceiver(mNetworkReceiver);
            mNetworkReceiver = null;
        }

        mUIHandler.removeMessages(MSG_OPERATION_LOAD_SUCCESS);
        mUIHandler.removeMessages(MSG_OPERATION_NOT_SUCCEED);

    }

    @Override
    public void onResume() {
        super.onResume();
        AVAnalytics.onResume(this);
        // 加载数据
        ThreadPoolManager.getPoolManager().addAsyncTask(
                new AsyncTaskLoadSession(mContext, MessageConstant.MSG_OPERATION_LOAD,
                        0, Integer.MAX_VALUE, mAsysResultListener)
        );

        mConversationAdapter.notifyDataSetChanged();

        View maskView = findViewById(R.id.page_mask_v);
        if (maskView.getVisibility() == View.VISIBLE) {
            maskView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        AVAnalytics.onPause(this);
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.back_rl:
                this.finish();
                break;
            case R.id.iv_old_friends:
                Intent oldFriend = new Intent(this, ConversationActivity.class);
                oldFriend.putExtra(CONV_PARAM_OLD_FRIEND, CONV_TYPE_OLD_FRIEND);
                startActivity(oldFriend);
                break;
            default:
                break;
        }
    }

    private void deleteConvMsg(MessageSessionBaseModel delItem) {

        if (delItem == null) {
            mAsysResultListener.onResult(null, TaskResult.FAILED, MessageConstant.MSG_OPERATION_REMOVE, null);
        }

        List<MessageSessionBaseModel> list = new ArrayList<>();
        list.add(delItem);

        mTaskOperation = new AsyncTaskOperation(mContext,
                MessageConstant.MSG_OPERATION_REMOVE, list,
                mAsysResultListener);
        ThreadPoolManager.getPoolManager().addAsyncTask(mTaskOperation);

        showCircleDialog();
    }

    /**
     * 注册消息监听
     */
    private void registerListener() {
        // 收到新的message消息
        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(MsgBroadcastConstants.ACTION_MESSAGE_NEW_RECEIVED);
        // 群聊通知消息
        commandFilter.addAction(MsgBroadcastConstants.ACTION_GROUP_MESSAGE_NEW_RECEIVED);
        commandFilter.addAction(MsgBroadcastConstants.ACTION_GROUP_INFO_CHANGED);
        commandFilter.addAction(MsgBroadcastConstants.ACTION_GROUP_CREATE_ERROR);
        commandFilter.addAction(MsgBroadcastConstants.ACTION_GROUP_CREATE);
        commandFilter.addAction(BuildMessageActivity.ACTION_NOTIFY_CONVERSATION_REFRESH);
        commandFilter.addAction(MsgBroadcastConstants.ACTION_MESSAGE_STATUS_CHANGED);
        commandFilter.addAction(MsgBroadcastConstants.ACTION_GROUP_INVITATION);

        registerReceiver(mMessageReceiver, commandFilter);

        // 网络状态监听
        IntentFilter networkFilter = new IntentFilter();
        networkFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkReceiver, networkFilter);
    }

    /**
     * 查询非通知会话
     */
    private void queryConversationList() {
        ThreadPoolManager.getPoolManager().addAsyncTask(
                new AsyncTaskLoadSession(mContext,
                        MessageConstant.MSG_OPERATION_LOAD, 0, 0, mAsysResultListener)
        );
    }

    /**
     * 会话内容有变化
     *
     * @param threadId 会话Id
     */
    private void sessionInfoChanged(long threadId) {
        MessageSessionBaseModel result = WalkArroundMsgManager.getInstance(mContext).getSessionByThreadId(threadId);
        if (result == null) {
            // 会话已经不存在
            mConversationAdapter.deleteItemData(threadId);
        } else {
            BaseConversationListAdapter adapter = mConversationAdapter;
            MessageSessionBaseModel item = adapter.findItemData(result.getThreadId());
            if (item == null) {
                item = mConversationAdapter.findItemData(result.getThreadId());
            }
            if (item == null) {
                // 新到会话
                if (mConvType == CONV_TYPE_CUR_FRIEND) {
                    //Update old friend flag & return;
                    mIvOldFriends.setVisibility(View.VISIBLE);
                    mIvOldFriendUnread.setVisibility(View.VISIBLE);
                    return;
                }

                if (adapter instanceof ConversationListAdapter) {
                    adapter.addListData(result);
                    adapter.sortListData(SessionComparator.TIME_DESC);
                    adapter.sortListData(SessionComparator.STATUS_DESC);
                    adapter.sortListData(SessionComparator.TOP_DESC);
                    adapter.notifyDataSetChanged();
                }
            } else if (item.getTop() == result.getTop()) {
                adapter.updateItemData(item, result);
                adapter.sortListData(SessionComparator.TIME_DESC);
                if (adapter instanceof ConversationListAdapter) {
                    adapter.sortListData(SessionComparator.STATUS_DESC);
                    adapter.sortListData(SessionComparator.TOP_DESC);
                }
                adapter.notifyDataSetChanged();
            } else {
                if (result.getTop() == MessageConstant.TopState.NOT_TOP) {
                    // 消息消顶
                    if (item.getItemType() == ConversationType.NOTICES_MSG) {
                        // 通知消息
                        mConversationAdapter.deleteItemData(item);
                        mConversationAdapter.notifyDataSetChanged();
                    } else {
                        // 普通消息消顶
                        adapter.updateItemData(item, result);
                        adapter.sortListData(SessionComparator.TIME_DESC);
                        if (adapter instanceof ConversationListAdapter) {
                            adapter.sortListData(SessionComparator.STATUS_DESC);
                            adapter.sortListData(SessionComparator.TOP_DESC);
                        }
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    // 消息置顶
                    if (item.getItemType() == ConversationType.NOTICES_MSG) {
                        // 通知类消息置顶
                        mConversationAdapter.addListData(result);
                    } else {
                        mConversationAdapter.updateItemData(item, result);
                    }
                    mConversationAdapter.sortListData(SessionComparator.TIME_DESC);
                    mConversationAdapter.sortListData(SessionComparator.STATUS_DESC);
                    mConversationAdapter.sortListData(SessionComparator.TOP_DESC);
                    mConversationAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    /**
     * 初始化基本View
     *
     * @param
     */
    private void findView() {

        //Title
        View title = findViewById(R.id.title);
        title.findViewById(R.id.back_rl).setOnClickListener(this);
        title.findViewById(R.id.more_rl).setVisibility(View.GONE);
        title.findViewById(R.id.middle_iv).setVisibility(View.VISIBLE);

        if (mConvType == CONV_TYPE_CUR_FRIEND) {
            ((ImageView) title.findViewById(R.id.middle_iv)).setImageResource(R.drawable.icon_conversation_title);
        } else {
            ((ImageView) title.findViewById(R.id.middle_iv)).setImageResource(R.drawable.grey_diag);
        }

        title.findViewById(R.id.display_name).setVisibility(View.GONE);

        // 网络提示
        mNetStatusView = findViewById(R.id.network_status_rl);

        // 会话列表
        mConversationListView = (ListView) findViewById(R.id.converstaion_list);
        mNoConversationView = LayoutInflater.from(this).inflate(R.layout.message_conversation_listview_empty, null);
        mNoConversationView.findViewById(R.id.tv_why_no_conversations).setOnClickListener(this);
        mConversationListView.addFooterView(mNoConversationView);
        mNoConversationView.setVisibility(View.GONE);
        mConversationAdapter = new ConversationListAdapter(this);
        mConversationAdapter.setItemListener(this);
        mConversationAdapter.setFriendMode(mConvType);
        mConversationListView.setAdapter(mConversationAdapter);
        mConversationListView.removeFooterView(mNoConversationView);

        mIvOldFriends = (ImageView) findViewById(R.id.iv_old_friends);
        mIvOldFriendUnread = (ImageView) findViewById(R.id.iv_msg_unread);
        mIvOldFriends.setOnClickListener(this);
        mIvOldFriends.setVisibility(View.GONE);
        mIvOldFriendUnread.setVisibility(View.GONE);

        findViewById(R.id.title).setOnClickListener(this);
    }

    /**
     * 等待Dialog
     */
    private void showCircleDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = DialogFactory.getLoadingDialog(this, true, null);
        }
        logger.d("Show dialog.");
        mLoadingDialog.show();
    }

    /**
     * 取消等待Dialog
     */
    private void dismissCircleDialog() {
        logger.d("Dismiss dialog.");
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    @Override
    public void conversationItemOnClick(int position, MessageSessionBaseModel listDO) {
        // 点击了会话
        if (listDO == null) {
            // show Toast
            int index = position;
            if (mConversationAdapter.hasMappingFriend()) {
                // 第一个是匹配中人员信息
                index = index - 1;
            }
            String[] textArray = getResources().getStringArray(R.array.conversation_empty_explain);
            if (index < 0 || index >= textArray.length) {
                return;
            }
            int iconResId = getConversationEmptyImage(index);
            Dialog dialog = DialogFactory.getConvEmptyDescriptionDialog(this, textArray[index], iconResId,
                    new DialogFactory.ConfirmDialogClickListener() {

                        @Override
                        public void onConfirmDialogConfirmClick() {
                            ConversationActivity.this.finish();
                        }
                    });
            dialog.show();
        } else {
            //Get conversation at first.
            WalkArroundMsgManager.getInstance(getApplicationContext()).getConversation(listDO.getContact(), null);

            // 普通会话消息
            Intent intent = transToBuildMsgIntent(listDO);
            if (intent == null) {
                return;
            }
            startActivity(intent);
        }
    }

    /**
     * 跳转至聊天页面
     *
     * @param listDO 会话消息
     * @return
     */
    private Intent transToBuildMsgIntent(MessageSessionBaseModel listDO) {
        Intent intent = new Intent(mContext, BuildMessageActivity.class);

        intent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_RECEIVER, listDO.getContact());
        intent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_THREAD_ID, listDO.getThreadId());

        intent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_TYPE, listDO.getChatType());
        intent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_DISPLAY_NAME, listDO.name);

        return intent;
    }

    @Override
    public void onSelectModeChanged(boolean isInSelectMode) {

    }

    @Override
    public void onDeleteConversationItem(final MessageSessionBaseModel listDO) {
        Dialog deleteConfirmDialog = DialogFactory.getNoticeDialog(
                ConversationActivity.this, getString(R.string.msg_delete_conversations_confirm,
                        1),
                new DialogFactory.NoticeDialogClickListener() {

                    @Override
                    public void onNoticeDialogConfirmClick(boolean isChecked, Object value) {
                        if (mConversationAdapter != null) {
                            String speedDateId = ProfileManager.getInstance().getMyProfile().getSpeedDateId();
                            if (!TextUtils.isEmpty(speedDateId) && mNetStatusView.getVisibility() == View.GONE) {
                                //There is speed date id and there is network.
                                ThreadPoolManager.getPoolManager().addAsyncTask(new CancelSpeedDateTask(getApplicationContext(),
                                        mCancelSpeedDateTaskListener,
                                        HttpUtil.HTTP_FUNC_CANCEL_SPEED_DATE,
                                        HttpUtil.HTTP_TASK_CANCEL_SPEED_DATE,
                                        CancelSpeedDateTask.getParams(speedDateId),
                                        TaskUtil.getTaskHeader()));

                                mConversationAdapter.put2ChoosenList(listDO);
                                deleteConvMsg(listDO);
                            } else {
                                //There is no network.
                                Toast.makeText(ConversationActivity.this, R.string.err_network_unavailable, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }, true
        );
        deleteConfirmDialog.show();
    }

    private int getConversationEmptyImage(int position) {
        int resId = R.drawable.conversation_empty_1;
        if (position == 1) {
            resId = R.drawable.conversation_empty_2;
        } else if (position == 2) {
            resId = R.drawable.conversation_empty_3;
        } else if (position == 3) {
            resId = R.drawable.conversation_empty_4;
        } else if (position == 4) {
            resId = R.drawable.conversation_empty_5;
        } else if (position == 5) {
            resId = R.drawable.conversation_empty_6;
        } else if (position == 6) {
            resId = R.drawable.conversation_empty_7;
        }
        return resId;
    }

}
