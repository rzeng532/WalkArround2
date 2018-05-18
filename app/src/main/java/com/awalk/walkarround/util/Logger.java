/**
 * Copyright (C) 2014-2015 Richard All rights reserved
 */
package com.awalk.walkarround.util;

/**
 * TODO: description
 * Date: 2015-11-26
 *
 * @author Administrator
 */

import android.util.Log;

import java.util.Hashtable;

/**
 * Utility log tool.
 *
 *
 */
public class Logger {

    private final static String LOG_TAG = "WalkArround";

    private static Hashtable<String, Logger> sLoggerTable = new Hashtable<String, Logger>();

    private String mClassName;

    public static Logger getLogger(String className) {
        Logger classLogger = sLoggerTable.get(className);
        if (classLogger == null) {
            classLogger = new Logger(className);
            sLoggerTable.put(className, classLogger);
        }
        return classLogger;
    }

    private Logger(String name) {
        mClassName = name;
    }

    public void v(String log) {
        if (AppConstant.LOG_OUTPUT) {
            Log.v(mClassName, "{Thread:" + Thread.currentThread().getName() + "}" + log);
        }
    }

    public void d(String log) {
        if (AppConstant.LOG_OUTPUT) {
            Log.d(mClassName, "{Thread:" + Thread.currentThread().getName() + "}" + log);
        }
    }

    public void i(String log) {
        if (AppConstant.LOG_OUTPUT) {
            Log.i(mClassName, "{Thread:" + Thread.currentThread().getName() + "}" + log);
        }
    }

    public void i(String log, Throwable tr) {
        if (AppConstant.LOG_OUTPUT) {
            Log.i(mClassName, "{Thread:" + Thread.currentThread().getName() + "}" + log + "\n"
                    + Log.getStackTraceString(tr));
        }
    }

    public void w(String log) {
        if (AppConstant.LOG_OUTPUT) {
            Log.w(mClassName, "{Thread:" + Thread.currentThread().getName() + "}" + log);
        }
    }

    public void w(String log, Throwable tr) {
        if (AppConstant.LOG_OUTPUT) {
            Log.w(mClassName, "{Thread:" + Thread.currentThread().getName() + "}" + log + "\n"
                    + Log.getStackTraceString(tr));
        }
    }

    public void e(String log) {
        if (AppConstant.LOG_OUTPUT)
            Log.e(mClassName, "{Thread:" + Thread.currentThread().getName() + "}" + log);
    }

    public void e(String log, Throwable tr) {
        if (AppConstant.LOG_OUTPUT)
            Log.e(mClassName, "{Thread:" + Thread.currentThread().getName() + "}" + log + "\n"
                    + Log.getStackTraceString(tr));
    }
}