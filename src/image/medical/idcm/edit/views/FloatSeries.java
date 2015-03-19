package image.medical.idcm.edit.views;

import java.util.ArrayList;
import java.util.List;

import image.medical.idcm.R;
import image.medical.idcm.model.IDCMContent;
import image.medical.idcm.model.IDCMSeries;
import image.medical.idcm.util.UiUtil;
import image.medical.idcm.widgets.HorizontalListView;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class FloatSeries extends LinearLayout implements OnItemClickListener {

    public interface FloatSeriesCallback {
        void seriesSelection(FloatSeries view, int postion);
    }

    private LayoutInflater      mInflater;
    private HorizontalListView  mHListView;
    private FloatSeriesAdapter  mAdapter;
    private List<IDCMSeries>    mSeries           = new ArrayList<IDCMSeries>();
    private int                 mSelectedPosition = 0;
    private ImageLoader         mImageLoader      = ImageLoader.getInstance();
    private DisplayImageOptions mOptions;

    FloatSeriesCallback         mCallback;

    public FloatSeries(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSubViews(context);
    }

    public FloatSeries(Context context) {
        super(context);
        initSubViews(context);
    }

    private void initSubViews(Context context) {
        mInflater = LayoutInflater.from(context);
        mInflater.inflate(R.layout.float_series, this);

        mHListView = (HorizontalListView) this.findViewById(R.id.seried_list);
        mAdapter = new FloatSeriesAdapter();
        mHListView.setAdapter(mAdapter);
        mHListView.setOnItemClickListener(this);

        mOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisc(true).build();
    }

    public void setFloatSeriesCallback(FloatSeriesCallback callback) {
        mCallback = callback;
    }

    public void setDataSource(List<IDCMSeries> series, int selectedPostion) {
        mSeries = series;
        mSelectedPosition = selectedPostion;
        mHListView.scrollTo(UiUtil.dip2px(getContext(), 70) * mSelectedPosition);
        mAdapter.notifyDataSetChanged();
    }

    class FloatSeriesAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mSeries.size();
        }

        @Override
        public Object getItem(int position) {
            return mSeries.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                LinearLayout layout = (LinearLayout) mInflater.inflate(R.layout.item_float_series, null);
                viewHolder = new ViewHolder();
                viewHolder.mSeriesThumb = (ImageView) layout.findViewById(R.id.series_thumb);
                viewHolder.mDicomNumber = (TextView) layout.findViewById(R.id.dicom_number);
                viewHolder.mThumbBg = layout.findViewById(R.id.thumb_bg);
                convertView = layout;
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            IDCMSeries series = (IDCMSeries) getItem(position);

            IDCMContent dcm = series.getDcms().get(0);

            if (position == mSelectedPosition) {
                viewHolder.mThumbBg.setBackgroundResource(R.drawable.thumb_bg_selected);
            } else {
                viewHolder.mThumbBg.setBackgroundResource(R.drawable.thumb_bg);
            }
            mImageLoader.displayImage(dcm.getThumbUrl(), viewHolder.mSeriesThumb, mOptions);
            viewHolder.mDicomNumber.setText(series.getDcms().size() + "");
            return convertView;
        }

        class ViewHolder {
            private ImageView mSeriesThumb;
            private TextView  mDicomNumber;
            private View      mThumbBg;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position != mSelectedPosition) {
            mSelectedPosition = position;
            mAdapter.notifyDataSetChanged();
            if (mCallback != null) {
                mCallback.seriesSelection(this, position);
            }
        }
    }
}
