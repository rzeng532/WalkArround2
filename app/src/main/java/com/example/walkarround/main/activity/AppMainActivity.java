package com.example.walkarround.main.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.example.walkarround.Location.manager.LocationManager;
import com.example.walkarround.Location.model.GeoData;
import com.example.walkarround.R;
import com.example.walkarround.assistant.AssistantHelper;
import com.example.walkarround.base.task.TaskUtil;
import com.example.walkarround.base.view.DialogFactory;
import com.example.walkarround.base.view.PortraitView;
import com.example.walkarround.main.model.ContactInfo;
import com.example.walkarround.main.model.FriendInfo;
import com.example.walkarround.main.parser.WalkArroundJsonResultParser;
import com.example.walkarround.main.task.GetFriendListTask;
import com.example.walkarround.main.task.QueryNearlyUsers;
import com.example.walkarround.main.task.QuerySpeedDateIdTask;
import com.example.walkarround.message.activity.ConversationActivity;
import com.example.walkarround.message.activity.EvaluateActivity;
import com.example.walkarround.message.manager.ContactsManager;
import com.example.walkarround.message.manager.WalkArroundMsgManager;
import com.example.walkarround.message.model.MessageSessionBaseModel;
import com.example.walkarround.message.task.AsyncTaskLoadSession;
import com.example.walkarround.message.util.MessageConstant;
import com.example.walkarround.message.util.MessageUtil;
import com.example.walkarround.myself.activity.PersonInformationActivity;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.myself.model.MyDynamicInfo;
import com.example.walkarround.myself.model.MyProfileInfo;
import com.example.walkarround.setting.activity.AppSettingActivity;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.AsyncTaskListener;
import com.example.walkarround.util.Logger;
import com.example.walkarround.util.ToastUtils;
import com.example.walkarround.util.http.HttpTaskBase;
import com.example.walkarround.util.http.HttpTaskBase.onResultListener;
import com.example.walkarround.util.http.HttpUtil;
import com.example.walkarround.util.http.ThreadPoolManager;
import com.example.walkarround.util.network.NetWorkManager;

import java.util.ArrayList;
import java.util.List;

import static com.example.walkarround.util.http.HttpTaskBase.TaskResult;

/**
 * Created by Richard on 2015/12/20.
 */
public class AppMainActivity extends Activity implements View.OnClickListener {

    private static final Logger amLogger = Logger.getLogger(AppMainActivity.class.getSimpleName());

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mCurFragmentPage = -1;

    /*
     * UI elements on main activity
     */
    private View mViewSetting;
    private View mViewFeedback;
    private RelativeLayout mViewPortrait;
    private LinearLayout mViewLeftMenu;
    private PortraitView mPvPortrait;
    private TextView mTvUserName;

    private MyProfileInfo myProfileInfo = null;

    private GeoData mMyGeo = null;

    List<FriendInfo> mFriendList = new ArrayList<>();

    private final int FRAGMENT_PAGE_ID_MAIN = 0;
    private final int FRAGMENT_PAGE_ID_CONVERSATION = 1;

    private final int MSG_DISPLAY_CONV_BE_DELETED = 1;
    private final int MSG_OPERATION_NOT_SUCCEED = 2;
    private final int MSG_OPERATION_LOAD_SUCCESS = 3;
    private static final String MSG_EVENT_EXTRA_LIST = "listData";
    private static final String MSG_OPERATION_KEY_REQUEST = "msg_operation_key_request";

    //Flag for check if first task for getting nearly user complete or not;
    private boolean bFirstSearchComplete = false;
    private boolean bSearching = false;

    private Dialog mMapDialog;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DISPLAY_CONV_BE_DELETED:
                    if(mMapDialog != null && mMapDialog.isShowing()) {
                        mMapDialog.dismiss();
                        mMapDialog = null;
                    }
                    Dialog noticeDialog = DialogFactory.getConfirmDialog(AppMainActivity.this,
                            R.string.msg_speed_date_be_canceled, R.string.common_ok, null);
                    noticeDialog.show();
                    break;
                case MSG_OPERATION_LOAD_SUCCESS:

