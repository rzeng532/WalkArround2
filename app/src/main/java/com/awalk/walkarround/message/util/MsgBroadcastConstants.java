/**
 * Copyright (C) 2014-2015 All rights reserved
 */
package com.awalk.walkarround.message.util;

/**
 * 消息相关的广播
 * Date: 2015-06-03
 *
 * @author
 */
public class MsgBroadcastConstants {

    /*消息接收/发送*/
    public static final String ACTION_MESSAGE_STATUS_CHANGED = "com.awalk.walkarround.ACTION_UI_MESSAGE_STATUS_CHANGE_NOTIFY";
    public static final String ACTION_MESSAGE_NEW_RECEIVED = "com.awalk.walkarround.ACTION_UI_SHOW_MESSAGE_NOTIFY";
    public static final String ACTION_CONTACT_COMPOSING_INFO = "com.awalk.walkarround.ACTION_UI_SHOW_COMPOSING_INFO";

    /*消息发送进度*/
    public static final String ACTION_SEND_PERCENT = "com.awalk.walkarround.ACTION_SEND_PERCENT";

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
