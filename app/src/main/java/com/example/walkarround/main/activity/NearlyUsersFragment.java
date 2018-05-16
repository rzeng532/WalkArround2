package com.example.walkarround.main.activity;

import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVUser;
import com.example.walkarround.R;
import com.example.walkarround.assistant.AssistantHelper;
import com.example.walkarround.base.task.TaskUtil;
import com.example.walkarround.base.view.DialogFactory;
import com.example.walkarround.base.view.PortraitView;
import com.example.walkarround.base.view.RippleView;
import com.example.walkarround.flingswipe.SwipeFlingAdapterView;
import com.example.walkarround.main.adapter.NearlyUserListAdapter;
import com.example.walkarround.main.model.ContactInfo;
import com.example.walkarround.main.parser.WalkArroundJsonResultParser;
import com.example.walkarround.main.task.LikeSomeOneTask;
import com.example.walkarround.message.activity.BuildMessageActivity;
import com.example.walkarround.message.activity.ConversationActivity;
import com.example.walkarround.message.manager.ContactsManager;
import com.example.walkarround.message.manager.WalkArroundMsgManager;
import com.example.walkarround.message.model.ChatMsgBaseInfo;
import com.example.walkarround.message.model.MessageRecipientInfo;
import com.example.walkarround.message.util.MessageConstant;
import com.example.walkarround.message.util.MessageUtil;
import com.example.walkarround.message.util.MsgBroadcastConstants;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.myself.model.MyProfileInfo;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.Logger;
import com.example.walkarround.util.http.HttpTaskBase;
import com.example.walkarround.util.http.HttpTaskBase.TaskResult;
import com.example.walkarround.util.http.HttpUtil;
import com.example.walkarround.util.http.ThreadPoolManager;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class NearlyUsersFragment extends Fragment implements View.OnClickListener {

    private static final Logger logger = Logger.getLogger(NearlyUsersFragment.class.getSimpleName());

    public static final String ARG_PLANET_NUMBER = "planet_number";
    private View mViewRoot;

    //For title and slide menu
    private PortraitView mPvPortrait;
    //private View mTvTitle;
    private ImageView mIvChatEntrance;
    private ImageView mIvUnreadIcon;

    //For nearly user list.
    private SwipeFlingAdapterView mUserFrame;
    //For buttons.
    private View mUserFrameButtons;

    //For radar display.
    private RelativeLayout mRlSearchArea;
    private ViewFlipper mSearchingNoticeView;
    private RippleView mSearchingView;
    private PortraitView mSearchingPortrait;

    //Image mLeftDislike / mRightLike = dislike / like
    private ImageView mLeftDislike;
    private ImageView mRightLike;
    private String mStrFromUsrId;
    private String mStrToUsrId;

    //Real data from server.
    private static List<ContactInfo> mNearlyUserList = new ArrayList<>();
    private static List<ContactInfo> mDeleletedUserList = new ArrayList<>();

    //Private static instance for getInstance.
    private static NearlyUsersFragment mNUFragment;

    private NearlyUserListAdapter mUserListAdapter;

    private Dialog mMapDialog;


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MsgBroadcastConstants.ACTION_MESSAGE_NEW_RECEIVED.equals(action)) {
                // 新到消息
                mIvUnreadIcon.setVisibility(View.VISIBLE);
            }
        }
    };

    private HttpTaskBase.onResultListener mLikeSomeoneListener = new HttpTaskBase.onResultListener() {
        @Override
        public void onPreTask(String requestCode) {

        }

        @Override
        public void onResult(Object object, TaskResult resultCode, String requestCode, String threadId) {
            //Task success.
            //If you like some and the reponse status is "2", it means "toUser" also like you.
            if (TaskResult.SUCCEESS == resultCode) {
                //Get status & Get TO user.
                String strState = WalkArroundJsonResultParser.parseRequireCode((String) object, HttpUtil.HTTP_RESPONSE_KEY_LIKE_STATUS);
                if (strState.equalsIgnoreCase(TaskUtil.RESPONSE_USR_STATUS_ACCEPT)) {
                    String strUser = MessageUtil.getFriendIdFromServerData((String) object);
                    addCacheContact(strUser);
                    Message msg = mFragmentHandler.obtainMessage();
                    msg.what = SOMEONE_LIKE_YOU;
                    msg.obj = strUser;
                    mFragmentHandler.sendMessage(msg);
                    long convThreadId = sayHello(strUser);
                    if(convThreadId >= 0) {
                        WalkArroundMsgManager.getInstance(getActivity().getApplicationContext()).updateConversationStatus(convThreadId, MessageUtil.WalkArroundState.STATE_IM);
                    }
                }

                //TODO: This log should be deleted later.
                logger.d("like someone response: \r\n" + (String) object);
            } else if (TaskResult.FAILED == resultCode) {
                AVAnalytics.onEvent(getActivity(), AppConstant.ANA_EVENT_LIKE, AppConstant.ANA_TAG_RET_FAIL);
                logger.d("like someone response failed");
            }
        }

        @Override
        public void onProgress(int progress, String requestCode) {

        }
    };

    private HttpTaskBase.onResultListener mAddFriendTaskListener = new HttpTaskBase.onResultListener() {
        @Override
        public void onPreTask(String requestCode) {

        }

        @Override
        public void onResult(Object object, HttpTaskBase.TaskResult resultCode, String requestCode, String threadId) {
            //Task success.
            if (HttpTaskBase.TaskResult.SUCCEESS == resultCode && requestCode.equalsIgnoreCase(HttpUtil.HTTP_FUNC_ADD_FRIEND)) {
                //If add friend ok, update friend list.

            } else {
                logger.d("add friend  response failed");
            }
        }

        @Override
        public void onProgress(int progress, String requestCode) {

        }
    };

    //Handler
    private final int RADAR_STOP_DELAY = 3 * 1000;
    private final int UPDATE_NEARLY_USERS = 0;
    private final int SOMEONE_LIKE_YOU = 1;
    private final int DISPLAY_RADAR = 2;
    private Handler mFragmentHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_NEARLY_USERS) {
                showNearyUser();
            } else if(msg.what == SOMEONE_LIKE_YOU) {
                if(TextUtils.isEmpty((String)msg.obj)) {
                    return;
                }
                ContactInfo user = ContactsManager.getInstance(NearlyUsersFragment.this.getActivity().getApplicationContext())
                        .getContactByUsrObjId((String)msg.obj);
                if(user == null) {
                    return;
                }

                ProfileManager.getInstance().setCurUsrDateState(MessageUtil.WalkArroundState.STATE_IM);

                String friendName = TextUtils.isEmpty(user.getUsername()) ? user.getMobilePhoneNumber() : user.getUsername();
                if(friendName.length() > AppConstant.SHORTNAME_LEN) {
                    friendName = friendName.substring(0, AppConstant.SHORTNAME_LEN) + "...";
                }

                DialogFactory.getMappingDialog(NearlyUsersFragment.this.getActivity()
                        , getActivity().getString(R.string.mapping_indication, friendName)
                        , null).show();
            } else if(msg.what == DISPLAY_RADAR) {
                mSearchingView.start();
            }
        }
    };

    protected void updateNearlyUserList(List<ContactInfo> list) {

        if (getActivity() == null || getActivity().isDestroyed()) {
            return;
        }

        if(list == null || list.size() <= 0) {
            return;
        }

        synchronized (NearlyUsersFragment.class) {
            mNearlyUserList.clear();
            mDeleletedUserList.clear();
            for(ContactInfo one : list) {
                if(TextUtils.isEmpty(one.getUsername())
                        || one.getPortrait() ==  null) {
                    //没有头像信息 & 没有名字信息，直接略过
                    continue;
                } else {
                    mNearlyUserList.add(one);
                }
            }

            if(mNearlyUserList.size() > 0) {
                mStrToUsrId = mNearlyUserList.get(0).getObjectId();
            }
        }

        mFragmentHandler.removeMessages(UPDATE_NEARLY_USERS);
        mFragmentHandler.sendEmptyMessageDelayed(UPDATE_NEARLY_USERS, RADAR_STOP_DELAY);
    }

    /*
     * False: nearly user list is empty;
     * True: there is nearly user on list;
     */
    protected boolean isThereNearlyUser() {
        return (mNearlyUserList == null || mNearlyUserList.size() <= 0) ? false : true;
    }

    public void clearNearlyUserList() {
        mNearlyUserList.clear();
        mDeleletedUserList.clear();
    }

    public NearlyUsersFragment() {
        // Empty constructor required for fragment subclasses
    }

    public static NearlyUsersFragment getInstance() {

        if (mNUFragment == null) {
            synchronized (NearlyUsersFragment.class) {
                if (mNUFragment == null) {
                    mNUFragment = new NearlyUsersFragment();
                    Bundle args = new Bundle();
                    args.putInt(NearlyUsersFragment.ARG_PLANET_NUMBER, 0);
                    mNUFragment.setArguments(args);
                }
            }
        }

        return mNUFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initView(inflater, container);
        initData(savedInstanceState);
        return mViewRoot;
    }

    @Override
    public void onResume() {
        super.onResume();
        AVAnalytics.onFragmentStart("NearlyUsersFragment");
        setUnreadState();

        mStrFromUsrId = AVUser.getCurrentUser().getObjectId();
        if (mNearlyUserList != null && mNearlyUserList.size() > 0) {
            showNearyUser();
        } else {
            showRadar();
        }


        MyProfileInfo myProfileInfo = ProfileManager.getInstance().getMyProfile();

        if (!TextUtils.isEmpty(myProfileInfo.getUsrName()) && !TextUtils.isEmpty(myProfileInfo.getMobileNum())) {
            mPvPortrait.setBaseData(myProfileInfo.getUsrName(), myProfileInfo.getPortraitPath(),
                    myProfileInfo.getUsrName().substring(0, 1), -1);

            mSearchingPortrait.setBaseData(myProfileInfo.getUsrName(), myProfileInfo.getPortraitPath(),
                    myProfileInfo.getUsrName().substring(0, 1), -1);
        }

        // 收到新的message消息
        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(MsgBroadcastConstants.ACTION_MESSAGE_NEW_RECEIVED);
        getActivity().registerReceiver(mMessageReceiver, commandFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        AVAnalytics.onFragmentEnd("NearlyUsersFragment");

        if(mSearchingView != null && mSearchingView.isStarting()) {
            mSearchingView.stop();
        }
        if (mSearchingNoticeView != null && mSearchingNoticeView.isFlipping()) {
            mSearchingNoticeView.stopFlipping();
        }

        if (null != mMessageReceiver) {
            getActivity().unregisterReceiver(mMessageReceiver);
        }

        if(mFragmentHandler != null) {
            mFragmentHandler.removeMessages(UPDATE_NEARLY_USERS);
            mFragmentHandler.removeMessages(DISPLAY_RADAR);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFragmentHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_title_portrait:
                //TODO: we should use handler for communication between activty and fragment later.
                DrawerLayout slideMenu = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                LinearLayout mViewLeftMenu = (LinearLayout) getActivity().findViewById(R.id.left_drawer);
                slideMenu.openDrawer(mViewLeftMenu);
                //getActivity().finish();
                break;
            case R.id.right_chat_iv:
                //Start build message activity
                startActivity(new Intent(getActivity(), ConversationActivity.class));
                break;
            default:
                break;
        }
    }

    private void initView(LayoutInflater inflater, ViewGroup container) {
        mViewRoot = inflater.inflate(R.layout.fragment_planet, container, false);

        //Set those elements as gone at first and display them while there is data from server.
        mUserFrame = ((SwipeFlingAdapterView) mViewRoot.findViewById(R.id.userFrame));
        mUserFrame.setVisibility(View.GONE);
        mUserFrameButtons = ((View) mViewRoot.findViewById(R.id.userFrameButtons));
        mUserFrameButtons.setVisibility(View.GONE);

        //Like and Dislike button - init view & set onClicklistener.
        mLeftDislike = (ImageView) mViewRoot.findViewById(R.id.left);
        mLeftDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                left();
            }
        });
        mRightLike = (ImageView) mViewRoot.findViewById(R.id.right);
        mRightLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                right();
            }
        });

        //Init title
        //Left portrait
        mPvPortrait = (PortraitView) mViewRoot.findViewById(R.id.iv_title_portrait);
        mPvPortrait.setOnClickListener(this);
        //Middle
        //mTvTitle = (View) mViewRoot.findViewById(R.id.title_name);
        //Right icon
        mIvChatEntrance = (ImageView) mViewRoot.findViewById(R.id.right_chat_iv);
        mIvChatEntrance.setOnClickListener(this);

        mIvUnreadIcon = (ImageView) mViewRoot.findViewById(R.id.iv_msg_unread);

        //Searching UI will be displayed at first.
        mRlSearchArea = (RelativeLayout) mViewRoot.findViewById(R.id.rlSearching);
        mSearchingNoticeView = (ViewFlipper) mRlSearchArea.findViewById(R.id.searching_notice_flipper_view);
        mSearchingNoticeView.setInAnimation(getActivity(), R.anim.slide_in_from_bottom);
        mSearchingNoticeView.setOutAnimation(getActivity(), R.anim.slide_out_to_top);
        mSearchingPortrait = (PortraitView) mViewRoot.findViewById(R.id.searching_center_portrait);
        mSearchingView = (RippleView) mViewRoot.findViewById(R.id.searchingView);

        int width =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        int height =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        mSearchingPortrait.measure(width,height);
        mSearchingView.measure(width,height);

        mSearchingView.setInitRadiusByPortraitWidth(mSearchingPortrait);
        mSearchingView.start();
    }

    private void initData(Bundle savedInstanceState) {
        mUserListAdapter = new NearlyUserListAdapter(getActivity(), mNearlyUserList);

        mUserFrame.setAdapter(mUserListAdapter);
        mUserFrame.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                if (mNearlyUserList != null && mNearlyUserList.size() > 0) {
                    synchronized (NearlyUsersFragment.class) {
                        mStrToUsrId = mNearlyUserList.get(0).getObjectId();
                        mDeleletedUserList.add(mNearlyUserList.remove(0));
                    }
                    mUserListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                AVAnalytics.onEvent(getActivity(), AppConstant.ANA_EVENT_DISLIKE);
                if (mNearlyUserList != null && mNearlyUserList.size() == 0) {
                    //If there is no data, display radar again.
                    showRadar();
                }
            }

            @Override
            public void onRightCardExit(Object dataObject) {

                if(AssistantHelper.isThereGuideStep()
                        && AssistantHelper.getInstance().validateStepState(AssistantHelper.STEP_SEARCHING)
                        && !TextUtils.isEmpty(mStrToUsrId) && mStrToUsrId.equals(AssistantHelper.ASSISTANT_OBJ_ID)) {
                    //Assistant's searching step is completed.
                    AssistantHelper.getInstance().updateStepState(AssistantHelper.STEP_INTRODUCE_MYSELF_MASK);
                    AssistantHelper.getInstance().updateStepState(AssistantHelper.STEP_SEARCHING_MASK);
                    //Generate a msg record
                    WalkArroundMsgManager.getInstance(getActivity().getApplicationContext())
                            .sendAssistantTextMsg(AssistantHelper.ASSISTANT_OBJ_ID, getString(R.string.msg_say_hello), null);
                    List<String> receipient = new ArrayList<String>();
                    receipient.add(AssistantHelper.ASSISTANT_OBJ_ID);
                    MessageRecipientInfo recipientInfo = WalkArroundMsgManager.getInstance(getActivity().getApplicationContext())
                            .abstractReceiptInfo(receipient, MessageConstant.ChatType.CHAT_TYPE_ONE2ONE);
                    ChatMsgBaseInfo messageInfo = BuildMessageActivity.generateAssistantMsg(recipientInfo);
                    messageInfo.setData(getString(R.string.assistant_hi, ProfileManager.getInstance().getMyContactInfo().getUsername()));
                    WalkArroundMsgManager.getInstance(getActivity().getApplicationContext())
                            .saveChatmsg(messageInfo);
                    WalkArroundMsgManager.getInstance(getActivity().getApplicationContext())
                            .addMsgUnreadCountByThreadId(recipientInfo.getThreadId());
                    mIvUnreadIcon.setVisibility(View.VISIBLE);
                }

                int curState = ProfileManager.getInstance().getCurUsrDateState();
                //If user state is mapping, we will skip LIKE step. Just display UI for user.
                if(!(curState == MessageUtil.WalkArroundState.STATE_IM
                        || curState == MessageUtil.WalkArroundState.STATE_WALK
                        || curState == MessageUtil.WalkArroundState.STATE_IMPRESSION)) {
                    AVAnalytics.onEvent(getActivity(), AppConstant.ANA_EVENT_LIKE);

                    ThreadPoolManager.getPoolManager().addAsyncTask(new LikeSomeOneTask(getActivity().getApplicationContext(),
                            mLikeSomeoneListener,
                            HttpUtil.HTTP_FUNC_LIKE_SOMEONE,
                            HttpUtil.HTTP_TASK_LIKE_SOMEONE,
                            LikeSomeOneTask.getParams(mStrFromUsrId, mStrToUsrId),
                            TaskUtil.getTaskHeader()));
                } else {
                    showMappingToast();
                }

                if (mNearlyUserList != null && mNearlyUserList.size() == 0) {
                    //If there is no data, display radar again.
                    showRadar();
                }
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                //mNearlyUserList.add(new CardMode("循环测试", 18, list.get(itemsInAdapter % imageUrls.length - 1)));
                mUserListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
                try {
                    View view = mUserFrame.getSelectedView();
                    view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                    view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mUserFrame.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                ;
            }
        });

    }

    /*
     * We will show radar on loading page / while there is no other data
     */
    private void showRadar() {
        mFragmentHandler.sendEmptyMessageDelayed(DISPLAY_RADAR, 500);
        mRlSearchArea.setVisibility(View.VISIBLE);
//        mSearchingView.setVisibility(View.VISIBLE);
//        mSearchingPortrait.setVisibility(View.VISIBLE);

        mUserFrameButtons.setVisibility(View.GONE);
        mUserFrame.setVisibility(View.GONE);
        mSearchingNoticeView.startFlipping();
    }

    private void showNearyUser() {

        //只有当有数据时才显示用户卡片页面
        if (mNearlyUserList != null && mNearlyUserList.size() > 0) {
            mSearchingView.stop();
            mRlSearchArea.setVisibility(View.GONE);
            mSearchingNoticeView.stopFlipping();

            mUserFrame.setVisibility(View.VISIBLE);
            mUserFrameButtons.setVisibility(View.VISIBLE);

            if (mUserListAdapter != null) {
                mUserListAdapter.notifyDataSetChanged();
            }
        }
    }

    private void right() {
        if(mUserFrame != null) {
            try{
                mUserFrame.getTopCardListener().selectRight();
            } catch (Exception e) {
                logger.e(" ------ right() exception: ");
            }
        }
    }

    private void left() {
        if(mUserFrame != null) {
            try{
                mUserFrame.getTopCardListener().selectLeft();
            } catch (Exception e) {
                logger.e(" ------ left() exception: ");
            }
        }
    }

    private void addCacheContact(String userId) {
        for (ContactInfo contact : mDeleletedUserList) {
            if (contact.getObjectId().equalsIgnoreCase(userId)) {
                //TODO: maybe we should create new a contact to save data.
                ContactsManager.getInstance(getActivity().getApplicationContext()).addContactInfo(contact);
                break;
            }
        }
    }

    private void setUnreadState() {
        //For unread msg icon
        int unreadCount = WalkArroundMsgManager.getInstance(getActivity().getApplicationContext()).getAllUnreadCount();
        if (unreadCount <= 0) {
            mIvUnreadIcon.setVisibility(View.GONE);
        } else {
            mIvUnreadIcon.setVisibility(View.VISIBLE);
        }
    }

    /*
     * Say hello to people who you like and he/she also liked you .
     */
    private long sayHello(String userId) {
        if (TextUtils.isEmpty(userId)) {
            logger.d("The user id is empty. Failed to say Hello!");
            return -1;
        }

        return WalkArroundMsgManager.getInstance(getActivity().getApplicationContext()).sayHello(userId, getString(R.string.msg_say_hello));
    }


    private void showMapResultDialog() {
        if(ProfileManager.getInstance() != null) {
            int curState = ProfileManager.getInstance().getCurUsrDateState();
            if(curState == MessageUtil.WalkArroundState.STATE_IM || curState == MessageUtil.WalkArroundState.STATE_WALK) {
                onMappingState();
                return;
            }
        }
    }

    private void onMappingState() {
        if(mMapDialog == null) {
            mMapDialog = DialogFactory.getMappingDialog(getActivity()
                    , getString(R.string.msg_u_on_map_state)
                    , new DialogFactory.ConfirmDialogClickListener() {
                        @Override
                        public void onConfirmDialogConfirmClick() {

                            startActivity(new Intent(getActivity(), ConversationActivity.class));

                            mMapDialog.dismiss();
                            //mMapDialog = null;
                        }
                    });
            mMapDialog.show();
        } else if(mMapDialog != null && !mMapDialog.isShowing()) {
            mMapDialog.show();
        }
    }

    private void showMappingToast() {
        int curState = ProfileManager.getInstance().getCurUsrDateState();
        if(curState == MessageUtil.WalkArroundState.STATE_IM
                || curState == MessageUtil.WalkArroundState.STATE_WALK
                || curState == MessageUtil.WalkArroundState.STATE_IMPRESSION) {
//            ToastUtils.show(getActivity(), WalkArroundApp.getInstance().getString(R.string.msg_u_on_map_state));
//            Toast.makeText(getActivity().getApplicationContext(),
//                    WalkArroundApp.getInstance().getString(R.string.msg_u_on_map_state), Toast.LENGTH_SHORT).show();
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.msg_u_on_map_state), Toast.LENGTH_SHORT).show();
        }
    }
}