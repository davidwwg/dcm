package image.medical.idcm.app;

import image.medical.idcm.cache.PageCache;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.graphics.Point;
import android.os.Build.VERSION;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class AppController {

    private static Map<String, PageCache> pageCacheMap = new HashMap<String, PageCache>();

    public static void addPageCache(PageCache cache) {
        if (cache != null) {
            String key = cache.hashCode() + "#" + cache.getClass().getName();
            pageCacheMap.put(key, cache);
        }
    }

    public static PageCache getPageCache(String key) {
        return pageCacheMap.get(key);
    }

    public static void removePageCache(PageCache cache) {
        if (cache != null) {
            String key = cache.hashCode() + "#" + cache.getClass().getName();
            pageCacheMap.remove(key);
        }
    }

    /**
     * 获取屏幕宽度.
     */
    @SuppressLint("NewApi")
    public static int getScreenWidth(Context mContext) {
        int width;
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Service.WINDOW_SERVICE);
        if (VERSION.SDK_INT <= 12) {
            width = windowManager.getDefaultDisplay().getWidth();
        } else {
            Point point = new Point();
            windowManager.getDefaultDisplay().getSize(point);
            width = point.x;
        }

        return width;
    }

    /**
     * 获取屏幕宽度.
     */
    @SuppressLint("NewApi")
    public static int getScreenHeight(Context mContext) {
        DisplayMetrics dm = new DisplayMetrics();
        int height;
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Service.WINDOW_SERVICE);
        if (VERSION.SDK_INT <= 12) {
            height = windowManager.getDefaultDisplay().getHeight();
        } else {
            windowManager.getDefaultDisplay().getRealMetrics(dm);
            height = dm.heightPixels;
        }

        return height;
    }

}
