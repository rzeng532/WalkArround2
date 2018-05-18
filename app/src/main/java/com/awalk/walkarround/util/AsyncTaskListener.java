/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.awalk.walkarround.util;

import com.avos.avoscloud.AVException;

/**
 * Date: 2015-11-26
 *
 * @author Richard Zeng
 *
 * A listener for login manager to get SUC or FAIL result.
 */
public interface AsyncTaskListener {
        public abstract void onSuccess(Object data);

        public abstract void onFailed(AVException e);
}
