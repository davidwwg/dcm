package image.medical.idcm.adapter;

import image.medical.idcm.R;
import image.medical.idcm.model.IDCMAbstract;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class AbstractAdapter extends BaseAdapter {

    private static final String TAG          = "IDCMAdapter";
    private LayoutInflater      mInflater;
    private List<IDCMAbstract>  mAbstracts;
    private ImageLoader         mImageLoader = ImageLoader.getInstance();
    private DisplayImageOptions mOptions;
    AbstractAdapterCallback     mCallback;

    public interface AbstractAdapterCallback {
        void imageClick(IDCMAbstract abs);
    }

    public AbstractAdapter(Context context, List<IDCMAbstract> abstracts) {
        mInflater = LayoutInflater.from(context);
        mAbstracts = abstracts;

        mOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisc(true).build();
    }

    public void setAbstractAdapterCallback(AbstractAdapterCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public int getCount() {
        return mAbstracts.size();
    }

    @Override
    public Object getItem(int position) {
        return mAbstracts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_dcm, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.dcmSample = (ImageView) convertView.findViewById(R.id.dcm_sample);
            viewHolder.patientName = (TextView) convertView.findViewById(R.id.patient_name);
            viewHolder.patientSex = (TextView) convertView.findViewById(R.id.patient_sex);
            viewHolder.modality = (TextView) convertView.findViewById(R.id.modality);
            viewHolder.patientId = (TextView) convertView.findViewById(R.id.patient_id);
            viewHolder.studyDate = (TextView) convertView.findViewById(R.id.study_date);
            viewHolder.patientBirth = (TextView) convertView.findViewById(R.id.patient_birth);
            viewHolder.seriesNumber = (TextView) convertView.findViewById(R.id.series_number);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final IDCMAbstract a = (IDCMAbstract) getItem(position);

        viewHolder.patientName.setText(a.getPaitentName());
        viewHolder.patientSex.setText(a.getPaitentSex());
        viewHolder.modality.setText(a.getModality());
        viewHolder.patientId.setText(a.getPaitentId());
        viewHolder.studyDate.setText(a.getStudyDate());
        viewHolder.patientBirth.setText(a.getPaitentBirth());
        viewHolder.seriesNumber.setText(a.getSeriesNumber());

        if (a.getImageUrls().size() > 0) {
            mImageLoader.displayImage(a.getImageUrls().get(0).getUrl(), viewHolder.dcmSample, mOptions);
        }

        viewHolder.dcmSample.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mCallback != null) {
                    mCallback.imageClick(a);
                }

            }
        });

        return convertView;
    }

    private class ViewHolder {
        ImageView dcmSample;
        TextView  patientName;
        TextView  patientSex;
        TextView  modality;
        TextView  patientId;
        TextView  studyDate;
        TextView  patientBirth;
        TextView  seriesNumber;
    }

}
