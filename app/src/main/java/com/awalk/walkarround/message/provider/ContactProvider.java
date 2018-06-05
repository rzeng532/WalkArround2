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
 * 联系人数据库管理
 *
 */
public class ContactProvider extends BaseContentProvider {

    private static final Logger logger = Logger.getLogger(ContactProvider.class.getSimpleName());

    private static final int URI_CODE_CONTACT_SINGLE = 1;
    private static final int URI_CODE_CONTACT_COLLECTION = 2;

    private static final String CONTENT_CONTACT_TYPE = "vnd.android.cursor.dir/walkarround_contact.contactinfo";
    private static final String CONTENT_CONTACT_ITEM_TYPE = "vnd.android.cursor.item/walkarround_contact.contactinfo";

    private static final String DEFAULT_DATABASE_NAME = "walkarround";
    private static final int DATABASE_VERSION = 1;

    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(ContactDatabase.AUTHORITY, ContactDatabase.Contact.TABLE_NAME, URI_CODE_CONTACT_COLLECTION);
        sUriMatcher.addURI(ContactDatabase.AUTHORITY, ContactDatabase.Contact.TABLE_NAME + "/#", URI_CODE_CONTACT_SINGLE);
    }

    private String getAccountDatabaseName() {
        String database = AppSharedPreference.getString(AppSharedPreference.ACCOUNT_PHONE, DEFAULT_DATABASE_NAME);
        return database + "_contact";
    }

    @Override
    protected String getTableName(Uri uri) {
        String table = null;
        switch (sUriMatcher.match(uri)) {
            case URI_CODE_CONTACT_COLLECTION:
            case URI_CODE_CONTACT_SINGLE:
                table = ContactDatabase.Contact.TABLE_NAME;
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
            case URI_CODE_CONTACT_COLLECTION:
                result = CONTENT_CONTACT_TYPE;
                break;
            case URI_CODE_CONTACT_SINGLE:
                result = CONTENT_CONTACT_ITEM_TYPE;
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
        return new ContactDatabase(context, databaseName, databaseVision);
    }
}
