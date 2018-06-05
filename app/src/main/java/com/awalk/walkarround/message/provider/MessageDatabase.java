/**
 * Copyright (C) 2014-2015 All rights reserved
 */
package com.awalk.walkarround.message.provider;

import android.content.Context;
import android.net.Uri;

import com.awalk.walkarround.message.provider.base.BaseSqliteOpenHelper;
import com.awalk.walkarround.message.util.MessageConstant.MessageState;

/**
 * 消息数据库
 * Date:
 */
public class MessageDatabase extends BaseSqliteOpenHelper {

    public static final String AUTHORITY = "com.awalk.walkarround.provider.message";
    public static Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public static final String _ID_BASE = "_id";

    public static class Message {
        public static final String TABLE_NAME = "message";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE_NAME);

        public static final String _ID = _ID_BASE;
        public static final String _PACKET_ID = "_packet_id";
        public static final String _CONVERSATION_ID = "_conversation_id";
        public static final String _CHAT_TYPE = "_chat_type";
        public static final String _CONTENT_TYPE = "_content_type";
        public static final String _CONTENT = "_content";
        public static final String _STATUS = "_status";
        public static final String _READ = "_read";
        public static final String _HIDE = "_hide";
        public static final String _SEND_RECV = "_send_recv";
        public static final String _ADDRESS = "_address";
        public static final String _SENDER = "_sender";
        public static final String _SEND_TIME = "_time";
        //消息创建时间
        public static final String _PLAN_SEND_TIME = "_create_time";
        public static final String _FILELENGTH = "_filelength";
        public static final String _FILENAME = "_filename";
        public static final String _DURATION = "_duration";
        public static final String _FILE_PATH = "_file_path";
        public static final String _THUMBNAIL_PATH = "_thumbnail_path";
        public static final String _ORIGINAL_URL = "_original_url";
        public static final String _THUMBNAIL_URL = "_thumbnail_url";
        public static final String _SMALL_URL = "_small_url";

        public static final String _LATITUDE = "_latitude";
        public static final String _LONGITUDE = "_longitude";
        public static final String _LOCATION_ADDRESS = "_location_address";
        public static final String _EXTRA_INFO = "_extra_info";

        /*冗余字段便于以后扩充*/
        public static final String _DATA1 = "_data1";
        public static final String _DATA2 = "_data2";
        public static final String _DATA3 = "_data3";
        public static final String _DATA4 = "_data4";
        public static final String _DATA5 = "_data5";

        public static final int MSG_READ = 1;// 已读
        public static final int MSG_UNREAD = 0;// 未读

