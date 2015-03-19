package image.medical.idcm.edit.views;

import image.medical.idcm.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public class FloatEdit extends LinearLayout implements OnClickListener {

    public enum EditType {
        ZOOM_PAN, MEASURE, ROTATE, NEGATIVE, RESET
    }

    public interface FloatEditCallback {
        void editDcm(FloatEdit edit, EditType type);
    }

    private LayoutInflater mInflater;
    private View mZoom, mMeasure, mRotate, mNegative, mReset;
    private FloatEditCallback mCallback;

    public FloatEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSubViews(context);
    }

    public FloatEdit(Context context) {
        super(context);
        initSubViews(context);
    }

    public void setFloatEditCallback(FloatEditCallback callback) {
        this.mCallback = callback;
    }

    private void initSubViews(Context context) {
        mInflater = LayoutInflater.from(context);
        mInflater.inflate(R.layout.float_edit, this);

        mZoom = this.findViewById(R.id.zoom);
        mMeasure = this.findViewById(R.id.measure);
        mRotate = this.findViewById(R.id.rotate);
        mNegative = this.findViewById(R.id.negative);
        mReset = this.findViewById(R.id.reset);

        mZoom.setOnClickListener(this);
        mMeasure.setOnClickListener(this);
        mRotate.setOnClickListener(this);
        mNegative.setOnClickListener(this);
        mReset.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (mCallback == null) {
            return;
        }

        if (v.equals(mZoom)) {
            mCallback.editDcm(this, EditType.ZOOM_PAN);
        } else if (v.equals(mMeasure)) {
            mCallback.editDcm(this, EditType.MEASURE);
        } else if (v.equals(mRotate)) {
            mCallback.editDcm(this, EditType.ROTATE);
        } else if (v.equals(mNegative)) {
            mCallback.editDcm(this, EditType.NEGATIVE);
        } else if (v.equals(mReset)) {
            mCallback.editDcm(this, EditType.RESET);
        }
    }

}
