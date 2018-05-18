package com.awalk.walkarround.message.task;

import android.content.Context;
import com.awalk.walkarround.message.manager.WalkArroundMsgManager;
import com.awalk.walkarround.message.model.MessageSessionBaseModel;
import com.awalk.walkarround.message.model.MessageSessionModelInfo;
import com.awalk.walkarround.message.util.MessageConstant;
import com.awalk.walkarround.message.util.MessageUtil;
import com.awalk.walkarround.message.util.SessionComparator;
import com.awalk.walkarround.util.Logger;
import com.awalk.walkarround.util.http.HttpTaskBase;

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
        isNotifyMsg = !MessageConstant.MSG_OPERATION_LOAD.equals(operate);
    }

    @Override
    public void run() {

        int delNum = WalkArroundMsgManager.getInstance(null).delOtherConversionsOverParamTime(MessageUtil._24_HOURS);
        logger.d("---------- Delete " + delNum);

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
