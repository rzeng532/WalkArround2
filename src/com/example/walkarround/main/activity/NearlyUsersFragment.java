package com.example.walkarround.main.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;
import com.example.walkarround.R;

import java.util.Locale;

/**
 * Created by cmcc on 16/1/11.
 */
public class NearlyUsersFragment extends Fragment implements View.OnClickListener {
    public static final String ARG_PLANET_NUMBER = "planet_number";
    private View mViewRoot;
    private View mTvTitle;
    private ImageView mIvImage;
    private ViewFlipper mVFlipper;

    public NearlyUsersFragment() {
        // Empty constructor required for fragment subclasses
    }

    public static Fragment newInstance(int position) {
        Fragment fragment = new NearlyUsersFragment();
        Bundle args = new Bundle();
        args.putInt(NearlyUsersFragment.ARG_PLANET_NUMBER, position);
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
        //mVFlipper = (ViewFlipper) mViewRoot.findViewById(R.id.viewFlipper);
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