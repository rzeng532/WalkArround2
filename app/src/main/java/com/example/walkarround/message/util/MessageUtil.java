package com.example.walkarround.message.util;

import android.app.NotificationManager;
import android.content.Context;
import android.text.TextUtils;
import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVGeoPoint;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.messages.*;
import com.example.walkarround.R;
import com.example.walkarround.base.WalkArroundApp;
import com.example.walkarround.main.model.ContactInfo;
import com.example.walkarround.main.parser.WalkArroundJsonResultParser;
import com.example.walkarround.message.manager.ContactsManager;
import com.example.walkarround.message.model.ChatMessageInfo;
import com.example.walkarround.message.model.ChatMsgBaseInfo;
import com.example.walkarround.message.util.MessageConstant.MessageType;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.AppSharedPreference;
import com.example.walkarround.util.Logger;
import com.example.walkarround.util.http.HttpUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

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
    public static final String MAP_DETAIL_INFOR_SPLIT = "#";

    //Message extra information key
    public static final String EXTRA_INFOR_KEY = "extra_key";
    public static final String EXTRA_INFOR_SPLIT = "#";
    public static final String EXTRA_AGREEMENT_2_WALKARROUND = "extra_agree_place";
    public static final String EXTRA_SELECT_PLACE_2_WALKARROUND = "extra_select_place";
    public static final String EXTRA_START_2_WALKARROUND = "extra_start_2_walk";
    public static final String EXTRA_START_2_WALK_REQUEST = "extra_start_2_walk_request";
    public static final String EXTRA_START_2_WALK_REPLY_OK = "extra_start_2_walk_reply_ok";
    public static final String EXTRA_START_2_WALK_REPLY_NEXT_TIME = "extra_start_2_walk_reply_next_time";
    public static final String EXTRA_SAY_HELLO = "extra_say_hello";

    public static final long _24_HOURS = 24 * 60 * 60 * 1000;

    //Get friend list count. If count is 4, it means server response 4 friends every time.
    public static final int GET_FRIENDS_LIST_COUNT = 7;
    public static final int FRIENDS_COUNT_ON_DB = 7; //From 0 to COUNT

    private static final List<Integer> mFriendColArray = Arrays.asList(R.color.friend_col_1,
            R.color.friend_col_2, R.color.friend_col_3,
            R.color.friend_col_4, R.color.friend_col_5,
            R.color.friend_col_6,R.color.friend_col_7);

    private static final List<Integer> mFriendColStrArray = Arrays.asList(R.string.friend_col1_description,
            R.string.friend_col2_description, R.string.friend_col3_description,
            R.string.friend_col4_description, R.string.friend_col5_description,
            R.string.friend_col6_description, R.string.friend_col7_description);

    public interface WalkArroundState {
        public static int STATE_INIT = 1; //初始状态，无匹配关系
        public static int STATE_IM = 2;  //匹配，并在IM 聊天状态
        public static int STATE_WALK = 3; //IM 中相互选择地点，并且未相互评价
        public static int STATE_IMPRESSION = 4; //完成走走，等待评价
        public static int STATE_END = 5; //评价完成
        public static int STATE_END_IMPRESSION = 6; //好友再次评价
        public static int STATE_POP = 7; //好友堆栈溢出 （例如只能存在7个好友，你排在第八，则溢出）
        public static int STATE_POP_IMPRESSION = 8; //好友堆栈溢出后再次评价
    }

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
            case MessageConstant.MessageType.MSG_TYPE_NOTIFICATION:
                displayStr = context.getString(R.string.msg_session_sys_msg);
                break;
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
        } else if (cmMessage instanceof AVIMLocationMessage) {
            msgInfo.setMsgType(MessageType.MSG_TYPE_MAP);
            msgInfo.setLocationLabel(((AVIMLocationMessage) cmMessage).getText());
            String content = ((AVIMLocationMessage) cmMessage).getContent();
            AVGeoPoint temp = ((AVIMLocationMessage) cmMessage).getLocation();
            if(temp != null) {
                msgInfo.setLatitute(temp.getLatitude());
                msgInfo.setLongitude(temp.getLongitude());
            }
            Map<String, Object> attri = ((AVIMLocationMessage)cmMessage).getAttrs();
            if(attri != null) {
                String extroInfor = (String)attri.get(MessageUtil.EXTRA_INFOR_KEY);
                if(!TextUtils.isEmpty(extroInfor)) {
                    msgInfo.setExtraInfo(extroInfor);
                }
            }
        } else if (cmMessage instanceof AVIMTextMessage) {
            msgInfo.setMsgType(MessageType.MSG_TYPE_TEXT);
            msgInfo.setData(((AVIMTextMessage)cmMessage).getText());
            Map<String, Object> attri = ((AVIMTextMessage)cmMessage).getAttrs();
            if(attri != null) {
                String extroInfor = (String)attri.get(MessageUtil.EXTRA_INFOR_KEY);
                if(!TextUtils.isEmpty(extroInfor)) {
                    msgInfo.setExtraInfo(extroInfor);
                    msgInfo.setMsgType(MessageType.MSG_TYPE_NOTIFICATION);
                }
            }
        }

        return msgInfo;
    }

    /**
     * 获取文件下载路径
     *
     * @param msgType 消息类型
     * @return 文件下载路径
     */
    public static String getMsgFileDownLoadPath(int msgType) {
        String basePath = WalkArroundApp.MTC_DATA_PATH;
        switch (msgType) {
            case MessageType.MSG_TYPE_AUDIO:
                basePath += AppConstant.AUDIO_FILE_PATH;
                break;
            case MessageType.MSG_TYPE_MAP:
                basePath += AppConstant.LOCATION_PIC_PATH;
                break;
            case MessageType.MSG_TYPE_IMAGE:
                basePath += AppConstant.CAMERA_TAKE_PIC_PATH;
                break;
            default:
                basePath += AppConstant.MSG_DOWNLOAD_PATH;
                break;
        }
        File folder = new File(basePath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return basePath;
    }

    /*
     * Download special file by HTTP connection.
     */
    public static boolean downloadFile(String urlSite, String localFilePath) {
        if (TextUtils.isEmpty(urlSite) || TextUtils.isEmpty(localFilePath)) {
            return false;
        }
        boolean success = false;
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(urlSite);
        HttpResponse response;
        try {
            response = client.execute(get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                if (is != null) {
                    File file = new File(localFilePath);
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    byte[] buf = new byte[1024];
                    int ch = -1;
                    while ((ch = is.read(buf)) != -1) {
                        fileOutputStream.write(buf, 0, ch);
                    }
                    fileOutputStream.flush();
                    success = true;
                    fileOutputStream.close();
                    is.close();
                }
            }
        } catch (ClientProtocolException e) {
            logger.e("downloadFile ClientProtocolException:" + e.getMessage());
            success = false;
        } catch (IOException e) {
            logger.e("downloadFile IOException:" + e.getMessage());
            success = false;
        }
        return success;
    }

    public static int getFriendColorIndex(long threadId) {
        int colIndex = (int)threadId % 7;
        logger.d("colIndex = " + colIndex);
        return colIndex;
    }

    public static int getFriendColor(int index) {
        if(index >= 0 && index < mFriendColArray.size()) {
            return mFriendColArray.get(index).intValue();
        } else {
            return R.color.bgcor1;
        }
    }

    public static int getFriendColorDescription(int index) {
        if(index >= 0 && index < mFriendColStrArray.size()) {
            return mFriendColStrArray.get(index).intValue();
        } else {
            return mFriendColStrArray.get(0).intValue();
        }
    }

    /*
     * Compare my obj id with "toUsr" and "fromUsr" and find friend's obj ID.
     */
    public static String getFriendIdFromServerData(String serverData) {
        String strUser = null;

        if(TextUtils.isEmpty(serverData)) {
            return strUser;
        }

        String strToUser = WalkArroundJsonResultParser.parseRequireCode(serverData, HttpUtil.HTTP_RESPONSE_KEY_LIKE_TO_USER);
        String strFromUser = WalkArroundJsonResultParser.parseRequireCode(serverData, HttpUtil.HTTP_RESPONSE_KEY_LIKE_FROM_USER);
        if(strToUser.equalsIgnoreCase(ProfileManager.getInstance().getCurUsrObjId())) {
            strUser = strFromUser;
        } else {
            strUser = strToUser;
        }

        return strUser;
    }

    /**
     * 取消通知消息
     */
    public static void cancelNotification(Context context, String sender, int conversationType) {
//        if (isReceiverEditable) {
//            return;
//        }

        if (conversationType == MessageConstant.ChatType.CHAT_TYPE_ONE2ONE) {
            try {
                ContactInfo contact = ContactsManager.getInstance(context).getContactByUsrObjId(sender);
                if(contact == null || TextUtils.isEmpty(contact.getMobilePhoneNumber())) {
                    return;
                }
                String number = contact.getMobilePhoneNumber();
                int startPos = number.length() > 5 ? number.length() - 5 : 0;
                int id = Integer.parseInt(number.substring(startPos));
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(number, id);
            } catch (NumberFormatException e) {
            }
        }
    }


}