                    break;
                default:
                    break;
            }
        }
    };

    private onResultListener mAsysResultListener = new onResultListener() {
        @Override
        public void onResult(Object object, TaskResult resultCode, String requestCode, String threadId) {
            Bundle dataBundle = new Bundle();
            int what = -1;

            if (resultCode == TaskResult.FAILED || resultCode == TaskResult.ERROR) {
                if (!requestCode.equals(MessageConstant.MSG_OPERATION_ADD_BLACKLIST)) {
                    what = MSG_OPERATION_NOT_SUCCEED;
                }
            } else {
                if (requestCode.equals(MessageConstant.MSG_OPERATION_LOAD)) {
                    what = MSG_OPERATION_LOAD_SUCCESS;

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            List<MessageSessionBaseModel> friendConvList = new ArrayList<>();
                            for (MessageSessionBaseModel conv : (List<MessageSessionBaseModel>) object) {
                                if (conv.status >= MessageUtil.WalkArroundState.STATE_END) {
                                    friendConvList.add(conv);
                                }
                            }
                            amLogger.d("Get friends: " + friendConvList.size());

                            compareFriendListVsThreadList((friendConvList));
                        }
                    }).start();
                }
            }
            mHandler.removeMessages(what);
            Message msg = mHandler.obtainMessage(what);
            dataBundle.putString(MSG_OPERATION_KEY_REQUEST, requestCode);
            msg.setData(dataBundle);
            mHandler.sendMessage(msg);
        }

        @Override
        public void onPreTask(String requestCode) {
        }

        @Override
        public void onProgress(final int progress, String requestCode) {
        }
    };

    private onResultListener mQueryNearUserListener = new onResultListener() {
        @Override
        public void onPreTask(String requestCode) {
            bSearching = true;
        }

        @Override
        public void onResult(Object object, TaskResult resultCode, String requestCode, String threadId) {
            amLogger.d("Query nearly user done.");

            //Check if user use our app at the first time.
            if(AssistantHelper.isThereGuideStep()) {
                amLogger.i("Query nearly user -- goto assistant step.");

                if(AssistantHelper.getInstance().validateStepState(AssistantHelper.STEP_SEARCHING)) {
                    List<ContactInfo> assistantList = new ArrayList<>();
                    assistantList.add(AssistantHelper.getInstance().genAssitantContact());
                    NearlyUsersFragment.getInstance().updateNearlyUserList(assistantList);
                }
            } else if (object != null &&
                    WalkArroundJsonResultParser.parseReturnCode((String) object).equals(HttpUtil.HTTP_RESPONSE_KEY_RESULT_CODE_SUC)) {
                List<ContactInfo> nearlyUserList = WalkArroundJsonResultParser.parse2NearlyUserModelList((String) object);
                if (!isFinishing() && nearlyUserList != null && nearlyUserList.size() > 0) {
                    amLogger.d("Query nearly user successful and user list size = " + nearlyUserList.size());
                    amLogger.d("Query nearly user successful first user: " + nearlyUserList.get(0).toString());
                    NearlyUsersFragment.getInstance().updateNearlyUserList(nearlyUserList);
                } else {
                    amLogger.d("Query nearly user successful and user list is empty.");
                }
            }

            if (TaskResult.SUCCEESS == resultCode) {
                amLogger.d("TaskResult.SUCCEESS");
            } else {
                amLogger.e("----- ! TaskRsult.FAIL");
            }

            if(!bFirstSearchComplete) {
                bFirstSearchComplete = true;
            }

            bSearching = false;
        }

        @Override
        public void onProgress(int progress, String requestCode) {

        }
    };

    /**
     * 解析服务器返回的朋友列表，如果有朋友，则判断当前DB 是否有该会话，如无则创建。
     */
    private onResultListener mGetFriendsTaskListener = new onResultListener() {
        @Override
        public void onPreTask(String requestCode) {

        }

        @Override
        public void onResult(Object object, TaskResult resultCode, String requestCode, String threadId) {
            amLogger.d("Get friend list done.");
            if (object != null &&
                    WalkArroundJsonResultParser.parseReturnCode((String) object).equals(HttpUtil.HTTP_RESPONSE_KEY_RESULT_CODE_SUC)) {

                mFriendList.clear();
                mFriendList = WalkArroundJsonResultParser.parse2FriendList((String) object);

                for (FriendInfo friend : mFriendList) {
                    if (friend != null) {
                        String friendId = friend.getFriendUserId();
                        if (!TextUtils.isEmpty(friendId)) {
                            //Check contact infor by contact manager.
                            ContactInfo friendInfo = ContactsManager.getInstance(AppMainActivity.this.getApplicationContext()).getContactByUsrObjId(friendId);
                            if (friendInfo == null) {
                                ContactsManager.getInstance(AppMainActivity.this.getApplicationContext()).getContactFromServer(friendId);
                            }
                        }
                    }
                }

                // 加载数据
                ThreadPoolManager.getPoolManager().addAsyncTask(
                        new AsyncTaskLoadSession(getApplicationContext(),
                                MessageConstant.MSG_OPERATION_LOAD, 0, Integer.MAX_VALUE, mAsysResultListener));

                amLogger.d("There is friend: " + (String) object);
            }
        }

        @Override
        public void onProgress(int progress, String requestCode) {

        }
    };

    AsyncTaskListener mDynUpdateListener = new AsyncTaskListener() {
        @Override
        public void onSuccess(Object data) {
            amLogger.d("update dynamic success.");

            if(data == null) {
                return;
            }

            String dynamicRecordId = WalkArroundJsonResultParser.parseUserDynamicRecordId((String)data);

            //Query nearly users
            if(!TextUtils.isEmpty(dynamicRecordId)) {

                if(myProfileInfo != null) {
                    myProfileInfo.setDynamicDataId(dynamicRecordId);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!NearlyUsersFragment.getInstance().isThereNearlyUser()) {
                            startQueryNearlyUserTask();
                        }
                    }
                });
            } else {
                bFirstSearchComplete = true;
            }
        }

        @Override
        public void onFailed(AVException e) {
            //TODO:
            amLogger.d("update dynamic failed.");
            if(AppMainActivity.this != null
                    && !(AppMainActivity.this.isDestroyed() || AppMainActivity.this.isFinishing())) {
                ToastUtils.show(AppMainActivity.this, getString(R.string.err_loc_unknow));
            }
            bFirstSearchComplete = true;
        }
    };

    AsyncTaskListener mLocListener = new AsyncTaskListener() {
        @Override
        public void onSuccess(Object data) {
            mMyGeo = LocationManager.getInstance(getApplicationContext()).getCurrentLoc();
            amLogger.d("Get loc infor done.");
            if (mMyGeo != null) {
                //Update user dynamic data - online state & GEO.
                ProfileManager.getInstance().updateDynamicData(new MyDynamicInfo(mMyGeo, true, 1), mDynUpdateListener);
                ProfileManager.getInstance().getMyProfile().setLocation(mMyGeo);
            }
        }

        @Override
        public void onFailed(AVException e) {
            amLogger.e("! Get loc infor failed. e: " + e.getMessage());
            if(AppMainActivity.this != null
                    && !(AppMainActivity.this.isDestroyed() || AppMainActivity.this.isFinishing())) {
                ToastUtils.show(AppMainActivity.this, e.getMessage());
            }
            bFirstSearchComplete = true;
        }
    };

    private HttpTaskBase.onResultListener mGetSpeedIdTaskListener = new HttpTaskBase.onResultListener() {
        @Override
        public void onPreTask(String requestCode) {

        }

        @Override
        public void onResult(Object object, HttpTaskBase.TaskResult resultCode, String requestCode, String threadId) {

            //Task success.
            if (HttpTaskBase.TaskResult.SUCCEESS == resultCode && requestCode.equalsIgnoreCase(HttpUtil.HTTP_FUNC_QUERY_SPEED_DATE)) {
                //Get status & Get TO user.
                String strSpeedDateId = WalkArroundJsonResultParser.parseRequireCode((String) object, HttpUtil.HTTP_RESPONSE_KEY_OBJECT_ID);
                String strUser = MessageUtil.getFriendIdFromServerData((String) object);
                String strColor = WalkArroundJsonResultParser.parseRequireCode((String) object, HttpUtil.HTTP_RESPONSE_KEY_COLOR);
                int iStatus = WalkArroundJsonResultParser.parseRequireIntCode((String) object, HttpUtil.HTTP_RESPONSE_KEY_LIKE_STATUS);
                amLogger.d("Speed date id is: " + (String) object);
                amLogger.d("Speed date color is: " + strColor);
                amLogger.d("Speed date status is: " + iStatus);
                if (!TextUtils.isEmpty(strSpeedDateId) && !TextUtils.isEmpty(strUser)) {
                    List<String> lRecipientList = new ArrayList<>();
                    lRecipientList.add(strUser);

                    //Save speed date id
                    ProfileManager.getInstance().setSpeedDateId(strSpeedDateId);

                    //Add contact infor if local DB does not contain this friend
                    ContactInfo friend = ContactsManager.getInstance(AppMainActivity.this.getApplicationContext()).getContactByUsrObjId(strUser);
                    if (friend == null) {
                        ContactsManager.getInstance(AppMainActivity.this.getApplicationContext()).getContactFromServer(strUser);
                    }
                    //Check local chatting IM record and create chat record if there is no record on local DB.
                    long chattingThreadId = WalkArroundMsgManager.getInstance(getApplicationContext()).getConversationId(MessageConstant.ChatType.CHAT_TYPE_ONE2ONE,
                            lRecipientList);
                    int localThreadStatus = iStatus;
                    if (chattingThreadId < 0) {
                        chattingThreadId = WalkArroundMsgManager.getInstance(getApplicationContext()).createConversationId(MessageConstant.ChatType.CHAT_TYPE_ONE2ONE, lRecipientList);
                        if (chattingThreadId >= 0) {
                            //Update conversation color & state.
                            WalkArroundMsgManager.getInstance(getApplicationContext()).updateConversationStatusAndColor(chattingThreadId, iStatus, (TextUtils.isEmpty(strColor) ? -1 : Integer.parseInt(strColor)));
                            amLogger.d("update conversation color index: " + (TextUtils.isEmpty(strColor) ? -1 : Integer.parseInt(strColor)) + ", status : " + iStatus);
                        }
                    } else {
                        localThreadStatus = WalkArroundMsgManager.getInstance(getApplicationContext()).getConversationStatus(chattingThreadId);
                        localThreadStatus = (iStatus > localThreadStatus) ? iStatus : localThreadStatus;
                    }

                    ProfileManager.getInstance().setCurUsrDateState(localThreadStatus);

                    if (localThreadStatus == MessageUtil.WalkArroundState.STATE_IM || localThreadStatus == MessageUtil.WalkArroundState.STATE_WALK) {
                        //Go to build message && Start IM directly:
//                        Intent imItent = new Intent(AppMainActivity.this, BuildMessageActivity.class);
//                        imItent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_RECEIVER, strUser);
//                        imItent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_THREAD_ID, chattingThreadId);
//                        imItent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_TYPE, MessageConstant.ChatType.CHAT_TYPE_ONE2ONE);
//
//                        String friendName = "";
//                        if (friend == null) {
//                            ContactInfo friendContact = ContactsManager.getInstance(AppMainActivity.this.getApplicationContext()).getContactByUsrObjId(strUser);
//                            friendName = (friendContact == null) ? "" : friendContact.getUsername();
//                        }
//                        imItent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_DISPLAY_NAME, friendName);
//                        imItent.putExtra(BuildMessageActivity.INTENT_RECEIVER_EDITABLE, false);

//                        startActivity(imItent);

                        //修改逻辑：直接进入conversation activity
                        startActivity(new Intent(AppMainActivity.this, ConversationActivity.class));
                    } else if (localThreadStatus == MessageUtil.WalkArroundState.STATE_IMPRESSION) {
                        Intent evaItent = new Intent(AppMainActivity.this, EvaluateActivity.class);
                        evaItent.putExtra(EvaluateActivity.PARAMS_FRIEND_OBJ_ID, strUser);
                        startActivity(evaItent);
                    }
                } else {
                    //There is no speed date now.
                    //Then delete local conversation which on mapping state & indicate user.
                    if (WalkArroundMsgManager.getInstance(getApplicationContext()).deleteMappingConversation() > 0) {
                        mHandler.sendEmptyMessage(MSG_DISPLAY_CONV_BE_DELETED);
                    }
                }
            } else {
                amLogger.d("Failed to get speed date id!!!");
            }
        }

        @Override
        public void onProgress(int progress, String requestCode) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        initView();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // improve performance by indicating the list if fixed size.

        // enable ActionBar app icon to behave as action to toggle nav drawer
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.main_ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            //Start main activity
            selectItem(FRAGMENT_PAGE_ID_MAIN);
        }

        //IM client init operation.
        WalkArroundMsgManager.getInstance(getApplicationContext()).getMsgClient();

        //Get speed data id and check local conversation later.
        getConversationDataFromServer();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);

