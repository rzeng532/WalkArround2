package com.example.walkarround.main.activity;

import android.app.Fragment;
import android.content.Intent;
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
import com.avos.avoscloud.AVUser;
import com.example.walkarround.R;
import com.example.walkarround.base.TestActivity;
import com.example.walkarround.base.view.PortraitView;
import com.example.walkarround.flingswipe.SwipeFlingAdapterView;
import com.example.walkarround.main.adapter.NearlyUserListAdapter;
import com.example.walkarround.main.model.ContactInfo;
import com.example.walkarround.main.parser.WalkArroundJsonResultParser;
import com.example.walkarround.main.task.LikeSomeOneTask;
import com.example.walkarround.main.task.TaskUtil;
import com.example.walkarround.message.manager.ContactsManager;
import com.example.walkarround.message.manager.WalkArroundMsgManager;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.myself.model.MyProfileInfo;
import com.example.walkarround.radar.RadarScanView;
import com.example.walkarround.util.Logger;
import com.example.walkarround.util.http.HttpTaskBase;
import com.example.walkarround.util.http.HttpTaskBase.TaskResult;
import com.example.walkarround.util.http.HttpUtil;
import com.example.walkarround.util.http.ThreadPoolManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cmcc on 16/1/11.
 */
public class NearlyUsersFragment extends Fragment implements View.OnClickListener {

    private static final Logger logger = Logger.getLogger(NearlyUsersFragment.class.getSimpleName());

    public static final String ARG_PLANET_NUMBER = "planet_number";
    private View mViewRoot;

    //For title and slide menu
    private PortraitView mPvPortrait;
    //private View mTvTitle;
    private ImageView mIvChatEntrance;

    //For nearly user list.
    private SwipeFlingAdapterView mUserFrame;
    //For buttons.
    private View mUserFrameButtons;

    //For radar display.
    private RadarScanView mRadarView;

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

    private HttpTaskBase.onResultListener mLikeSomeoneListener = new HttpTaskBase.onResultListener() {
        @Override
        public void onPreTask(String requestCode) {

        }

        @Override
        public void onResult(Object object, TaskResult resultCode, String requestCode, String threadId) {
            //Task success.
            //If you like some and the reponse status is "2", it means "toUser" also like you.
            if(TaskResult.SUCCEESS == resultCode) {
                //Get status & Get TO user.
                String strState = WalkArroundJsonResultParser.parseRequireCode((String)object, HttpUtil.HTTP_RESPONSE_KEY_LIKE_STATUS);
                if(strState.equalsIgnoreCase(TaskUtil.RESPONSE_USR_STATUS_ACCEPT)) {
                    String strUser = WalkArroundJsonResultParser.parseRequireCode((String)object, HttpUtil.HTTP_RESPONSE_KEY_LIKE_TO_USER);
                    addCacheContact(strUser);
                    sayHello(strUser);
                }

                //TODO: This log should be deleted later.
                logger.d("like someone response: \r\n" + (String)object);
            } else if(TaskResult.FAILED == resultCode) {
                logger.d("like someone response failed");
            }
        }

        @Override
        public void onProgress(int progress, String requestCode) {

        }
    };

    private int i = 0;

