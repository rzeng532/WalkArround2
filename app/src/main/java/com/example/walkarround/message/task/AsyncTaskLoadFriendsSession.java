package com.example.walkarround.message.task;

import android.content.Context;
import com.example.walkarround.base.task.TaskUtil;
import com.example.walkarround.message.manager.WalkArroundMsgManager;
import com.example.walkarround.message.model.MessageSessionBaseModel;
import com.example.walkarround.message.util.MessageUtil;
import com.example.walkarround.message.util.SessionComparator;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.util.Logger;
import com.example.walkarround.util.http.HttpTaskBase;
import com.example.walkarround.util.http.HttpUtil;
import com.example.walkarround.util.http.ThreadPoolManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AsyncTaskLoadFriendsSession extends HttpTaskBase {

    private static final Logger logger = Logger.getLogger(AsyncTaskLoadFriendsSession.class.getSimpleName());
    private HttpTaskBase.onResultListener mInactiveFriendtaskListener = null;

    public AsyncTaskLoadFriendsSession(Context context, String operate, onResultListener listener, onResultListener inactiveFriendlistener) {
        super(context, listener, operate);
        mInactiveFriendtaskListener = inactiveFriendlistener;
    }

    @Override
    public void run() {

        List<MessageSessionBaseModel> list = WalkArroundMsgManager.getInstance(null).getFriendsConversationList();
        if (list == null) {
            list = new ArrayList<>();
        }

        Collections.sort(list, new SessionComparator(SessionComparator.TIME_DESC));

        List<String> friendUsrId = new ArrayList<>();

        logger.d("Load friends list & change state.");
        if(list.size() > MessageUtil.FRIENDS_COUNT_ON_DB) {
            int i = MessageUtil.FRIENDS_COUNT_ON_DB;
            for(; i < list.size(); i++) {
                MessageSessionBaseModel model = list.get(i);
                if(model != null) {
                    //Local DB change to INIT state.
                    logger.d("Update satate to INIT & add friend id. i = " + i);
                    WalkArroundMsgManager.getInstance(null).updateConversationStatus(model.getThreadId(), MessageUtil.WalkArroundState.STATE_POP);
                    friendUsrId.add(model.getContact());
                }
            }

            //TODO: Send friends user id to server to inactive friends.
            String curUsrId = ProfileManager.getInstance().getCurUsrObjId();
            for(String friendItemId : friendUsrId) {
                ThreadPoolManager.getPoolManager().addAsyncTask(new InActivieFriendTask(mContext,
                        mInactiveFriendtaskListener,
                        HttpUtil.HTTP_FUNC_INACTIVE_FRIEND,
                        HttpUtil.HTTP_TASK_INACTIVE_FRIEND,
                        InActivieFriendTask.getParams(curUsrId, friendItemId),
                        TaskUtil.getTaskHeader()));
            }
        }
    }
}
