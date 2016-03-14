package com.example.walkarround.message.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;
import com.example.walkarround.R;
import com.example.walkarround.main.model.ContactInfo;
import com.example.walkarround.message.activity.BuildMessageActivity;
import com.example.walkarround.message.manager.WalkArroundMsgManager;
import com.example.walkarround.message.model.ChatMsgBaseInfo;
import com.example.walkarround.message.util.MessageConstant;
import com.example.walkarround.message.util.MessageUtil;
import com.example.walkarround.message.util.MsgBroadcastConstants;
import com.example.walkarround.util.CommonUtils;
import com.example.walkarround.util.Logger;
import com.example.walkarround.util.TimeFormattedUtil;
import com.example.walkarround.util.image.ImageLoaderManager;

/**
 * 短信监听
 */
public class MessageReceiver extends BroadcastReceiver {

    private static final Logger sLogger = Logger.getLogger(MessageReceiver.class.getSimpleName());

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
       if (MsgBroadcastConstants.ACTION_MESSAGE_NEW_RECEIVED.equals(action)) {
            // 新到一对一消息
            int msgType = intent.getIntExtra(MsgBroadcastConstants.BC_VAR_MSG_TYPE, 0);
            String extraInfo = intent.getStringExtra(MsgBroadcastConstants.BC_VAR_MSG_EXTRA);
            if (!TextUtils.isEmpty(extraInfo)
                    && (msgType == MessageConstant.MessageType.MSG_TYPE_IMAGE
                    || msgType == MessageConstant.MessageType.MSG_TYPE_MAP)) {
                // 通话主题和位置消息
                return;
            }
            long messageId = intent.getLongExtra(MsgBroadcastConstants.BC_VAR_MSG_ID, 0);
            String contact = intent.getStringExtra(MsgBroadcastConstants.BC_VAR_CONTACT); //User object id

            long threadId = intent.getLongExtra(MsgBroadcastConstants.BC_VAR_THREAD_ID,
                    BuildMessageActivity.CONVERSATION_DEFAULT_THREAD_ID);
            onReceivedNewImMsg(context, threadId, messageId, contact);
        }
    }

    /**
     * 收到新的IM消息，是否显示通知bar
     *
     * @param context
     * @param messageId
     */
    private void onReceivedNewImMsg(Context context, long threadId, long messageId, String contact) {
        if (BuildMessageActivity.sCurrentReceiverNum != null
                && BuildMessageActivity.sCurrentReceiverNum.equals(contact)) {
            return;
        }

        // 没有打开会话页面，通知bar显示
        if (MessageUtil.isNewMsgNotifyReceive()) {
            ChatMsgBaseInfo message = WalkArroundMsgManager.getInstance(context).getMessageById(messageId);
            notification(context, message);
        }
    }

    private void notification(Context context, ChatMsgBaseInfo message) {
        if (message == null) {
            return;
        }
        Intent intent = new Intent();
        intent.setClass(context.getApplicationContext(), BuildMessageActivity.class);
        intent.putExtra(BuildMessageActivity.INTENT_RECEIVER_EDITABLE, false);
        intent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_RECEIVER, message.getContact());
        intent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_THREAD_ID, message.getMsgThreadId());
        intent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_TYPE, message.getChatType());

        //TODO: contact should write to DB & we should crate a content provider for those contacts.
        ContactInfo contact = null;//NewContactManager.getInstance(context).getDetailByPhoneNumber(message.getContact());
        if (contact != null) {
            intent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_DISPLAY_NAME, contact.getUsername());
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, (int) message.getMsgThreadId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String displayName = contact == null ? message.getContact() : contact.getUsername();
        message.setData(MessageUtil.getDisplayStr(context, displayName, message));
        Notification notification = new Notification();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.icon = R.drawable.msg_notify_icon;

        //TODO: Add Emoji parser later.
        //notification.tickerText = EmojiParser.getInstance(context).getSmileyText(message.getData());
        notification.tickerText = message.getData();

        if (Build.VERSION.SDK_INT == 19) {
            contentIntent.cancel();
            contentIntent = PendingIntent.getActivity(context, (int) message.getMsgThreadId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        notification.contentIntent = contentIntent;
        notification.contentView = new RemoteViews(context.getPackageName(), R.layout.msg_notify_panel);
//        notification.defaults=Notification.DEFAULT_SOUND;
        initNotifyView(context, notification.contentView, message, contact);
        try {
            String number = message.getContact();
            int startPos = number.length() > 5 ? number.length() - 5 : 0;
            int id = Integer.parseInt(number.substring(startPos));
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(number, id, notification);
        } catch (NumberFormatException e) {
            sLogger.e("notification NumberFormatException:" + e.getMessage());
        }
    }

    private void initNotifyView(Context context, RemoteViews panelView, ChatMsgBaseInfo message, ContactInfo contact) {
        if (contact != null) {
            Bitmap srcPhoto = ImageLoaderManager.getSyncImage(contact.getPortrait().getUrl());
            Bitmap photo = ImageLoaderManager.createCircleImage(srcPhoto);
            if (srcPhoto != null) {
                srcPhoto.recycle();
            }
            if (photo != null) {
                panelView.setViewVisibility(R.id.notify_profile_logogram_tv, View.GONE);
                panelView.setViewVisibility(R.id.notify_profile_iv, View.VISIBLE);
                panelView.setImageViewBitmap(R.id.notify_profile_iv, photo);
            } else {
                panelView.setViewVisibility(R.id.notify_profile_iv, View.VISIBLE);
                panelView.setImageViewResource(R.id.notify_profile_iv,
                        CommonUtils.getPhotoBgResId(contact.getUsername()));
                panelView.setViewVisibility(R.id.notify_profile_logogram_tv, View.VISIBLE);
                panelView.setTextViewText(R.id.notify_profile_logogram_tv,
                        contact.getUsername().substring(contact.getUsername().length() - 1));
            }
            panelView.setTextViewText(R.id.notify_sender_tv, contact.getUsername());
        } else {
            panelView.setTextViewText(R.id.notify_sender_tv, message.getContact());
            panelView.setViewVisibility(R.id.notify_profile_logogram_tv, View.GONE);
            panelView.setViewVisibility(R.id.notify_profile_iv, View.VISIBLE);
            panelView.setImageViewResource(R.id.notify_profile_iv, R.drawable.contact_default_profile);
        }

        //TODO: ADD Emoji parser later.
        //panelView.setTextViewText(R.id.notify_msg_content_tv, EmojiParser.getInstance(context).getSmileyText(message.getData()));
        panelView.setTextViewText(R.id.notify_msg_content_tv, message.getData());
        panelView.setTextViewText(R.id.notify_time_tv,
                TimeFormattedUtil.getDetailDisplayTime(context, message.getTime()));
    }
}
