package image.medical.idcm.home;

import image.medical.idcm.R;
import image.medical.idcm.util.LogUtil;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;

import com.crittercism.app.Crittercism;

public class SplashActivity extends FragmentActivity {

    private static final String TAG         = "SplashActivity";
    private static final int    SPLASH_TIME = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.TYPE_STATUS_BAR, WindowManager.LayoutParams.TYPE_STATUS_BAR);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        LogUtil.i(TAG, "isTaskRoot:" + isTaskRoot());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SplashActivity.this);

        // Crittercism.initialize(SplashActivity.this,
        // "537da5ff28ae454471000006");

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                startActivity(intent);
                SplashActivity.this.finish();

            }

        }, SPLASH_TIME);
    }

}
