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
public class LoadMessageTask extends AsyncTask<String, Void, ChatMsgAndSMSReturn> {

    private Context mContext;
    private MessageLoadListener mMessageLoadListener;

    public LoadMessageTask(Context context, MessageLoadListener messageLoadListener) {
        mContext = context;
        mMessageLoadListener = messageLoadListener;
    }

    @Override
    protected ChatMsgAndSMSReturn doInBackground(String... params) {
        if (params == null || params.length < 3) {
            return null;
        }
        long thread = Long.parseLong(params[0]);
        long beginChatId = Long.parseLong(params[2]);
        return WalkArroundMsgManager.getInstance(mContext).getChatMsgList(mContext, thread, params[1], beginChatId,
                true);
    }

    @Override
    protected void onPostExecute(ChatMsgAndSMSReturn result) {
        if (mMessageLoadListener != null) {
            mMessageLoadListener.onMessageLoaded(result);
        }
    }

    public interface MessageLoadListener {
        public void onMessageLoaded(ChatMsgAndSMSReturn result);
    }

}
