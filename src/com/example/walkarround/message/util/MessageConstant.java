/**
 * Copyright (C) 2014-2015 CMCC All rights reserved
 */
package com.example.walkarround.message.util;


/**
 * 消息相关的定常量(与小溪对接)
 * Date=0; 2015-06-03
 *
 * @author mss
 */
public class MessageConstant {

    public interface MessageType {

        //SDK defined
        int MSG_TYPE_TEXT = -1;// Message.TYPE_TEXT;
        int MSG_TYPE_FILE = -6;//Message.TYPE_FILE;
        int MSG_TYPE_AUDIO = -3;//Message.TYPE_AUDIO;
        int MSG_TYPE_VIDEO = -4;//Message.TYPE_VIDEO;
        int MSG_TYPE_IMAGE = -2;//Message.TYPE_PIC;
        int MSG_TYPE_MAP = -5;//Message.TYPE_LOCATION;

        //User defined
        int MSG_TYPE_GIF = 7;//Message.TYPE_GIF;
        int MSG_TYPE_NOTIFICATION = 9;//Message.TYPE_SYSTEM_MSG;
    }

    public interface MessageSendReceive {
       int MSG_SEND = 0;//Message.MSG_SEND;
       int MSG_RECEIVE = 1;//Message.MSG_RECV;
    }

    public interface MessageReadState {
        int MSG_READ = 0;
        int MSG_UNREAD = 1;
    }


    public interface MessageState {

       int MSG_STATE_SEND_ING = 1;//AVIMMessage.AVIMMessageStatus.AVIMMessageStatusSending;
       int MSG_STATE_SENT = 2;//AVIMMessage.AVIMMessageStatus.AVIMMessageStatusSent;
       int MSG_STATE_RECEIVED = 3;//AVIMMessage.AVIMMessageStatus.;
       int MSG_STATE_SEND_FAIL = 4;//AVIMMessage.AVIMMessageStatus.AVIMMessageStatusFailed;
       int MSG_STATE_RECEIVING = 5;//Message.STATUS_RECVING;
       int MSG_STATE_RECEIVE_FAIL = 6;//Message.STATUS_RECV_FAIL;
       int MSG_STATE_SEND_DRAFT = 7;//AVIMMessage.AVIMMessageStatus.;
       int MSG_STATE_UNRECEIVE = 100;
    }

    public interface ChatType {
        int CHAT_TYPE_ONE2ONE = 1;//Conversation.TYPE_SINGLE;
        int CHAT_TYPE_GROUP = 2;//Conversation.TYPE_GROUP;
    }

    public interface GroupJoinState {
        int DEFAULT = 0;
        int AGREE = 1;
        int REFUSE = 2;
        int CAN_NOT_JOIN = 3;
    }

    public interface ConversationType {
        int GENERAL = 0;
        int PUBLIC_ACCOUNT = 1;
        int NOTICES_MSG = 2;
        int SYSTEM = 3;
    }

    public interface TopState {
        int TOP = 1;
        int NOT_TOP = 0;
    }
    public interface ResultCode {
        int SUCCESS = 0;
        int OFFLINE = 1;
        int OTHER_ERROR = 3;
        int GROUP_NOT_EXIST = 4;
        int GROUP_IS_FULL = 5;
    }

    /* 100 == NO compress, 70 == Original quality * 70% */
    public static final int MSG_IMAGE_COMPRESS_QUALITY = 100;
    public static final int MSG_IMAGE_MAXIMUM_QUALITY = 100;
    public static final int MSG_IMAGE_MINIMUM_COMPRESSING_SIZE = 512;
    public static final int MSG_IMAGE_1M_SENDING_SIZE = 1 * 1024;
    public static final int MSG_IMAGE_3M_SENDING_SIZE = 3 * 1024;
    public static final int MSG_IMAGE_5M_SENDING_SIZE = 5 * 1024;
    /* 最大可压缩的图片 */
    public static final int MSG_IMAGE_MAXIMUM_PICTURE_SIZE = 10 * 1024;
    
    
    /* 批处理操作类型 */
    public static final String MSG_OPERATION_NOTIFY_LOAD = "msg_operation_notify_load";
    public static final String MSG_OPERATION_LOAD = "msg_operation_load";
    public static final String MSG_OPERATION_REMOVE = "msg_operation_remove";
    public static final String MSG_OPERATION_SET_READ = "msg_operation_set_read"; 
    public static final String MSG_OPERATION_SET_TOP = "msg_operation_set_top"; 
    public static final String MSG_OPERATION_CANCEL_TOP = "msg_operation_cancel_top"; 
    public static final String MSG_OPERATION_ADD_BLACKLIST = "msg_operation_add_blacklist";

    /*  */
    public static final String MSG_NEW_MSG_NOTIFICATION_REC_SWITCH = "_csbNewMsgNotifyReceive";
}
