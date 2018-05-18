package com.awalk.walkarround.message.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.awalk.walkarround.R;
import com.awalk.walkarround.base.view.DialogFactory;
import com.awalk.walkarround.base.view.DialogFactory.NoticeDialogClickListener;
import com.awalk.walkarround.base.view.PhotoView;
import com.awalk.walkarround.base.view.RoundProgressBar;
import com.awalk.walkarround.base.view.gifview.GifView;
import com.awalk.walkarround.base.view.gifview.GifView.GifImageType;
import com.awalk.walkarround.main.model.ContactInfo;
import com.awalk.walkarround.message.activity.PlaintTextDetailActivity;
import com.awalk.walkarround.message.listener.PressTalkTouchListener;
import com.awalk.walkarround.message.manager.ContactsManager;
import com.awalk.walkarround.message.manager.WalkArroundMsgManager;
import com.awalk.walkarround.message.model.ChatMsgBaseInfo;
import com.awalk.walkarround.message.util.MessageConstant.MessageSendReceive;
import com.awalk.walkarround.message.util.MessageConstant.MessageState;
import com.awalk.walkarround.message.util.MessageConstant.MessageType;
import com.awalk.walkarround.message.util.MessageUtil;
import com.awalk.walkarround.myself.manager.ProfileManager;
import com.awalk.walkarround.util.CommonUtils;
import com.awalk.walkarround.util.DialogShowSharedPreferencesUtil;
import com.awalk.walkarround.util.Logger;
import com.awalk.walkarround.util.TimeFormattedUtil;
import com.awalk.walkarround.util.image.ImageLoaderManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * 创建不同类型消息视图 Date: 2015-04-14
 *
 * @author mss
 */
