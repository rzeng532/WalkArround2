/**
 * Copyright (C) 2014-2015 All rights reserved
 */
package com.awalk.walkarround.message.provider;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import com.awalk.walkarround.base.WalkArroundApp;
import com.awalk.walkarround.util.AppSharedPreference;
import com.awalk.walkarround.util.Logger;

/**
 * 消息数据库管理
 *
 */
public class MessageProvider extends ContentProvider {

    private static final Logger logger = Logger.getLogger(MessageProvider.class.getSimpleName());

    private static final int URI_CODE_MSG_SINGLE = 1;
    private static final int URI_CODE_MSG_COLLECTION = 2;
    private static final int URI_CODE_GROUP_SINGLE = 3;
    private static final int URI_CODE_GROUP_COLLECTION = 4;
    private static final int URI_CODE_CONVERSATION_SINGLE = 5;
    private static final int URI_CODE_CONVERSATION_COLLECTION = 6;
    private static final int URI_CODE_GROUP_INVITATION_SINGLE = 7;
    private static final int URI_CODE_GROUP_INVITATION_COLLECTION = 8;

    private static final String CONTENT_MSG_TYPE = "vnd.android.cursor.dir/walkarround_message.message";
    private static final String CONTENT_MSG_ITEM_TYPE = "vnd.android.cursor.item/walkarround_message.message";
    private static final String CONTENT_GROUP_TYPE = "vnd.android.cursor.dir/walkarround_message.groups";
    private static final String CONTENT_GROUP_ITEM_TYPE = "vnd.android.cursor.item/walkarround_message.groups";
    private static final String CONTENT_CONVERSATION_TYPE = "vnd.android.cursor.dir/walkarround_message.conversations";
    private static final String CONTENT_CONVERSATION_ITEM_TYPE = "vnd.android.cursor.item/walkarround_message.conversations";
    private static final String CONTENT_GROUP_INVITATION_TYPE = "vnd.android.cursor.dir/walkarround_message.invitation";
    private static final String CONTENT_GROUP_INVITATION_ITEM_TYPE = "vnd.android.cursor.item/walkarround_message.invitation";

    private static final String DEFAULT_DATABASE_NAME = "walkarround_message.db";
    private final static String MSG_DATABASE_SHARED_PREFERENCES = "msgDatabase";
    private final static String MSG_DATABASE_NAME = "databaseName";
    private static final int DATABASE_VERSION = 1;

    private static final UriMatcher sUriMatcher;

