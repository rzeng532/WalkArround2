package com.awalk.walkarround.base;

import com.awalk.walkarround.base.BaseView;

/**
 * BasePresenter
 * Date: 2018-06-10
 *
 * @author mass
 */
public class BasePresenter<T extends BaseView> {
    protected T mView;

    public void attach(T mvp) {
        mView = mvp;
    }

    public void detach() {
        mView = null;
    }
}
