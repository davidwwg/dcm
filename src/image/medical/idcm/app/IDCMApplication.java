package image.medical.idcm.app;

import image.medical.idcm.util.LogUtil;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class IDCMApplication extends Application {

    private static final String    TAG = "IDCMApplication";
    private static IDCMApplication instance;

    @SuppressWarnings("unused")
    @Override
    public void onCreate() {
        if (false && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyDeath().build());
        }

        super.onCreate();
        instance = this;

        int pid = android.os.Process.myPid();
        String pName = "";
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        List<RunningAppProcessInfo> infos = am.getRunningAppProcesses();
        for (RunningAppProcessInfo info : infos) {
            if (info.pid == pid) {
                pName = info.processName;
            }
        }

        LogUtil.i(TAG, "pid:" + pid + " pName:" + pName);

        initImageLoader(getApplicationContext());

        doConfigureInBackground();
    }

    private void doConfigureInBackground() {
        new Thread(new Runnable() {

            @Override
            public void run() {

            }
        }).start();
    }

    public static IDCMApplication getInstance() {
        return instance;
    }

    private void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you
        // may tune some of them,
        // or you can create default configuration by
        // ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO)
                // .writeDebugLogs() // Remove for release app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

}
