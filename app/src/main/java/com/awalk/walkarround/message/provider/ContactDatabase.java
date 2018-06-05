package com.awalk.walkarround.message.provider;

import android.content.Context;
import android.net.Uri;

import com.awalk.walkarround.message.provider.base.BaseSqliteOpenHelper;

import net.sqlcipher.database.SQLiteDatabase;

/**
 * 联系人数据库
 * Date: 2018-06-04
 *
 * @author cmcc
 */
public class ContactDatabase extends BaseSqliteOpenHelper {

    public static final String AUTHORITY = "com.awalk.walkarround.provider.contactinfo";
    public static Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public ContactDatabase(Context context, String databaseName, int databaseVersion) {
        super(context, databaseName, databaseVersion);
    }

    public ContactDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    protected String[] getExecSQLStr() {
        String sql = "CREATE TABLE " + Contact.TABLE_NAME + "("
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
                + Contact.PORTRAIT + " TEXT, "
                + Contact._DATA1 + " TEXT, "
                + Contact._DATA2 + " TEXT, "
                + Contact._DATA3 + " TEXT, "
                + Contact._DATA4 + " TEXT, "
                + Contact._DATA5 + " TEXT"
                + " );";
        return new String[]{sql};
    }

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
        public static final String PORTRAIT = "portrait";
        /*冗余字段便于以后扩充*/
        public static final String _DATA1 = "_data1";
        public static final String _DATA2 = "_data2";
        public static final String _DATA3 = "_data3";
        public static final String _DATA4 = "_data4";
        public static final String _DATA5 = "_data5";
    }

}
