package com.example.walkarround.message.util;

import android.content.Context;
import android.text.TextUtils;
import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.messages.*;
import com.example.walkarround.R;
import com.example.walkarround.base.WalkArroundApp;
import com.example.walkarround.message.model.ChatMessageInfo;
import com.example.walkarround.message.model.ChatMsgBaseInfo;
import com.example.walkarround.message.util.MessageConstant.MessageType;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.AppSharedPreference;
import com.example.walkarround.util.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 消息相关的工具类
 * Date: 2015-03-18
 *
 * @author mss
 */
public class MessageUtil {

    public static final String VCARD_FILE_UNICODE = "UTF-8";
    private static final Logger logger = Logger.getLogger(MessageUtil.class.getSimpleName());
    private static final String GIF_FILE_EXT = "gif";

    /**
     * 判断是否gif文件
     *
     * @param filePath
     * @return
     */
    public static boolean isGifFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        int dot = filePath.lastIndexOf('.');
        if ((dot > -1) && (dot < (filePath.length() - 1))) {
            if (GIF_FILE_EXT.equals(filePath.substring(dot + 1).toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /*
    public static String getGroupInfo(Context context, String msg, HashMap<String, String> nickNameMap) {
        // 解析 group通知类消息
        // XXX加入群 （成员号码） 数据格式：join,18966669999
        // 修改群名 （修改后的名称） 数据格式：subject,rcsGroup
        // 修改群备注 （群备注名称，不需要同步网络侧） 数据格式：remark,Suntek Rcs
        // 成员设置自己别名 （成员号码，别名） 数据格式：alias,18966669999,james
        // 群主改变 （新群主号码） 数据格式：chairman,18966669999
        // XXX被踢出 （被踢出成员号码） 数据格式：tick,18966669999
        // XXX退出群 （退出成员号码） 数据格式：quit,18966669999
        // 群解散 （群ID） 数据格式：disband,1
        // 群聊策略 policy,1
        // 群聊创建 create
        if (TextUtils.isEmpty(msg)) {
            return "";
        }
        String groupInfo[] = msg.split(",");
        String msgType = groupInfo[0];
        if ("create".equals(msgType)) {
            msg = context.getString(R.string.group_info_create);
        } else if ("join".equals(msgType)) {
            msg = context.getString(R.string.group_info_join, getContactDisplayName(context, groupInfo[1], nickNameMap));
        } else if ("subject".equals(msgType)) {
            msg = context.getString(R.string.group_info_subject, groupInfo[1]);
        } else if ("alias".equals(msgType)) {
            if (groupInfo.length == 3) {
                msg = context.getString(R.string.group_info_alias, groupInfo[1], groupInfo[2]);
            }
        } else if ("remark".equals(msgType)) {
            msg = context.getString(R.string.group_info_remark, groupInfo[1]);
        } else if ("chairman".equals(msgType)) {
            msg = context.getString(R.string.group_info_chairman, getContactDisplayName(context, groupInfo[1], nickNameMap));
        } else if ("tick".equals(msgType)) {
            msg = context.getString(R.string.group_info_tick, getContactDisplayName(context, groupInfo[1], nickNameMap));
        } else if ("quit".equals(msgType)) {
            msg = context.getString(R.string.group_info_quit, getContactDisplayName(context, groupInfo[1], nickNameMap));
        } else if ("disband".equals(msgType)) {
            msg = context.getString(R.string.group_info_disband);
        } else if ("policy".equals(msgType)) {
            if (groupInfo.length >= 2 && !TextUtils.isEmpty(groupInfo[1])) {
                if (groupInfo[1].equals("0")) {
                    msg = context.getString(R.string.group_info_policy_0);
                } else if (groupInfo[1].equals("1")) {
                    msg = context.getString(R.string.group_info_policy_1);
                } else if (groupInfo[1].equals("2")) {
                    msg = context.getString(R.string.group_info_policy_2);
                }
            }
        }
        return msg;
    }

    public static String getContactDisplayName(Context context, String number, HashMap<String, String> nickNameMap) {
        String phoneNum = CommonUtil.getPhoneNum(number);
        if (nickNameMap != null && nickNameMap.containsKey(phoneNum)) {
            return nickNameMap.get(phoneNum);
        }
        ContactInfo contactInfo = NewContactManager.getInstance(context)
                .getDetailByPhoneNumber(phoneNum);
        if (contactInfo != null) {
            return contactInfo.getFirstName();
        }

        return number;
    }
    */

    /**
     * 获取拍照图片路径
     *
     * @return
     */
    public static String createCameraTakePicFile() {
        StringBuilder fileName = new StringBuilder();
        fileName.append(WalkArroundApp.MTC_DATA_PATH);
        fileName.append(AppConstant.CAMERA_TAKE_PIC_PATH);
        File folder = new File(fileName.toString());
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                return null;
            }
        }
        // 指定照片路径
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        fileName.append("IMG_").append(timeStamp).append(".jpg");
        return fileName.toString();
    }

    /**
     * 转译显示的通知消息内容
     *
     * @param context
     * @param sender
     * @param message
     * @return
     */
    public static String getDisplayStr(Context context, String sender, ChatMsgBaseInfo message) {
        //if (message.isBurnAfterMsg()) {
        //    return context.getString(R.string.msg_session_burn_after);
        //}
        String displayStr = null;
        switch (message.getMsgType()) {
            case MessageType.MSG_TYPE_TEXT:
                displayStr = message.getData();
                break;
            //case MessageConstant.MessageType.MSG_TYPE_CONTACT:
            //    displayStr = context.getString(R.string.msg_session_contact, sender, message.getExtraInfo());
            //    break;
            case MessageType.MSG_TYPE_AUDIO:
                displayStr = context.getString(R.string.msg_session_audio);
                break;
            case MessageType.MSG_TYPE_VIDEO:
                displayStr = context.getString(R.string.msg_session_video);
                break;
            case MessageType.MSG_TYPE_IMAGE:
                displayStr = context.getString(R.string.msg_session_picture);
                break;
            case MessageType.MSG_TYPE_MAP:
                displayStr = context.getString(R.string.msg_session_location);
                break;
            //case MessageConstant.MessageType.MSG_TYPE_NOTIFICATION:
            //    displayStr = MessageUtil.getGroupInfo(context, message.getData(), null);
            //    break;
            default:
                displayStr = message.getData();
                break;
        }
        return displayStr;
    }

    /**
     * 获取文件大小
     *
     * @param filePath
     * @return
     */
    public static long getFileSize(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file.length();
        } else {
            return 0;
        }
    }

