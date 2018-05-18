package com.awalk.walkarround.util;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;

import com.awalk.walkarround.R;
import com.awalk.walkarround.base.WalkArroundApp;
import com.awalk.walkarround.base.supertoasts.Style;
import com.awalk.walkarround.base.supertoasts.SuperActivityToast;
import com.awalk.walkarround.base.supertoasts.SuperToast;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

import static com.awalk.walkarround.base.supertoasts.Style.TEXTSIZE_SMALL;

/**
 *
 */
public class ToastUtils {
    private static final Logger SMYLOGGER = Logger.getLogger(ToastUtils.class.getSimpleName());
    private static volatile SuperToast toast = null;
    private static volatile SuperActivityToast activityToast = null;
    private static volatile Toast originalToast = null;
    private static Context context;
    private static final int TOP_PADDING_DP = 150;

    /**
     *
     */
    public static void init(Context appContext) {
        context = appContext;
    }

    /**
     * 未传入activity, 使用系统toast
     */
    public static void show(String msg) {
        showSystemToast(msg, Style.DURATION_SHORT);
    }

    /**
     *未传入activity, 使用系统toast
     */
    public static void show(int msg) {
        showSystemToast(context.getString(msg), Style.DURATION_SHORT);
    }

    /**
     *未传入activity, 使用系统toast
     */
    public static void showLong(String msg) {
        showSystemToast(msg, Style.DURATION_LONG);
    }

    /**
     * 系统toast使用appcontext
     * @param msg
     * @param len
     */
    private static void showSystemToast(final String msg, final int len){
        Observable.just("")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        if (originalToast == null){
                            originalToast = Toast.makeText(context, msg, len > Style.DURATION_SHORT ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
                        }else {
                            originalToast.setDuration(len > Style.DURATION_SHORT ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
                            originalToast.setText(msg);
                        }
                        originalToast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, TOP_PADDING_DP);
                        originalToast.show();
                    }
                });
    }

    /**
     * Context 必须是activity, 不能是app context
     * @param act must be activity
     * @param msg
     */
    public static void show(final Context act, final String msg) {
        show(act, msg, Style.DURATION_SHORT);
    }

    /**
     *Context 必须是activity, 不能是app context
     * @param act must be activity
     * @param msg
     */
    public static void showLong(final Context act, final String msg) {
        show(act, msg, Style.DURATION_LONG);
    }

    /**
     *Context 必须是activity, 不能是app context
     * @param act must be activity
     * @param msg
     */
    public static void show(final Context act, final int msg) {
        show(act, msg, Style.DURATION_SHORT);
    }

    /**
     *Context 必须是activity, 不能是app context
     * @param act must be activity
     * @param msg
     */
    public static void showLong(final Context act, final int msg) {
        show(act, msg, Style.DURATION_LONG);
    }

    /**
     *Context 必须是activity, 不能是app context
     * @param act must be activity
     * @param msg
     */
    public static void show(final Context act, final int msg,
                            final int len) {
        show(act, act.getString(msg), len);
    }


    /**
     *Context 必须是activity, 不能是app context
     * @param act must be activity
     * @param msg
     */
    public static void show(final Context act, final int msg,
                            final int len, final int drawableIcon) {
        show(act, act.getString(msg), len, drawableIcon);
    }

    /**
     * 真正实现
     * actual implementation
     * note that len  reqire Toast.LENGTH_LONG or Toast.LENGTH_SHORT
     * context must be instanceof Activity!!!
     */
    public static void show(final Context context, final String msg,
                            final int len) {
        show(context, msg, len, -1);
    }

    /**
     * 真正实现
     * actual implementation
     * note that len  reqire Toast.LENGTH_LONG or Toast.LENGTH_SHORT
     * context must be instanceof Activity!!!
     */
    public static void show(final Context context, final String msg,
                            final int len, final int drawableIcon) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        //如果应用在后台toast不提示
        if (context==null||CommonUtils.isBackground(context)) {
            return;
        }
        if (Looper.myLooper() == Looper.getMainLooper()){
            doShow(context, msg, len, drawableIcon);
        }
    }

    private static void doShow(final Context activity,
                               final String msg,
                               final int len,
                               final int drawableIcon) {
        if (!(activity instanceof Activity)) {
            showSystemToast(msg, len);
            return;
        }

        int actualLen = len;
        if (Toast.LENGTH_LONG == len) {
            actualLen = Style.DURATION_LONG;
        } else if (Toast.LENGTH_SHORT == len) {
            actualLen = Style.DURATION_SHORT;
        }
        getDefaultActivityToast(activity, drawableIcon).setText(msg)
                .setDuration(actualLen)
                .show();
    }

    /**
     * cancelCurrentToast
     */
    public static void cancelCurrentToast() {
        if (toast != null) {
            toast.dismiss();
        }
        if (originalToast!=null){
            originalToast.cancel();
        }
        if (activityToast!=null){
            activityToast.dismiss();
        }
    }

    /**
     * clearAllToasts
     */
    public static void clearAllToasts() {
        if (originalToast!=null){
            originalToast.cancel();
        }
        try {
            SuperToast.cancelAllSuperToasts();
        }catch (Exception e){
            SMYLOGGER.e(e.getMessage());
        }
    }


    private static synchronized SuperToast getDefaultToast() {
        if (toast==null){
            toast =new SuperToast(context);
            setToastStyle(toast, -1);
        }
        return toast;
    }

    private static SuperActivityToast getDefaultActivityToast(Context activity, final int drawableIcon) {
        SuperActivityToast activityToast = SuperActivityToast.create(activity, new Style(), Style.TYPE_STANDARD);
        setToastStyle(activityToast, drawableIcon);
        return activityToast;
    }

    /**
     * 统一toast style 设置
     * @param toast
     */
    private static void setToastStyle(SuperToast toast,final int drawableIcon){
        toast.setFrame(Style.FRAME_CUSTOM)
                .setTextSize(TEXTSIZE_SMALL)
                .setTextColor(WalkArroundApp.getInstance().getResources().getColor(R.color.cor1))
                //.setColor(PaletteUtils.getSolidColor(PaletteUtils.DARK_GREY))
                //.setAnimations(ANIMATIONS_POP)
                .setAnimations(R.style.CustomAnimationToast)
                .setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, TOP_PADDING_DP);
    }
}
