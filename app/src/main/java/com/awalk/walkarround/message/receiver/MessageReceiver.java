package com.awalk.walkarround.message.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.avos.avoscloud.AVException;
import com.awalk.walkarround.R;
import com.awalk.walkarround.main.model.ContactInfo;
import com.awalk.walkarround.message.activity.BuildMessageActivity;
import com.awalk.walkarround.message.activity.CountdownActivity;
import com.awalk.walkarround.message.activity.ShowDistanceActivity;
import com.awalk.walkarround.message.manager.ContactsManager;
import com.awalk.walkarround.message.manager.WalkArroundMsgManager;
import com.awalk.walkarround.message.model.ChatMsgBaseInfo;
import com.awalk.walkarround.message.util.MessageUtil;
import com.awalk.walkarround.message.util.MsgBroadcastConstants;
import com.awalk.walkarround.util.AsyncTaskListener;
import com.awalk.walkarround.util.CommonUtils;
import com.awalk.walkarround.util.Logger;
import com.awalk.walkarround.util.TimeFormattedUtil;
import com.awalk.walkarround.util.image.ImageLoaderManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

/**
 * Listener for message.
 */
public class MessageReceiver extends BroadcastReceiver {

    private static final String DEFAULT_NOTIFICATION_ID = "12345";
    private static final Logger sLogger = Logger.getLogger(MessageReceiver.class.getSimpleName());
    Context mContext;
    private static final int MSG_UPDATE_NOTIFICATION = 0;
    private static final String MSG_UPDATE_NOTIFICATION_MSG_ID = "msg_id";