    public static String gsontoJson(List<String> memberList) {
        return JSON.toJSONString(memberList);
    }

    public static List<String> jsontoGson(String memberString) {
        return JSON.parseArray(memberString, String.class);

    }

    /*
    public static List<GroupMemberBaseInfo> jsontoGroupUser(String memberString) {
        List<GroupMemberBaseInfo> list = new ArrayList<GroupMemberBaseInfo>();
        list.addAll(JSON.parseArray(memberString, GroupMemberInfo.class));
        return list;
    }

    public static String groupUserToGjson(List<GroupMemberBaseInfo> memberList) {
        return JSON.toJSONString(memberList);
    }
    */

    public static String getDisplayUnreadCount(int unreadCount) {
        if (unreadCount < 0) {
            return "";
        }
        if (unreadCount < 100) {
            return Integer.toString(unreadCount);
        }
        return "99+";
    }

    public static boolean isNewMsgNotifyReceive() {
        String curUsrId = ProfileManager.getInstance().getCurUsrObjId();
        return  AppSharedPreference.getBoolean(curUsrId + MessageConstant.MSG_NEW_MSG_NOTIFICATION_REC_SWITCH, true);
    }

    //This is for received message
    public static ChatMsgBaseInfo convertMsg(AVIMTypedMessage cmMessage) {
        ChatMsgBaseInfo msgInfo = new ChatMessageInfo();

        List<String> receiver = new ArrayList<>();
        receiver.add(ProfileManager.getInstance().getCurUsrObjId());
        msgInfo.setReceiver(receiver);
        msgInfo.setContact(cmMessage.getFrom());

        //TODO: extend chat type while there is group chat.
        msgInfo.setChatType(MessageConstant.ChatType.CHAT_TYPE_ONE2ONE);
        msgInfo.setTime(cmMessage.getTimestamp());
        msgInfo.setSendReceive(MessageConstant.MessageSendReceive.MSG_RECEIVE);
        msgInfo.setMsgState(MessageConstant.MessageState.MSG_STATE_UNRECEIVE);
        msgInfo.setIsRead(false);

        if (cmMessage instanceof AVIMAudioMessage) {
            msgInfo.setMsgType(MessageType.MSG_TYPE_AUDIO);
            msgInfo.setDuration((int)(((AVIMAudioMessage) cmMessage).getDuration()));
            msgInfo.setFileName(((AVIMAudioMessage) cmMessage).getLocalFilePath()); //Check
            msgInfo.setFileUrlPath(((AVIMAudioMessage) cmMessage).getFileUrl());
        } else if (cmMessage instanceof AVIMImageMessage) {
//            msgInfo.setFileSize(((ImageMessageBody) messageBody).getFileLength());
//            msgInfo.setFileName(((ImageMessageBody) messageBody).getFileName());
//            msgInfo.setFileUrlPath(((ImageMessageBody) messageBody).getOriginalUri());
//            msgInfo.setThumbUrlPath(((ImageMessageBody) messageBody).getSmallUri());
//            msgInfo.setImageWidth(((ImageMessageBody) messageBody).getWidth());
//            msgInfo.setImageHeight(((ImageMessageBody) messageBody).getHeight());
//            msgInfo.setData(msgInfo.getImageWidth() + LittleCDbManager.IMAGE_SIZE_SEPARATOR + msgInfo.getImageHeight());
        } else if (cmMessage instanceof AVIMVideoMessage) {
//            msgInfo.setDuration(((VideoMessageBody) messageBody).getDuration());
//            msgInfo.setFileSize(((VideoMessageBody) messageBody).getFileLength());
//            msgInfo.setFileName(((VideoMessageBody) messageBody).getFileName());
//            msgInfo.setFileUrlPath(((VideoMessageBody) messageBody).getOriginalUri());
//            msgInfo.setThumbUrlPath(((VideoMessageBody) messageBody).getThumbnailUrl());
        } else if (cmMessage instanceof AVIMLocationMessage) {
//            msgInfo.setLocationLabel(((LocationMessageBody) messageBody).getAddress());
//            msgInfo.setLatitute(((LocationMessageBody) messageBody).getLatitude());
//            msgInfo.setLongitude(((LocationMessageBody) messageBody).getLongitude());
//            msgInfo.setFileSize(((LocationMessageBody) messageBody).getFileLength());
//            msgInfo.setFileName(((LocationMessageBody) messageBody).getFileName());
//            msgInfo.setFileUrlPath(((LocationMessageBody) messageBody).getOriginalUri());
        } else if (cmMessage instanceof AVIMLocationMessage) {
            msgInfo.setMsgType(MessageType.MSG_TYPE_TEXT);
        } else if (cmMessage instanceof AVIMTextMessage) {
            msgInfo.setData(((AVIMTextMessage)cmMessage).getText());
        }

        return msgInfo;
    }
}
