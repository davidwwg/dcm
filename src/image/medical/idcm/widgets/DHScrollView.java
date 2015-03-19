package image.medical.idcm.widgets;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.HorizontalScrollView;

/**
 * 侧滑动ScrollView.
 */
public class DHScrollView extends HorizontalScrollView {

    public interface SizeCallback {
        /**
         * 获取childs的宽�?
         */
        void setViewSize(int idx, int w, int h, int[] dims);
    }

    public DHScrollView(Context context) {
        super(context);
        initScrollView(context);
    }

    public DHScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initScrollView(context);
    }

    public DHScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initScrollView(context);
    }

    private void initScrollView(Context context) {
        setHorizontalFadingEdgeEnabled(false);
        setVerticalFadingEdgeEnabled(false);
    }

    /**
     * 重置滑动view的位�?
     * 
     * @param children
     *            child
     * @param scrollToViewIdx
     *            index
     * @param sizeCallback
     *            callback
     */
    public void layout(View[] children, int scrollToViewIdx,
            SizeCallback callback) {
        ViewGroup parent = (ViewGroup) getChildAt(0);
        OnGlobalLayoutListener listener = new MyOnGlobalLayoutListener(parent,
                children, scrollToViewIdx, callback);
        getViewTreeObserver().addOnGlobalLayoutListener(listener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    class MyOnGlobalLayoutListener implements OnGlobalLayoutListener {
        private ViewGroup mParent;
        private View[] mChildren;
        private int mScrollToViewIdx;
        private int mScrollToViewPos = 0;
        private SizeCallback mCallback;

        public MyOnGlobalLayoutListener(ViewGroup parent, View[] children,
                int scrollToViewIdx, SizeCallback sizeCallback) {
            this.mParent = parent;
            this.mChildren = children;
            this.mScrollToViewIdx = scrollToViewIdx;
            this.mCallback = sizeCallback;
        }

        @Override
        public void onGlobalLayout() {
            final HorizontalScrollView me = DHScrollView.this;
            me.getViewTreeObserver().removeGlobalOnLayoutListener(this);

            mParent.removeViewsInLayout(0, mChildren.length);

            final int w = me.getMeasuredWidth();
            final int h = me.getMeasuredHeight();

            int[] dims = new int[2];
            mScrollToViewPos = 0;
            for (int i = 0; i < mChildren.length; i++) {
                mCallback.setViewSize(i, w, h, dims);
                mChildren[i].setVisibility(View.VISIBLE);
                mParent.addView(mChildren[i], dims[0], dims[1]);
                if (i < mScrollToViewIdx) {
                    mScrollToViewPos += dims[0];
                }
            }

            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    me.scrollBy(mScrollToViewPos, 0);
                }
            });
        }
    }

}
