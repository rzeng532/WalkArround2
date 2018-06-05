/*
 *  Copyright (C) 2014-2015 CMCC All rights reserved
 */
package com.awalk.walkarround.message.provider.base;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

/**
 * 消息数据库管理
 * Date: 2015-06-04
 *
 * @author mss
 */
public abstract class BaseContentProvider extends ContentProvider {

    private BaseSqliteOpenHelper mOpenHelper;

    @Override
    public final boolean onCreate() {
        return true;
    }

    @Override
    public final Cursor query(@NonNull Uri uri, String[] projection, String selection,
                              String[] selectionArgs, String sortOrder) {
        String table = getTableName(uri);
        if (table == null) {
            return null;
        }
        SQLiteDatabase db = getDatabase();
        return db.query(table, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public final String getType(@NonNull Uri uri) {
        return getUriType(uri);
    }

    @Override
    public final Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        String table = getTableName(uri);
        if (table == null) {
            return null;
        }
        try {
            SQLiteDatabase db = getDatabase();
            long rowId = db.insert(table, null, contentValues);
            if (rowId > 0) {
                Uri insertedBookUri = ContentUris.withAppendedId(uri, rowId);
                getContext().getContentResolver().notifyChange(insertedBookUri, null);
                return insertedBookUri;
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public final int delete(@NonNull Uri uri, String where, String[] whereArgs) {
        String table = getTableName(uri);
        if (table == null) {
            return 0;
        }
        SQLiteDatabase db = getDatabase();
        return db.delete(table, where, whereArgs);
    }

    @Override
    public final int update(@NonNull Uri uri, ContentValues values, String where, String[] whereArgs) {
        String table = getTableName(uri);
        if (table == null) {
            return 0;
        }
        SQLiteDatabase db = getDatabase();
        return db.update(table, values, where, whereArgs);
    }

    @Override
    public final int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
//        if (values == null) {
//            return 0;
//        }
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
        }
        return count;
    }

    @NonNull
    @Override
    public final ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        SQLiteDatabase db = getDatabase();
        db.beginTransaction();// 开始事务
        try {
            ContentProviderResult[] results = super.applyBatch(operations);
            db.setTransactionSuccessful();// 设置事务标记为successful
            return results;
        } finally {
            db.endTransaction();// 结束事务
        }
    }

    /**
     * 获取数据库对象
     *
     * @return
     */
    private SQLiteDatabase getDatabase() {
        String databaseName = getDatabaseName();
        if (mOpenHelper == null) {
            SQLiteDatabase.loadLibs(getContext());
            mOpenHelper = getDatabaseOpenHelper(getContext(), databaseName, getDatabaseVersion());
        } else if (!databaseName.equals(mOpenHelper.getDatabaseName())) {
            mOpenHelper = getDatabaseOpenHelper(getContext(), databaseName, getDatabaseVersion());
        }
        return mOpenHelper.getWritableDatabase(databaseName);
    }

    /**
     * 获取操作表名
     *
     * @param uri
     * @return
     */
    protected abstract String getTableName(Uri uri);

    protected abstract String getUriType(Uri uri);

    protected abstract String getDatabaseName();

    protected abstract int getDatabaseVersion();

    protected abstract BaseSqliteOpenHelper getDatabaseOpenHelper(Context context, String databaseName, int databaseVision);
}
