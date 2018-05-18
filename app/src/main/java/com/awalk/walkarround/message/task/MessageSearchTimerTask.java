package com.awalk.walkarround.message.task;

import android.content.Context;
import com.awalk.walkarround.main.model.ContactInfo;
import com.awalk.walkarround.message.manager.WalkArroundMsgManager;
import com.awalk.walkarround.message.model.ChatMsgBaseInfo;
import com.awalk.walkarround.util.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageSearchTimerTask extends TimerTask {
    private static final Logger logger = Logger.getLogger(MessageSearchTimerTask.class.getSimpleName());
    private List<ContactInfo> mAllContacts;
    private Context mContext;
    private String mKey;
    private onResultListener mListener;
    private boolean mIsCancelled;
    private boolean mIsRunning;
    private boolean mIsNotify;
    private Map<String, String> mChineseKeys = new HashMap<String, String>();

    public interface onResultListener {
        void onResult(boolean isNotify, List<ChatMsgBaseInfo> result, Map<String, String> map);
    }

    public MessageSearchTimerTask(Context context, List<ContactInfo> allContacts, String key,
                                  boolean bfilter, onResultListener l) {
        mKey = key;
        mContext = context;
        mAllContacts = allContacts;
        mListener = l;
        mIsCancelled = false;
        mIsNotify = bfilter;

        mChineseKeys.clear();
    }

    public void setCancelled(boolean n) {
        mIsCancelled = n;
    }

    public boolean getRunning() {
        return mIsRunning;
    }

    @Override
    public void run() {
        mIsRunning = true;
        if (isLetters(mKey)) {
            List<ChatMsgBaseInfo> list = null;
            Map<String, String> map = null;

            if (!mIsCancelled) {
                map = pinyinSearchNum(mAllContacts, mKey);
            }

            if (!mIsCancelled) {
                list = WalkArroundMsgManager.getInstance(mContext).searchSmsAndMsgByKey(mContext, mKey,
                        mIsNotify, map);
            }

            if (!mIsCancelled && mListener != null) {
                if (null != list && 0 != list.size()) {
                    mListener.onResult(mIsNotify, list, mChineseKeys);
                } else {
                    mListener.onResult(mIsNotify, null, null);
                }
            }

        } else {
            List<ChatMsgBaseInfo> list = null;
            Map<String, String> numMap1 = null;
            Map<String, String> numMap2 = null;
            List<ChatMsgBaseInfo> resultList = null;

            if (isNumbers(mKey)) {
                if (!mIsCancelled) {
                    numMap1 = WalkArroundMsgManager.getInstance(mContext).queryMsgSession(mContext, mKey);
                }
            }

            if (!mIsCancelled) {
                //numMap2 = NewContactManager.getInstance(mContext).queryContactsWithName(mKey);
            }

            if (!mIsCancelled) {
                if (numMap1 != null) {
                    numMap1.putAll(numMap2);
                } else {
                    numMap1 = numMap2;
                }
            }

            if (!mIsCancelled) {
                list = WalkArroundMsgManager.getInstance(mContext).searchSmsAndMsgByKey(mContext, mKey, mIsNotify, numMap1);
            }

            if (!mIsCancelled) {
                resultList = new ArrayList<ChatMsgBaseInfo>();
                if (null != list && 0 != list.size()) {
                    resultList = list;
                }
            }

            if (!mIsCancelled && mListener != null) {
                mListener.onResult(mIsNotify, resultList, null);
            }
        }
        mIsRunning = false;
    }

    private boolean isNumbers(String key) {
        Pattern p = Pattern.compile("^[0-9]+$");
        Matcher m = p.matcher(key);
        return m.matches();
    }

    private boolean isLetters(String key) {
        Pattern p = Pattern.compile("^[A-Za-z]+$");
        Matcher m = p.matcher(key);
        return m.matches();
    }

    private Map<String, String> pinyinSearchNum(List<ContactInfo> contactData, String str) {
        String temp = str.toLowerCase();
        Map<String, String> map = new HashMap<String, String>();

        if (contactData == null || contactData.size() == 0) {
            return map;
        }

//        for (int i = 0; i < contactData.size() && !mIsCancelled; i++) {
//            ContactInfo cb = contactData.get(i);
//
//            List<PinyinInfo> pinyinUnits = cb.getNamePinyinUnits();
//            if (pinyinUnits != null) {
//                StringBuffer chineseKeyWord = new StringBuffer();// In order to get
//                if (true == QwertyMatchPinyinUnits.matchPinyinUnits(pinyinUnits, cb.getFirstName(), temp,
//                        chineseKeyWord)) {
//
//                    List<String> numList = cb.getPhoneNumList();
//                    for (int j = 0; j < numList.size() && !mIsCancelled; j++) {
//                        map.put(numList.get(j), numList.get(j));
////                        logger.i("key: " + numList.get(j) + " value: " + chineseKeyWord.toString());
//                        mChineseKeys.put(numList.get(j), chineseKeyWord.toString());
//                    }
//                }
//            }
//        }

        return map;
    }
}