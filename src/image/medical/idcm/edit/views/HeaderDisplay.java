package image.medical.idcm.edit.views;

import image.medical.idcm.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HeaderDisplay extends LinearLayout implements OnClickListener {

    private LayoutInflater mInflater;
    private View           mBack, mPlay;
    private TextView       mIndex;

    public interface HeaderDisplayCallback {
        void back(View view);

        void play(View view);
    }

    HeaderDisplayCallback mCallback;

    public HeaderDisplay(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSubViews(context);
    }

    public HeaderDisplay(Context context) {
        super(context);
        initSubViews(context);
    }

    public void setHeaderDisplayCallback(HeaderDisplayCallback callback) {
        this.mCallback = callback;
    }

    private void initSubViews(Context context) {
        mInflater = LayoutInflater.from(context);
        mInflater.inflate(R.layout.header_display, this);

        mBack = this.findViewById(R.id.back);
        mPlay = this.findViewById(R.id.play);
        mIndex = (TextView) this.findViewById(R.id.index);

        mBack.setOnClickListener(this);
        mPlay.setOnClickListener(this);
    }

    public void setPageIndex(String pageIndex) {
        mIndex.setText(pageIndex);
    }

    @Override
    public void onClick(View v) {
        if (mCallback == null) {
            return;
        }
        if (v.equals(mBack)) {
            mCallback.back(this);
        } else if (v.equals(mPlay)) {
            mCallback.play(this);
        }
    }
}
