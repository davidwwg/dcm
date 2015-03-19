package image.medical.idcm.view;

import image.medical.idcm.R;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class FilterView extends LinearLayout implements OnClickListener,
        OnItemClickListener {

    public interface FilterViewCallback {
        void dismissPopupWindow();
    }

    FilterViewCallback mCallback;
    private final static int MAX_COLUMN = 6;
    private ListView mListView;
    private FilterAdapter mAdapter;

    private TextView mStartDate;
    private TextView mEndDate;
    private TextView mDTAllSelected;
    private GridView mDTGrid;
    private DTAdapter mDTAdapter;
    private TextView mRSAllSelected;
    private TextView mNoApply;
    private TextView mApplyed;
    private TextView mNoCheck;
    private TextView mChecked;

    private Context mContext;
    private LayoutInflater mInflater;
    private View mConfirm;
    private View mCancel;
    private List<TextView> mRSlist = new ArrayList<TextView>();
    private static String[] DT_TEXT = new String[] { "CR", "CT", "DX", "DR",
            "MG", "MR", "NM", "OT", "PT", "RF", "RG", "SC", "US", "VL", "XA" };

    private static int[] Filter_menus = new int[] { R.string.patient_name,
            R.string.hosp_number, R.string.outp_number, R.string.patient_id,
            R.string.check_id, R.string.operation_number };

    private List<String> mFilterValues = new ArrayList<String>();

    public FilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSubViews(context);
    }

    public FilterView(Context context) {
        super(context);
        initSubViews(context);
    }

    private void initSubViews(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mInflater.inflate(R.layout.filter_content, this);
        mListView = (ListView) this.findViewById(R.id.filter_list);

        View header = mInflater.inflate(R.layout.filter_content_header, null);

        mStartDate = (TextView) header.findViewById(R.id.start_date);
        mEndDate = (TextView) header.findViewById(R.id.end_date);
        mDTAllSelected = (TextView) header
                .findViewById(R.id.dicom_all_selected);
        mDTGrid = (GridView) header.findViewById(R.id.dicom_type_grid);
        mRSAllSelected = (TextView) header
                .findViewById(R.id.report_all_selected);
        mNoApply = (TextView) header.findViewById(R.id.no_apply);
        mApplyed = (TextView) header.findViewById(R.id.applyed);
        mNoCheck = (TextView) header.findViewById(R.id.no_check);
        mChecked = (TextView) header.findViewById(R.id.checked);

        mDTAdapter = new DTAdapter();
        mDTGrid.setAdapter(mDTAdapter);
        mDTGrid.setNumColumns(MAX_COLUMN);
        mDTGrid.setOnItemClickListener(this);

        mListView.addHeaderView(header);

        mAdapter = new FilterAdapter();
        mListView.setAdapter(mAdapter);

        mConfirm = this.findViewById(R.id.f_confirm);
        mCancel = this.findViewById(R.id.f_cancel);

        mConfirm.setOnClickListener(this);
        mCancel.setOnClickListener(this);
    }

    public void setFilterViewCallback(FilterViewCallback callback) {
        mCallback = callback;
    }

    class FilterAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return Filter_menus.length;
        }

        @Override
        public Object getItem(int position) {
            return Filter_menus[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.filter_content_ot,
                        null);
                viewHolder = new ViewHolder();
                viewHolder.mLabel = (TextView) convertView
                        .findViewById(R.id.label);
                viewHolder.mValue = (TextView) convertView
                        .findViewById(R.id.value);
                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();

            }

            int labelId = (Integer) getItem(position);

            viewHolder.mLabel.setText(labelId);

            return convertView;
        }

        class ViewHolder {
            private TextView mLabel;
            private TextView mValue;

        }

    }

    class DTAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return DT_TEXT.length;
        }

        @Override
        public Object getItem(int position) {
            return DT_TEXT[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHoler viewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_dicom_type, null);
                viewHolder = new ViewHoler();
                viewHolder.mDTText = (TextView) convertView
                        .findViewById(R.id.dicom_text);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHoler) convertView.getTag();
            }
            String dt = (String) getItem(position);
            viewHolder.mDTText.setText(dt);
            return convertView;
        }

        class ViewHoler {
            private TextView mDTText;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mConfirm)) {
            if (mCallback != null) {
                mCallback.dismissPopupWindow();
            }
        } else if (v.equals(mCancel)) {
            if (mCallback != null) {
                mCallback.dismissPopupWindow();
            }
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        // TODO Auto-generated method stub

    }
}
