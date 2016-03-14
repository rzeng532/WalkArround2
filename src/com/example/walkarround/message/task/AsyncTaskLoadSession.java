package com.example.walkarround.message.task;

import android.content.Context;
import com.example.walkarround.message.manager.WalkArroundMsgManager;
import com.example.walkarround.message.model.MessageSessionBaseModel;
import com.example.walkarround.message.util.MessageConstant;
import com.example.walkarround.message.util.SessionComparator;
import com.example.walkarround.util.Logger;
import com.example.walkarround.util.http.HttpTaskBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AsyncTaskLoadSession extends HttpTaskBase {

    private static final Logger logger = Logger.getLogger(AsyncTaskLoadSession.class.getSimpleName());

    private boolean isNotifyMsg;
    private int offset;
    private int count;

    public AsyncTaskLoadSession(Context context, String operate, int offset,
                                int count, HttpTaskBase.onResultListener listener) {
        super(context, listener, operate);
        this.offset = offset;
        this.count = count;
        isNotifyMsg = MessageConstant.MSG_OPERATION_NOTIFY_LOAD.equals(operate);
    }

    @Override
    public void run() {
        List<MessageSessionBaseModel> list = WalkArroundMsgManager.getInstance(null).getConversationList(isNotifyMsg, offset, count);
        if (list == null) {
            list = new ArrayList<MessageSessionBaseModel>();
        }
//        if (!isNotifyMsg) {
//            // 非通知会话才显示固定公众号等入口
//            list.addAll(0, getFixedEntrances());
//        }
        Collections.sort(list, new SessionComparator(SessionComparator.TIME_DESC));
        Collections.sort(list, new SessionComparator(SessionComparator.TOP_DESC));
        Collections.sort(list, new SessionComparator(SessionComparator.PA_DESC));
        doResultCallback(list, TaskResult.SUCCEESS);
    }

    /**
     * 会话页面固定3个入口
     *
     * @return
     */
//    private List<MessageSessionBaseModel> getFixedEntrances() {
//        List<MessageSessionBaseModel> list = new ArrayList<MessageSessionBaseModel>();
//        MessageSessionBaseModel model = new MessageSessionModelInfo();
//        // 公众号
//        model.setItemType(ConversationType.PUBLIC_ACCOUNT);
//        model.name = mContext.getString(R.string.msg_page_publicaccount);
//        model.setData(mContext.getResources().getString(R.string.msg_enter_publicaccount_message));
//        model.defaultResId = R.drawable.contacts_icon_list_publicaccount;
//        list.add(model);
//
//        // 系统通知
//        model = new MessageSessionModelInfo();
//        model.setItemType(ConversationType.SYSTEM);
//        model.name = mContext.getString(R.string.group_invitation);
//        model.unReadCount = MessageManager.getService().getUnreadInvitationCount();
//        model.defaultResId = R.drawable.message_icon_list_bell;
//        model.setData(MessageManager.getService().getGroupInvitationListTopItemInfo(mContext));
//        list.add(model);
//
//        return list;
//    }
}
