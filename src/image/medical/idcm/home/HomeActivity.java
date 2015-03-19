package image.medical.idcm.home;

import image.medical.idcm.R;
import image.medical.idcm.fragment.MainFragment;
import image.medical.idcm.fragment.MainFragment.ActionCallback;
import image.medical.idcm.slide.BaseSildeActivity;
import image.medical.idcm.slide.SlidingMenu;
import android.os.Bundle;

public class HomeActivity extends BaseSildeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSlidingMenu().setMode(SlidingMenu.LEFT);
        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

        setContentView(R.layout.content_frame);

        MainFragment frag = new MainFragment();
        frag.homePage(true);
        frag.setActionCallback(new ActionCallback() {

            @Override
            public void showMenu() {

                HomeActivity.this.showMenu();
            }
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, frag).commit();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}
