/**
 * Copyright (C) 2014-2015 All rights reserved
 */
package com.awalk.walkarround.base.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.awalk.walkarround.R;
import com.awalk.walkarround.assistant.AssistantHelper;
import com.awalk.walkarround.main.model.ContactInfo;
import com.awalk.walkarround.message.manager.ContactsManager;
import com.awalk.walkarround.util.AppConstant;
import com.awalk.walkarround.util.Logger;

import java.util.ArrayList;

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
        layoutParams.width = mDisplayMetrics.widthPixels / 10 * 9;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

//        dialogView.findViewById(R.id.tv_i_see).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//                if (listener != null) {
//                    listener.onConfirmDialogConfirmClick();
//                }
//            }
//        });

        return dialog;
    }

    public static Dialog getMappingDialog(Context context, String indicate, final ConfirmDialogClickListener listener) {
        final Dialog dialog = new Dialog(context, R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_find_mapping, null);
        dialog.setContentView(dialogView);
        //dialog.setCancelable(false);

        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        //layoutParams.width = mDisplayMetrics.widthPixels / 10 * 9;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

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


    /**
     * 阶梯联系人说明
     * @param context
     * @param explanation
     * @return
     */
    public static Dialog getConvEmptyDescriptionDialog(Context context, String explanation,int iconResId,
                                                       final ConfirmDialogClickListener listener) {
        final Dialog dialog = new Dialog(context, R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_countdown_2_evaluate, null);
        TextView textView = (TextView) dialogView.findViewById(R.id.tv_walk_title);
        TextView closeView = (TextView) dialogView.findViewById(R.id.close_tv);
        closeView.setVisibility(View.VISIBLE);
        closeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        TextView searchAroundView = (TextView) dialogView.findViewById(R.id.tv_start2evaluate);
        searchAroundView.setText(R.string.search_around);
        searchAroundView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (listener != null) {
                    listener.onConfirmDialogConfirmClick();
                }

            }
        });
        textView.setText(explanation);

        ImageView iconView = (ImageView) dialogView.findViewById(R.id.iv_bye_icon);
        iconView.setImageResource(iconResId);
        dialog.setContentView(dialogView);

        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        //layoutParams.width = mDisplayMetrics.widthPixels / 10 * 9;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

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

    public static Dialog getStart2WalkDialog(final Context context, String usrObjId, final ConfirmDialogClickListener listener) {
        final Dialog dialog = new Dialog(context, R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_start_2_walk, null);
        dialog.setContentView(dialogView);

        PortraitView portrait = (PortraitView)dialogView.findViewById(R.id.pv_start2walk);
        final ContactInfo usr = ContactsManager.getInstance(context).getContactByUsrObjId(usrObjId);
        portrait.setBaseData(usr.getUsername(), usr.getPortrait().getUrl(),
                usr.getUsername().substring(0, 1), usr.getPortrait().getDefaultId());

        String name = usr.getUsername();
        if(name.length() > AppConstant.SHORTNAME_LEN) {
            name = name.substring(0, AppConstant.SHORTNAME_LEN) + "...";
        }
        TextView tvName = (TextView)dialogView.findViewById(R.id.tv_friend_name);
        tvName.setText(name);

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

        View confirmView = dialogView.findViewById(R.id.tv_start2walk);
        if (AssistantHelper.ASSISTANT_OBJ_ID.equalsIgnoreCase(usrObjId)) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.shake_anim);
            confirmView.startAnimation(animation);
        }
        confirmView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.clearAnimation();
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
        dialog.setCanceledOnTouchOutside(false);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_start_2_walk_reply, null);
        dialog.setContentView(dialogView);

        PortraitView portrait = (PortraitView)dialogView.findViewById(R.id.pv_start2walk);
        final ContactInfo usr = ContactsManager.getInstance(context).getContactByUsrObjId(usrObjId);
        portrait.setBaseData(usr.getUsername(), usr.getPortrait().getUrl(),
                usr.getUsername().substring(0, 1), usr.getPortrait().getDefaultId());

        String name = usr.getUsername();
        if(name.length() > AppConstant.SHORTNAME_LEN) {
            name = name.substring(0, AppConstant.SHORTNAME_LEN) + "...";
        }
        TextView tvName = (TextView)dialogView.findViewById(R.id.tv_friend_name);
        tvName.setText(name);

        TextView tvInvitationDesc = (TextView)dialogView.findViewById(R.id.tv_friend_invitation);
        tvInvitationDesc.setText(context.getString(R.string.agree_2_walk_invitation_description));

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

    public static Dialog showNaviListDialog(Context context, String title,
                                                    ArrayList<String> itemData,
                                                    final AdapterView.OnItemClickListener onItemClickListener) {
        ArrayAdapter actionAdapter = new ArrayAdapter(context, R.layout.list_item_alter_icon_dialog, R.id.item_text, itemData);

        final Dialog dialog = new Dialog(context, R.style.shareDialogTheme);
        View view = LayoutInflater.from(context).inflate(
                R.layout.dialog_alter_icon, null);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        window.setContentView(view);

        TextView titleView = (TextView) view.findViewById(R.id.textview_title);
        View divider = (View) view.findViewById(R.id.divider1);
        ListView list = (ListView) view.findViewById(R.id.listview_event);
        //TextView textView = (TextView) view.findViewById(R.id.textview_cancel);
        if (title == null || TextUtils.isEmpty(title)) {
            titleView.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        } else {
            titleView.setText(title);
            titleView.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
        }

        list.setAdapter(actionAdapter);

        list.setOnItemClickListener(onItemClickListener);
//        if (cancelListener == null) {
//            textView.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//
//                    dialog.dismiss();
//                }
//            });
//        } else {
//            textView.setOnClickListener(cancelListener);
//        }

        return dialog;
    }

}