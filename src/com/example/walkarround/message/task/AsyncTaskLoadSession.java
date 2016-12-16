package com.example.walkarround.message.task;

import android.content.Context;
import com.example.walkarround.message.manager.WalkArroundMsgManager;
import com.example.walkarround.message.model.MessageSessionBaseModel;
import com.example.walkarround.message.model.MessageSessionModelInfo;
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
            list = new ArrayList<>();
        }

        Collections.sort(list, new SessionComparator(SessionComparator.TIME_DESC));
        Collections.sort(list, new SessionComparator(SessionComparator.STATUS_DESC));
        Collections.sort(list, new SessionComparator(SessionComparator.TOP_DESC));

        doResultCallback(list, TaskResult.SUCCEESS);
    }

    /**
     * All conversation which state is
     * @return
     */
    private MessageSessionBaseModel getFixedEntrance() {
        MessageSessionBaseModel oldFriend = new MessageSessionModelInfo();
        oldFriend.setItemType(MessageConstant.ConversationType.OLD_FRIEND);

        return oldFriend;
    }
}
