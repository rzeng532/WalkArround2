package com.awalk.walkarround.message.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.awalk.walkarround.message.manager.WalkArroundMsgManager;
import com.awalk.walkarround.message.model.ChatMsgBaseInfo;
import com.awalk.walkarround.message.model.MessageRecipientInfo;
import com.awalk.walkarround.message.util.MsgBroadcastConstants;
import com.awalk.walkarround.myself.manager.ProfileManager;
import com.awalk.walkarround.util.Logger;

/**
 * 定时短信通知 Date: 2015-02-13
 *
 * @author mss
 */
public class AlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_SEND_MESSAGE_ALARM = "send message alarm";
    public static final String ACTION_TIME_MSG_SEND = "send time message";
    public static final String INTENT_TIME_MSG_ID = "time message id";
    public static final String INTENT_MSG_SENDER = "message sender";
    // 只能发送当前时间5分钟之后的定时短信
    public static final int MINIMAL_SEND_INTERVAL = 5;
    private static final Logger logger = Logger.getLogger(AlarmReceiver.class.getSimpleName());

    @Override
    public void onReceive(Context context, Intent intent) {
        logger.d("onReceive(), Action=" + intent.getAction());
        if (ACTION_SEND_MESSAGE_ALARM.equals(intent.getAction())) {
            // 定时短信
            long messageId = intent.getLongExtra(INTENT_TIME_MSG_ID, -1);
            String sender = intent.getStringExtra(INTENT_MSG_SENDER);
            sendMessage(context, sender, messageId);
        }
    }

    /**
     * 发送定时短信
     *
     * @param messageId
     */
    private void sendMessage(Context context, String sender, long messageId) {
        logger.d("sendMessage(), messageId=" + messageId);
        if (TextUtils.isEmpty(sender)) {
            // 发送人不合法
            return;
        }
        String masterPhoneNum = ProfileManager.getInstance().getCurUsrObjId();
        if (!sender.equals(masterPhoneNum)) {
            // 其他账户登录
            return;
        }
        ChatMsgBaseInfo message = WalkArroundMsgManager.getInstance(context).getMessageById(messageId);
        if (message == null) {
            return;
        }
        // 接收人
        MessageRecipientInfo recipientInfo = new MessageRecipientInfo();
        recipientInfo.setConversationType(message.getChatType());
        recipientInfo.setRecipientList(message.getReceiver());
        recipientInfo.setConversationId(message.getConversationId());
        recipientInfo.setGroupId(message.getGroupId());
        recipientInfo.setThreadId(message.getMsgThreadId());
        sendPlainText(context, recipientInfo, message);
    }

    /**
     * 发送纯文本消息
     *
     * @param context
     * @param recipientInfo
     * @param message
     */
    private void sendPlainText(Context context, MessageRecipientInfo recipientInfo, ChatMsgBaseInfo message) {
        logger.d("send PlainText message");
        // 目前只支持发送纯文本定时消息
        long messageId = WalkArroundMsgManager.getInstance(context).sendTextMsg(recipientInfo, message.getData(), null);

        Intent intent = new Intent();
        intent.setAction(ACTION_TIME_MSG_SEND);
        // 要发送的内容
        intent.putExtra(MsgBroadcastConstants.BC_VAR_MSG_ID, messageId);
        intent.putExtra(MsgBroadcastConstants.BC_VAR_THREAD_ID, recipientInfo.getThreadId());
        // delete time send message
        intent.putExtra(INTENT_TIME_MSG_ID, message.getMsgId());
        context.sendBroadcast(intent);
        WalkArroundMsgManager.getInstance(context).delMessageById(message.getMsgId());
    }
}
