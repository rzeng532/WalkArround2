package com.example.walkarround.message.util;


import com.example.walkarround.message.model.MessageSessionBaseModel;
import com.example.walkarround.message.util.MessageConstant.ConversationType;

import java.util.Comparator;


public class SessionComparator implements Comparator<MessageSessionBaseModel> {

    /* 按时间升序 */
    public static final int TIME_ASC = 1;
    /* 按时间降序 */
    public static final int TIME_DESC = 2;
    /* 按置顶降序 */
    public static final int TOP_DESC = 3;

    public static final int PA_DESC = 4;/*public account 降序排列*/

    private int sortOrder = TIME_DESC;

    public SessionComparator(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public int compare(MessageSessionBaseModel lhs, MessageSessionBaseModel rhs) {
        if (sortOrder == PA_DESC) {
            return Boolean.valueOf(isPubOrSysItem(rhs.getItemType()))
                    .compareTo(isPubOrSysItem(lhs.getItemType()));
        } else if (sortOrder == TOP_DESC) {
            return Integer.compare(rhs.getTop(), lhs.getTop());
        } else {
            if (sortOrder == TIME_ASC) {
                return Long.compare(lhs.getLastTime(), rhs.getLastTime());
            } else {
                return Long.compare(rhs.getLastTime(), lhs.getLastTime());
            }
        }

    }

    private boolean isPubOrSysItem(int itemType) {
        return itemType == ConversationType.PUBLIC_ACCOUNT
                || itemType == ConversationType.SYSTEM;
    }
}
