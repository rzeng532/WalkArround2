package com.example.walkarround.message.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
        initHashMap();
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
        if(addOne != null && mInstance.mUserMap != null) {

            ContactInfo curOne = mInstance.mUserMap.get(addOne.getObjectId());
            if(curOne != null) {
                //Just reture if there is already a contact.
                return;
            }

            mInstance.mUserMap.put(addOne.getObjectId(), addOne);

            //Insert item to DB
            ContentValues values = new ContentValues();
            values.put(ContactInfoDatabase.Contact.OBJECTID, addOne.getObjectId());
            values.put(ContactInfoDatabase.Contact.USERNAME, addOne.getUsername());
            values.put(ContactInfoDatabase.Contact.GENDER, addOne.getGender());
            values.put(ContactInfoDatabase.Contact.SIGNATURE, addOne.getSignature());
            values.put(ContactInfoDatabase.Contact.BIRTHDAY, addOne.getBirthday());
            values.put(ContactInfoDatabase.Contact.CREATEDAT, addOne.getCreatedAt());
            values.put(ContactInfoDatabase.Contact.UPDATEAT, addOne.getUpdatedAt());
            values.put(ContactInfoDatabase.Contact.MOBILEPHONENUMBER, addOne.getMobilePhoneNumber());
            if(addOne.getPortrait() != null) {
                values.put(ContactInfoDatabase.Contact.PORTRAIT, addOne.getPortrait().getUrl());
            }

            dbContactInfo.insert(ContactInfoDatabase.Contact.TABLE_NAME, null, values);
        }
    }

    //Get all contacts
    public List<ContactInfo> getAllContacts() {
        //Maybe we should use a temp list to save data rather than new a list everytime.
        return new ArrayList<ContactInfo>(mUserMap.values());
    }

    /*
     * Init hashmap from DB
     */
    private void initHashMap() {
        //Check environment
        if(dbContactInfo == null) {
            return;
        }

        //Query from DB
        ContactInfo tempContact = null;
        Cursor cursor = dbContactInfo.query(ContactInfoDatabase.Contact.TABLE_NAME, null, null, null, null, null, null);
        if(cursor != null && cursor.moveToFirst()) {
            //Convert DB data to ContactInfo
            do{
                //Get object ID
                tempContact = convertCursor2ContactInfo(cursor);
                if(tempContact != null && !TextUtils.isEmpty(tempContact.getObjectId())) {
                    mUserMap.put(tempContact.getObjectId(), tempContact);
                }
            }while (cursor.moveToNext());
        }
    }

    private ContactInfo convertCursor2ContactInfo(Cursor cursor) {
        if(cursor == null) {
            return null;
        }

        ContactInfo contact = new ContactInfo();

        String objId = cursor.getString(cursor.getColumnIndex(ContactInfoDatabase.Contact.OBJECTID));
        if(TextUtils.isEmpty(objId)) {
            //We should confirm object id is NOT null.
            return null;
        }
        String name = cursor.getString(cursor.getColumnIndex(ContactInfoDatabase.Contact.USERNAME));
        String signature = cursor.getString(cursor.getColumnIndex(ContactInfoDatabase.Contact.SIGNATURE));
        String birth = cursor.getString(cursor.getColumnIndex(ContactInfoDatabase.Contact.BIRTHDAY));
        String mobile = cursor.getString(cursor.getColumnIndex(ContactInfoDatabase.Contact.MOBILEPHONENUMBER));
        String createTime = cursor.getString(cursor.getColumnIndex(ContactInfoDatabase.Contact.CREATEDAT));
        String updateTime = cursor.getString(cursor.getColumnIndex(ContactInfoDatabase.Contact.UPDATEAT));
        String portrait = cursor.getString(cursor.getColumnIndex(ContactInfoDatabase.Contact.PORTRAIT));

        contact.setObjectId(objId);
        contact.setUsername(name);
        contact.setSignature(signature);
        contact.setBirthday(birth);
        contact.setMobilePhoneNumber(mobile);
        contact.setCreatedAt(createTime);
        contact.setUpdatedAt(updateTime);
        ContactInfo.PortraitEntity entry = contact.getPortrait();
        entry.setUrl(portrait);
        contact.setPortrait(entry);

        return contact;
    }
}
