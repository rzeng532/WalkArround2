package com.example.walkarround.message.manager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.example.walkarround.main.model.ContactInfo;
import com.example.walkarround.message.provider.ContactInfoDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by cmcc on 16/3/1.
 */
public class ContactsManager {

    private static ContactsManager mInstance;
    private HashMap<String, ContactInfo> mUserMap = new HashMap<>();
    private static final int DATABASE_VERSION = 1;
    private Context mContext;

    SQLiteDatabase dbContactInfo;

    private ContactsManager(Context context) {
        this.dbContactInfo = (new ContactInfoDatabase(context, ContactInfoDatabase.Contact.TABLE_NAME, DATABASE_VERSION)).getWritableDatabase();
    }

    public static ContactsManager getInstance(Context context) {
        if(mInstance == null) {
            synchronized(ContactsManager.class) {
                if(mInstance == null) {
                    mInstance = new ContactsManager(context);
                }
            }
        }

        return mInstance;
    }

    public ContactInfo getContactByUsrObjId(String userId) {
        ContactInfo userInfo = null;

        if(mInstance.mUserMap != null && !TextUtils.isEmpty(userId)) {
            userInfo = mInstance.mUserMap.get(userId);
        }

        return userInfo;
    }

    public void addContactInfo(ContactInfo addOne) {
        if(mInstance.mUserMap != null) {
            mInstance.mUserMap.put(addOne.getObjectId(), addOne);
        }
    }

    //Get all contacts
    public List<ContactInfo> getAllContacts() {
        //Maybe we should use a temp list to save data rather than new a list everytime.
        return new ArrayList<ContactInfo>(mUserMap.values());
    }
}
