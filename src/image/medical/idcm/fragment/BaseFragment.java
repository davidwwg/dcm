package image.medical.idcm.fragment;

import image.medical.idcm.cache.PageCache;
import image.medical.idcm.exchange.IDCMPageExchanger;
import image.medical.idcm.util.Constant;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BaseFragment extends Fragment {

    protected PageCache         mPageCache;

    protected IDCMPageExchanger mPageExchanger;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mPageExchanger = arguments.getParcelable(Constant.PAGE_EXCHANGER);
            mPageCache = mPageExchanger.getPageCache();
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    public String getTagName() {
        return BaseFragment.this.getClass().getName();
    }
}
