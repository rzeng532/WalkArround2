package com.example.walkarround.main.activity;

import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.example.walkarround.R;
import com.example.walkarround.base.view.PortraitView;
import com.example.walkarround.Location.manager.LocationManager;
import com.example.walkarround.Location.model.GeoData;
import com.example.walkarround.myself.activity.DetailInformationActivity;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.myself.model.MyProfileInfo;
import com.example.walkarround.setting.activity.AppSettingActivity;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.AsyncTaskListener;
import com.example.walkarround.util.Logger;

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
    private View mViewMain;
    private RelativeLayout mViewPortrait;
    private LinearLayout mViewLeftMenu;
    private FrameLayout mFrame;
    private PortraitView mPvPortrait;
    private TextView mTvUserName;

    private MyProfileInfo myProfileInfo = null;

    private GeoData mMyGeo = null;

    AsyncTaskListener mLocListener = new AsyncTaskListener() {
        @Override
        public void onSuccess() {
            mMyGeo = LocationManager.getInstance(getApplicationContext()).getCurrentLoc();
            if (mMyGeo != null) {
                amLogger.d("latitude: " + mMyGeo.getLatitude());
                amLogger.d("longitude: " + mMyGeo.getLongitude());
                amLogger.d("Addr: " + mMyGeo.getAddrInfor());
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mViewLeftMenu)) {
            mDrawerLayout.closeDrawers();
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
        Fragment fragment = PlanetFragment.newInstance(position);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();

        // update selected item title, then close the drawer
        setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawers();
        //mDrawerLayout.closeDrawer(mViewLeftMenu);
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

            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocationManager.getInstance(getApplicationContext()).onDestroy();
    }

    /**
     * Fragment that appears in the "content_frame", shows a planet
     */
    public static class PlanetFragment extends Fragment implements View.OnClickListener {
        public static final String ARG_PLANET_NUMBER = "planet_number";
        private View mViewRoot;
        private View mTvTitle;
        private ImageView mIvImage;

        public PlanetFragment() {
            // Empty constructor required for fragment subclasses
        }

        public static Fragment newInstance(int position) {
            Fragment fragment = new PlanetFragment();
            Bundle args = new Bundle();
            args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
            fragment.setArguments(args);
            return fragment;
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
                case R.id.title:
                    //TODO: we should use handler for communication between activty and fragment later.
                    DrawerLayout slideMenu = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                    LinearLayout mViewLeftMenu = (LinearLayout) getActivity().findViewById(R.id.left_drawer);
                    slideMenu.openDrawer(mViewLeftMenu);
                    //getActivity().finish();
                    break;

                default:
                    break;
            }
        }

        private void initView(LayoutInflater inflater, ViewGroup container) {
            mViewRoot = inflater.inflate(R.layout.fragment_planet, container, false);

            mIvImage = ((ImageView) mViewRoot.findViewById(R.id.image));

            mTvTitle = (View) mViewRoot.findViewById(R.id.title);
            mTvTitle.setOnClickListener(this);
        }

        private void initData(Bundle savedInstanceState) {
            int i = getArguments().getInt(ARG_PLANET_NUMBER);
            String planet = getResources().getStringArray(R.array.planets_array)[i];
            int imageId = getResources().getIdentifier(planet.toLowerCase(Locale.getDefault()),
                    "drawable", getActivity().getPackageName());
            mIvImage.setImageResource(imageId);
        }
    }
}
