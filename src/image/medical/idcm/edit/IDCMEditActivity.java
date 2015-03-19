package image.medical.idcm.edit;

import image.medical.idcm.activity.BaseActivity;
import image.medical.idcm.manager.FragmentController;
import android.os.Bundle;

public class IDCMEditActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IDCMEditFragment fragment = new IDCMEditFragment();
        fragment.setArguments(bundle);
        FragmentController.addFragment(getSupportFragmentManager(), fragment, fragment.getTagName());
    }

    
    
}
