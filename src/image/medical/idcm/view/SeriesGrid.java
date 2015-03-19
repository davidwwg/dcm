package image.medical.idcm.view;

import image.medical.idcm.R;
import image.medical.idcm.model.IDCMAbstract;
import image.medical.idcm.model.IDCMAbstract.ImageUrl;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SeriesGrid extends LinearLayout implements OnItemClickListener {

    private LayoutInflater      mInflater;
    private GridView            mGridView;
    private ImageAdapter        mAdapter;
    private IDCMAbstract        mAbstract    = new IDCMAbstract();

    private ImageLoader         mImageLoader = ImageLoader.getInstance();
    private DisplayImageOptions mOptions;
    SeriesCallback              mCallback;

    public interface SeriesCallback {
        void dialogDismiss();

        void SeriesSelected(String studyUid, String seriesUid);
    }

    public SeriesGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSubViews(context);
    }

    public SeriesGrid(Context context) {
        super(context);
        initSubViews(context);
    }

    private void initSubViews(Context context) {
        mInflater = LayoutInflater.from(context);

        View root = mInflater.inflate(R.layout.series_grid, this);

        mGridView = (GridView) root.findViewById(R.id.image_grid);

        mAdapter = new ImageAdapter();
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);

        mOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisc(true).build();
    }

    public void setSeriesCallback(SeriesCallback callback) {
        mCallback = callback;
    }

    public void setDataSource(IDCMAbstract abs) {
        this.mAbstract = abs;
        if (abs.getImageUrls().size() == 1) {
            mGridView.setNumColumns(1);
        } else if (abs.getImageUrls().size() == 2) {
            mGridView.setNumColumns(2);
        } else if (abs.getImageUrls().size() >= 3) {
            mGridView.setNumColumns(3);
        }

        mAdapter.notifyDataSetChanged();
    }

    class ImageAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mAbstract.getImageUrls().size();
        }

        @Override
        public Object getItem(int position) {
            return mAbstract.getImageUrls().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_series_image, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.idcm_image);
                viewHolder.textView = (TextView) convertView.findViewById(R.id.idcm_number);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            ImageUrl imageUrl = (ImageUrl) getItem(position);

            viewHolder.textView.setText(imageUrl.getNumber());
            mImageLoader.displayImage(imageUrl.getUrl(), viewHolder.imageView, mOptions);

            return convertView;
        }

        class ViewHolder {
            ImageView imageView;
            TextView  textView;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ImageUrl imageUrl = (ImageUrl) mAdapter.getItem(position);

        if (mCallback != null) {
            mCallback.SeriesSelected(mAbstract.getId(), imageUrl.getId());
        }

    }

}
