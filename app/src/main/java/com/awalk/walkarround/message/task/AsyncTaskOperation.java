package com.awalk.walkarround.message.task;

import android.content.Context;
import com.awalk.walkarround.message.manager.WalkArroundMsgManager;
import com.awalk.walkarround.message.model.MessageSessionBaseModel;
import com.awalk.walkarround.message.util.MessageConstant;
import com.awalk.walkarround.util.http.HttpTaskBase;

import java.util.List;

public class AsyncTaskOperation extends HttpTaskBase {


    private List<MessageSessionBaseModel> mList;
    private boolean mIsCanceled = false;

    public AsyncTaskOperation(Context context, String operationType, List<MessageSessionBaseModel> list,
                              onResultListener listener) {
        super(context, listener, operationType);
        this.mList = list;
        mIsCanceled = false;
    }

    @Override
    public void run() {
        int addBlackOKCount = 0;
        if (mList == null || mList.isEmpty()) {
            return;
        }
        if (getRequestCode().equals(MessageConstant.MSG_OPERATION_REMOVE)) {
            batchRemove();
        }
//        else if (getRequestCode().equals(MessageConstant.MSG_OPERATION_SET_TOP)) {
//            batchMarkTop();
//        } else if (getRequestCode().equals(MessageConstant.MSG_OPERATION_CANCEL_TOP)) {
//            List<Long> deleteConversationList = batchDisMarkTop();
//            doResultCallback(deleteConversationList, TaskResult.SUCCEESS);
//        }
        else if (getRequestCode().equals(MessageConstant.MSG_OPERATION_SET_READ)) {
            batchMarkRead();
        } else if (getRequestCode().equals(MessageConstant.MSG_OPERATION_ADD_BLACKLIST)) {
            batchSetBlack();
        }
        /* 回调执行结果 */
        if (getRequestCode().equals(MessageConstant.MSG_OPERATION_ADD_BLACKLIST)) {
            if (addBlackOKCount == 0) {
                doResultCallback(null, TaskResult.SUCCEESS);
            } else {
                doResultCallback(null, TaskResult.FAILED);
            }
        } else if (!getRequestCode().equals(MessageConstant.MSG_OPERATION_CANCEL_TOP)) {
            doResultCallback(null, TaskResult.SUCCEESS);
        }

    }

    private void batchMarkRead() {
        int progress = 0;
        for (MessageSessionBaseModel m : mList) {
            if (mIsCanceled) {
                break;
            }
            if (WalkArroundMsgManager.getInstance(null).markConversationRead(getContext(), m)) {
                progress++;
            }
            doProgressCallback(progress);
        }
    }


    public void setCancel(boolean cancel) {
        mIsCanceled = cancel;
    }

    private void batchRemove() {
        int progress = 0;
        for (MessageSessionBaseModel m : mList) {
            if (mIsCanceled) {
                break;
            }
            long newThreadId = WalkArroundMsgManager.getInstance(null).removeConversation(getContext(), m);
            m.setThreadId(newThreadId);
            if (newThreadId < 0) {
                progress++;
            }
            doProgressCallback(progress);
        }
    }

//    private void batchMarkTop() {
//        int progress = 0;
//        for (MessageSessionBaseModel m : mList) {
//            if (mIsCanceled) {
//                break;
//            }
//            if (WalkArroundMsgManager.getInstance(null).markConversationTopOrNot(mContext, m, true)) {
//                progress++;
//            }
//            doProgressCallback(progress);
//        }
//    }

//    private List<Long> batchDisMarkTop() {
//        int progress = 0;
//        List<Long> deleteConversationList = new ArrayList<Long>();
//        for (MessageSessionBaseModel m : mList) {
//            if (mIsCanceled) {
//                break;
//            }
//            if (WalkArroundMsgManager.getInstance(null).markConversationTopOrNot(mContext, m, false)) {
//                deleteConversationList.add(m.getThreadId());
//            }
//            progress++;
//            doProgressCallback(progress);
//        }
//        return deleteConversationList;
//    }

    private void batchSetBlack() {
//        int progress = 0;
//        for (MessageSessionBaseModel m : mList) {
//            if (mIsCanceled){
//                break;
//            }
//            progress += NewContactManager.getInstance(getContext()).addBlackNumber(m.getContact());
//            doProgressCallback(progress);
//        }
    }
}
