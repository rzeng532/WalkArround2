/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.awalk.walkarround.retrofit;

/**
 * Date: 2015-11-26
 *
 * @author Richard Zeng
 * <p>
 * A listener for login manager to get SUC or FAIL result.
 */
public interface ApiListener<T> {
    public abstract void onSuccess(String code, T data);

    public abstract void onFailed(Exception e);
}
