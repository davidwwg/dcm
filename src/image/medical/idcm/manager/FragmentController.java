package image.medical.idcm.manager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class FragmentController {

    public static void addFragment(FragmentManager fragmentManager, Fragment fragment, String tag) {
        addFragment(fragmentManager, fragment, tag, android.R.id.content);
    }

    public static void addFragment(FragmentManager fragmentManager, Fragment fragment, String tag, int position) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(position, fragment, tag);
        transaction.commitAllowingStateLoss();

    }
}