    private Handler mNotifyHandler = new android.os.Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_NOTIFICATION:
                    Bitmap photo = (Bitmap) msg.obj;
                    Bundle extra = msg.getData();
                    if (extra == null) {
                        return;
                    }
                    ChatMsgBaseInfo message = (ChatMsgBaseInfo) extra.getSerializable(MSG_UPDATE_NOTIFICATION_MSG_ID);
                    doNotification(mContext, message, photo);
                    mContext = null;
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (MsgBroadcastConstants.ACTION_MESSAGE_NEW_RECEIVED.equals(action)) {
            // Get new 1v1 chat message.
            long messageId = intent.getLongExtra(MsgBroadcastConstants.BC_VAR_MSG_ID, 0);
            String contact = intent.getStringExtra(MsgBroadcastConstants.BC_VAR_CONTACT); //User object id

            long threadId = intent.getLongExtra(MsgBroadcastConstants.BC_VAR_THREAD_ID,
                    BuildMessageActivity.CONVERSATION_DEFAULT_THREAD_ID);
            onReceivedNewImMsg(context, messageId, contact);
        }
    }

    /**
     * 收到新的IM消息，是否显示通知bar
     *
     * @param context
     * @param messageId
     */
    private void onReceivedNewImMsg(Context context, long messageId, String userId) {
        if (BuildMessageActivity.sCurrentReceiverNum != null
                && BuildMessageActivity.sCurrentReceiverNum.equals(userId)) {
            return;
        }


        ChatMsgBaseInfo message = WalkArroundMsgManager.getInstance(context).getMessageById(messageId);
        if (message != null && !TextUtils.isEmpty(message.getExtraInfo())
                && (ShowDistanceActivity.sCurrentReceiverNum == null
                || !ShowDistanceActivity.sCurrentReceiverNum.equals(userId))) {
            String[] extraArray = message.getExtraInfo().split(MessageUtil.EXTRA_INFOR_SPLIT);
            if (extraArray != null && extraArray.length >= 2 && !TextUtils.isEmpty(extraArray[0])) {
                if (extraArray[1].equalsIgnoreCase(MessageUtil.EXTRA_START_2_WALK_REPLY_OK)) {
                    //Friend send agreement.
                    Intent intent = new Intent(context, CountdownActivity.class);
                    intent.putExtra(CountdownActivity.PARAMS_FRIEND_OBJ_ID, userId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    return;
                } else if (extraArray[1].equalsIgnoreCase(MessageUtil.EXTRA_START_2_WALK_REPLY_NEXT_TIME)) {
                    //Friend refuse your request this time.
                } else if (extraArray[1].equalsIgnoreCase(MessageUtil.EXTRA_START_2_WALK_REQUEST)) {
                    //Friend send a start to walk request at the same time.
                }
            }
        }
        //If there is no build message activity on foreground.
        mContext = context;
        notification(context, userId, message);
    }

    private void notification(final Context context, final String userId, final ChatMsgBaseInfo message) {
        //Get contact infor from server if local data doesn't contain this user.
        ContactInfo contact = ContactsManager.getInstance(context).getContactByUsrObjId(userId);
        if (contact != null) {
            //Get portrait and display notification.
            Glide.with(context)
                    .load(contact.getPortrait().getUrl())
                    .asBitmap()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(new SimpleTarget<Bitmap>(82, 82) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            Bundle data = new Bundle();
                            Message msg = mNotifyHandler.obtainMessage();
                            msg.what = MSG_UPDATE_NOTIFICATION;
                            data.putSerializable(MSG_UPDATE_NOTIFICATION_MSG_ID, message);
                            if (resource != null) {
                                msg.obj = resource;
                            }
                            msg.setData(data);
                            mNotifyHandler.sendMessage(msg);
                        }
                    });
        } else if (!TextUtils.isEmpty(userId)) {
            //Get contact infor from server.
            ContactsManager.getInstance(context).getContactFromServer(userId, new AsyncTaskListener() {
                @Override
                public void onSuccess(Object data) {
                    //Got contact infor & try to get contact portrait.
                    ContactsManager.getInstance(context).addContactInfo((ContactInfo) data);
                    notification(context, userId, message);
                }

                @Override
                public void onFailed(AVException e) {
                    //If we failed to get contact information, we will use default portrait.
                    doNotification(context, message, null);
                }
            });
        }
    }

    //Init notification UI.
    private void initNotifyView(Context context, RemoteViews panelView, ChatMsgBaseInfo message, ContactInfo contact, Bitmap srcPhoto) {
        if (contact != null) {
            Bitmap photo = ImageLoaderManager.createCircleImage(srcPhoto);
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
            panelView.setImageViewResource(R.id.notify_profile_iv, R.drawable.default_profile_portrait);
        }

        //TODO: ADD Emoji parser later.
        //panelView.setTextViewText(R.id.notify_msg_content_tv, EmojiParser.getInstance(context).getSmileyText(message.getData()));
        panelView.setTextViewText(R.id.notify_msg_content_tv, message.getData());
        panelView.setTextViewText(R.id.notify_time_tv,
                TimeFormattedUtil.getDetailDisplayTime(context, message.getTime()));
    }


    //Prepare notification data and display it.
    private void doNotification(Context context, Long messageId, Bitmap srcPhoto) {

        ChatMsgBaseInfo message = WalkArroundMsgManager.getInstance(context).getMessageById(messageId);
        //Prepare notification data and display it.
        doNotification(context, message, srcPhoto);

    }

    private void doNotification(Context context, ChatMsgBaseInfo message, Bitmap srcPhoto) {
        if (message == null) {
            return;
        }

        ContactInfo contact = ContactsManager.getInstance(context).getContactByUsrObjId(message.getContact());

        Intent intent = new Intent();
        intent.setClass(context.getApplicationContext(), BuildMessageActivity.class);
        intent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_RECEIVER, message.getContact());
        intent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_THREAD_ID, message.getMsgThreadId());
        intent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_TYPE, message.getChatType());

        //TODO: contact should write to DB & we should crate a content provider for those contacts.
        if (contact != null) {
            intent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_DISPLAY_NAME, contact.getUsername());
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, (int) message.getMsgThreadId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //If the contact is null, set the user name as empty.
        String displayName = (contact == null ? "" : contact.getUsername());
        message.setData(MessageUtil.getDisplayStr(context, displayName, message));
        Notification notification = new Notification();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.icon = R.drawable.msg_notify_icon;

        notification.tickerText = message.getData();

        if (Build.VERSION.SDK_INT == 19) {
            contentIntent.cancel();
            contentIntent = PendingIntent.getActivity(context, (int) message.getMsgThreadId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        notification.contentIntent = contentIntent;
        notification.contentView = new RemoteViews(context.getPackageName(), R.layout.msg_notify_panel);
//        notification.defaults=Notification.DEFAULT_SOUND;
        initNotifyView(context, notification.contentView, message, contact, srcPhoto);
        try {
            String number = (contact == null ? DEFAULT_NOTIFICATION_ID : contact.getMobilePhoneNumber());
            int startPos = number.length() > 5 ? number.length() - 5 : 0;
            int id = Integer.parseInt(number.substring(startPos));
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(number, id, notification);
        } catch (NumberFormatException e) {
            sLogger.e("notification NumberFormatException:" + e.getMessage());
        }
    }
}
