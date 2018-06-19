package com.awalk.walkarround.myself.presenter;

import android.content.Context;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.awalk.walkarround.Location.model.GeoData;
import com.awalk.walkarround.base.BasePresenter;
import com.awalk.walkarround.myself.activity.DetailInformationActivity;
import com.awalk.walkarround.myself.iview.DetailInformationView;
import com.awalk.walkarround.myself.manager.ProfileManager;
import com.awalk.walkarround.util.AppConstant;
import com.awalk.walkarround.util.AsyncTaskListener;

/**
 * DetailInformationPresenter
 * Date: 2018-06-18
 *
 * @author mass
 */
public class DetailInformationPresenter extends BasePresenter<DetailInformationView> {

    public void updatePortrait(Context context, String path) {
        final Context appContext = context.getApplicationContext();
        ProfileManager.getInstance().updatePortrait(path, new AsyncTaskListener() {
            @Override
            public void onSuccess(Object data) {
                if (mView != null) {
                    mView.updatePortraitResult(true);
                }
            }

            @Override
            public void onFailed(AVException e) {
                AVAnalytics.onEvent(appContext, AppConstant.ANA_EVENT_CHANGE_PORTRAIT, AppConstant.ANA_TAG_RET_FAIL);
                if (mView != null) {
                    mView.updatePortraitResult(false);
                }
            }
        });

    }

}