    private MessageDatabase mOpenHelper;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(MessageDatabase.AUTHORITY, MessageDatabase.Message.TABLE_NAME, URI_CODE_MSG_COLLECTION);
        sUriMatcher.addURI(MessageDatabase.AUTHORITY, MessageDatabase.Message.TABLE_NAME + "/#", URI_CODE_MSG_SINGLE);
        //sUriMatcher.addURI(MessageDatabase.AUTHORITY, MessageDatabase.Group.TABLE_NAME, URI_CODE_GROUP_COLLECTION);
        //sUriMatcher.addURI(MessageDatabase.AUTHORITY, MessageDatabase.Group.TABLE_NAME + "/#", URI_CODE_GROUP_SINGLE);
        sUriMatcher.addURI(MessageDatabase.AUTHORITY, MessageDatabase.Conversation.TABLE_NAME, URI_CODE_CONVERSATION_COLLECTION);
        sUriMatcher.addURI(MessageDatabase.AUTHORITY, MessageDatabase.Conversation.TABLE_NAME + "/#", URI_CODE_CONVERSATION_SINGLE);
        //sUriMatcher.addURI(MessageDatabase.AUTHORITY, MessageDatabase.GroupInvitationMsg.TABLE_NAME, URI_CODE_GROUP_INVITATION_COLLECTION);
        //sUriMatcher.addURI(MessageDatabase.AUTHORITY, MessageDatabase.GroupInvitationMsg.TABLE_NAME + "/#", URI_CODE_GROUP_INVITATION_SINGLE);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String table = getTableName(uri);
        if (table == null) {
            return null;
        }
        String whereClause = appendWhere(uri, selection);
        SQLiteDatabase db = getDatabase();
        return db.query(table, projection, whereClause, selectionArgs, null, null, sortOrder);
    }

    @Override
    public String getType(Uri uri) {
        String result = null;
        switch (sUriMatcher.match(uri)) {
            case URI_CODE_MSG_COLLECTION:
                result = CONTENT_MSG_TYPE;
                break;
            case URI_CODE_MSG_SINGLE:
                result = CONTENT_MSG_ITEM_TYPE;
                break;
            case URI_CODE_GROUP_COLLECTION:
                result = CONTENT_GROUP_TYPE;
                break;
            case URI_CODE_GROUP_SINGLE:
                result = CONTENT_GROUP_ITEM_TYPE;
                break;
            case URI_CODE_CONVERSATION_COLLECTION:
                result = CONTENT_CONVERSATION_TYPE;
                break;
            case URI_CODE_CONVERSATION_SINGLE:
                result = CONTENT_CONVERSATION_ITEM_TYPE;
                break;
            case URI_CODE_GROUP_INVITATION_SINGLE:
                result = CONTENT_GROUP_INVITATION_ITEM_TYPE;
                break;
            case URI_CODE_GROUP_INVITATION_COLLECTION:
                result = CONTENT_GROUP_INVITATION_TYPE;
                break;
            default:
                logger.e("Unknown URI :" + uri);
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return result;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        String table = getTableName(uri);
        if (table == null) {
            return null;
        }
        boolean bNeedNotify = false;
        switch (sUriMatcher.match(uri)) {
            case URI_CODE_MSG_COLLECTION:
            case URI_CODE_MSG_SINGLE:
                bNeedNotify = true;
                break;
        }
        try {
            SQLiteDatabase db = getDatabase();
            long rowId = db.insert(table, null, contentValues);
            if (rowId > 0) {
                Uri insertedBookUri = ContentUris.withAppendedId(uri, rowId);
                getContext().getContentResolver().notifyChange(insertedBookUri, null);
                if (bNeedNotify) {
                    getContext().getContentResolver().notifyChange(MessageDatabase.Conversation.CONTENT_URI, null);
                }
                return insertedBookUri;
            }
        } catch (Exception e) {
            logger.e("Failed to insert row into :" + uri + ". Exception: " + e.getMessage());
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        String table = getTableName(uri);
        if (table == null) {
            return 0;
        }
        boolean bNeedNotify = false;
        switch (sUriMatcher.match(uri)) {
            case URI_CODE_MSG_COLLECTION:
            case URI_CODE_MSG_SINGLE:
                bNeedNotify = true;
                break;
        }
        String whereClause = appendWhere(uri, where);
        SQLiteDatabase db = getDatabase();
        int rows = db.delete(table, whereClause, whereArgs);

        if (bNeedNotify) {
            getContext().getContentResolver().notifyChange(MessageDatabase.Conversation.CONTENT_URI, null);
        }
        return rows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        String table = getTableName(uri);
        if (table == null) {
            return 0;
        }
        String whereClause = appendWhere(uri, where);
        SQLiteDatabase db = getDatabase();
        return db.update(table, values, whereClause, whereArgs);
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        if (values == null) {
            return 0;
        }
        String table = getTableName(uri);
        if (table == null) {
            return 0;
        }
        int count = 0;
        try {
            SQLiteDatabase db = getDatabase();
            db.beginTransaction();
            for (ContentValues value : values) {
                long rowId = db.insert(table, null, value);
                if (rowId > 0) {
                    count++;
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            logger.e("bulkInsert Exception: " + e.getMessage());
        }
        return count;
    }

    /**
     * 获取数据库对象
     *
     * @return
     */
    private SQLiteDatabase getDatabase() {
        String databaseName = getAccountDatabaseName();
        if (mOpenHelper == null) {
            mOpenHelper = new MessageDatabase(getContext(), databaseName, DATABASE_VERSION);
        } else if (!databaseName.equals(mOpenHelper.getDatabaseName())) {
            mOpenHelper = new MessageDatabase(getContext(), databaseName, DATABASE_VERSION);
        }
        return mOpenHelper.getWritableDatabase();
    }

    private String getAccountDatabaseName() {
        return AppSharedPreference.getString(AppSharedPreference.ACCOUNT_PHONE, "");
//        Context context = WalkArroundApp.getInstance().getApplicationContext();
//        if (context == null) {
//            return DEFAULT_DATABASE_NAME;
//        }
//        return context.getSharedPreferences(MSG_DATABASE_SHARED_PREFERENCES,
//                Context.MODE_PRIVATE).getString(MSG_DATABASE_NAME, DEFAULT_DATABASE_NAME);
    }

    public static void setAccountName(String account) {
        Context context = WalkArroundApp.getInstance().getApplicationContext();
        if (context == null) {
            return;
        }
        SharedPreferences.Editor editor = context.getSharedPreferences(MSG_DATABASE_SHARED_PREFERENCES,
                Context.MODE_PRIVATE).edit();
        editor.putString(MSG_DATABASE_NAME, account + "_message.db");
        editor.commit();
    }

    /**
     * @param uri
     * @param where
     * @return
     */
    private String appendWhere(Uri uri, String where) {
        String whereClause = where;
        switch (sUriMatcher.match(uri)) {
            case URI_CODE_MSG_SINGLE:
            case URI_CODE_GROUP_SINGLE:
                long rowId = ContentUris.parseId(uri);
                whereClause = MessageDatabase._ID_BASE + "=" + rowId;
                if (!TextUtils.isEmpty(where)) {
                    whereClause += " AND ( " + where + ')';
                }
                break;
            default:
                break;
        }

        return whereClause;
    }

    /**
     * 获取操作表名
     *
     * @param uri
     * @return
     */
    private String getTableName(Uri uri) {

        String table = null;
        switch (sUriMatcher.match(uri)) {
            case URI_CODE_MSG_COLLECTION:
            case URI_CODE_MSG_SINGLE:
                table = MessageDatabase.Message.TABLE_NAME;
                break;
            case URI_CODE_GROUP_COLLECTION:
            case URI_CODE_GROUP_SINGLE:
                //table = MessageDatabase.Group.TABLE_NAME;
                break;
            case URI_CODE_CONVERSATION_COLLECTION:
            case URI_CODE_CONVERSATION_SINGLE:
                table = MessageDatabase.Conversation.TABLE_NAME;
                break;
            case URI_CODE_GROUP_INVITATION_COLLECTION:
            case URI_CODE_GROUP_INVITATION_SINGLE:
                //table = MessageDatabase.GroupInvitationMsg.TABLE_NAME;
                break;
            default:
                break;
        }
        return table;
    }
}
