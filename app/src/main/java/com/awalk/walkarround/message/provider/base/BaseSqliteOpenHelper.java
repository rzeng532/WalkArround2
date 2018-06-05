package com.awalk.walkarround.message.provider.base;

import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

/**
 * 数据库基类
 */
public abstract class BaseSqliteOpenHelper extends SQLiteOpenHelper {

    public static final String _ID_BASE = "_id";

    private String mDatabaseName = null;

    public BaseSqliteOpenHelper(Context context, String databaseName, int databaseVersion) {
        super(context, databaseName, null, databaseVersion);
        mDatabaseName = databaseName;
    }

    public BaseSqliteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mDatabaseName = name;
    }

    public final String getDatabaseName() {
        return mDatabaseName;
    }

    @Override
    public final void onCreate(SQLiteDatabase db) {
//        db.beginTransaction();
        try {
            //Uncomment line below if you want to enable foreign keys
            //db.execSQL("PRAGMA foreign_keys=ON;");
            String[] execSQL = getExecSQLStr();
            if (execSQL != null && execSQL.length > 0) {
                for (String execStr : execSQL) {
                    db.execSQL(execStr);
                }
            }
            //Add other tables here
//            db.setTransactionSuccessful();
        } finally {
//            db.endTransaction();
        }
    }

    @Override
    public final void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        upgrade(db, oldVersion, newVersion);
    }

    protected abstract String[] getExecSQLStr();

    protected void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}