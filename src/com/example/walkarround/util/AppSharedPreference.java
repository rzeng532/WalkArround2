/**
 * Copyright (C) 2014-2015 CMCC All rights reserved
 */
package com.example.walkarround.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import com.example.walkarround.base.WalkArroundApp;

import java.io.*;

/**
 * TODO: description
 * Date: 2015-12-02
 *
 * @author Administrator
 */
public class AppSharedPreference {
    private static final Logger logger = Logger.getLogger(AppSharedPreference.class.getSimpleName());

    private final static String WALKARROUND_PREFERENCES = "walkarround_preferences";

    private final static SharedPreferences sp = (SharedPreferences) WalkArroundApp.getAppInstance().getSharedPreferences(
            WALKARROUND_PREFERENCES, Context.MODE_PRIVATE);

    // >>>>>>>>>>>>>>>>>define keys start>>>>>>>>>>>>>>>>>
    public static final String ACCOUNT_USERNAME = "account_username";
    public static final String ACCOUNT_PHONE = "account_phone";
    public static final String ACCOUNT_PASSWORD = "account_password";
    public static final String ACCOUNT_PORTRAIT = "account_portrait_path";
    // <<<<<<<<<<<<<<<<<define keys end<<<<<<<<<<<<<<<<<

    public static int getInt(String key, int defValue) {
        return sp.getInt(key, defValue);
    }

    public static boolean getBoolean(String key, boolean defValue) {
        return sp.getBoolean(key, defValue);
    }

    public static String getString(String key, String defValue) {
        String value = sp.getString(key, defValue);

        return value;
    }

    /**
     *
     * Set an int value in the preferences file.
     *
     * @param key
     *            The name of the preference to modify.
     * @param value
     *            The new value for the preference.
     * @return
     */
    public static boolean putInt(String key, int value) {
        return sp.edit().putInt(key, value).commit();
    }

    public static boolean putBoolean(String key, boolean value) {
        return sp.edit().putBoolean(key, value).commit();
    }

    /**
     * Set a string value in the preferences file.
     *
     * @param key
     *            The name of the preference to modify.
     * @param value
     *            The new value for the preference.
     * @return
     */
    public static boolean putString(String key, String value) {
        return sp.edit().putString(key, value).commit();
    }

    /**
     * 针对复杂类型存储<对象>
     *
     * @param key
     * @param object
     */
    public static void setObject(String key, Object object) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {

            out = new ObjectOutputStream(baos);
            out.writeObject(object);
            String objectVal = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(key, objectVal);
            editor.commit();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Object getObject(String key) {
        if (sp.contains(key)) {
            String objectVal = sp.getString(key, null);
            byte[] buffer = Base64.decode(objectVal, Base64.DEFAULT);
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(bais);
                return ois.readObject();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bais != null) {
                        bais.close();
                    }
                    if (ois != null) {
                        ois.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}