public class MessageDetailViewFactory implements OnClickListener, OnLongClickListener,
        NoticeDialogClickListener {

    private final static Logger logger = Logger.getLogger(MessageDetailViewFactory.class.getSimpleName());

    private final static float FILE_SIZE_BASE = 1024f;
    private final static int MAX_THUMBNAIL_WITH = 200;
    private int AUDIO_BUBBLE_MIN_WIDTH;
    private int AUDIO_BUBBLE_MAX_WIDTH;
    private Context mContext;
    private ItemListener mMessageItemListener;
    /* 重发提示Dialog */
    private Dialog mNoticesDialog;
    private HashMap<String, ContactInfo> mSearchContactInfo = new HashMap<String, ContactInfo>();
    private boolean isCollectMsgPage = false;
    /* 消息操作：复制、转发、收藏、删除、更多 */
    private PopupWindow mPopupWindow;

    private HashMap<String, String> mNickNameMap = new HashMap<String, String>();

    public MessageDetailViewFactory(Context context, ItemListener itemListener,
                                    boolean isCollectMsgPage) {
        mContext = context;
        mMessageItemListener = itemListener;
        this.isCollectMsgPage = isCollectMsgPage;

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        AUDIO_BUBBLE_MAX_WIDTH = dm.widthPixels * 2 / 5;
        AUDIO_BUBBLE_MIN_WIDTH = CommonUtils.dip2px(context, 20);
    }

    public void dismissDialog() {
        if (mNoticesDialog != null && mNoticesDialog.isShowing()) {
            mNoticesDialog.dismiss();
            mNoticesDialog = null;
        }
    }

    public void setNickNameMap(HashMap<String, String> nickNameMap) {
        if (nickNameMap != null) {
            this.mNickNameMap = nickNameMap;
        } else {
            mNickNameMap.clear();
        }
    }

    public void updateNickNameMap(String phoneNumber, String nickName) {
        mNickNameMap.put(CommonUtils.getPhoneNum(phoneNumber), nickName);
    }

    public void clearCachName() {
        mSearchContactInfo.clear();
    }

    public View getMessageView(View convertView, ChatMsgBaseInfo messageInfo, int msgType,
                               boolean isSelectMode, boolean isShowPhotoView) {
        return getMessageView(convertView, messageInfo, msgType, isSelectMode, isShowPhotoView, false);
    }

    public View getMessageView(View convertView, ChatMsgBaseInfo messageInfo, int msgType,
                               boolean isSelectMode, boolean isShowPhotoView, boolean isShowNickName) {
        switch (msgType) {
            case MessageDetailListAdapter.MESSAGE_TYPE_PLAIN_TEXT_SEND:
            case MessageDetailListAdapter.MESSAGE_TYPE_PLAIN_TEXT_REC:
                convertView = initPlaintTextView(mContext, convertView, messageInfo, isSelectMode);
                break;
            case MessageDetailListAdapter.MESSAGE_TYPE_VIDEO_SEND:
            case MessageDetailListAdapter.MESSAGE_TYPE_VIDEO_REC:
                convertView = initVideoView(mContext, convertView, messageInfo);
                break;
            case MessageDetailListAdapter.MESSAGE_TYPE_PICTURE_SEND:
            case MessageDetailListAdapter.MESSAGE_TYPE_PICTURE_REC:
                convertView = initPictureView(mContext, convertView, messageInfo);
                break;
            case MessageDetailListAdapter.MESSAGE_TYPE_GIF_PICTURE_SEND:
            case MessageDetailListAdapter.MESSAGE_TYPE_GIF_PICTURE_REC:
                convertView = initGifPictureView(mContext, convertView, messageInfo);
                break;
            case MessageDetailListAdapter.MESSAGE_TYPE_LOCATION_SEND:
            case MessageDetailListAdapter.MESSAGE_TYPE_LOCATION_REC:
                convertView = initLocationView(mContext, convertView, messageInfo);
                break;
            case MessageDetailListAdapter.MESSAGE_TYPE_AUDIO_SEND:
            case MessageDetailListAdapter.MESSAGE_TYPE_AUDIO_REC:
                convertView = initAudioView(mContext, convertView, messageInfo);
                break;
            case MessageDetailListAdapter.MESSAGE_TYPE_SYSTEM:
                convertView = initSysMsgView(mContext, convertView, messageInfo);
                convertView.setTag(R.id.msg_item_bg_layout, messageInfo);
                return convertView;
            default:
                convertView = new LinearLayout(mContext);
                return convertView;
        }

        convertView.setClickable(isSelectMode);
        convertView.setTag(R.id.msg_item_bg_layout, messageInfo);
        BaseViewHolder viewHolder = (BaseViewHolder) convertView.getTag();
        initMsgCommonView(mContext, messageInfo, viewHolder, isSelectMode, isShowPhotoView, isShowNickName);

        return convertView;
    }

    private View initSysMsgView(Context context, View convertView, ChatMsgBaseInfo message) {
        SysMsgViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.message_info_system_text_rec, null, false);

            viewHolder = new SysMsgViewHolder();
            viewHolder.checkedTextView = (CheckedTextView) convertView.findViewById(R.id.select_check_ctv);
            viewHolder.photoView = (PhotoView) convertView.findViewById(R.id.msg_contact_profile_pv);
            viewHolder.sendTimeTv = (TextView) convertView.findViewById(R.id.msg_send_time_tv);
            viewHolder.sendNameTv = (TextView) convertView.findViewById(R.id.msg_contact_name_tv);
            viewHolder.msgTextTv = (TextView) convertView.findViewById(R.id.msg_content_tv);
            viewHolder.clickAreaView = convertView.findViewById(R.id.msg_item_bg_layout);
            convertView.setOnClickListener(this);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (SysMsgViewHolder) convertView.getTag();
        }

        //getString(R.string.agree_2_walkarround_postfix)
        viewHolder.msgTextTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        viewHolder.msgTextTv.setText(getNotifyMsgContentText(context, message));
        setTextColor(context, viewHolder.msgTextTv, message);
        setDateElement(context, message.getTime(), viewHolder.sendTimeTv);

        return convertView;
    }

    /**
     * 获取通知类型消息在build message 中的text 内容。
     * @param context
     * @param msg
     * @return
     */
    private String getNotifyMsgContentText(Context context, ChatMsgBaseInfo msg) {
        String result = null;

        if(msg == null) {
            return result;
        }

        result = msg.getData();

        if(!TextUtils.isEmpty(msg.getExtraInfo())) {
            String[] extraArray = msg.getExtraInfo().split(MessageUtil.EXTRA_INFOR_SPLIT);
            if(extraArray != null && extraArray.length >= 2 && !TextUtils.isEmpty(extraArray[0])) {
                if(extraArray[1].equalsIgnoreCase(MessageUtil.EXTRA_START_2_WALK_REQUEST)) {
                    result = context.getString(msg.getSendReceive() == MessageSendReceive.MSG_RECEIVE ? R.string.msg_walk_req_receiver_text : R.string.msg_walk_req_sender_text);
                } else if(extraArray[1].equalsIgnoreCase(MessageUtil.EXTRA_START_2_WALK_REPLY_OK)) {
                    result = context.getString(msg.getSendReceive() == MessageSendReceive.MSG_RECEIVE ? R.string.msg_walk_reply_receiver_ok : R.string.msg_walk_reply_sender_ok);
                } else if(extraArray[1].equalsIgnoreCase(MessageUtil.EXTRA_START_2_WALK_REPLY_NEXT_TIME)) {
                    result = context.getString(msg.getSendReceive() == MessageSendReceive.MSG_RECEIVE ? R.string.msg_walk_reply_receiver_next_time : R.string.msg_walk_reply_sender_next_time);
                } else if(extraArray[0].equalsIgnoreCase(MessageUtil.EXTRA_AGREEMENT_2_WALKARROUND)) {
                    String preFix = context.getString(msg.getSendReceive() == MessageSendReceive.MSG_RECEIVE ? R.string.agree_2_walkarround : R.string.receive_agree_2_walkarround);
                    result = preFix + result;
                } else if (extraArray[0].equalsIgnoreCase(MessageUtil.EXTRA_SAY_HELLO)) {
                    result = context.getString(msg.getSendReceive() == MessageSendReceive.MSG_RECEIVE ? R.string.msg_received_hello : R.string.msg_say_hello);
                }
            }
        }

        return result;
    }

    private void setTextColor(Context context, TextView tv, ChatMsgBaseInfo msg) {
        if(context != null && tv != null && msg != null) {
            String result = msg.getData();

            if(!TextUtils.isEmpty(result) && !TextUtils.isEmpty(msg.getExtraInfo())) {
                String[] extraArray = msg.getExtraInfo().split(MessageUtil.EXTRA_INFOR_SPLIT);

                if(extraArray[0].equalsIgnoreCase(MessageUtil.EXTRA_AGREEMENT_2_WALKARROUND)) {
                    tv.setTextColor(context.getResources().getColor(R.color.emerald_green));
                }
            }
        }
    }

    /**
     * 初始化文本消息
     *
     * @param convertView
     * @param message
     * @return
     */
    private View initPlaintTextView(Context context, View convertView, ChatMsgBaseInfo message, boolean isSelectMode) {
        PlaintTextViewHolder viewHolder;
        if (convertView == null) {
            if (message.getSendReceive() == MessageSendReceive.MSG_SEND) {
                convertView = LayoutInflater.from(context).inflate(R.layout.message_info_plain_text_send, null, false);
            } else {
                convertView = LayoutInflater.from(context).inflate(R.layout.message_info_plain_text_rec, null, false);
            }
            viewHolder = new PlaintTextViewHolder();
            viewHolder.checkedTextView = (CheckedTextView) convertView.findViewById(R.id.select_check_ctv);
            viewHolder.photoView = (PhotoView) convertView.findViewById(R.id.msg_contact_profile_pv);
            viewHolder.sendTimeTv = (TextView) convertView.findViewById(R.id.msg_send_time_tv);
            viewHolder.sendNameTv = (TextView) convertView.findViewById(R.id.msg_contact_name_tv);
            viewHolder.msgTextTv = (TextView) convertView.findViewById(R.id.msg_content_tv);
            viewHolder.msgHintTv = (TextView) convertView.findViewById(R.id.msg_hint_tv);
            viewHolder.msgStatusIv = (ImageView) convertView.findViewById(R.id.msg_status_fail_iv);
            viewHolder.clickAreaView = convertView.findViewById(R.id.msg_item_bg_layout);

            //Cancel long click here.
            //viewHolder.clickAreaView.setOnLongClickListener(this);
            convertView.setOnClickListener(this);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (PlaintTextViewHolder) convertView.getTag();
        }
        int msgBgResId = -1;
        if (message.getSendReceive() == MessageSendReceive.MSG_SEND) {
            // 发送的消息
            msgBgResId = R.drawable.chat_to_bg;
            viewHolder.msgHintTv.setVisibility(View.GONE);
        } else {
            // 接收的消息
            msgBgResId = R.drawable.chat_from_bg;
        }


        viewHolder.msgHintTv.setVisibility(View.GONE);
        viewHolder.msgTextTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        viewHolder.msgTextTv.setText(message.getData());

        viewHolder.clickAreaView.setBackgroundResource(msgBgResId);


        viewHolder.clickAreaView.setOnClickListener(null);
        final GestureDetector gestureDetector = new GestureDetector(context,
                new OnDoubleClick(context,message.getData()));
        viewHolder.clickAreaView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        return convertView;
    }

    /**
     * 初始化文本消息
     *
     * @param convertView
     * @param message
     * @return
     */
    private View initPictureView(Context context, View convertView, ChatMsgBaseInfo message) {
        PictureViewHolder viewHolder;
        if (convertView == null) {
            if (message.getSendReceive() == MessageSendReceive.MSG_SEND) {
                convertView = LayoutInflater.from(context).inflate(R.layout.message_info_picture_send, null, false);
            } else {
                convertView = LayoutInflater.from(context).inflate(R.layout.message_info_picture_rec, null, false);
            }
            viewHolder = new PictureViewHolder();
            viewHolder.checkedTextView = (CheckedTextView) convertView.findViewById(R.id.select_check_ctv);
            viewHolder.photoView = (PhotoView) convertView.findViewById(R.id.msg_contact_profile_pv);
            viewHolder.sendTimeTv = (TextView) convertView.findViewById(R.id.msg_send_time_tv);
            viewHolder.sendNameTv = (TextView) convertView.findViewById(R.id.msg_contact_name_tv);
            viewHolder.msgImageView = (ImageView) convertView.findViewById(R.id.msg_picture_iv);
            viewHolder.msgStatusIv = (ImageView) convertView.findViewById(R.id.msg_status_fail_iv);
            viewHolder.clickAreaView = convertView.findViewById(R.id.msg_item_bg_layout);
            viewHolder.clickAreaView.setOnClickListener(this);

            //viewHolder.clickAreaView.setOnLongClickListener(this);
            convertView.setOnClickListener(this);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (PictureViewHolder) convertView.getTag();
        }
        LayoutParams layoutParams = viewHolder.msgImageView.getLayoutParams();
        int width = message.getImageWidth();
        int height = message.getImageHeight();
        int maxSize = MAX_THUMBNAIL_WITH;
        if (width > maxSize && height > maxSize) {
            if (width >= height) {
                int orginHeight = height;
                height = height > maxSize ? maxSize : height;
                width = width * height / orginHeight;
            } else {
                int orginWidth = width;
                width = width > maxSize ? maxSize : width;
                height = height * width / orginWidth;
            }
        }
        layoutParams.width = width + viewHolder.msgImageView.getPaddingLeft() + viewHolder.msgImageView.getPaddingRight();
        layoutParams.height = height + viewHolder.msgImageView.getPaddingTop() + viewHolder.msgImageView.getPaddingBottom();
        viewHolder.msgImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        String imagePath;
        if (TextUtils.isEmpty(message.getThumbpath())) {
            imagePath = message.getFilepath();
        } else {
            imagePath = message.getThumbpath();
        }
        ImageLoaderManager.displayImage(imagePath, message.getThumbUrlPath(),
                R.drawable.default_image, viewHolder.msgImageView);
        viewHolder.msgImageView.setTag(message.getThumbUrlPath());

        return convertView;
    }

    /**
     * 初始化Gif消息
     *
     * @param convertView
     * @param message
     * @return
     */
    private View initGifPictureView(Context context, View convertView, ChatMsgBaseInfo message) {
        PictureViewHolder viewHolder;
        if (convertView == null) {
            if (message.getSendReceive() == MessageSendReceive.MSG_SEND) {
                convertView = LayoutInflater.from(context).inflate(R.layout.message_info_gif_picture_send, null, false);
            } else {
                convertView = LayoutInflater.from(context).inflate(R.layout.message_info_gif_picture_rec, null, false);
            }
            viewHolder = new PictureViewHolder();
            viewHolder.checkedTextView = (CheckedTextView) convertView.findViewById(R.id.select_check_ctv);
            viewHolder.photoView = (PhotoView) convertView.findViewById(R.id.msg_contact_profile_pv);
            viewHolder.sendTimeTv = (TextView) convertView.findViewById(R.id.msg_send_time_tv);
            viewHolder.sendNameTv = (TextView) convertView.findViewById(R.id.msg_contact_name_tv);
            viewHolder.msgImageView = (ImageView) convertView.findViewById(R.id.msg_picture_iv);
            viewHolder.gifView = (GifView) convertView.findViewById(R.id.msg_picture_gv);
            viewHolder.gifView.setGifImageType(GifImageType.COVER);
            viewHolder.msgStatusIv = (ImageView) convertView.findViewById(R.id.msg_status_fail_iv);
            viewHolder.clickAreaView = convertView.findViewById(R.id.msg_item_bg_layout);
            viewHolder.clickAreaView.setOnClickListener(this);
            //viewHolder.clickAreaView.setOnLongClickListener(this);
            convertView.setOnClickListener(this);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (PictureViewHolder) convertView.getTag();
        }
        if (message.getSendReceive() == MessageSendReceive.MSG_RECEIVE
                && message.getDownStatus() != ChatMsgBaseInfo.LOADED) {
            message.updateFilePercent();
        }
        if (message.getSendReceive() == MessageSendReceive.MSG_RECEIVE
                && message.getDownStatus() != ChatMsgBaseInfo.LOADED
                && !MessageUtil.isGifFile(message.getThumbpath())) {
            viewHolder.msgImageView.setVisibility(View.VISIBLE);
            ImageLoaderManager.displayImage(message.getThumbpath(), message.getThumbUrlPath(),
                    viewHolder.msgImageView);
            viewHolder.gifView.setGifImage((InputStream) null);
            viewHolder.gifView.setVisibility(View.GONE);
        } else {
            viewHolder.msgImageView.setVisibility(View.GONE);
            String filePah = message.getDownStatus() == ChatMsgBaseInfo.LOADED ?
                    message.getFilepath() : message.getThumbpath();
            InputStream inputStream;
            try {
                inputStream = new FileInputStream(filePah);
                viewHolder.gifView.setGifImage(inputStream);
            } catch (FileNotFoundException e) {
                logger.e("initGifPictureView, FileNotFoundException: " + e.getMessage());
            }
            viewHolder.gifView.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    /**
     * 初始化地图位置消息
     *
     * @param convertView
     * @param message
     * @return
     */
    private View initLocationView(Context context, View convertView, ChatMsgBaseInfo message) {
        MapLocationViewHolder viewHolder;
        if (convertView == null) {
            if (message.getSendReceive() == MessageSendReceive.MSG_SEND) {
                convertView = LayoutInflater.from(context).inflate(R.layout.message_info_location_send, null, false);
            } else {
                convertView = LayoutInflater.from(context).inflate(R.layout.message_info_location_rec, null, false);
            }
            viewHolder = new MapLocationViewHolder();
            viewHolder.checkedTextView = (CheckedTextView) convertView.findViewById(R.id.select_check_ctv);
            viewHolder.photoView = (PhotoView) convertView.findViewById(R.id.msg_contact_profile_pv);
            viewHolder.sendTimeTv = (TextView) convertView.findViewById(R.id.msg_send_time_tv);
            viewHolder.sendNameTv = (TextView) convertView.findViewById(R.id.msg_contact_name_tv);
            viewHolder.contentShowTv = (TextView) convertView.findViewById(R.id.msg_content_tv);
            viewHolder.detailShowTv = (TextView) convertView.findViewById(R.id.msg_detail_tv);
            //viewHolder.mapView = (ImageView) convertView.findViewById(R.id.map_position_iv);
            viewHolder.msgStatusIv = (ImageView) convertView.findViewById(R.id.msg_status_fail_iv);
            viewHolder.clickAreaView = convertView.findViewById(R.id.msg_item_bg_layout);
            viewHolder.clickAreaView.setOnClickListener(this);
            //viewHolder.clickAreaView.setOnLongClickListener(this);
            convertView.setOnClickListener(this);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (MapLocationViewHolder) convertView.getTag();
        }
        String[] address = message.getLocationLabel().split(MessageUtil.MAP_DETAIL_INFOR_SPLIT);
        String addressInfo = null;
        if(address.length > 1) {
            addressInfo = address[1];
        }
        viewHolder.contentShowTv.setText(address[0]);
        viewHolder.detailShowTv.setText(addressInfo);
        //ImageLoaderManager.displayImage(message.getFilepath(), message.getFileUrlPath(),
        //        R.drawable.message_icon_chat_position, viewHolder.mapView);
        return convertView;
    }

    /**
     * @param context
     * @param convertView
     * @param message
     * @描述：初始化视频消息
     */
    private View initVideoView(Context context, View convertView, ChatMsgBaseInfo message) {
        VideoViewHolder viewHolder;
        if (convertView == null) {
            if (message.getSendReceive() == MessageSendReceive.MSG_SEND) {
                convertView = LayoutInflater.from(context).inflate(R.layout.message_info_video_send, null, false);
            } else {
                convertView = LayoutInflater.from(context).inflate(R.layout.message_info_video_rec, null, false);
            }
            viewHolder = new VideoViewHolder();
            viewHolder.thumbView = (ImageView) convertView.findViewById(R.id.message_video_thumb);
            viewHolder.controlButton = (ImageView) convertView.findViewById(R.id.message_video_btn_control);
            viewHolder.progressBar = (RoundProgressBar) convertView.findViewById(R.id.message_video_progressBar);
            viewHolder.checkedTextView = (CheckedTextView) convertView.findViewById(R.id.select_check_ctv);
            viewHolder.sendTimeTv = (TextView) convertView.findViewById(R.id.msg_send_time_tv);
            viewHolder.sendNameTv = (TextView) convertView.findViewById(R.id.msg_contact_name_tv);
            viewHolder.photoView = (PhotoView) convertView.findViewById(R.id.msg_contact_profile_pv);
            viewHolder.msgStatusIv = (ImageView) convertView.findViewById(R.id.msg_status_fail_iv);
            viewHolder.clickAreaView = convertView.findViewById(R.id.msg_item_bg_layout);
            viewHolder.clickAreaView.setOnClickListener(this);
            //viewHolder.clickAreaView.setOnLongClickListener(this);
            convertView.setOnClickListener(this);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (VideoViewHolder) convertView.getTag();
        }
        ImageLoaderManager.displayImage(message.getThumbpath(), message.getThumbUrlPath(),
                R.drawable.downvideoerror, viewHolder.thumbView);
        return convertView;
    }

    /**
     * 初始化语音消息
     *
     * @param convertView
     * @param message
     * @param context
     * @return convertView
     */
    private View initAudioView(Context context, View convertView, final ChatMsgBaseInfo message) {
        final AudioViewHolder viewHolder;
        if (convertView == null) {
            int inflateResource = R.layout.message_info_audio_receive;
            if (message.getSendReceive() == MessageSendReceive.MSG_SEND) {
                inflateResource = R.layout.message_info_audio_send;
            } else {
                inflateResource = R.layout.message_info_audio_receive;
            }
            convertView = LayoutInflater.from(context).inflate(inflateResource, null, false);
            viewHolder = new AudioViewHolder();
            viewHolder.photoView = (PhotoView) convertView.findViewById(R.id.msg_contact_profile_pv);
            viewHolder.checkedTextView = (CheckedTextView) convertView.findViewById(R.id.select_check_ctv);
            viewHolder.sendTimeTv = (TextView) convertView.findViewById(R.id.msg_send_time_tv);
            viewHolder.sendNameTv = (TextView) convertView.findViewById(R.id.msg_contact_name_tv);
            viewHolder.audioDurationTv = (TextView) convertView.findViewById(R.id.msg_audio_duration);
            viewHolder.audioVoice = (ImageView) convertView.findViewById(R.id.msg_audio_voice);
            if (message.getSendReceive() == MessageSendReceive.MSG_SEND) {
                viewHolder.msgStatusIv = (ImageView) convertView.findViewById(R.id.msg_status_fail_iv);
            } else {
                viewHolder.audioUnreadView = (ImageView) convertView.findViewById(R.id.msg_audio_unread);
            }
            viewHolder.clickAreaView = convertView.findViewById(R.id.msg_item_bg_layout);
            viewHolder.clickAreaView.setOnClickListener(this);
            //viewHolder.clickAreaView.setOnLongClickListener(this);
            convertView.setOnClickListener(this);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (AudioViewHolder) convertView.getTag();
        }
        int audioDurationSec = message.getDuration();
        viewHolder.audioDurationTv.setText(audioDurationSec + "''");

        int audioWidth = (AUDIO_BUBBLE_MAX_WIDTH - AUDIO_BUBBLE_MIN_WIDTH) *
                audioDurationSec / PressTalkTouchListener.MAX_VOICE_DURATION + AUDIO_BUBBLE_MIN_WIDTH;

        int extPadding = viewHolder.clickAreaView.getPaddingTop() * 2;
        int paddingLeft = audioWidth / 2;
        int paddingRight = paddingLeft;
        if (message.getSendReceive() == MessageSendReceive.MSG_SEND) {
            paddingRight += extPadding;
        } else {
            paddingLeft += extPadding;
            if (message.getIsRead()) {
                viewHolder.audioUnreadView.setVisibility(View.GONE);
            } else {
                viewHolder.audioUnreadView.setVisibility(View.VISIBLE);
            }
        }
        viewHolder.clickAreaView.setPadding(paddingLeft, viewHolder.clickAreaView.getPaddingTop(), paddingRight,
                viewHolder.clickAreaView.getPaddingBottom());

        AnimationDrawable currentAnim = (AnimationDrawable) viewHolder.audioVoice.getBackground();
        if (message.getDownStatus() == ChatMsgBaseInfo.LOADED) {
            currentAnim.stop();
            currentAnim.selectDrawable(0);
        } else if (message.getDownStatus() == ChatMsgBaseInfo.DOWNLOADING) {
            currentAnim.start();
        }

        return convertView;
    }

    /**
     * 初始化消息中共通部分：头像、消息状态、时间、选择状态
     *
     * @param message
     * @param viewHolder
     */
    private void initMsgCommonView(Context context, ChatMsgBaseInfo message, BaseViewHolder viewHolder,
                                   boolean isInSelectMode, boolean isShowPhoto, boolean isShowName) {
        // 发送状态
        if (message.getSendReceive() == MessageSendReceive.MSG_SEND) {
            viewHolder.msgStatusIv.setTag(message);
            viewHolder.msgStatusIv.setOnClickListener(this);
            // 发送的消息
            if (message.getMsgState() == MessageState.MSG_STATE_SEND_FAIL) {
                viewHolder.msgStatusIv.setImageResource(R.drawable.chat_msg_status_fail);
                viewHolder.msgStatusIv.setVisibility(View.VISIBLE);
                viewHolder.msgStatusIv.setAnimation(null);
                viewHolder.msgStatusIv.setEnabled(true);
            } else if (message.getMsgState() == MessageState.MSG_STATE_SEND_ING) {
                viewHolder.msgStatusIv.setImageResource(R.drawable.chat_msg_status_sendding);
                viewHolder.msgStatusIv.setVisibility(View.VISIBLE);
                viewHolder.msgStatusIv.setEnabled(false);
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.rotate_animation);
                LinearInterpolator lir = new LinearInterpolator();
                animation.setInterpolator(lir);
                viewHolder.msgStatusIv.startAnimation(animation);
            } else {
                viewHolder.msgStatusIv.setVisibility(View.GONE);
                viewHolder.msgStatusIv.setAnimation(null);
            }
        }
        // 是否选中状态
        if (isInSelectMode) {
            viewHolder.checkedTextView.setChecked(message.isChecked());
            viewHolder.checkedTextView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.checkedTextView.setVisibility(View.GONE);
        }
        // 发送时间
        setDateElement(context, message.getTime(), viewHolder.sendTimeTv);

        // 名字和头像相关信息获取
        String photoNum = message.getContact();
        if (message.getDisplayName() == null) {
            // 获取联系人信息
            String key = CommonUtils.getPhoneNum(photoNum);
            ContactInfo contactInfo = null;
            if (mSearchContactInfo.containsKey(key)) {
                contactInfo = mSearchContactInfo.get(key);
            } else if (message.getSendReceive() == MessageSendReceive.MSG_SEND) {
                contactInfo = ProfileManager.getInstance().getMyContactInfo();
            } else {
                contactInfo = ContactsManager.getInstance(mContext).getContactByUsrObjId(photoNum);
            }
            mSearchContactInfo.put(key, contactInfo);
            if (contactInfo != null) {
                message.setDisplayName(contactInfo.getUsername());
                message.setProfileKey(contactInfo.getPortrait().getUrl());
            } else {
                if (message.getSendReceive() == MessageSendReceive.MSG_SEND) {
                    message.setDisplayName(context.getResources().getString(R.string.me));
                } else {
                    message.setDisplayName("");
                    message.setProfileKey(null);
                }
            }
        }
        // 头像
        if (isShowPhoto) {
            if (viewHolder.photoView.getTag() == null) {
                viewHolder.photoView.setOnClickListener(this);
            }
            viewHolder.photoView.setTag(photoNum);
            viewHolder.photoView.setBaseData(message.getDisplayName(), message.getProfileKey(),
                    message.getNamePinyin(), R.drawable.default_profile_portrait);
            viewHolder.photoView.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) viewHolder.checkedTextView.getLayoutParams();
            lp.topMargin = context.getResources().getDimensionPixelSize(R.dimen.public_padding_10);
        } else if (viewHolder.photoView != null) {
            viewHolder.photoView.setVisibility(View.GONE);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) viewHolder.checkedTextView.getLayoutParams();
            lp.topMargin = 0;
        }

        if (viewHolder.sendNameTv != null) {
            if (isShowName) {
                String number = CommonUtils.getPhoneNum(message.getContact());
                if (!TextUtils.isEmpty(message.getDisplayName())) {
                    viewHolder.sendNameTv.setText(message.getDisplayName());
                } else if (mNickNameMap.containsKey(number)) {
                    viewHolder.sendNameTv.setText(mNickNameMap.get(number));
                } else {
                    viewHolder.sendNameTv.setText(message.getContact());
                }
                viewHolder.sendNameTv.setVisibility(View.VISIBLE);
            } else {
                viewHolder.sendNameTv.setVisibility(View.GONE);
            }
        }

        viewHolder.clickAreaView.setTag(R.id.msg_item_bg_layout, message);
    }

    /**
     * 显示消息发送时间
     *
     * @param context
     * @param time
     * @param timeTv
     */
    private void setDateElement(Context context, Long time, TextView timeTv) {
        long currentTime = System.currentTimeMillis();
        if (time != null) {
            long sendTime = time;
            if ((currentTime - sendTime) < 5 * 60 * 1000) {
                timeTv.setVisibility(View.GONE);
            } else {
                timeTv.setVisibility(View.VISIBLE);
                timeTv.setText(TimeFormattedUtil.getDetailDisplayTime(context, sendTime));
            }
        } else {
            timeTv.setVisibility(View.VISIBLE);
            timeTv.setText(TimeFormattedUtil.getDetailDisplayTime(context, currentTime));
        }
    }

    /**
     * 纯文本消息
     */
    public class BaseViewHolder {
        CheckedTextView checkedTextView;
        PhotoView photoView;
        TextView sendTimeTv;
        TextView sendNameTv;
        ImageView msgStatusIv;
        View clickAreaView;
    }

    public class SysMsgViewHolder extends BaseViewHolder {
        TextView msgTextTv;
        TextView msgHintTv;
    }


    /**
     * 纯文本消息
     */
    public class PlaintTextViewHolder extends BaseViewHolder {
        TextView msgTextTv;
        TextView msgHintTv;
    }

    /**
     * Vcard消息
     */
    public class VCardViewHolder extends BaseViewHolder {
        TextView contentShowTv;
    }

    /**
     * Map location消息
     */
    public class MapLocationViewHolder extends BaseViewHolder {
        TextView contentShowTv;
        TextView detailShowTv;
        ImageView mapView;
    }

    /**
     * 视频消息
     */
    public class VideoViewHolder extends BaseViewHolder {
        public ImageView thumbView;
        public ImageView controlButton;
        public RoundProgressBar progressBar;
    }

    /**
     * 音频消息
     */
    public class AudioViewHolder extends BaseViewHolder {
        TextView audioDurationTv;
        ImageView audioVoice;
        ImageView audioUnreadView;
    }

    /**
     * 图片消息
     */
    public class PictureViewHolder extends BaseViewHolder {
        ImageView msgImageView;
        GifView gifView;
    }

    /**
     * 图文消息
     */
    public class MixedViewHolder  extends BaseViewHolder {
        TextView msgTitle;
        ImageView msgImageIv;
        TextView msgContentTv;
        TextView msgCreateTimeTv;
    }

    @Override
    public void onClick(View view) {
        if (mMessageItemListener == null) {
            return;
        }
        switch (view.getId()) {
            case R.id.msg_popup_menu_layout:
                mPopupWindow.dismiss();
                break;
            case R.id.msg_mark_collect_tv:
                // 收藏
                mPopupWindow.dismiss();
                View popupView = mPopupWindow.getContentView();
                ChatMsgBaseInfo collectMsg = (ChatMsgBaseInfo) popupView.getTag();
                mMessageItemListener.onMsgCollect((View) popupView.getTag(R.id.msg_item_layout), collectMsg);
                break;
            case R.id.msg_mark_delete_tv:
                // 删除
                mPopupWindow.dismiss();
                ChatMsgBaseInfo deleteMsg = (ChatMsgBaseInfo) mPopupWindow.getContentView().getTag();
                mMessageItemListener.onMsgDelete(deleteMsg);
                break;
            case R.id.msg_mark_forward_tv:
                // 转发
                mPopupWindow.dismiss();
                View contentView = mPopupWindow.getContentView();
                ChatMsgBaseInfo forwardMsg = (ChatMsgBaseInfo) contentView.getTag();
                mMessageItemListener.onMsgForward((View) contentView.getTag(R.id.msg_item_layout), forwardMsg);
                break;
            case R.id.msg_mark_copy_tv:
                // 拷贝
                mPopupWindow.dismiss();
                ChatMsgBaseInfo copyMsg = (ChatMsgBaseInfo) mPopupWindow.getContentView().getTag();
                ClipData clip = ClipData.newPlainText("message", copyMsg.getData());
                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(clip);
                break;
            case R.id.msg_mark_more_tv:
                // 更多
                mPopupWindow.dismiss();
                View popupContentView = mPopupWindow.getContentView();
                ChatMsgBaseInfo moreMsg = (ChatMsgBaseInfo) popupContentView.getTag();
                mMessageItemListener.onMsgMore((View) popupContentView.getTag(R.id.msg_item_layout), moreMsg);
                break;
            case R.id.msg_status_fail_iv:
                // 发送失败，重新发送按钮
                ChatMsgBaseInfo chatMessageInfo = (ChatMsgBaseInfo) view.getTag();
                boolean isShowNotice = DialogShowSharedPreferencesUtil.isDialogShow(mContext,
                        DialogShowSharedPreferencesUtil.DIALOG_SHOW_RESEND_MESSAGE_NOTICE);
                if (isShowNotice) {
                    mNoticesDialog = DialogFactory.getNoticeDialog(mContext, R.string.msg_send_fail, this,
                            chatMessageInfo);
                    mNoticesDialog.show();
                } else {
                    mMessageItemListener.messageResend(chatMessageInfo);
                }
                break;
            case R.id.msg_contact_profile_pv:
                // 点击了头像，查看联系人详情
                //TODO:
                String number = (String) view.getTag();
                break;
            default:
                // 点击整个条目
                ChatMsgBaseInfo clickedMessage = (ChatMsgBaseInfo) view.getTag(R.id.msg_item_bg_layout);
                mMessageItemListener.messageItemOnClick(view, clickedMessage);
                break;
        }
    }

    @Override
    public void onNoticeDialogConfirmClick(boolean isChecked, Object value) {
        DialogShowSharedPreferencesUtil.setDialogShow(mContext,
                DialogShowSharedPreferencesUtil.DIALOG_SHOW_RESEND_MESSAGE_NOTICE, !isChecked);
        mMessageItemListener.messageResend((ChatMsgBaseInfo) value);
        mNoticesDialog = null;
    }

    @Override
    public boolean onLongClick(View view) {
        if (mMessageItemListener != null) {
            ChatMsgBaseInfo clickedMessage = (ChatMsgBaseInfo) view.getTag(R.id.msg_item_bg_layout);
            boolean isShowMenu = mMessageItemListener.onItemLongClicked(view, clickedMessage);
            if (isShowMenu) {
                if (mPopupWindow == null) {
                    mPopupWindow = createPopupWindow(clickedMessage, view);
                } else {
                    refreshPopupMenu(mPopupWindow.getContentView(), clickedMessage, view);
                }
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                mPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY, 0, 0);
            }
        }
        return true;
    }

    /**
     * 创建消息操作菜单
     *
     * @param clickedMessage
     * @return
     */
    private PopupWindow createPopupWindow(ChatMsgBaseInfo clickedMessage, View clickedView) {
        View popupContentView = View.inflate(mContext, R.layout.message_popup_menu, null);
        popupContentView.setOnClickListener(this);
        popupContentView.findViewById(R.id.msg_mark_copy_tv).setOnClickListener(this);
        popupContentView.findViewById(R.id.msg_mark_forward_tv).setOnClickListener(this);
        popupContentView.findViewById(R.id.msg_mark_collect_tv).setOnClickListener(this);
        if (isCollectMsgPage) {
            ((TextView) popupContentView.findViewById(R.id.msg_mark_collect_tv)).setText(R.string.menu_mark_cancle_collect);
            popupContentView.findViewById(R.id.msg_mark_delete_ll).setVisibility(View.GONE);
        } else {
            popupContentView.findViewById(R.id.msg_mark_delete_tv).setOnClickListener(this);
        }
        popupContentView.findViewById(R.id.msg_mark_more_tv).setOnClickListener(this);
        refreshPopupMenu(popupContentView, clickedMessage, clickedView);
        PopupWindow popupWindow = new PopupWindow(popupContentView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        return popupWindow;
    }

    /**
     * 刷新菜单项
     *
     * @param popupContentView
     * @param msgInfo
     * @param clickedView
     */
    private void refreshPopupMenu(View popupContentView, ChatMsgBaseInfo msgInfo, View clickedView) {
        popupContentView.setTag(msgInfo);
        popupContentView.setTag(R.id.msg_item_layout, clickedView);
        TextView contactView = (TextView) popupContentView.findViewById(R.id.contact_name_tv);
        if (!TextUtils.isEmpty(msgInfo.getDisplayName())) {
            contactView.setText(msgInfo.getDisplayName());
        } else if (mNickNameMap.containsKey(CommonUtils.getPhoneNum(msgInfo.getContact()))) {
            contactView.setText(mNickNameMap.get(CommonUtils.getPhoneNum(msgInfo.getContact())));
        } else {
            contactView.setText(msgInfo.getContact());
        }
        boolean isEnableCopy = false;
        boolean isEnableForward = true;
        boolean isEnableCollect = true;
        if (msgInfo.getMsgType() == MessageType.MSG_TYPE_AUDIO) {
            isEnableForward = false;
        } else if (msgInfo.getMsgType() == MessageType.MSG_TYPE_TEXT) {
            isEnableCopy = true;
        }
        if (msgInfo.isBurnAfterMsg()) {
            // 阅后即焚/图文混排消息不能收藏、转发和收藏
            isEnableForward = false;
            isEnableCopy = false;
            isEnableCollect = false;
        }
        if ((msgInfo.getMsgType() == MessageType.MSG_TYPE_VIDEO
                || msgInfo.getMsgType() == MessageType.MSG_TYPE_IMAGE)
                && TextUtils.isEmpty(msgInfo.getFilepath())) {
            // 文件未下载，更新数据
            ChatMsgBaseInfo msg = WalkArroundMsgManager.getInstance(mContext).getMessageById(msgInfo.getMsgId());
            if (msg != null) {
                msgInfo.setFilePath(msg.getFilepath());
            }
        }
        if ((msgInfo.getMsgType() == MessageType.MSG_TYPE_VIDEO
                || msgInfo.getMsgType() == MessageType.MSG_TYPE_IMAGE)
                && msgInfo.getDownStatus() != ChatMsgBaseInfo.LOADED
                && TextUtils.isEmpty(msgInfo.getFilepath())) {
            // 有未下载的消息，未下载的消息不能收藏
            isEnableForward = false;
            isEnableCollect = false;
        }
        popupContentView.findViewById(R.id.msg_mark_collect_tv).setEnabled(isEnableCollect);
        popupContentView.findViewById(R.id.msg_mark_copy_tv).setEnabled(isEnableCopy);
        popupContentView.findViewById(R.id.msg_mark_forward_tv).setEnabled(isEnableForward);
    }

    public interface ItemListener {

        /**
         * 点击事件
         *
         * @param clickedItemView
         * @param clickedMessage
         */
        public void messageItemOnClick(View clickedItemView, ChatMsgBaseInfo clickedMessage);

        /**
         * 重发消息
         */
        public void messageResend(ChatMsgBaseInfo clickedMessage);

        /**
         * 选择模式变化了
         *
         * @param clickedMessage
         */
        public boolean onItemLongClicked(View clickedItemView, ChatMsgBaseInfo clickedMessage);

        /**
         * 收藏消息
         *
         * @param clickedMessage
         */
        public void onMsgCollect(View clickedView, ChatMsgBaseInfo clickedMessage);

        /**
         * 删除消息
         *
         * @param clickedMessage
         */
        public void onMsgDelete(ChatMsgBaseInfo clickedMessage);

        /**
         * 更多操作
         *
         * @param clickedMessage
         */
        public void onMsgMore(View clickedView, ChatMsgBaseInfo clickedMessage);

        /**
         * 转发消息
         *
         * @param clickedView
         * @param clickedMessage
         */
        public void onMsgForward(View clickedView, ChatMsgBaseInfo clickedMessage);

    }

    /**
     * 双击
     */
    public class OnDoubleClick extends GestureDetector.SimpleOnGestureListener {

        private Context context;
        private String mContent;

        public OnDoubleClick(Context context, String content) {
            this.context = context;
            mContent = content;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            Intent intent = new Intent(context, PlaintTextDetailActivity.class);
            intent.putExtra(PlaintTextDetailActivity.INTENT_CONTENT, mContent);
            context.startActivity(intent);
            return false;
        }
    }
}
