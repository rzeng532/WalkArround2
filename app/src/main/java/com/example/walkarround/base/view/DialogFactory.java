/**
 * Copyright (C) 2014-2015 CMCC All rights reserved
 */
package com.example.walkarround.base.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.*;
import com.example.walkarround.R;
import com.example.walkarround.main.model.ContactInfo;
import com.example.walkarround.message.manager.ContactsManager;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.Logger;

import static android.app.AlertDialog.Builder;

/**
 * TODO: description
 * Date: 2015-11-26
 *
 * @author Administrator
 */
public class DialogFactory {
    private static final Logger myLogger = Logger.getLogger(DialogFactory.class.getSimpleName());

    public static Dialog getNoticeDialog(Context context, String content, final NoticeDialogClickListener listener,
                                         final Object value) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.notice_dialog_common, null);
        CheckedTextView checkedTextView = (CheckedTextView) dialogView.findViewById(R.id.not_show_ctv);
        checkedTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                CheckedTextView checkedView = (CheckedTextView) view;
                checkedView.setChecked(!checkedView.isChecked());
            }
        });
        dialog.setContentView(dialogView);
        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        layoutParams.width = mDisplayMetrics.widthPixels / 10 * 9;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
        ((TextView) dialogView.findViewById(R.id.dialog_notice_tv)).setText(content);
        dialogView.findViewById(R.id.dialog_cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (listener != null && listener instanceof NoticeDialogCancelClickListener) {
                    ((NoticeDialogCancelClickListener) listener).onNoticeDialogCancelClick();
                }
            }
        });
        dialogView.findViewById(R.id.dialog_ok_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (listener != null) {
                    CheckedTextView checkView = (CheckedTextView) dialogView.findViewById(R.id.not_show_ctv);
                    listener.onNoticeDialogConfirmClick(checkView.isChecked(), value);
                }
            }
        });
        return dialog;
    }

    /**
     * 复制弹窗
     */
    public static Dialog getCopyDialog(Context context, String content, final ConfirmDialogClickListener listener) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.notice_dialog_common, null);
        dialog.setContentView(dialogView);
        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        //layoutParams.width = mDisplayMetrics.widthPixels / 10 * 9;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
        TextView noticeTv = (TextView) dialogView.findViewById(R.id.dialog_notice_tv);
        noticeTv.setText(content);

        Button btn = (Button) dialogView.findViewById(R.id.dialog_cancel_btn);
        btn.setText(R.string.common_copy_to_clipboard);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (listener != null) {
                    listener.onConfirmDialogConfirmClick();
                }
            }
        });
        dialogView.findViewById(R.id.dialog_ok_btn).setVisibility(View.INVISIBLE);
        return dialog;
    }

    public static Dialog getCompleteDialog(Context context, String content, final ConfirmDialogClickListener listener) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.notice_dialog_common, null);
        dialog.setContentView(dialogView);
        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        //layoutParams.width = mDisplayMetrics.widthPixels / 10 * 9;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
        TextView noticeTv = (TextView) dialogView.findViewById(R.id.dialog_notice_tv);
        noticeTv.setText(content);

        Button btn = (Button) dialogView.findViewById(R.id.dialog_ok_btn);
        btn.setText(R.string.common_i_know);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (listener != null) {
                    listener.onConfirmDialogConfirmClick();
                }
            }
        });
        dialogView.findViewById(R.id.dialog_cancel_btn).setVisibility(View.INVISIBLE);
        return dialog;
    }

    public static Dialog getNoticeDialog(Context context, int noticesResId, NoticeDialogClickListener listener,
                                         Object value) {
        String content = context.getResources().getString(noticesResId);
        return getNoticeDialog(context, content, listener, value);
    }

    public static Dialog getNoticeDialog(Context context, int noticesResId, NoticeDialogClickListener okListener, NoticeDialogClickListener cancelClickListener,
                                         Object value) {
        String content = context.getResources().getString(noticesResId);
        return getNoticeDialog(context, content, okListener, cancelClickListener, value);
    }
    public static Dialog getNoticeDialog(Context context, String content, final NoticeDialogClickListener okListener,final NoticeDialogClickListener cancelClickListener,
                                         final Object value) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.notice_dialog_common, null);
        CheckedTextView checkedTextView = (CheckedTextView) dialogView.findViewById(R.id.not_show_ctv);
        checkedTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                CheckedTextView checkedView = (CheckedTextView) view;
                checkedView.setChecked(!checkedView.isChecked());
            }
        });
        dialog.setContentView(dialogView);
        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        //layoutParams.width = mDisplayMetrics.widthPixels / 10 * 9;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
        ((TextView) dialogView.findViewById(R.id.dialog_notice_tv)).setText(content);
        dialogView.findViewById(R.id.dialog_cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (cancelClickListener != null && cancelClickListener instanceof NoticeDialogCancelClickListener) {
                    ((NoticeDialogCancelClickListener) cancelClickListener).onNoticeDialogCancelClick();
                }
            }
        });
        dialogView.findViewById(R.id.dialog_ok_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (okListener != null) {
                    CheckedTextView checkView = (CheckedTextView) dialogView.findViewById(R.id.not_show_ctv);
                    okListener.onNoticeDialogConfirmClick(checkView.isChecked(), value);
                }
            }
        });
        return dialog;
    }

    public static Dialog getConfirmDialog(Context context, int noticesResId, int confirmBtnResId,
                                          final ConfirmDialogClickListener listener) {
        final Dialog dialog = new Dialog(context, R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.notice_dialog_common, null);
        dialog.setContentView(dialogView);
        View checkView = dialogView.findViewById(R.id.not_show_ctv);
        checkView.setVisibility(View.GONE);
        TextView confirmBtn = (TextView) dialogView.findViewById(R.id.dialog_ok_btn);
        confirmBtn.setText(confirmBtnResId);
        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        //layoutParams.width = mDisplayMetrics.widthPixels / 10 * 9;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
        ((TextView) dialogView.findViewById(R.id.dialog_notice_tv)).setText(noticesResId);
        dialogView.findViewById(R.id.dialog_cancel_btn).setVisibility(View.GONE);
        dialogView.findViewById(R.id.dialog_ok_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (listener != null) {
                    listener.onConfirmDialogConfirmClick();
                }
            }
        });
        return dialog;
    }

    public static Dialog getWalkRuleDialog(Context context, final ConfirmDialogClickListener listener) {
        final Dialog dialog = new Dialog(context, R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_walk_rule, null);
        dialog.setContentView(dialogView);

        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        //layoutParams.width = mDisplayMetrics.widthPixels / 10 * 9;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialogView.findViewById(R.id.tv_i_see).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (listener != null) {
                    listener.onConfirmDialogConfirmClick();
                }
            }
        });

        return dialog;
    }

    public static Dialog getMappingDialog(Context context, String userName, final ConfirmDialogClickListener listener) {
        final Dialog dialog = new Dialog(context, R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_find_mapping, null);
        dialog.setContentView(dialogView);

        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        //layoutParams.width = mDisplayMetrics.widthPixels / 10 * 9;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        String friendName = userName;
        if(friendName.length() > AppConstant.SHORTNAME_LEN) {
            friendName = friendName.substring(0, AppConstant.SHORTNAME_LEN) + "...";
        }
        String indicate = context.getApplicationContext().getString(R.string.mapping_indication, friendName);

        ((TextView)dialogView.findViewById(R.id.tv_mapping)).setText(indicate);

        dialogView.findViewById(R.id.tv_i_see).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (listener != null) {
                    listener.onConfirmDialogConfirmClick();
                }
            }
        });

        return dialog;
    }


    public static Dialog getCountDownEndDialog(Context context, final ConfirmDialogClickListener listener) {
        final Dialog dialog = new Dialog(context, R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_countdown_2_evaluate, null);
        dialog.setContentView(dialogView);

        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        //layoutParams.width = mDisplayMetrics.widthPixels / 10 * 9;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialogView.findViewById(R.id.tv_start2evaluate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (listener != null) {
                    listener.onConfirmDialogConfirmClick();
                }
            }
        });

        return dialog;
    }

    public static Dialog getStart2WalkDialog(Context context, String usrObjId, final ConfirmDialogClickListener listener) {
        final Dialog dialog = new Dialog(context, R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_start_2_walk, null);
        dialog.setContentView(dialogView);

        PortraitView portrait = (PortraitView)dialogView.findViewById(R.id.pv_start2walk);
        final ContactInfo usr = ContactsManager.getInstance(context).getContactByUsrObjId(usrObjId);
        portrait.setBaseData(usr.getUsername(), usr.getPortrait().getUrl(),
                usr.getUsername().substring(0, 1), -1);

        TextView tvName = (TextView)dialogView.findViewById(R.id.tv_friend_name);
        tvName.setText(usr.getUsername());

        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        //layoutParams.width = mDisplayMetrics.widthPixels / 10 * 9;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialogView.findViewById(R.id.iv_finish_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.tv_start2walk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogView.findViewById(R.id.tv_start2walk).setVisibility(View.GONE);
                dialogView.findViewById(R.id.tv_wait_response).setVisibility(View.VISIBLE);
                Toast.makeText(context, R.string.msg_walk_req_time_indicate, Toast.LENGTH_SHORT).show();
                String name = usr.getUsername();
                if(name.length() > AppConstant.SHORTNAME_LEN) {
                    name = name.substring(0, AppConstant.SHORTNAME_LEN) + "...";
                }
                ((TextView)(dialogView.findViewById(R.id.tv_wait_response))).setText(context.getString(R.string.walk_rule_wait_for_response, name));
                if (listener != null) {
                    listener.onConfirmDialogConfirmClick();
                }
            }
        });
        return dialog;
    }

    public static Dialog getStart2WalkReplyDialog(Context context, String usrObjId, final NoticeDialogCancelClickListener listener) {
        final Dialog dialog = new Dialog(context, R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_start_2_walk_reply, null);
        dialog.setContentView(dialogView);

        PortraitView portrait = (PortraitView)dialogView.findViewById(R.id.pv_start2walk);
        final ContactInfo usr = ContactsManager.getInstance(context).getContactByUsrObjId(usrObjId);
        portrait.setBaseData(usr.getUsername(), usr.getPortrait().getUrl(),
                usr.getUsername().substring(0, 1), -1);

        TextView tvName = (TextView)dialogView.findViewById(R.id.tv_friend_name);
        tvName.setText(usr.getUsername());

        TextView tvInvitationDesc = (TextView)dialogView.findViewById(R.id.tv_friend_invitation);
        String name = usr.getUsername();
        if(name.length() > AppConstant.SHORTNAME_LEN) {
            name = name.substring(0, AppConstant.SHORTNAME_LEN) + "...";
        }
        tvInvitationDesc.setText(context.getString(R.string.agree_2_walk_invitation_description, name));

        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        //layoutParams.width = mDisplayMetrics.widthPixels / 10 * 9;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialogView.findViewById(R.id.tv_next_time).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onNoticeDialogCancelClick();
                }
            }
        });

        dialogView.findViewById(R.id.tv_agree).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onNoticeDialogConfirmClick(true, null);
                }
            }
        });
        return dialog;
    }

    public interface NoticeDialogCancelClickListener extends NoticeDialogClickListener {
        /**
         * 点击取消按钮
         */
        public void onNoticeDialogCancelClick();

    }

    public interface NoticeDialogClickListener {
        /**
         * 点击确认按钮
         *
         * @param isChecked 不再显示是否选中
         */
        public void onNoticeDialogConfirmClick(boolean isChecked, Object value);

    }

    public interface ConfirmDialogClickListener {
        /**
         * 点击确认按钮
         */
        public void onConfirmDialogConfirmClick();

    }

    public static class MyDialogListAdapter extends BaseAdapter {
        private String[] data;
        private int[] imageId;
        private LayoutInflater mInflater;

        public MyDialogListAdapter(Context context, String[] data, int[] imageIds) {
            this.data = data;
            mInflater = LayoutInflater.from(context);
            imageId = imageIds;
        }

        @Override
        public int getCount() {
            return data.length;
        }

        @Override
        public Object getItem(int position) {
            return data[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.dialog_list_item, null);
                convertView.setTag(convertView.findViewById(R.id.dialog_item_title));
            }
            TextView itemText = ((TextView) convertView.getTag());
            itemText.setText(data[position]);
            if (imageId != null && position < imageId.length) {
                itemText.setGravity(Gravity.CENTER_HORIZONTAL);
                itemText.setCompoundDrawablesWithIntrinsicBounds(imageId[position], 0, 0, 0);
            } else {
                itemText.setGravity(Gravity.LEFT);
                itemText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
            return convertView;
        }

    }

    public static Dialog createDialog(Context context, String title, View contentView) {
        Dialog dialog = new Dialog(context);
        if (TextUtils.isEmpty(title)) {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        } else {
            dialog.setTitle(title);
        }
        dialog.setContentView(contentView);
        return dialog;
    }

    public static Dialog createNoShadowDialog(Context context, String title, View contentView) {
        Dialog dialog = new Dialog(context,R.style.Theme_NoBackDialog);
        if (TextUtils.isEmpty(title)) {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        } else {
            dialog.setTitle(title);
        }
//        ((LinearLayout)contentView).setGravity(Gravity.CENTER);
        dialog.setContentView(contentView);
        return dialog;
    }

    public static Builder getImageListDialog(Context context, String title, String[] items,
                                                         int[] imageResIds, DialogInterface.OnClickListener listener) {
        myLogger.d("getImageListDialog 开始");

        Builder builder = new Builder(context);
        builder.setTitle(title);
        MyDialogListAdapter listAdapter = new MyDialogListAdapter(context, items, imageResIds);
        builder.setAdapter(listAdapter, listener);
        myLogger.d("getImageListDialog 结束");
        return builder;
    }

    public static Dialog getLoadingDialog(Context activity, boolean cancelable,
                                          final DialogInterface.OnCancelListener cancelEvent) {
        Dialog dialog = new Dialog(activity,R.style.Theme_CustomDialog);
        View contentView = LayoutInflater.from(activity).inflate(R.layout.dialog_emptyloading,null);

        if (cancelable) {
            if (cancelEvent != null) {
                dialog.setOnCancelListener(cancelEvent);
            }
        } else {
            dialog.setCancelable(false);
        }

        dialog.setContentView(contentView);
        return dialog;
    }

    public static Dialog getLoadingDialog(Context activity, String msg, boolean cancelable,
                                          final DialogInterface.OnCancelListener cancelEvent) {
        Dialog dialog = new Dialog(activity, R.style.Theme_CustomDialog);
        View contentView = LayoutInflater.from(activity).inflate(R.layout.dialog_emptyloading, null);

        if (cancelable) {
            if (cancelEvent != null) {
                dialog.setOnCancelListener(cancelEvent);
            }
        } else {
            dialog.setCancelable(false);
        }

        dialog.setContentView(contentView);
        return dialog;
    }
}