    //Handler
    private final int RADAR_STOP_DELAY = 5 * 1000;
    private final int UPDATE_NEARLY_USERS = 0;
    private Handler mFragmentHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_NEARLY_USERS) {
                showNearyUser();
            }
        }
    };

    protected void updateNearlyUserList(List<ContactInfo> list) {

        if (getActivity() == null || getActivity().isDestroyed()) {
            return;
        }

        synchronized (NearlyUsersFragment.class) {
            mNearlyUserList.clear();
            mDeleletedUserList.clear();
            mNearlyUserList.addAll(list);
            mStrToUsrId = mNearlyUserList.get(0).getObjectId();
        }

        mFragmentHandler.sendEmptyMessageDelayed(UPDATE_NEARLY_USERS, RADAR_STOP_DELAY);
    }

    protected void clearNearlyUserList() {
        mNearlyUserList.clear();
        mDeleletedUserList.clear();
    }

    public NearlyUsersFragment() {
        // Empty constructor required for fragment subclasses
    }

    public static NearlyUsersFragment getInstance() {

        if(mNUFragment == null) {
            synchronized (NearlyUsersFragment.class) {
                if(mNUFragment == null) {
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
                //startActivity(new Intent(getActivity(), ConversationActivity.class));
                startActivity(new Intent(getActivity(), TestActivity.class));
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
        mLeftDislike = (ImageView)  mViewRoot.findViewById(R.id.left);
        mLeftDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                left();
            }
        });
        mRightLike = (ImageView)  mViewRoot.findViewById(R.id.right);
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

        //Radar will be displayed at first.
        mRadarView = (RadarScanView) mViewRoot.findViewById(R.id.radar);
    }

    private void initData(Bundle savedInstanceState) {
        mUserListAdapter = new NearlyUserListAdapter(getActivity(), mNearlyUserList);

        mUserFrame.setAdapter(mUserListAdapter);
        mUserFrame.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                if(mNearlyUserList != null && mNearlyUserList.size() > 0) {
                    synchronized (NearlyUsersFragment.class) {
                        mStrToUsrId = mNearlyUserList.get(0).getObjectId();
                        mDeleletedUserList.add(mNearlyUserList.remove(0));
                    }
                    mUserListAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onLeftCardExit(Object dataObject) {
                if (mNearlyUserList != null && mNearlyUserList.size() == 0) {
                    //If there is no data, display radar again.
                    showRadar();
                };
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                ThreadPoolManager.getPoolManager().addAsyncTask(new LikeSomeOneTask(getActivity().getApplicationContext(),
                        mLikeSomeoneListener,
                        HttpUtil.HTTP_FUNC_LIKE_SOMEONE,
                        HttpUtil.HTTP_TASK_LIKE_SOMEONE,
                        LikeSomeOneTask.getParams(mStrFromUsrId, mStrToUsrId),
                        TaskUtil.getTaskHeader()));

                if (mNearlyUserList != null && mNearlyUserList.size() == 0) {
                    //If there is no data, display radar again.
                    showRadar();
                };
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                //mNearlyUserList.add(new CardMode("循环测试", 18, list.get(itemsInAdapter % imageUrls.length - 1)));
                mUserListAdapter.notifyDataSetChanged();
                i++;
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

        mStrFromUsrId = AVUser.getCurrentUser().getObjectId();

        if(mNearlyUserList != null && mNearlyUserList.size() > 0) {
            showNearyUser();
        }

        MyProfileInfo myProfileInfo = ProfileManager.getInstance().getMyProfile();

        if (!TextUtils.isEmpty(myProfileInfo.getUsrName()) && !TextUtils.isEmpty(myProfileInfo.getMobileNum())) {
            mPvPortrait.setBaseData(myProfileInfo.getUsrName(), myProfileInfo.getPortraitPath(),
                    myProfileInfo.getUsrName().substring(0, 1), -1);
        }
    }

    /*
     * We will show radar on loading page / while there is no other data
     */
    private void showRadar() {
        mRadarView.setVisibility(View.VISIBLE);

        mUserFrameButtons.setVisibility(View.GONE);
        mUserFrame.setVisibility(View.GONE);
    }

    private void showNearyUser() {
        mRadarView.setVisibility(View.GONE);

        mUserFrame.setVisibility(View.VISIBLE);
        mUserFrameButtons.setVisibility(View.VISIBLE);

        if(mUserListAdapter != null) {
            mUserListAdapter.notifyDataSetChanged();
        }
    }

    private void right() {
        mUserFrame.getTopCardListener().selectRight();
    }

    private void left() {
        mUserFrame.getTopCardListener().selectLeft();
    }

    private void addCacheContact(String userId) {
        for (ContactInfo contact : mDeleletedUserList) {
            if(contact.getObjectId().equalsIgnoreCase(userId)) {
                //TODO: maybe we should new a contact to save data.
                ContactsManager.getInstance(getActivity().getApplicationContext()).addContactInfo(contact);
                break;
            }
        }
    }
    /*
     * Say hello to people who you like and he/she also like you .
     */
    private void sayHello(String userId) {
        if(TextUtils.isEmpty(userId)) {
            logger.d("The user id is empty. Failed to say Hello!");
            return;
        }

        //TODO: Check if there is conversation.
        WalkArroundMsgManager.getInstance(getActivity().getApplicationContext()).sayHello(userId, getString(R.string.msg_say_hello));
    }
}