/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.example.walkarround.login.manager;

import com.avos.avoscloud.AVException;

/**
 * Date: 2015-11-26
 *
 * @author Richard Zeng
 *
 * A listener for login manager to get SUC or FAIL result.
 */
public interface RegAndLoginListener {
        public abstract void onSuccess();

        public abstract void onFailed(AVException e);
}
