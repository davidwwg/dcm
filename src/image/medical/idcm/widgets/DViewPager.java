package image.medical.idcm.widgets;

import image.medical.idcm.edit.views.DcmView.OnSingleTapListener;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class DViewPager extends ViewPager {

    private boolean mBScrollEnabled = true;
    private GestureDetector mTapGestureDetector;
    OnSingleTapListener mOnSingleTapTapListener;

    public DViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initGestureDetector(context);
    }

    public DViewPager(Context context) {
        super(context);
        initGestureDetector(context);
    }

    private void initGestureDetector(Context context) {
        mTapGestureDetector = new GestureDetector(context,
                new ViewPagerGestureDetector());

    }

    public void setOnSingleTapListener(OnSingleTapListener listener) {
        mOnSingleTapTapListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (!mBScrollEnabled) {
            return false;
        }
        return super.onInterceptTouchEvent(arg0);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent arg0) {
//        return super.onTouchEvent(arg0)
//                | mTapGestureDetector.onTouchEvent(arg0);
//    }

    public void enableScroll(boolean scroll) {
        mBScrollEnabled = scroll;
    }

    class ViewPagerGestureDetector implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mOnSingleTapTapListener != null) {
                mOnSingleTapTapListener.onSingleTap(DViewPager.this);
            }
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            return false;
        }

    }
}
