package com.example.walkarround.main.activity;

import android.app.*;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
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
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.example.walkarround.Location.manager.LocationManager;
import com.example.walkarround.Location.model.GeoData;
import com.example.walkarround.R;
import com.example.walkarround.base.view.PortraitView;
import com.example.walkarround.main.model.ContactInfo;
import com.example.walkarround.main.parser.WalkArroundJsonResultParser;
import com.example.walkarround.main.task.GetFriendListTask;
import com.example.walkarround.main.task.QueryNearlyUsers;
import com.example.walkarround.main.task.QuerySpeedDateIdTask;
import com.example.walkarround.main.task.TaskUtil;
import com.example.walkarround.message.manager.ContactsManager;
import com.example.walkarround.message.manager.WalkArroundMsgManager;
import com.example.walkarround.message.util.MessageConstant;
import com.example.walkarround.message.util.MessageUtil;
import com.example.walkarround.myself.activity.DetailInformationActivity;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.myself.model.MyDynamicInfo;
import com.example.walkarround.myself.model.MyProfileInfo;
import com.example.walkarround.setting.activity.AppSettingActivity;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.AsyncTaskListener;
import com.example.walkarround.util.Logger;
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
    private RelativeLayout mViewPortrait;
    private LinearLayout mViewLeftMenu;
    private PortraitView mPvPortrait;
    private TextView mTvUserName;

    private MyProfileInfo myProfileInfo = null;

    private GeoData mMyGeo = null;

    private final int FRAGMENT_PAGE_ID_MAIN = 0;
    private final int FRAGMENT_PAGE_ID_CONVERSATION = 1;

    private onResultListener mQueryNearUserListener = new onResultListener() {
        @Override
        public void onPreTask(String requestCode) {

        }

        @Override
        public void onResult(Object object, TaskResult resultCode, String requestCode, String threadId) {
            amLogger.d("Query nearly user done.");
            if (object != null &&
                    WalkArroundJsonResultParser.parseReturnCode((String) object).equals(HttpUtil.HTTP_RESPONSE_KEY_RESULT_CODE_SUC)) {
                List<ContactInfo> nearlyUserList = WalkArroundJsonResultParser.parse2NearlyUserModelList((String) object);
                if (!isFinishing() && nearlyUserList != null && nearlyUserList.size() > 0) {
                    amLogger.d("Query nearly user successful and user list size = " + nearlyUserList.size());
                    NearlyUsersFragment.getInstance().updateNearlyUserList(nearlyUserList);
                } else {
                    amLogger.d("Query nearly user successful and user list is empty.");
                }
            }

            if (TaskResult.SUCCEESS == resultCode) {
                amLogger.d("TaskResult.SUCCEESS");
            }
        }

        @Override
        public void onProgress(int progress, String requestCode) {

        }
    };

    private onResultListener mGetFriendsTaskListener = new onResultListener() {
        @Override
        public void onPreTask(String requestCode) {

        }

        @Override
        public void onResult(Object object, TaskResult resultCode, String requestCode, String threadId) {
            amLogger.d("Get friend list done.");
            if (object != null &&
                    WalkArroundJsonResultParser.parseReturnCode((String) object).equals(HttpUtil.HTTP_RESPONSE_KEY_RESULT_CODE_SUC)) {
                amLogger.d("There is friend: " + (String)object);
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

            //Query nearly users
            ThreadPoolManager.getPoolManager().addAsyncTask(new QueryNearlyUsers(getApplicationContext(),
                    mQueryNearUserListener,
                    HttpUtil.HTTP_FUNC_QUERY_NEARLY_USERS,
                    HttpUtil.HTTP_TASK_QUERY_NEARLY_USERS,
                    QueryNearlyUsers.getParams((String) data),
                    TaskUtil.getTaskHeader()));
        }

        @Override
        public void onFailed(AVException e) {
            //TODO:
            amLogger.d("update dynamic failed.");
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
            }
        }

        @Override
        public void onFailed(AVException e) {
            //TODO:
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
                if(!TextUtils.isEmpty(strSpeedDateId) && !TextUtils.isEmpty(strUser)) {
                    List<String> lRecipientList = new ArrayList<>();
                    lRecipientList.add(strUser);

                    //Save speed date id
                    ProfileManager.getInstance().setSpeedDateId(strSpeedDateId);

                    //Add contact infor if local DB does not contain this friend
                    ContactInfo friend = ContactsManager.getInstance(AppMainActivity.this.getApplicationContext()).getContactByUsrObjId(strUser);
                    if(friend == null) {
                        ContactsManager.getInstance(AppMainActivity.this.getApplicationContext()).getContactFromServer(strUser);
                    }
                    //Check local chatting IM record and create chat record if there is no record on local DB.
                    long chattingThreadId = WalkArroundMsgManager.getInstance(getApplicationContext()).getConversationId(MessageConstant.ChatType.CHAT_TYPE_ONE2ONE,
                            lRecipientList);
                    if(chattingThreadId < 0) {
                        chattingThreadId = WalkArroundMsgManager.getInstance(getApplicationContext()).createConversationId(MessageConstant.ChatType.CHAT_TYPE_ONE2ONE, lRecipientList);
                        if(chattingThreadId >= 0 && !TextUtils.isEmpty(strColor)) {
                            //Update conversation color & state.
                            WalkArroundMsgManager.getInstance(getApplicationContext()).updateConversationStatusAndColor(chattingThreadId, iStatus, Integer.parseInt(strColor));
                            amLogger.d("update conversation color index: " + Integer.parseInt(strColor) + ", status : " + iStatus);
                        }
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

        LocationManager.getInstance(getApplicationContext()).locateCurPosition(AppConstant.KEY_MAP_ASYNC_LISTERNER_MAIN, mLocListener);

        //IM client init operation.
        WalkArroundMsgManager.getInstance(getApplicationContext()).open(WalkArroundMsgManager.getInstance(getApplicationContext()).getClientId(),
                new AVIMClientCallback() {
                    @Override
                    public void done(AVIMClient avimClient, AVIMException e) {
                        if (e == null) {
                            amLogger.d("Open client success.");
                        } else {
                            amLogger.d("Open client fail.");
                        }
                    }
                });

        //Get speed data id and check local conversation later.
        getConversationDataFromServer();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //Clear nearly user list while user select to exit main page. So user can search user while he/she enter next time.
        NearlyUsersFragment.getInstance().clearNearlyUserList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mViewLeftMenu)) {
            mDrawerLayout.closeDrawers();
        }

        if (!NetWorkManager.getInstance(getApplicationContext()).isNetworkAvailable()) {
            Toast.makeText(getApplicationContext(), getString(R.string.err_network_unavailable), Toast.LENGTH_SHORT).show();
            return;
        }
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

        mViewLeftMenu = (LinearLayout) findViewById(R.id.left_drawer);
        //mViewLeftMenu.setOnClickListener(this);

        mViewPortrait = (RelativeLayout) findViewById(R.id.menu_portrait);
        mPvPortrait = (PortraitView) mViewPortrait.findViewById(R.id.iv_portrait);
        mTvUserName = (TextView) mViewPortrait.findViewById(R.id.tv_username);
        mViewPortrait.setOnClickListener(this);
    }

    private void initData() {
        myProfileInfo = ProfileManager.getInstance().getMyProfile();

        if (!TextUtils.isEmpty(myProfileInfo.getUsrName()) && !TextUtils.isEmpty(myProfileInfo.getMobileNum())) {
            mPvPortrait.setBaseData(myProfileInfo.getUsrName(), myProfileInfo.getPortraitPath(),
                    myProfileInfo.getUsrName().substring(0, 1), -1);
            mTvUserName.setText(myProfileInfo.getUsrName());
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
                startActivity(new Intent(AppMainActivity.this, AppSettingActivity.class));
                break;
            case R.id.menu_portrait://goto setting activity
                startActivity(new Intent(AppMainActivity.this, DetailInformationActivity.class));
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
        LocationManager.getInstance(getApplicationContext()).onDestroy();
    }

    /*
     * This API will get speed data id (chatting record) and friend list from server.
     * And those data from server will compare with local DB record.
     * If server record > local record number, will create conversation on local DB.
     */
    private void getConversationDataFromServer() {
        String userObjId = ProfileManager.getInstance().getCurUsrObjId();

        if(TextUtils.isEmpty(userObjId)) {
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
}
