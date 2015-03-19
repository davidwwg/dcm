package image.medical.idcm.edit.views;

import image.medical.idcm.R;
import image.medical.idcm.edit.EditCallback;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public class HeaderMeasure extends LinearLayout implements OnClickListener {

    private LayoutInflater mInflater;
    private View           mCancel, mDelete, mConfirm;
    EditCallback           mCallback;

    public HeaderMeasure(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSubViews(context);
    }

    public HeaderMeasure(Context context) {
        super(context);
        initSubViews(context);
    }

    public void setEditCallback(EditCallback callback) {
        this.mCallback = callback;
    }

    private void initSubViews(Context context) {
        mInflater = LayoutInflater.from(context);
        mInflater.inflate(R.layout.header_measure, this);

        mCancel = this.findViewById(R.id.m_cancel);
        mDelete = this.findViewById(R.id.m_delete);
        mConfirm = this.findViewById(R.id.m_confirm);

        mCancel.setOnClickListener(this);
        mDelete.setOnClickListener(this);
        mConfirm.setOnClickListener(this);
    }

    public void hiddenDustbin() {
        mDelete.setVisibility(View.INVISIBLE);
    }

    public void showDustbin() {
        mDelete.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        if (mCallback == null) {
            return;
        }
        if (v.equals(mCancel)) {
            mCallback.cancel(this);
        } else if (v.equals(mDelete)) {
            mCallback.delete(this);
        } else if (v.equals(mConfirm)) {
            mCallback.confirm(this);
        }

    }

}
