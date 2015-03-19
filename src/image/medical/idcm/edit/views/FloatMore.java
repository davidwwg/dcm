package image.medical.idcm.edit.views;

import image.medical.idcm.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class FloatMore extends LinearLayout {

    private LayoutInflater mInflater;

    public FloatMore(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSubViews(context);
    }

    public FloatMore(Context context) {
        super(context);
        initSubViews(context);
    }

    private void initSubViews(Context context) {
        mInflater = LayoutInflater.from(context);
        mInflater.inflate(R.layout.float_edit, this);

    }

}