//        //Clear nearly user list while user select to exit main page. So user can search user while he/she enter next time.
//        NearlyUsersFragment.getInstance().clearNearlyUserList();
//
//        //Goto main activity and exit, so we can skip intermediate activities.
//        //NOTE: DO NOT set CLEAR flag here.
//        Intent intent = new Intent(AppMainActivity.this, EntranceActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//
//        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AVAnalytics.onResume(this);

        initData();
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mViewLeftMenu)) {
            mDrawerLayout.closeDrawers();
        }

        if (!NetWorkManager.getInstance(getApplicationContext()).isNetworkAvailable()) {
            Toast.makeText(getApplicationContext(), getString(R.string.err_network_unavailable), Toast.LENGTH_SHORT).show();
            return;
        }

        LocationManager.getInstance(getApplicationContext()).locateCurPosition(AppConstant.KEY_MAP_ASYNC_LISTERNER_MAIN, mLocListener);

        if(bFirstSearchComplete && !NearlyUsersFragment.getInstance().isThereNearlyUser()) {
            startQueryNearlyUserTask();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {

        //IM client init operation.
        WalkArroundMsgManager.getInstance(getApplicationContext()).getMsgClient();

        //Get speed data id and check local conversation later.
        getConversationDataFromServer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        AVAnalytics.onPause(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
        return true;
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mViewLeftMenu);
        menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch (item.getItemId()) {
            case R.id.action_websearch:
                // create intent to perform web search for this planet
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, "");
                // catch event that there's no activity to handle intent
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initView() {
        mViewSetting = (RelativeLayout) findViewById(R.id.rl_slide_setting);
        mViewSetting.setOnClickListener(this);
        mViewFeedback = (RelativeLayout) findViewById(R.id.rl_slide_feedback);
        mViewFeedback.setOnClickListener(this);

        mViewLeftMenu = (LinearLayout) findViewById(R.id.left_drawer);
        //mViewLeftMenu.setOnClickListener(this);

        mViewPortrait = (RelativeLayout) findViewById(R.id.menu_portrait);
        mPvPortrait = (PortraitView) mViewPortrait.findViewById(R.id.iv_portrait);
        mTvUserName = (TextView) mViewPortrait.findViewById(R.id.tv_username);
        mViewPortrait.setOnClickListener(this);
    }

    private void initData() {
        myProfileInfo = ProfileManager.getInstance().getMyProfile();

        if(myProfileInfo == null) {
            return;
        }

        if (!TextUtils.isEmpty(myProfileInfo.getUsrName()) && !TextUtils.isEmpty(myProfileInfo.getMobileNum())) {
            mPvPortrait.setBaseData(myProfileInfo.getUsrName(), myProfileInfo.getPortraitPath(),
                    myProfileInfo.getUsrName().substring(0, 1), -1);

            String displayName = myProfileInfo.getUsrName();
            if(displayName.length() > AppConstant.SHORTNAME_LEN) {
                displayName = displayName.substring(0, AppConstant.SHORTNAME_LEN) + "...";
            }
            mTvUserName.setText(displayName);
        } else {
            mTvUserName.setText(myProfileInfo.getMobileNum());
        }
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        if (position == FRAGMENT_PAGE_ID_MAIN) {
            mCurFragmentPage = FRAGMENT_PAGE_ID_MAIN;
            fragment = NearlyUsersFragment.getInstance();
        }
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();

        mDrawerLayout.closeDrawers();
    }

//    private void removeFragment() {
//        Fragment fragment = NearlyUsersFragment.getInstance();
//
//        FragmentManager fragmentManager = getFragmentManager();
//        FragmentTransaction ft = fragmentManager.beginTransaction();
//        ft.remove(fragment);
//        ft.commit();
//    }

    @Override
    public void setTitle(CharSequence title) {
        //mTitle = title;
        //getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        //mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_slide_setting://goto setting activity
                //Test code, will not merge
                if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mViewLeftMenu)) {
                    mDrawerLayout.closeDrawers();
                }
                startActivity(new Intent(AppMainActivity.this, AppSettingActivity.class));
                break;
            case R.id.rl_slide_feedback://goto setting activity
                doFeedback();
                break;
            case R.id.menu_portrait://goto setting activity
                if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mViewLeftMenu)) {
                    mDrawerLayout.closeDrawers();
                }

                Intent intentDisplayFriend = new Intent(this, PersonInformationActivity.class);
                intentDisplayFriend.putExtra(AppConstant.PARAM_USR_OBJ_ID,ProfileManager.getInstance().getCurUsrObjId());
                startActivity(intentDisplayFriend);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDynUpdateListener = null;
        mQueryNearUserListener = null;
        if(ProfileManager.getInstance().getMyProfile() != null) {
            ProfileManager.getInstance().getMyProfile().setLocation(null);
        }
        LocationManager.getInstance(getApplicationContext()).onDestroy();
    }

    /*
     * This API will get speed data id (chatting record) and friend list from server.
     * And those data from server will compare with local DB record.
     * If server record > local record number, will create conversation on local DB.
     */
    private void getConversationDataFromServer() {
        String userObjId = ProfileManager.getInstance().getCurUsrObjId();

        if (TextUtils.isEmpty(userObjId)) {
            return;
        }

        //Get speed date id.
        ThreadPoolManager.getPoolManager().addAsyncTask(new QuerySpeedDateIdTask(getApplicationContext(),
                mGetSpeedIdTaskListener,
                HttpUtil.HTTP_FUNC_QUERY_SPEED_DATE,
                HttpUtil.HTTP_TASK_QUERY_SPEED_DATE,
                QuerySpeedDateIdTask.getParams(userObjId),
                TaskUtil.getTaskHeader()));

        //Get friend list.
        ThreadPoolManager.getPoolManager().addAsyncTask(new GetFriendListTask(getApplicationContext(),
                mGetFriendsTaskListener,
                HttpUtil.HTTP_FUNC_GET_FRIEND_LIST,
                HttpUtil.HTTP_TASK_GET_FRIEND_LIST,
                GetFriendListTask.getParams(userObjId, MessageUtil.GET_FRIENDS_LIST_COUNT),
                TaskUtil.getTaskHeader()));
    }

    /**
     * 对比服务端friend list 和本地 MSG 会话列表
     *
     * @param list
     */
    private void compareFriendListVsThreadList(List<MessageSessionBaseModel> list) {
        List<Long> removeThreadIdList = new ArrayList<>();
        List<FriendInfo> addThreadIdList = new ArrayList<>();

        //Clear local thread list if there is no friend list on server side.
        if (mFriendList == null || mFriendList.size() <= 0) {
            if (list != null && list.size() > 0) {
                for (MessageSessionBaseModel item : list) {
                    if (AssistantHelper.ASSISTANT_OBJ_ID.equalsIgnoreCase(item.getContact())) {
                        continue;
                    }
                    if (item.status == MessageUtil.WalkArroundState.STATE_END
                            || item.status == MessageUtil.WalkArroundState.STATE_END_IMPRESSION) {
                        removeThreadIdList.add(item.getThreadId());
                    }
                }
            }
        }

        //If there is Friend list & there is no local thread, all friend list items should be added to local.
        if (list == null || list.size() <= 0) {
            addThreadIdList.addAll(mFriendList);
        }

        //If there is friend list && there is local thread data, we should compare two lists.
        if (mFriendList != null && mFriendList.size() > 0
                && list != null && list.size() > 0) {

            removeThreadIdList.clear();
            addThreadIdList.clear();

            List<MessageSessionBaseModel> removedModelList = new ArrayList<>();
            addThreadIdList.addAll(mFriendList);
            removedModelList.addAll(list);

            String friendUsrId; //local variant
            for (FriendInfo friend : mFriendList) {
                if (friend != null) {
                    friendUsrId = friend.getFriendUserId();
                    for (MessageSessionBaseModel model : list) {
                        if (model != null && friendUsrId.equalsIgnoreCase(model.getContact())) {
                            addThreadIdList.remove(friend);
                            removedModelList.remove(model);
                            break;
//                                if(model.msgStatus != MessageUtil.WalkArroundState.STATE_END) {
//                                    updateThreadIdList.add(model.getThreadId());
//                                }
                        }
                    }
                }
            }

            //  助手不删除
            for (MessageSessionBaseModel model : list) {
                if (model != null && AssistantHelper.ASSISTANT_OBJ_ID
                        .equalsIgnoreCase(model.getContact())) {
                    removedModelList.remove(model);
                    break;
                }
            }

            //Get deleted items list.
            if (removedModelList != null && removedModelList.size() > 0) {
                for (MessageSessionBaseModel item : removedModelList) {
                    if (item.status == MessageUtil.WalkArroundState.STATE_END
                            || item.status == MessageUtil.WalkArroundState.STATE_END_IMPRESSION) {
                        removeThreadIdList.add(item.getThreadId());
                    }
                }
            }
        }

        //Delete conversation
        if (removeThreadIdList != null && removeThreadIdList.size() > 0) {
            amLogger.d("compareFriendListVsThreadList -> del conv : " + removeThreadIdList.size());
            WalkArroundMsgManager.getInstance(getApplicationContext()).removeConversation(removeThreadIdList);
        }

        //Add conversation
        List<String> lRecipientList = new ArrayList<>();
        for (FriendInfo friend : addThreadIdList) {
            if (friend != null) {
                lRecipientList.clear();
                lRecipientList.add(friend.getFriendUserId());
                long chattingThreadId = WalkArroundMsgManager.getInstance(getApplicationContext()).getConversationId(MessageConstant.ChatType.CHAT_TYPE_ONE2ONE, lRecipientList);
                if (chattingThreadId < 0) {
                    chattingThreadId = WalkArroundMsgManager.getInstance(getApplicationContext()).createConversationId(MessageConstant.ChatType.CHAT_TYPE_ONE2ONE, lRecipientList);
                    if (chattingThreadId >= 0 && !TextUtils.isEmpty(friend.getColor())) {
                        //Update conversation color & state.
                        WalkArroundMsgManager.getInstance(getApplicationContext()).updateConversationStatusAndColor(chattingThreadId, MessageUtil.WalkArroundState.STATE_END, Integer.parseInt(friend.getColor()));
                        amLogger.d("compareFriendListVsThreadList -> add conv" + Integer.parseInt(friend.getColor()));
                    }
                }
            }
        }
    }

    private void startQueryNearlyUserTask() {

        if(bSearching) {
            return;
        }

        ThreadPoolManager.getPoolManager().addAsyncTask(new QueryNearlyUsers(getApplicationContext(),
                mQueryNearUserListener,
                HttpUtil.HTTP_FUNC_QUERY_NEARLY_USERS,
                HttpUtil.HTTP_TASK_QUERY_NEARLY_USERS,
                QueryNearlyUsers.getParams(myProfileInfo.getDynamicDataId()),
                TaskUtil.getTaskHeader()));
    }

    /****************
     *
     * 发起添加群流程。群号：走走反馈群(619714748) 的 key 为： oAiTP6oUr2awPgQEBHr2eflKzcQEIvxI
     * 调用 doFeedback(oAiTP6oUr2awPgQEBHr2eflKzcQEIvxI) 即可发起手Q客户端申请加群 走走反馈群(619714748)
     *
     * key: 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
     ******************/
    public boolean doFeedback() {
        Intent intent = new Intent();
        intent.setData(Uri.parse(
                "mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D"
                        + "oAiTP6oUr2awPgQEBHr2eflKzcQEIvxI"));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }
}
