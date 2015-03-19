package image.medical.idcm.activity;

import image.medical.idcm.cache.PageCache;
import image.medical.idcm.exchange.IDCMPageExchanger;
import image.medical.idcm.util.Constant;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public abstract class BaseActivity extends FragmentActivity {

    protected PageCache         mPageCache;

    protected IDCMPageExchanger mPageExchanger;

    protected Bundle            bundle;

    @Override
    protected void onCreate(Bundle saveInstanceState) {

        if (saveInstanceState != null) {
            bundle = saveInstanceState;
            mPageExchanger = bundle.getParcelable(Constant.PAGE_EXCHANGER);
            mPageCache = mPageExchanger.getPageCache();
        }

        if (getIntent() != null && getIntent().getExtras() != null) {
            bundle = getIntent().getExtras();
            mPageExchanger = bundle.getParcelable(Constant.PAGE_EXCHANGER);
            mPageCache = mPageExchanger.getPageCache();
        }

        super.onCreate(saveInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (outState != null) {
            IDCMPageExchanger pageExchanger = new IDCMPageExchanger();
            pageExchanger.setPageCache(mPageCache);
            outState.putParcelable(Constant.PAGE_EXCHANGER, pageExchanger);
        }
        super.onSaveInstanceState(outState);
    }

}
