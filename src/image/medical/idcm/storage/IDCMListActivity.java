package image.medical.idcm.storage;

import image.medical.idcm.activity.BaseActivity;
import image.medical.idcm.fragment.MainFragment;
import image.medical.idcm.manager.FragmentController;
import android.os.Bundle;

public class IDCMListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        MainFragment fragment = new MainFragment();
        fragment.homePage(false);
        fragment.setArguments(bundle);
        FragmentController.addFragment(getSupportFragmentManager(), fragment, fragment.getTagName());
    }

}
