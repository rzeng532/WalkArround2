/**
 * Copyright (C) 2014-2015 All rights reserved
 */
package com.example.walkarround.message.util;

/**
 * 消息相关的广播
 * Date: 2015-06-03
 *
 * @author
 */
public class MsgBroadcastConstants {

    /*消息接收/发送*/
    public static final String ACTION_MESSAGE_STATUS_CHANGED = "com.example.walkarround.ACTION_UI_MESSAGE_STATUS_CHANGE_NOTIFY";
    public static final String ACTION_MESSAGE_NEW_RECEIVED = "com.example.walkarround.ACTION_UI_SHOW_MESSAGE_NOTIFY";
    public static final String ACTION_GROUP_MESSAGE_NEW_RECEIVED = "com.example.walkarround.ACTION_UI_SHOW_GROUP_MESSAGE_NOTIFY";
    public static final String ACTION_CONTACT_COMPOSING_INFO = "com.example.walkarround.ACTION_UI_SHOW_COMPOSING_INFO";

    /*消息发送进度*/
    public static final String ACTION_SEND_PERCENT = "com.example.walkarround.ACTION_SEND_PERCENT";

    /*下载文件*/
    public static final String ACTION_PA_MESSAGE_DOWNLOADING_CHANGE = "PA_MESSAGE_DOWNLOADING_FILE_CHANGE";
    public static final String ACTION_DOWNLOADING_FILE_CHANGE = "com.example.walkarround.UI_DOWNLOADING_FILE_CHANGE";
    public static final String ACTION_FILE_TRANSFER_PROGRESS = "ui_file_transfre_progress";
    public static final String ACTION_DOWNLOADING_FILE_FAIL = "com.example.walkarround.UI_DOWNLOADING_FILE_FAIL";
    /*群组管理*/
    public static final String ACTION_GROUP_CREATE = "com.example.walkarround.ACTION_UI_GROUP_CREATE";// 创建群
    public static final String ACTION_GROUP_CREATE_ERROR = "com.example.walkarround.ACTION_UI_GROUP_ERROR";// 创建失败
    public static final String ACTION_GROUP_INFO_CHANGED = "com.example.walkarround.ACTION_UI_GROUP_MANAGE_NOTIFY";
    public static final String ACTION_GROUP_INVITATION = "com.example.walkarround.ACTION_UI_GROUP_INVITATION";

    /*群变更消息类型*/
    public static final String GROUP_CHANGED_TYPE_UPDATE_SUBJECT = "updateSubject";
    public static final String GROUP_CHANGED_TYPE_UPDATE_REMARK = "updateRemark";
    public static final String GROUP_CHANGED_TYPE_UPDATE_ALIAS = "updateAlias";
    public static final String GROUP_CHANGED_TYPE_UPDATE_CHAIRMAN = "updateChairman";
    public static final String GROUP_CHANGED_TYPE_DELETED = "deleted";
    public static final String GROUP_CHANGED_TYPE_DEPARTED = "departed";
    public static final String GROUP_CHANGED_TYPE_BOOTED = "booted";
    public static final String GROUP_CHANGED_TYPE_CONNECTED = "connected";
    public static final String GROUP_CHANGED_TYPE_POLICY_UPDATE = "updatePolicy";
    public static final String GROUP_CHANGED_TYPE_DESTROY_GROUP = "destroyGroup";

    /*消息下载进度相关*/
    public static final String BC_VAR_DOWN_PRG_ID = "id";
    public static final String BC_VAR_TRANSFER_PRG_END = "end";
    public static final String BC_VAR_TRANSFER_PRG_TOTAL = "total";
    public static final String BC_VAR_TRANSFER_FILE_PATH = "filePath";

    /*群组相关*/
    public static final String BC_VAR_MSG_ACTION_TYPE = "actionType";
    public static final String BC_VAR_GROUP_SUBJECT = "subject";
    public static final String BC_VAR_MSG_PHONE = "phoneNumber";
    public static final String BC_VAR_ALIAS = "alias";
    public static final String BC_VAR_GROUP_ID = "groupId";
    public static final String BC_VAR_POLICY = "policy";

    /*消息接收/发送*/
    public static final String BC_VAR_THREAD_ID = "threadId";
    public static final String BC_VAR_IS_COLLECT_MSG = "IsCollectMsg";
    public static final String BC_VAR_CONTACT = "contact";
    public static final String BC_VAR_MSG_TYPE = "msgType";
    public static final String BC_VAR_MSG_EXTRA= "msgExtraInfo";
    public static final String BC_VAR_MSG_CONTENT = "tickerText";
    public static final String BC_VAR_MSG_ID = "id";
    public static final String BC_VAR_MSG_STATUS = "status";
    public static final String BC_VAR_MSG_COUNT = "msgCount";
    public static final String BC_VAR_MSG_ID_LIST = "msgIdList";
}
