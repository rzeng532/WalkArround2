package com.example.walkarround.message.provider;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

/**
 * Created by Richard on 16/3/13.
 */
public class ContactInfoDatabase extends SQLiteOpenHelper {
    public static final String AUTHORITY = "com.example.walkarround.provider.contactinfo";
    public static Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public static final String _ID_BASE = "_id";

    public static class Contact {
        public static final String TABLE_NAME = "contactinfo";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE_NAME);

        public static final String _ID = _ID_BASE;
        public static final String SIGNATURE = "signature";
        public static final String USERNAME = "username";
        public static final String BIRTHDAY = "birth";
        public static final String EMAILVERIFIED = "emailverified";
        public static final String MOBILEPHONENUMBER = "mobilephonenumber";
        public static final String GENDER = "gender";
        public static final String MOBILEPHONEVERIFIED = "mobileverified";
        public static final String OBJECTID = "objid";
        public static final String CREATEDAT = "createtime";
        public static final String UPDATEAT = "updatetime";
        /*冗余字段便于以后扩充*/
        public static final String _DATA1 = "_data1";
        public static final String _DATA2 = "_data2";
        public static final String _DATA3 = "_data3";
        public static final String _DATA4 = "_data4";
        public static final String _DATA5 = "_data5";
    }

    public ContactInfoDatabase(Context context, String databaseName, int databaseVersion) {
        super(context, databaseName, null, databaseVersion);
    }

    public ContactInfoDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public ContactInfoDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + Contact.TABLE_NAME + "("
                + Contact._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Contact.SIGNATURE + " TEXT, "
                + Contact.USERNAME + " TEXT, "
                + Contact.BIRTHDAY + " TEXT, "
                + Contact.EMAILVERIFIED + " INTEGER DEFAULT 0, "
                + Contact.MOBILEPHONENUMBER + " TEXT, "
                + Contact.GENDER + " INTEGER DEFAULT 0, "
                + Contact.MOBILEPHONEVERIFIED + " INTEGER DEFAULT 0, "
                + Contact.OBJECTID + " TEXT, "
                + Contact.CREATEDAT + " TEXT, "
                + Contact.UPDATEAT + " TEXT, "
                + Contact._DATA1 + " TEXT, "
                + Contact._DATA2 + " TEXT, "
                + Contact._DATA3 + " TEXT, "
                + Contact._DATA4 + " TEXT, "
                + Contact._DATA5 + " TEXT"
                + " );");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
