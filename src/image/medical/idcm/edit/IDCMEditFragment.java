package image.medical.idcm.edit;

import image.medical.idcm.R;
import image.medical.idcm.adapter.IDCMEditPagerAdapter;
import image.medical.idcm.cache.IDCMEditData;
import image.medical.idcm.edit.views.DcmSnap;
import image.medical.idcm.edit.views.DcmView;
import image.medical.idcm.edit.views.DcmView.OnSingleTapListener;
import image.medical.idcm.edit.views.FloatEdit;
import image.medical.idcm.edit.views.FloatEdit.EditType;
import image.medical.idcm.edit.views.FloatEdit.FloatEditCallback;
import image.medical.idcm.edit.views.FloatSeries;
import image.medical.idcm.edit.views.FloatSeries.FloatSeriesCallback;
import image.medical.idcm.edit.views.HeaderDisplay;
import image.medical.idcm.edit.views.HeaderDisplay.HeaderDisplayCallback;
import image.medical.idcm.edit.views.HeaderMeasure;
import image.medical.idcm.edit.views.HeaderZoom;
import image.medical.idcm.fragment.BaseFragment;
import image.medical.idcm.model.IDCMContent;
import image.medical.idcm.model.IDCMSeries;
import image.medical.idcm.widgets.DViewPager;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class IDCMEditFragment extends BaseFragment implements OnClickListener, OnSingleTapListener,
        OnPageChangeListener, FloatSeriesCallback, FloatEditCallback, EditCallback, HeaderDisplayCallback {

    private final static String  TAG               = "IDCMEditFragment";

    private final static int     REQUEST_CODE_MORE = 100;

    private DViewPager           mViewPager;
    private IDCMEditPagerAdapter mAdapter;
    private View                 mSeriesThumb, mEdit, mMore, mFooter;
    private FloatSeries          mFloatSeries;
    private FloatEdit            mFloatEdit;
    private View                 mCurrentFloat;
    private HeaderDisplay        mHeader;
    private HeaderMeasure        mMeausureHeader;
    private HeaderZoom           mZoomHeader;
    private DcmSnap              mDcmSnap;

    private List<View>           mTabs             = new ArrayList<View>();

    // private List<View> mFloats = new ArrayList<View>();

    // private DicomFolder mFolder = new DicomFolder();
    // private DicomSeries mSeries = new DicomSeries();

    IDCMEditData                 mData             = new IDCMEditData();
    IDCMSeries                   mCurSeries        = new IDCMSeries();

    private interface AnimationCallback {
        void animationFinished();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mPageCache != null) {
            mData = (IDCMEditData) mPageCache;
        } else {
            return null;
        }

        View root = inflater.inflate(R.layout.frag_idcm_edit, null);

        mHeader = (HeaderDisplay) root.findViewById(R.id.dicom_header_display);
        mHeader.setHeaderDisplayCallback(this);
        mFooter = root.findViewById(R.id.footer);
        mFooter.setOnClickListener(this);

        mHeader.setVisibility(View.INVISIBLE);
        mFooter.setVisibility(View.INVISIBLE);

        mZoomHeader = (HeaderZoom) root.findViewById(R.id.dicom_header_zoom);
        mZoomHeader.setEditCallback(this);
        mZoomHeader.setVisibility(View.INVISIBLE);

        mMeausureHeader = (HeaderMeasure) root.findViewById(R.id.dicom_header_measure);
        mMeausureHeader.setEditCallback(this);
        mMeausureHeader.setVisibility(View.INVISIBLE);

        mViewPager = (DViewPager) root.findViewById(R.id.dicom_view_pager);

        mCurSeries = mData.series.get(mData.getKeyIndex());

        mAdapter = new IDCMEditPagerAdapter(this);
        mAdapter.setDataSource(mCurSeries);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(this);

        setPageIndex(0);
        // mViewPager.setOnSingleTapListener(this);

        mFloatSeries = (FloatSeries) root.findViewById(R.id.float_series);
        mFloatSeries.setDataSource(mData.series, mData.getKeyIndex());
        mFloatSeries.setFloatSeriesCallback(this);

        mFloatEdit = (FloatEdit) root.findViewById(R.id.float_edit);
        mFloatEdit.setFloatEditCallback(this);

        mFloatSeries.setVisibility(View.INVISIBLE);
        mFloatEdit.setVisibility(View.INVISIBLE);

        mSeriesThumb = root.findViewById(R.id.series_thumb);
        mEdit = root.findViewById(R.id.edit);
        mMore = root.findViewById(R.id.more);

        mSeriesThumb.setOnClickListener(this);
        mEdit.setOnClickListener(this);
        mMore.setOnClickListener(this);

        mTabs.add(mSeriesThumb);
        mTabs.add(mEdit);
        mTabs.add(mMore);

        mDcmSnap = (DcmSnap) root.findViewById(R.id.dicom_player);
        mDcmSnap.setOnClickListener(this);
        mDcmSnap.setVisibility(View.GONE);
        return root;
    }

    // public String getTagName() {
    // return TAG;
    // }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        if (mFooter.getAnimation() != null || mHeader.getAnimation() != null) {
            return;
        }
        if (v.equals(mSeriesThumb)) {

            showFloatView(v, mFloatSeries);
        } else if (v.equals(mEdit)) {

            showFloatView(v, mFloatEdit);
        } else if (v.equals(mMore)) {
            Intent intent = new Intent(getActivity(), IDCMMoreActivity.class);
            startActivityForResult(intent, REQUEST_CODE_MORE);
        } else if (v.equals(mDcmSnap)) {
            mViewPager.setVisibility(View.VISIBLE);

            mDcmSnap.setVisibility(View.GONE);
            mDcmSnap.close();
        }
    }

    // @Override
    // protected void onActivityResult(int requestCode, int resultCode, Intent
    // data) {
    // super.onActivityResult(requestCode, resultCode, data);
    // if (resultCode == RESULT_OK) {
    // switch (requestCode) {
    // case REQUEST_CODE_MORE:
    // DicomView dicomView = mAdapter.getCurrentDicomView();
    // dicomView.clearLines();
    // dicomView.invalidate();
    // break;
    //
    // default:
    // break;
    // }
    // }
    // }

    private void showFloatView(View tabView, View floatView) {
        setFocused(tabView, floatView.getVisibility() == View.VISIBLE);

        if (floatView.getVisibility() == View.INVISIBLE) {
            translateAnimation(floatView, 0, 0, floatView.getHeight(), 0, true, null);

            if (mCurrentFloat != null) {
                mCurrentFloat.setVisibility(View.INVISIBLE);
            }

            mCurrentFloat = floatView;

        } else {
            translateAnimation(floatView, 0, 0, 0, floatView.getHeight(), false, null);

            mCurrentFloat = null;
        }
    }

    private void setFocused(View v, boolean visible) {
        for (View view : mTabs) {
            if (view.equals(v) && !visible) {
                view.setSelected(true);
            } else {
                view.setSelected(false);
            }
        }
    }

    private void translateAnimation(final View view, float fromX, float toX, float fromY, float toY,
            final boolean visible, final AnimationCallback callback) {
        if (!visible) {
            view.setVisibility(View.INVISIBLE);
        }

        TranslateAnimation translateAnimation = new TranslateAnimation(fromX, toX, fromY, toY);

        translateAnimation.setDuration(200);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
                if (visible) {
                    view.setVisibility(View.VISIBLE);
                }
                if (callback != null) {
                    callback.animationFinished();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(translateAnimation);
    }

    @Override
    public void onSingleTap(View view) {

        if (mCurrentFloat != null) {
            translateAnimation(mCurrentFloat, 0, 0, 0, mCurrentFloat.getHeight(), false, new AnimationCallback() {

                @Override
                public void animationFinished() {
                    alphaAnimation(mHeader, 1, 0, false, null);
                    alphaAnimation(mFooter, 1, 0, false, null);

                    setFocused(null, false);
                    mCurrentFloat = null;
                }
            });
        } else {
            if (mHeader.getVisibility() == View.INVISIBLE) {
                alphaAnimation(mHeader, 0, 1, true, null);
                alphaAnimation(mFooter, 0, 1, true, null);
            } else {
                alphaAnimation(mHeader, 1, 0, false, null);
                alphaAnimation(mFooter, 1, 0, false, null);
            }
        }

    }

    @Override
    public void onSingleTapLine(View view) {
        if (mCurrentFloat != null) {
            translateAnimation(mCurrentFloat, 0, 0, 0, mCurrentFloat.getHeight(), false, null);
            mCurrentFloat = null;
        }

        setFocused(null, false);

        DcmView dcmView = (DcmView) view;

        mViewPager.enableScroll(false);
        dcmView.setZoomPanable(true);
        dcmView.setSingleTapable(false);
        dcmView.enableScaling(false);

        dcmView.setLineEdit(true);

        mMeausureHeader.showDustbin();
        translateAnimation(mMeausureHeader, 0, 0, -mMeausureHeader.getHeight(), 0, true, new AnimationCallback() {

            @Override
            public void animationFinished() {

                if (mHeader.getVisibility() == View.VISIBLE) {
                    alphaAnimation(mHeader, 1, 0, false, null);
                    alphaAnimation(mFooter, 1, 0, false, null);
                }
            }
        });

    }

    private void alphaAnimation(final View view, float fromAlpha, float toAlpha, final boolean visible,
            final AnimationCallback callback) {
        if (!visible) {
            view.setVisibility(View.INVISIBLE);
        }

        AlphaAnimation alphaAnimation = new AlphaAnimation(fromAlpha, toAlpha);

        alphaAnimation.setDuration(200);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
                if (visible) {
                    view.setVisibility(View.VISIBLE);
                }
                if (callback != null) {
                    callback.animationFinished();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(alphaAnimation);
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int arg0) {
        setPageIndex(arg0);
    }

    @Override
    public void seriesSelection(FloatSeries view, int postion) {
        if (view.getAnimation() != null) {
            return;
        }
        mCurSeries = mData.series.get(postion);

        mAdapter.setDataSource(mCurSeries);
        mAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(0, false);

        setPageIndex(0);
    }

    public void setPageIndex(int postion) {
        int count = mAdapter.getCount();
        mHeader.setPageIndex(postion + 1 + "/" + count);
    }

    @Override
    public void editDcm(FloatEdit edit, EditType type) {
        if (edit.getAnimation() != null) {
            return;
        }

        translateAnimation(edit, 0, 0, 0, edit.getHeight(), false, null);

        mCurrentFloat = null;
        setFocused(null, false);

        DcmView dcmView = mAdapter.getCurDcmView(mViewPager.getCurrentItem());

        SeriesAttribute attr = null;

        switch (type) {
        case ZOOM_PAN:
            mViewPager.enableScroll(false);
            dcmView.setZoomPanable(true);
            dcmView.setSingleTapable(false);

            translateAnimation(mZoomHeader, 0, 0, -mZoomHeader.getHeight(), 0, true, new AnimationCallback() {

                @Override
                public void animationFinished() {
                    alphaAnimation(mHeader, 1, 0, false, null);
                    alphaAnimation(mFooter, 1, 0, false, null);
                }
            });

            break;
        case ROTATE:
            dcmView.translateAttributesWithRotateAngle();
            attr = mCurSeries.getAttribute();

            dcmView.updateAttributes(attr);
            mAdapter.notifyDataSetChanged();

            break;
        case NEGATIVE:
            boolean negative = mCurSeries.getAttribute().negative;
            mCurSeries.getAttribute().negative = !negative;

            mAdapter.notifyDataSetChanged();

            break;
        case MEASURE:
            mViewPager.enableScroll(false);
            dcmView.setZoomPanable(true);
            dcmView.setSingleTapable(false);
            dcmView.enableScaling(false);

            dcmView.setMeasureLine(true);

            mMeausureHeader.hiddenDustbin();

            translateAnimation(mMeausureHeader, 0, 0, -mMeausureHeader.getHeight(), 0, true, new AnimationCallback() {

                @Override
                public void animationFinished() {
                    alphaAnimation(mHeader, 1, 0, false, null);
                    alphaAnimation(mFooter, 1, 0, false, null);
                }
            });
            break;
        case RESET:
            dcmView.resetDicom();
            attr = mCurSeries.getAttribute();

            dcmView.updateAttributes(attr);
            mAdapter.notifyDataSetChanged();

            break;
        default:
            break;
        }
    }

    @Override
    public void cancel(View view) {
        translateAnimation(view, 0, 0, 0, -view.getHeight(), false, null);

        DcmView dcmView = mAdapter.getCurDcmView(mViewPager.getCurrentItem());
        dcmView.setZoomPanable(false);
        dcmView.setSingleTapable(true);
        mViewPager.enableScroll(true);

        if (view.equals(mMeausureHeader)) {
            dcmView.setMeasureLine(false);
            dcmView.setLineEdit(false);

            IDCMContent dcm = mCurSeries.getDcms().get(mViewPager.getCurrentItem());

            dcm.linestack.clearLineTapped();
            dcm.linestack.unConfirm();
            dcmView.invalidate();

        } else if (view.equals(mZoomHeader)) {
            SeriesAttribute attr = mCurSeries.getAttribute();

            dcmView.initAttributes(attr);
        }
    }

    @Override
    public void delete(View view) {
        // translateAnimation(view, 0, 0, 0, -view.getHeight(), false, null);

        DcmView dcmView = mAdapter.getCurDcmView(mViewPager.getCurrentItem());
        // dicomView.setZoomPanable(false);
        // dicomView.setSingleTapable(true);
        // mViewPager.enableScroll(true);

        if (view.equals(mMeausureHeader)) {
            // dicomView.setMeasureLine(false);
            // dicomView.setLineEdit(false);

            dcmView.deleteTappedLine();
            dcmView.invalidate();
        }
    }

    @Override
    public void confirm(View view) {
        translateAnimation(view, 0, 0, 0, -view.getHeight(), false, null);

        DcmView dcmView = mAdapter.getCurDcmView(mViewPager.getCurrentItem());
        dcmView.setZoomPanable(false);
        dcmView.setSingleTapable(true);
        mViewPager.enableScroll(true);

        if (view.equals(mMeausureHeader)) {
            dcmView.setMeasureLine(false);
            dcmView.setLineEdit(false);

            IDCMContent dcm = mCurSeries.getDcms().get(mViewPager.getCurrentItem());

            dcm.linestack.clearLineTapped();
            dcm.linestack.confirm();
            dcmView.invalidate();

        } else if (view.equals(mZoomHeader)) {
            SeriesAttribute attr = mCurSeries.getAttribute();

            dcmView.updateAttributes(attr);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void back(View view) {

        getActivity().finish();
    }

    @Override
    public void play(View view) {

        onSingleTap(null);
        mViewPager.setVisibility(View.GONE);

        mDcmSnap.setVisibility(View.VISIBLE);
        mDcmSnap.play();
        mDcmSnap.initSeries(mCurSeries);
    }

}
