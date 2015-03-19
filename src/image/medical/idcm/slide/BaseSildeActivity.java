package image.medical.idcm.slide;

import image.medical.idcm.R;
import image.medical.idcm.app.IDCMApplication;
import image.medical.idcm.fragment.MenuFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;

public class BaseSildeActivity extends SlidingFragmentActivity {

    protected Fragment mFrag;
    protected IDCMApplication mApplication;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // set the Behind View
        setBehindContentView(R.layout.menu_frame);
        if (savedInstanceState == null) {
            FragmentTransaction t = this.getSupportFragmentManager()
                    .beginTransaction();
            mFrag = new MenuFragment();
            t.replace(R.id.menu_frame, mFrag);
            t.commit();
        } else {
            mFrag = (Fragment) this.getSupportFragmentManager()
                    .findFragmentById(R.id.menu_frame);
        }

        // customize the SlidingMenu
        SlidingMenu sm = getSlidingMenu();
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setFadeDegree(0.35f);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

        // getActionBar().setDisplayHomeAsUpEnabled(true);
        
    }


}
