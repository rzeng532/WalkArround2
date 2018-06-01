package com.awalk.walkarround.message.util;


import com.awalk.walkarround.message.model.MessageSessionBaseModel;
import com.awalk.walkarround.message.util.MessageConstant.ConversationType;

import java.util.Comparator;


public class SessionComparator implements Comparator<MessageSessionBaseModel> {

    /* 按时间升序 */
    public static final int TIME_ASC = 1;
    /* 按时间降序 */
    public static final int TIME_DESC = 2;
    /* 按置顶降序 */
    public static final int TOP_DESC = 3;

    public static final int STATUS_DESC = 5;/*Conv 状态降序排列*/

    private int sortOrder = TIME_DESC;

    public SessionComparator(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public int compare(MessageSessionBaseModel lhs, MessageSessionBaseModel rhs) {
        if (sortOrder == TOP_DESC) {
            return Integer.compare(rhs.getTop(), lhs.getTop());
        } else if (sortOrder == STATUS_DESC) {
            return Integer.compare(lhs.status, rhs.status);
        } else {
            if (sortOrder == TIME_ASC) {
                return Long.compare(lhs.getLastTime(), rhs.getLastTime());
            } else {
                return Long.compare(rhs.getLastTime(), lhs.getLastTime());
            }
        }

    }

}
