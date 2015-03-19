package image.medical.idcm.util;

import android.util.Log;

public class LogUtil {
    public static boolean DEGUG = true;

    public static void d(String tag, String message) {
        if (DEGUG) {
            Log.d(tag, message);
        }
    }

    public static void v(String tag, String message) {
        if (DEGUG) {
            Log.v(tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (DEGUG) {
            Log.i(tag, message);
        }
    }
}
