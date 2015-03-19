package image.medical.idcm.adapter;

import image.medical.idcm.edit.IDCMEditFragment;
import image.medical.idcm.edit.SeriesAttribute;
import image.medical.idcm.edit.views.DcmView;
import image.medical.idcm.model.IDCMContent;
import image.medical.idcm.model.IDCMSeries;
import image.medical.idcm.util.DCMUtil;

import java.util.HashMap;
import java.util.Map;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.imebra.dicom.Image;
import com.imebra.dicom.TransformsChain;

public class IDCMEditPagerAdapter extends PagerAdapter {

    private IDCMEditFragment     mFragment;
    private IDCMSeries           mSeries   = new IDCMSeries();
    private Map<String, DcmView> mDcmViews = new HashMap<String, DcmView>();

    public IDCMEditPagerAdapter(IDCMEditFragment fragment) {
        mFragment = fragment;
    }

    public void setDataSource(IDCMSeries series) {
        mSeries = series;
    }

    public DcmView getCurDcmView(int postion) {
        return mDcmViews.get(postion + "");
    }

    @Override
    public int getCount() {
        return mSeries.getDcms().size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Object instantiateItem(View container, int position) {
        String key = position + "";
        DcmView dcmView;
        if (mDcmViews.containsKey(key)) {
            dcmView = mDcmViews.get(key);
        } else {
            dcmView = new DcmView(mFragment.getActivity());
            dcmView.setZoomPanable(false);
            dcmView.setSingleTapable(true);
            dcmView.setOnSingleTapListener(mFragment);

            IDCMContent dcm = mSeries.getDcms().get(position);
            TransformsChain chain = new TransformsChain();

            Image image;

            if (mSeries.getAttribute().negative) {
                image = DCMUtil.getNegativeDcmImageTranfromChain(dcm.getDataSet(), chain);
            } else {
                image = DCMUtil.getDcmImageTranfromChain(dcm.getDataSet(), chain);
            }

            dcmView.setDcmContent(dcm);

            dcmView.setImage(image, chain);

            dcmView.initAttributes(mSeries.getAttribute());
            dcmView.setLineStack(dcm.linestack);

            ((ViewPager) container).addView(dcmView, 0);
            mDcmViews.put(key, dcmView);
        }

        return dcmView;
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        ((ViewPager) container).removeView((View) object);
        String key = position + "";
        mDcmViews.remove(key);
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == ((View) arg1);
    }

    @Override
    public void notifyDataSetChanged() {
        mDcmViews.clear();
        super.notifyDataSetChanged();
    }
}