        public static final int NOT_HIDE = 0;
        public static final int HIDE = 1;
    }

    public static class Conversation {
        public static final String TABLE_NAME = "conversation";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE_NAME);

        public static final String _ID = "_id";
        public static final String _MSG_ID = "_msg_id";
        // public static final String _RECIPIENT_ID = "_recipient_id";
        public static final String _RECIPIENT_ADDRESS = "_recipient_address";
        public static final String _TYPE = "_type";
        public static final String _TOP = "_top";
        public static final String _HIDE = "_hide";
        public static final String _MSG_CONTENT_TYPE = "_msg_content_type";
        public static final String _MSG_CONTENT = "_msg_content";
        public static final String _MSG_STATUS = "_msg_status";
        public static final String _MSG_SEND_RECV = "_msg_send_recv";
        public static final String _TOTAL_COUNT = "_total_count";
        public static final String _READ = "_read";
        public static final String _UNREAD_COUNT = "_unread_count";
        public static final String _DATE = "_date";
        public static final String _DRAFT_MSG_CONTENT = "_draft_content";
        public static final String _DRAFT_MSG_TIME = "_draft_msg_time";
        public static final String _CONVERSATION_STATUS = "_conversation_status";
        public static final String _COLOR = "_color";

        /*冗余字段便于以后扩充*/
        public static final String _DATA1 = "_data1"; // 是否通知类号码会话
        public static final String _DATA2 = "_data2";
        public static final String _DATA3 = "_data3";
        public static final String _DATA4 = "_data4";
        public static final String _DATA5 = "_data5";

        public static final int NOT_TOP = 0;
        public static final int TOP = 1;

        public static final int NOT_HIDE = Message.NOT_HIDE;
        public static final int HIDE = Message.HIDE;

    }

    public MessageDatabase(Context context, String databaseName, int databaseVersion) {
        super(context, databaseName, null, databaseVersion);
    }

    @Override
    protected String[] getExecSQLStr() {
        String messageSql = "CREATE TABLE " + Message.TABLE_NAME + "("
                + Message._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Message._CONVERSATION_ID + " LONG NOT NULL, "
                + Message._PACKET_ID + " TEXT, "
                + Message._CONTENT_TYPE + " INTEGER NOT NULL, "
                + Message._CONTENT + " TEXT, "
                + Message._STATUS + " INTEGER NOT NULL, "
                + Message._READ + " INTEGER NOT NULL, "
                + Message._HIDE + " INTEGER DEFAULT " + Message.NOT_HIDE + ", "
                + Message._SEND_RECV + " INTEGER NOT NULL, "
                + Message._ADDRESS + " TEXT, "
                + Message._SENDER + " TEXT, "
                + Message._SEND_TIME + " INTEGER DEFAULT 0, "
                + Message._PLAN_SEND_TIME + " INTEGER DEFAULT 0, "
                + Message._CHAT_TYPE + " INTEGER DEFAULT 0, "
                + Message._FILELENGTH + " INTEGER, "
                + Message._FILENAME + " TEXT, "
                + Message._DURATION + " INTEGER, "
                + Message._ORIGINAL_URL + " TEXT, "
                + Message._THUMBNAIL_URL + " TEXT, "
                + Message._FILE_PATH + " TEXT, "
                + Message._THUMBNAIL_PATH + " TEXT, "
                + Message._SMALL_URL + " TEXT, "
                + Message._EXTRA_INFO + " TEXT, "
                + Message._LATITUDE + " TEXT, "
                + Message._LONGITUDE + " TEXT, "
                + Message._LOCATION_ADDRESS + " TEXT, "
                + Message._DATA1 + " TEXT, "
                + Message._DATA2 + " TEXT, "
                + Message._DATA3 + " TEXT, "
                + Message._DATA4 + " TEXT, "
                + Message._DATA5 + " TEXT"
                + " );";
        String conversationSql = "CREATE TABLE "
                + Conversation.TABLE_NAME + "("
                + Conversation._ID + " INTEGER PRIMARY KEY, "
                + Conversation._MSG_ID + " INTEGER DEFAULT 0, "
                + // one conversation may have many recipients
                Conversation._RECIPIENT_ADDRESS + " TEXT NOT NULL, "
                + Conversation._TYPE + " INTEGER, "
                + Conversation._TOP + " INTEGER DEFAULT "
                + Conversation.TOP + ", "
                + Conversation._HIDE + " INTEGER DEFAULT "
                + Conversation.NOT_HIDE + ", "
                + Conversation._DRAFT_MSG_CONTENT + " TEXT, "
                + Conversation._DRAFT_MSG_TIME + " INTEGER, "
                + Conversation._MSG_CONTENT_TYPE + " INTEGER, "
                + Conversation._MSG_CONTENT + " TEXT, "
                + Conversation._MSG_STATUS + " INTEGER DEFAULT " + MessageState.MSG_STATE_SENT + ", "
                + Conversation._MSG_SEND_RECV + " INTEGER, "
                + Conversation._TOTAL_COUNT + " INTEGER DEFAULT 0, "
                + Conversation._READ + " INTEGER DEFAULT " + Message.MSG_READ + ", "
                + Conversation._UNREAD_COUNT + " INTEGER DEFAULT 0, "
                + Conversation._CONVERSATION_STATUS + " INTEGER DEFAULT 2, " //Default: MessageUtil.WalkArroundState.STATE_IM
                + Conversation._COLOR + " INTEGER DEFAULT -1, "
                + Conversation._DATE + " INTEGER DEFAULT 0, "
                + Conversation._DATA1 + " TEXT, "
                + Conversation._DATA2 + " TEXT, "
                + Conversation._DATA3 + " TEXT, "
                + Conversation._DATA4 + " TEXT, "
                + Conversation._DATA5 + " TEXT"
                + " );";
        String addUpdateSql = "CREATE TRIGGER update_conversation_after_insert_message AFTER INSERT ON "
                + Message.TABLE_NAME
                + " BEGIN "
                +
                // update conversation's message info
                "   UPDATE " + Conversation.TABLE_NAME + " SET "
                + Conversation._MSG_ID + " = new." + Message._ID + ", "
                + Conversation._DATE + " = new." + Message._SEND_TIME + ", "
                + Conversation._MSG_CONTENT + " = new." + Message._CONTENT + ", "
                + Conversation._MSG_CONTENT_TYPE + " = new." + Message._CONTENT_TYPE + ", "
                + Conversation._MSG_STATUS + " = new." + Message._STATUS + ", "
                + Conversation._MSG_SEND_RECV + " = new." + Message._SEND_RECV + ", "
                + Conversation._HIDE + " = new." + Message._HIDE
                + " WHERE " + Conversation._ID + " = new." + Message._CONVERSATION_ID
                + " AND (" + Conversation._DATE + " is null OR " + Conversation._DATE + " < new." + Message._SEND_TIME + ")"
                + " ; END;";

        String deleteUpdateSql = "CREATE TRIGGER update_conversation_after_delete_message AFTER DELETE ON "
                + Message.TABLE_NAME + " BEGIN  UPDATE " + Conversation.TABLE_NAME
                + " SET "
                //expr
                + Conversation._MSG_CONTENT + " = (" +
                " SELECT " + Message.TABLE_NAME + "." + Message._CONTENT + " FROM " + Message.TABLE_NAME
                + " WHERE " + Message._CONVERSATION_ID + " = old." + Message._CONVERSATION_ID
                + " ORDER BY " + Message._SEND_TIME + " DESC LIMIT 1 OFFSET 0), "
                //expr
                + Conversation._MSG_STATUS + " = (" +
                " SELECT " + Message.TABLE_NAME + "." + Message._STATUS + " FROM " + Message.TABLE_NAME
                + " WHERE " + Message._CONVERSATION_ID + " = old." + Message._CONVERSATION_ID
                + " ORDER BY " + Message._SEND_TIME + " DESC LIMIT 1 OFFSET 0), "
                //expr
                + Conversation._DATE + " = (" +
                " SELECT " + Message.TABLE_NAME + "." + Message._SEND_TIME + " FROM " + Message.TABLE_NAME
                + " WHERE " + Message._CONVERSATION_ID + " = old." + Message._CONVERSATION_ID
                + " ORDER BY " + Message._SEND_TIME + " DESC LIMIT 1 OFFSET 0), "
                //expr
                + Conversation._HIDE + " = (" +
                " SELECT " + Message.TABLE_NAME + "." + Message._HIDE + " FROM " + Message.TABLE_NAME
                + " WHERE " + Message._CONVERSATION_ID + " = old." + Message._CONVERSATION_ID
                + " ORDER BY " + Message._SEND_TIME + " DESC LIMIT 1 OFFSET 0), "
                //expr
                + Conversation._MSG_ID + " = (" +
                " SELECT " + Message.TABLE_NAME + "." + Message._ID + " FROM " + Message.TABLE_NAME
                + " WHERE " + Message._CONVERSATION_ID + " = old." + Message._CONVERSATION_ID
                + " ORDER BY " + Message._SEND_TIME + " DESC LIMIT 1 OFFSET 0), "
                //expr
                + Conversation._MSG_CONTENT_TYPE + " = (" +
                " SELECT " + Message.TABLE_NAME + "." + Message._CONTENT_TYPE + " FROM " + Message.TABLE_NAME
                + " WHERE " + Message._CONVERSATION_ID + " = old." + Message._CONVERSATION_ID
                + " ORDER BY " + Message._SEND_TIME + " DESC LIMIT 1 OFFSET 0)"
                + " WHERE " + Conversation._ID + " = old." + Message._CONVERSATION_ID + " ; END;";
        String deleteAllUpdateSql = "CREATE TRIGGER delete_conversation_after_delete_entire_message AFTER DELETE ON " + Message.TABLE_NAME
                + " WHEN ( SELECT COUNT ( " + Message._ID + " ) FROM " + Message.TABLE_NAME + " WHERE " + Message._CONVERSATION_ID + " = old." + Message._CONVERSATION_ID + " ) = 0"
                + " BEGIN  DELETE FROM " + Conversation.TABLE_NAME
                + " WHERE " + Conversation._ID + " = old." + Message._CONVERSATION_ID + " AND " + Conversation._TOP + " = " + Conversation.NOT_TOP
                + " AND ( " + Conversation._TYPE + " = 0 " + " OR " + Conversation._TYPE + " = 2 " + " ) ; END;";
        return new String[]{messageSql, conversationSql, addUpdateSql, deleteUpdateSql, deleteAllUpdateSql};
    }

}
