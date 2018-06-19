package com.awalk.walkarround.myself.presenter;

import com.avos.avoscloud.AVException;
import com.awalk.walkarround.base.BasePresenter;
import com.awalk.walkarround.myself.iview.EditStrProfileInfoView;
import com.awalk.walkarround.myself.manager.ProfileManager;
import com.awalk.walkarround.util.AsyncTaskListener;

/**
 * EditStrProfileInfoPresenter
 * Date: 2018-06-18
 *
 * @author mass
 */
public class EditStrProfileInfoPresenter extends BasePresenter<EditStrProfileInfoView> {

    /**
     * 更新名字
     *
     * @param newName
     */
    public void updateUsername(final String newName) {
        ProfileManager.getInstance().updateUsername(newName, new AsyncTaskListener() {
            @Override
            public void onSuccess(Object data) {
                ProfileManager.getInstance().getMyProfile().setUsrName(newName);
                if (mView != null) {
                    mView.updateUsernameResult(true);
                }
            }

            @Override
            public void onFailed(AVException e) {
                if (mView != null) {
                    mView.updateUsernameResult(false);
                }
            }
        });

    }

    /**
     * 更新签名
     *
     * @param signature
     */
    public void updateSignature(final String signature) {
        ProfileManager.getInstance().updateSignature(signature, new AsyncTaskListener() {
            @Override
            public void onSuccess(Object data) {
                ProfileManager.getInstance().getMyProfile().setSignature(signature);
                if (mView != null) {
                    mView.updateSignatureResult(true);
                }
            }

            @Override
            public void onFailed(AVException e) {
                if (mView != null) {
                    mView.updateSignatureResult(false);
                }
            }
        });
    }
}
