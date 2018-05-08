package com.example.walkarround.message.task;

import android.content.Context;
import android.os.AsyncTask;
import com.example.walkarround.message.manager.WalkArroundMsgManager;
import com.example.walkarround.message.model.ChatMsgAndSMSReturn;

/**
 * 加载消息
 * Date: 2015-03-23
 *
 * @author mss
 */
public class LoadSearchResultMessageTask extends AsyncTask<String, Void, ChatMsgAndSMSReturn> {

    private Context mContext;
    private SearchMessageLoadListener mMessageLoadListener;

    public LoadSearchResultMessageTask(Context context, SearchMessageLoadListener messageLoadListener) {
        mContext = context;
        mMessageLoadListener = messageLoadListener;
    }

    @Override
    protected ChatMsgAndSMSReturn doInBackground(String... params) {
        if (params == null) {
            return null;
        }
        int length = params.length;
        if (length == 5) {
            long thread = Long.parseLong(params[0]);
            long beginChatId = Long.parseLong(params[2]);
            return WalkArroundMsgManager.getInstance(mContext).getChatMsgList(mContext, thread, params[1], beginChatId,
                    false);
        } else if (length == 4) {
            long thread = Long.parseLong(params[0]);
            long msgId = Long.parseLong(params[2]);
            return WalkArroundMsgManager.getInstance(mContext).getChatMsgListByTime(mContext, thread, params[1], msgId);
        }
        return null;
    }

    @Override
    protected void onPostExecute(ChatMsgAndSMSReturn result) {
        if (mMessageLoadListener != null) {
            mMessageLoadListener.onDownMessageLoaded(result);
        }
    }

    public interface SearchMessageLoadListener {
        public void onDownMessageLoaded(ChatMsgAndSMSReturn result);
    }

}
