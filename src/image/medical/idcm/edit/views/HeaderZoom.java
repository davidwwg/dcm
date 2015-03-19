package image.medical.idcm.edit.views;

import image.medical.idcm.R;
import image.medical.idcm.edit.EditCallback;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public class HeaderZoom extends LinearLayout implements OnClickListener {

    private LayoutInflater mInflater;
    private View           mCancel, mConfirm;
    EditCallback           mCallback;

    public HeaderZoom(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSubViews(context);
    }

    public HeaderZoom(Context context) {
        super(context);
        initSubViews(context);
    }

    private void initSubViews(Context context) {
        mInflater = LayoutInflater.from(context);
        mInflater.inflate(R.layout.header_zoom, this);

        mCancel = this.findViewById(R.id.z_cancel);
        mConfirm = this.findViewById(R.id.z_confirm);

        mCancel.setOnClickListener(this);
        mConfirm.setOnClickListener(this);

    }

    public void setEditCallback(EditCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void onClick(View v) {
        if (mCallback == null) {
            return;
        }
        if (v.equals(mCancel)) {
            mCallback.cancel(this);
        } else if (v.equals(mConfirm)) {
            mCallback.confirm(this);
        }

    }

}
