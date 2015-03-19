package image.medical.idcm.manager;

import image.medical.idcm.activity.BaseActivity;
import image.medical.idcm.app.IDCMApplication;
import image.medical.idcm.cache.PageCache;
import image.medical.idcm.exchange.IDCMPageExchanger;
import image.medical.idcm.helper.ActivityConfig;
import image.medical.idcm.helper.ActivityModel;
import image.medical.idcm.helper.FragmentConfig;
import image.medical.idcm.helper.FragmentModel;
import image.medical.idcm.util.Constant;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

public class ExchangeManager {
    public static void jump(String code, PageCache cache, Fragment curFragment, BaseActivity activity) {
        Bundle bundle = new Bundle();
        if (cache != null) {
            IDCMPageExchanger exchanger = new IDCMPageExchanger();
            exchanger.setPageCache(cache);
            bundle.putParcelable(Constant.PAGE_EXCHANGER, exchanger);
        }

        if (code.startsWith("F")) {
            FragmentModel fragmentModel = FragmentConfig.getInstance().getFragmentModel(code);
            if (fragmentModel != null && !TextUtils.isEmpty(fragmentModel.className)) {
                Fragment fragment = Fragment.instantiate(IDCMApplication.getInstance(), fragmentModel.className);
                fragment.setArguments(bundle);

                if (curFragment != null) {
                    fragment.setTargetFragment(curFragment, -1);
                }

            }
        } else {
            ActivityModel activityModel = ActivityConfig.getInstance().getActivityModel(code);
            if (activityModel != null && !TextUtils.isEmpty(activityModel.className)) {
                try {
                    Intent intent = new Intent(IDCMApplication.getInstance(), Class.forName(activityModel.className));
                    intent.putExtras(bundle);

                    activity.startActivity(intent);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
