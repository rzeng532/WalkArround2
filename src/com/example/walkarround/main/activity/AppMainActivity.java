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
import android.widget.*;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.example.walkarround.Location.manager.LocationManager;
import com.example.walkarround.Location.model.GeoData;
import com.example.walkarround.R;
import com.example.walkarround.base.view.PortraitView;
import com.example.walkarround.main.model.NearlyUser;
import com.example.walkarround.main.parser.WalkArroundJsonResultParser;
import com.example.walkarround.main.task.QueryNearlyUsers;
import com.example.walkarround.main.task.TaskUtil;
import com.example.walkarround.message.manager.WalkArroundMsgManager;
import com.example.walkarround.myself.activity.DetailInformationActivity;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.myself.model.MyDynamicInfo;
import com.example.walkarround.myself.model.MyProfileInfo;
import com.example.walkarround.setting.activity.AppSettingActivity;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.AsyncTaskListener;
import com.example.walkarround.util.Logger;
import com.example.walkarround.util.http.HttpTaskBase.onResultListener;
import com.example.walkarround.util.http.HttpUtil;
import com.example.walkarround.util.http.ThreadPoolManager;
import com.example.walkarround.util.network.NetWorkManager;

import java.util.List;

import static com.example.walkarround.util.http.HttpTaskBase.TaskResult;

/**
 * Created by Richard on 2015/12/20.
 */
public class AppMainActivity extends Activity implements View.OnClickListener {

    private static final Logger amLogger = Logger.getLogger(AppMainActivity.class.getSimpleName());

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private String[] mPlanetTitles;

    /*
     * UI elements on main activity
     */
    private View mViewSetting;
    private View mViewChat;
    private View mViewMain;
    private RelativeLayout mViewPortrait;
    private LinearLayout mViewLeftMenu;
    private FrameLayout mFrame;
    private PortraitView mPvPortrait;
    private TextView mTvUserName;

    private MyProfileInfo myProfileInfo = null;

    private GeoData mMyGeo = null;

    private onResultListener mQueryListener = new onResultListener() {
        @Override
        public void onPreTask(String requestCode) {

        }

        @Override
        public void onResult(Object object, TaskResult resultCode, String requestCode, String threadId) {
            if (object != null &&
                    WalkArroundJsonResultParser.parseReturnCode((String) object).equals(HttpUtil.HTTP_RESPONSE_KEY_RESULT_CODE_SUC)) {
                List<NearlyUser> nearlyUserList = WalkArroundJsonResultParser.parse2NearlyUserModelList((String) object);
                if (!isFinishing() && nearlyUserList != null && nearlyUserList.size() > 0) {
                    NearlyUsersFragment.getInstance().updateNearlyUserList(nearlyUserList);
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

    AsyncTaskListener mDynUpdateListener = new AsyncTaskListener() {
        @Override
        public void onSuccess(Object data) {
            amLogger.d("update dynamic success.");

            //Query nearly users
            ThreadPoolManager.getPoolManager().addAsyncTask(new QueryNearlyUsers(getApplicationContext(),
                    mQueryListener,
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        initView();

        mPlanetTitles = getResources().getStringArray(R.array.planets_array);
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
            selectItem(0);
        }

        LocationManager.getInstance(getApplicationContext()).locateCurPosition(AppConstant.KEY_MAP_ASYNC_LISTERNER_MAIN, mLocListener);

        //IM client init operation.
        WalkArroundMsgManager.getInstance().open(WalkArroundMsgManager.getInstance().getClientId(),
                new AVIMClientCallback() {
                    @Override
                    public void done(AVIMClient avimClient, AVIMException e) {
                        if (e == null) {
                            //Test code
                            String memberId = "567e95ec60b2e1871e04a8ae";
                            if(WalkArroundMsgManager.getInstance().getClientId().equalsIgnoreCase(memberId)) {
                                memberId = "565eb4fd60b25b0435209c10";
                            }
                            WalkArroundMsgManager.getInstance().getConversation(memberId);
                            amLogger.d("Open client success.");
                        } else {
                            amLogger.d("Open client fail.");
                        }
                    }
                });
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
        mFrame = (FrameLayout) findViewById(R.id.content_frame);
        //mTvTitle = (TextView) findViewById(R.id.title_name);
        //mTvTitle.setOnClickListener(this);

        mViewSetting = (RelativeLayout) findViewById(R.id.rl_slide_setting);
        mViewSetting.setOnClickListener(this);

        mViewMain = (RelativeLayout) findViewById(R.id.rl_slide_main);
        mViewMain.setOnClickListener(this);

        mViewChat = (RelativeLayout) findViewById(R.id.rl_slide_chat);
        mViewChat.setOnClickListener(this);

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
        Fragment fragment = NearlyUsersFragment.getInstance();

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();

        // update selected item title, then close the drawer
        setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawers();
        //mDrawerLayout.closeDrawer(mViewLeftMenu);
    }

    private void removeFragment() {
        Fragment fragment = NearlyUsersFragment.getInstance();

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.remove(fragment);
        ft.commit();
    }


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
        mDrawerToggle.syncState();
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

            case R.id.rl_slide_main://goto setting activity
                //startActivity(new Intent(AppMainActivity.this, AppSettingActivity.class));
                mDrawerLayout.closeDrawers();
                break;

            case R.id.menu_portrait://goto setting activity
                startActivity(new Intent(AppMainActivity.this, DetailInformationActivity.class));
                break;

            case R.id.rl_slide_chat:
                WalkArroundMsgManager.getInstance().sendTextMsg();
                break;

            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDynUpdateListener = null;
        mQueryListener = null;
        LocationManager.getInstance(getApplicationContext()).onDestroy();
    }
}
