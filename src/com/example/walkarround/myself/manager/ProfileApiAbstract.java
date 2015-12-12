/**
 * Copyright (C) 2014-2015 CMCC All rights reserved
 */
package com.example.walkarround.myself.manager;

import com.example.walkarround.util.AsyncTaskListener;

/**
 * TODO: description
 * Date: 2015-12-08
 *
 * @author Administrator
 */
public abstract class ProfileApiAbstract {
    public abstract void updateGendle(int value) throws Exception;

    public abstract void updateSignature(String newSignature, AsyncTaskListener listener)throws Exception;

    public abstract void updateBirthday(String birth)throws Exception;

    public abstract void updateUsername(String username, AsyncTaskListener listener)throws Exception;

    public abstract void updatePortrait(String path)throws Exception;
}
