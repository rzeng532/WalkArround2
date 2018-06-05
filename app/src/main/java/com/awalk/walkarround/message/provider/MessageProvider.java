/**
 * Copyright (C) 2014-2015 All rights reserved
 */
package com.awalk.walkarround.message.provider;

import android.content.Context;
import android.content.UriMatcher;
import android.net.Uri;

import com.awalk.walkarround.message.provider.base.BaseContentProvider;
import com.awalk.walkarround.message.provider.base.BaseSqliteOpenHelper;
import com.awalk.walkarround.util.AppSharedPreference;
import com.awalk.walkarround.util.Logger;

/**
 * 消息数据库管理
 *
 */
public class MessageProvider extends BaseContentProvider {

    private static final Logger logger = Logger.getLogger(MessageProvider.class.getSimpleName());

    private static final int URI_CODE_MSG_SINGLE = 1;
    private static final int URI_CODE_MSG_COLLECTION = 2;
    private static final int URI_CODE_CONVERSATION_SINGLE = 3;
    private static final int URI_CODE_CONVERSATION_COLLECTION = 4;

    private static final String CONTENT_MSG_TYPE = "vnd.android.cursor.dir/walkarround_message.message";
    private static final String CONTENT_MSG_ITEM_TYPE = "vnd.android.cursor.item/walkarround_message.message";
    private static final String CONTENT_CONVERSATION_TYPE = "vnd.android.cursor.dir/walkarround_message.conversations";
    private static final String CONTENT_CONVERSATION_ITEM_TYPE = "vnd.android.cursor.item/walkarround_message.conversations";

    private static final String DEFAULT_DATABASE_NAME = "walkarround_message.db";
    private static final int DATABASE_VERSION = 1;

    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(MessageDatabase.AUTHORITY, MessageDatabase.Message.TABLE_NAME, URI_CODE_MSG_COLLECTION);
        sUriMatcher.addURI(MessageDatabase.AUTHORITY, MessageDatabase.Message.TABLE_NAME + "/#", URI_CODE_MSG_SINGLE);
        sUriMatcher.addURI(MessageDatabase.AUTHORITY, MessageDatabase.Conversation.TABLE_NAME, URI_CODE_CONVERSATION_COLLECTION);
        sUriMatcher.addURI(MessageDatabase.AUTHORITY, MessageDatabase.Conversation.TABLE_NAME + "/#", URI_CODE_CONVERSATION_SINGLE);
    }

    private String getAccountDatabaseName() {
        return AppSharedPreference.getString(AppSharedPreference.ACCOUNT_PHONE, DEFAULT_DATABASE_NAME);
    }

    @Override
    protected String getTableName(Uri uri) {
        String table = null;
        switch (sUriMatcher.match(uri)) {
            case URI_CODE_MSG_COLLECTION:
            case URI_CODE_MSG_SINGLE:
                table = MessageDatabase.Message.TABLE_NAME;
                break;
            case URI_CODE_CONVERSATION_COLLECTION:
            case URI_CODE_CONVERSATION_SINGLE:
                table = MessageDatabase.Conversation.TABLE_NAME;
                break;
            default:
                break;
        }
        return table;
    }

    @Override
    protected String getUriType(Uri uri) {
        String result = null;
        switch (sUriMatcher.match(uri)) {
            case URI_CODE_MSG_COLLECTION:
                result = CONTENT_MSG_TYPE;
                break;
            case URI_CODE_MSG_SINGLE:
                result = CONTENT_MSG_ITEM_TYPE;
                break;
            case URI_CODE_CONVERSATION_COLLECTION:
                result = CONTENT_CONVERSATION_TYPE;
                break;
            case URI_CODE_CONVERSATION_SINGLE:
                result = CONTENT_CONVERSATION_ITEM_TYPE;
                break;
            default:
                logger.e("Unknown URI :" + uri);
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return result;
    }

    @Override
    protected String getDatabaseName() {
        return getAccountDatabaseName();
    }

    @Override
    protected int getDatabaseVersion() {
        return DATABASE_VERSION;
    }

    @Override
    protected BaseSqliteOpenHelper getDatabaseOpenHelper(Context context, String databaseName, int databaseVision) {
        return new MessageDatabase(context, databaseName, databaseVision);
    }
}
