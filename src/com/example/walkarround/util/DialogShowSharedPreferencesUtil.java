package com.example.walkarround.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 是否再次显示提示Dialog的数据存储
 * Date: 2015-03-14
 *
 * @author mss
 */
public class DialogShowSharedPreferencesUtil {

    private final static String DIALOG_SHARED_PREFERENCES = "dialogShow";

    public final static String DIALOG_SHOW_NORMAL_CALL_NOTICE = "showNormalCallNotice";
    public final static String DIALOG_SHOW_DELETE_CALL_RECORDS_NOTICE = "showDeleteCallRecordsNotice";
    public final static String DIALOG_SHOW_RESEND_MESSAGE_NOTICE = "showResendMessageNotice";

    /**
     * 获取是否显示Dialog
     *
     * @param context
     * @param dialogKey
     * @return
     */
    public static boolean isDialogShow(Context context, String dialogKey) {
        boolean value = context.getSharedPreferences(DIALOG_SHARED_PREFERENCES,
                Context.MODE_PRIVATE).getBoolean(dialogKey, true);
        return value;
    }

    /**
     * 设置是否显示Dialog
     *
     * @param context
     * @param dialogKey
     * @param isShow
     */
    public static void setDialogShow(Context context, String dialogKey, boolean isShow) {
        SharedPreferences.Editor editor = context.getSharedPreferences(DIALOG_SHARED_PREFERENCES,
                Context.MODE_PRIVATE).edit();
        editor.putBoolean(dialogKey, isShow);
        editor.commit();
    }
}
