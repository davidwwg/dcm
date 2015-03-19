package image.medical.idcm.util;

import android.annotation.SuppressLint;
import android.content.Context;

/**
 */
@SuppressLint("DefaultLocale")
public class UiUtil {

    /**
     * 弹出键盘时间(毫秒).
     */
    public static final int KEYBOARD_WAIT_TIME = 200;

    /**
     * 根据手机的分辨率从dp的单位转成为px（像素）.
     * 
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从px（像素）的单位转成为dp.
     * 
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

}
