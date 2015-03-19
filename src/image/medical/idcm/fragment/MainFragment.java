package image.medical.idcm.fragment;

import image.medical.idcm.R;
import image.medical.idcm.activity.BaseActivity;
import image.medical.idcm.adapter.AbstractAdapter;
import image.medical.idcm.adapter.AbstractAdapter.AbstractAdapterCallback;
import image.medical.idcm.app.AppController;
import image.medical.idcm.cache.IDCMEditData;
import image.medical.idcm.cache.IDCMListData;
import image.medical.idcm.edit.IDCMEditActivity;
import image.medical.idcm.helper.ActivityConfig;
import image.medical.idcm.manager.ExchangeManager;
import image.medical.idcm.model.IDCMAbstract;
import image.medical.idcm.view.FilterView;
import image.medical.idcm.view.FilterView.FilterViewCallback;
import image.medical.idcm.view.SeriesGrid;
import image.medical.idcm.view.SeriesGrid.SeriesCallback;
import image.medical.idcm.widgets.XListView;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupWindow;

public class MainFragment extends BaseFragment implements OnClickListener, FilterViewCallback, OnItemClickListener,
        AbstractAdapterCallback, SeriesCallback {

    private static final String TAG       = "MainFragment";

    private boolean             bHomePage = true;
    private View                mBack;
    private View                mAction;
    private View                mFilter;
    private PopupWindow         mFilterPop;
    private FilterView          mFilterView;
    private XListView           mXListView;
    private AbstractAdapter     mAdapter;
    private SeriesGrid          mGrid;
    private PopupWindow         mGridPop;

    private IDCMListData        mData     = new IDCMListData();

    public interface ActionCallback {
        void showMenu();
    }

    ActionCallback mCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root;

        if (bHomePage) {
            root = inflater.inflate(R.layout.frag_main, null);
            mAction = root.findViewById(R.id.top_action);
            mFilter = root.findViewById(R.id.top_filter);
            mAction.setOnClickListener(this);
            mFilter.setOnClickListener(this);
        } else {
            root = inflater.inflate(R.layout.frag_list, null);

            mBack = root.findViewById(R.id.back);
            mBack.setOnClickListener(this);
            if (mPageCache != null) {
                mData = (IDCMListData) mPageCache;
            } else {
                return root;
            }
        }

        mXListView = (XListView) root.findViewById(R.id.xlist_view);

        if (bHomePage) {
            mXListView.setPullLoadEnable(true);
            mXListView.setPullRefreshEnable(true);
        } else {
            mXListView.setPullLoadEnable(false);
            mXListView.setPullRefreshEnable(false);
        }

        mAdapter = new AbstractAdapter(getActivity(), mData.abstracts);
        mAdapter.setAbstractAdapterCallback(this);

        mXListView.setAdapter(mAdapter);
        mXListView.setOnItemClickListener(this);
        return root;
    }

    public void homePage(boolean home) {
        this.bHomePage = home;
    }

    public void setActionCallback(ActionCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onClick(View v) {
        if (v.equals(mAction)) {

            if (mCallback != null) {
                mCallback.showMenu();
            }
        } else if (v.equals(mFilter)) {
            if (mFilterPop == null) {
                mFilterView = new FilterView(getActivity());
                mFilterView.setFilterViewCallback(this);
                mFilterPop = new PopupWindow(mFilterView, android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT);

                mFilterPop.setBackgroundDrawable(new BitmapDrawable());
                mFilterPop.setAnimationStyle(R.style.PopFilterAnimation);
                mFilterPop.setOutsideTouchable(true);
                mFilterPop.setFocusable(true);

                mFilterPop.update();
            } else {

            }
            mFilterPop.showAsDropDown(mFilter);
        } else if (v.equals(mBack)) {
            getActivity().finish();
        }
    }

    @Override
    public void dismissPopupWindow() {
        if (mFilterPop != null) {
            mFilterPop.dismiss();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position <= 0 || position - 1 >= mData.abstracts.size()) {
            return;
        }
        IDCMAbstract abs = mData.abstracts.get(position - 1);

        String jumpCode = ActivityConfig.getInstance().getActivityCode(IDCMEditActivity.class);
        IDCMEditData edit = new IDCMEditData();
        edit.key = mData.getDefaultSeriesUid();
        edit.series = mData.getSeriesByStudyUid(abs.getId());
        ExchangeManager.jump(jumpCode, edit, null, (BaseActivity) getActivity());
    }

    @Override
    public void imageClick(IDCMAbstract abs) {
        if (mGridPop == null) {
            mGrid = new SeriesGrid(getActivity());
            mGrid.setSeriesCallback(this);
            int screenHeight = AppController.getScreenHeight(getActivity());
            mGridPop = new PopupWindow(mGrid, android.view.ViewGroup.LayoutParams.MATCH_PARENT, screenHeight / 2);

            mGridPop.setAnimationStyle(R.style.PopSeriesAnimation);
            mGridPop.setBackgroundDrawable(new BitmapDrawable());
            mGridPop.setOutsideTouchable(true);
            mGridPop.setFocusable(true);

            mGridPop.update();
        }
        mGrid.setDataSource(abs);

        mGridPop.showAtLocation(mXListView, Gravity.LEFT | Gravity.RIGHT, 0, 0);

    }

    @Override
    public void dialogDismiss() {

    }

    @Override
    public void SeriesSelected(String studyUid, String seriesUid) {
        IDCMEditData edit = new IDCMEditData();
        edit.key = seriesUid;
        edit.series = mData.getSeriesByStudyUid(studyUid);

        String jumpCode = ActivityConfig.getInstance().getActivityCode(IDCMEditActivity.class);

        ExchangeManager.jump(jumpCode, edit, null, (BaseActivity) getActivity());
    }
}
