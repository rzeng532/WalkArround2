/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.awalk.walkarround.myself.manager;

import com.awalk.walkarround.Location.model.GeoData;
import com.awalk.walkarround.myself.model.MyDynamicInfo;
import com.awalk.walkarround.util.AsyncTaskListener;

/**
 * TODO: description
 * Date: 2015-12-08
 *
 * @author Administrator
 */
public abstract class ProfileApiAbstract {
    public abstract void updateGendle(String value) throws Exception;

    public abstract void updateSignature(String newSignature, AsyncTaskListener listener)throws Exception;

    public abstract void updateBirthday(String birth)throws Exception;

    public abstract void updateUsername(String username, AsyncTaskListener listener)throws Exception;

    public abstract void updatePortrait(String path, AsyncTaskListener listener)throws Exception;

    public abstract void updateLocation(GeoData location, AsyncTaskListener listener)throws Exception;

    public abstract void updateDynamicData(MyDynamicInfo dynamicInfo, AsyncTaskListener listener)throws Exception;

    public abstract void queryCurUserDynData(AsyncTaskListener listener);
}
