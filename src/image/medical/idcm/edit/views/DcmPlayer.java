package image.medical.idcm.edit.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class DcmPlayer extends LinearLayout {

    private LayoutInflater mInflater;
    
    public DcmPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSubViews(context);
    }

    public DcmPlayer(Context context) {
        super(context);
        initSubViews(context);
    }

    private void initSubViews(Context context) {
        mInflater = LayoutInflater.from(context);
        
    }
    
    
}
