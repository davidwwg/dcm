package image.medical.idcm.edit.views;

import image.medical.idcm.R;
import image.medical.idcm.edit.IDCMRotateAngle;
import image.medical.idcm.edit.Line;
import image.medical.idcm.edit.LineStack;
import image.medical.idcm.edit.SeriesAttribute;
import image.medical.idcm.model.IDCMContent;
import image.medical.idcm.util.UiUtil;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;

import com.imebra.dicom.DrawBitmap;
import com.imebra.dicom.Image;
import com.imebra.dicom.TransformsChain;

/**
 * 显示dicom图像，提供滑动、放大功能.
 * 
 * 在屏幕上显示的图像之前，transformschain中指定转换应用到image.
 * 色彩空间转换由视图自动应用，它并没有被添加到transformschain.
 * 
 * 在视图呈现图像时，通过计算碎片要在单独的线程上显示.
 * 
 */
public class DcmView extends View {

    private Paint                         mBackgroundPaint             = null;

    private IDCMRotateAngle               mRotateAngle                 = IDCMRotateAngle.ANGLE_0;

    private float                         mZoomScale                   = 1.0f;
    private float                         mCenterPointX                = 0.5f;
    private float                         mCenterPointY                = 0.5f;

    /** 放大缩小之后的图片宽度，通过 updateCoordinates()方法计算. */
    private int                           mTotalWidth;
    /** 放大缩小之后的图片高度，通过 updateCoordinates()方法计算. */

    private int                           mTotalHeight;
    /** 放大缩小之后的第一个可见左上方像素X坐标. */
    private int                           mTopLeftX;
    /** 放大缩小之后的第一个可见左上方像素Y坐标. */
    private int                           mTopLeftY;
    /** 放大缩小之后的第一个不可见右下方像素X坐标. */
    private int                           mBottomRightX;
    /** 放大缩小之后的第一个不可见右下方像素Y坐标. */
    private int                           mBottomRightY;

    /** 偏移应用于X坐标显示位图之前（用于滚动）. */
    private int                           mBitmapOffsetX;
    /** 偏移应用于Y坐标显示位图之前（用于滚动）. */
    private int                           mBitmapOffsetY;

    /** 图像碎片的宽度. */
    private static int                    mTileSizeX                   = 256;
    /** 图像碎片的高度. */
    private static int                    mTileSizeY                   = 256;

    /** 转换链. */
    private TransformsChain               mTransformsChain;
    /** 被渲染的Image对象. */
    private Image                         mImage;
    /** 被渲染的DrawBitmap对象. */
    private DrawBitmap                    mDrawBitmap;
    /** 每一次drawBitmap的改变或转换链的变化,计数器增加. */
    private long                          mDrawBitmapCounter           = 0;

    /** 碎片的集合显示. */
    private Bitmap                        mFullBitmap;
    /** mDrawBitmapCounter发生变化重新绘制碎片集合. */
    private long                          mFullDrawBitmapCounter       = 0;

    /** 渲染对片的请求序列. */
    final private List<RenderTileRequest> mRenderTileRequests          = new ArrayList<RenderTileRequest>();
    /** 碎片集合的请求. */
    RenderTileRequest                     mFullImageRenderRequest      = null;
    /** 渲染碎片集合的标记. */
    private long                          mRenderFullDrawBitmapCounter = 0;

    /** 第一次调用draw函数flag. */
    private boolean                       mBFirstDraw;

    /** 碎片信息序列. */
    private List<TileInfo>                mTiles                       = new ArrayList<TileInfo>();

    /** 渲染线程关闭flag. */
    boolean                               mBTerminate                  = false;

    /** 缩放手势侦测器. */
    private ScaleGestureDetector          mScaleGestureDetector;
    /** 滑动手势侦测器. */
    private GestureDetector               mScrollGestureDetector;

    private boolean                       mBScaleEnabled               = true;
    private boolean                       mBPanEnabled                 = true;
    private boolean                       mBSingleTapEnable            = true;
    private boolean                       mBPanMoveEnable              = true;

    private Thread                        mRenderTilesThread           = null;

    private float                         mOriginalZoomScale;
    private LineStack                     mLineStatck                  = new LineStack();

    private double                        mVrw, mVrh;
    private IDCMContent                   mDcm;
    private Paint                         mTextPaint;
    private Paint                         mOrientationPaint;
    private Paint                         mLoadPaint;

    private int                           mTextMargin;
    private int                           mTextSpace;

    private Paint                         mMetricPaint;

    private int                           mMetricMargin;

    /**
     * 图像覆盖层监听器.
     */
    public interface DrawOverlayListener {
        void onDrawOverlay(Canvas canvas, int totalWidth, int totalHeight, Image image);
    }

    DrawOverlayListener mDrawOverlayListener = null;

    /**
     * 固定覆盖层.
     */
    public interface DrawFixedOverlayListener {
        void onDrawFixedOverlay(Canvas canvas);
    }

    DrawFixedOverlayListener mDrawFixedOverlayListener = null;

    /**
     * tap事件.
     */
    public interface OnSingleTapListener {
        void onSingleTap(View view);

        void onSingleTapLine(View view);
    }

    OnSingleTapListener mOnSingleTapTapListener = null;

    public DcmView(android.content.Context context) {
        super(context);
        initializeParameters(context, null);
    }

    public DcmView(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        initializeParameters(context, attrs);
    }

    public DcmView(android.content.Context context, android.util.AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeParameters(context, attrs);
    }

    public void setDrawOverlayListener(DrawOverlayListener listener) {
        mDrawOverlayListener = listener;
    }

    public void setDrawFixedOverlayListener(DrawFixedOverlayListener listener) {
        mDrawFixedOverlayListener = listener;
    }

    public void setOnSingleTapListener(OnSingleTapListener listener) {
        mOnSingleTapTapListener = listener;
    }

    /**
     * 设置图像和转换链.
     * 
     * @param image
     *            图像
     * @param transforms
     *            转换链.
     */
    public void setImage(Image image, TransformsChain transforms) {
        if (mImage == null || image.getSizeY() != mImage.getSizeY() || image.getSizeX() != mImage.getSizeX()) {
            mZoomScale = 1.0f;
        }
        mImage = image;
        mTransformsChain = transforms;
        mDrawBitmap = new DrawBitmap(image, transforms);
        mDrawBitmapCounter++;
        renderTiles();

        mOriginalZoomScale = getOriginalZoomScale();
    }

    public void enableScaling(boolean bEnabled) {
        mBScaleEnabled = bEnabled;
    }

    public void enablePanning(boolean bEnabled) {
        mBPanEnabled = bEnabled;
    }

    public void setZoomPanable(boolean bEnable) {
        mBPanEnabled = true;
        mBScaleEnabled = bEnable;
        mBPanMoveEnable = bEnable;
    }

    public void setSingleTapable(boolean bEnable) {
        mBSingleTapEnable = bEnable;
    }

    /**
     * attached to window 开始渲染线程.
     */
    protected void onAttachedToWindow() {
        mBFirstDraw = true;

        if (mRenderTilesThread == null) {
            mRenderTilesThread = new Thread(new RenderImage());
            mRenderTilesThread.start();
        }
    }

    /**
     * detached from window 结束渲染线程.
     */
    protected void onDetachedFromWindow() {
        synchronized (mRenderTileRequests) {
            mBTerminate = true;
            mRenderTileRequests.clear();
            mRenderTileRequests.notifyAll();
        }
        mTiles.clear();
        try {
            mRenderTilesThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mRenderTilesThread = null;
    }

    /**
     * 初始化.
     * 
     * @param context
     *            上写文
     * @param attrs
     *            属性
     */
    protected void initializeParameters(android.content.Context context, android.util.AttributeSet attrs) {
        DicomViewGestureDetector gestureListener = new DicomViewGestureDetector();
        mScaleGestureDetector = new ScaleGestureDetector(context, gestureListener);
        mScrollGestureDetector = new GestureDetector(context, gestureListener);

        if (mBackgroundPaint == null) {
            TypedArray privateAttributes = context.getTheme()
                    .obtainStyledAttributes(attrs, R.styleable.DcmView, 0, 0);

            int backgroundColor = privateAttributes == null ? 0xff000000 : privateAttributes.getColor(
                    R.styleable.DcmView_backgroundColor, 0xff000000) & 0xffffffff;

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(Color.BLACK);
            mBackgroundPaint.setStyle(Paint.Style.FILL);
            mBackgroundPaint.setStrokeWidth(0);
        }

        Typeface textFont = Typeface.create(Typeface.SERIF, Typeface.ITALIC);

        if (mTextPaint == null) {
            mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
            mTextPaint.setColor(getResources().getColor(R.color.yellow_deep));
            mTextPaint.setTextSize(getResources().getDimension(R.dimen.text_size_10));
            mTextPaint.setTextAlign(Align.LEFT);
            mTextPaint.setTypeface(textFont);
            // mTextPaint.setAntiAlias(true);
        }

        mTextMargin = UiUtil.dip2px(getContext(), 10);
        mTextSpace = UiUtil.dip2px(getContext(), 5);

        if (mMetricPaint == null) {
            mMetricPaint = new Paint();
            mMetricPaint.setDither(true);
            mMetricPaint.setAntiAlias(true);
            mMetricPaint.setStrokeWidth(2);
            mMetricPaint.setColor(getResources().getColor(R.color.red));
            mMetricPaint.setStyle(Paint.Style.FILL);
        }

        mMetricMargin = UiUtil.dip2px(getContext(), 10);

        if (mOrientationPaint == null) {
            mOrientationPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
            mOrientationPaint.setColor(getResources().getColor(R.color.yellow_deep));
            mOrientationPaint.setTextSize(getResources().getDimension(R.dimen.text_size_12));
            mOrientationPaint.setTypeface(textFont);
            mOrientationPaint.setTextAlign(Align.LEFT);
        }

        if (mLoadPaint == null) {
            mLoadPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
            mLoadPaint.setColor(getResources().getColor(R.color.white));
            mLoadPaint.setTextSize(getResources().getDimension(R.dimen.text_size_14));
            mLoadPaint.setTextAlign(Align.CENTER);
            // mTextPaint.setAntiAlias(true);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mDrawLine != null) {
                if (mDrawLine.getMoved()) {
                    mLineStatck.addLine(mDrawLine);
                    mLineStatck.setTappedLine(mDrawLine);
                }
                mDrawLine = null;
            }
        }

        return (mBPanEnabled && mScrollGestureDetector.onTouchEvent(event))
                | (mBScaleEnabled && mScaleGestureDetector.onTouchEvent(event));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBFirstDraw) {
            renderTiles();
            mBFirstDraw = false;
        }

        RectF paintBackground = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.drawRect(paintBackground, mBackgroundPaint);

        switch (mRotateAngle) {
        case ANGLE_0:
            canvas.rotate(0);
            canvas.translate(0, 0);
            break;
        case ANGLE_90:
            canvas.rotate(90);
            canvas.translate(0, -getWidth());
            break;
        case ANGLE_180:
            canvas.rotate(180);
            canvas.translate(-getWidth(), -getHeight());
            break;
        case ANGLE_270:
            canvas.rotate(270);
            canvas.translate(-getHeight(), 0);
            break;
        default:
            break;
        }

        canvas.translate(mBitmapOffsetX - mTopLeftX, mBitmapOffsetY - mTopLeftY);

        if (mImage == null) {
            return;
        }

        updateCoordinates();

        // Draw full bitmap
        if (mFullDrawBitmapCounter != mDrawBitmapCounter) {
            mFullBitmap = null;
        }
        if (mFullBitmap != null) {
//            canvas.drawBitmap(mFullBitmap, null, new Rect(0, 0, mTotalWidth, mTotalHeight), null);
        } else {
            canvas.drawText("加载中...", mTotalWidth / 2, mTotalHeight / 2, mLoadPaint);
        }

        // 需要删除的碎片.
        List<TileInfo> tilesToRemove = new ArrayList<TileInfo>();
        List<TileInfo> differentSizeTiles = new ArrayList<TileInfo>();

        // 区域需求的碎片.
        int numTiles = 0;
        for (int scanY = 0; scanY < mTotalHeight; scanY += mTileSizeY) {
            if (scanY >= mBottomRightY || scanY + mTileSizeY <= mTopLeftY) {
                continue;
            }
            for (int scanX = 0; scanX < mTotalWidth; scanX += mTileSizeX) {
                if (scanX < mBottomRightX && scanX + mTileSizeX > mTopLeftX) {
                    ++numTiles;
                }

            }
        }

        int numCorrectTiles = 0;
        for (TileInfo tile : mTiles) {
            Rect displayPosition = findTileDisplayPosition(tile);

            if (displayPosition.right <= mTopLeftX || displayPosition.bottom <= mTopLeftY
                    || displayPosition.left >= mBottomRightX || displayPosition.top >= mBottomRightY
                    || tile.drawBitmapCounter != mDrawBitmapCounter) {
                tilesToRemove.add(tile);
                continue;
            }

            if (tile.totalWidth != mTotalWidth || tile.totalHeight != mTotalHeight) {
                differentSizeTiles.add(tile);
            } else if (tile.topLeftX < mBottomRightX && tile.topLeftY < mBottomRightY && tile.bottomRightX > mTopLeftX
                    && tile.bottomRightY > mTopLeftY) {
                numCorrectTiles++;
            }

            canvas.drawBitmap(tile.bitmap, null, displayPosition, null);
        }

        mTiles.removeAll(tilesToRemove);
        if (numCorrectTiles == numTiles) {
            mTiles.removeAll(differentSizeTiles);
        }

        drawLines(canvas);

        if (mDrawOverlayListener != null) {
            mDrawOverlayListener.onDrawOverlay(canvas, mTotalWidth, mTotalHeight, mImage);
        }

        canvas.translate(mTopLeftX - mBitmapOffsetX, mTopLeftY - mBitmapOffsetY);

        switch (mRotateAngle) {
        case ANGLE_0:
            canvas.translate(0, 0);
            canvas.rotate(0);
            break;
        case ANGLE_90:
            canvas.translate(0, getWidth());
            canvas.rotate(-90);
            break;
        case ANGLE_180:
            canvas.translate(getWidth(), getHeight());
            canvas.rotate(-180);
            break;
        case ANGLE_270:
            canvas.translate(getHeight(), 0);
            canvas.rotate(-270);
            break;
        default:
            break;
        }

        if (mDrawFixedOverlayListener != null) {
            mDrawFixedOverlayListener.onDrawFixedOverlay(canvas);
        }

        onDrawFixedOverlay(canvas);
    }

    private void drawLines(Canvas canvas) {
        if (mDrawLine != null) {
            mDrawLine.drawSelf(canvas);
        }

        for (Line line : mLineStatck.getLines()) {
            if (!line.getDeleted()) {
                line.drawSelf(canvas);
            }
        }
    }

    protected void onDrawOverlay(Canvas canvas, int totalWidth, int totalHeight, Image image) {

    }

    protected void onDrawFixedOverlay(Canvas canvas) {

        drawLeftTopText(canvas);

        drawRightTopText(canvas);

        drawLeftBottomText(canvas);

        drawRightBottomText(canvas);

        drawMetric(canvas);

        drawOrientation(canvas);
    }

    private void drawLeftTopText(Canvas canvas) {
        mTextPaint.setTextAlign(Align.LEFT);

        String series = "Se:" + mDcm.getSeriesNumber();
        String image = "Im:" + mDcm.getInstanceNumber();

        int height = measureTextHeight(series);

        int left = mTextMargin;
        int top = mTextMargin + height;

        canvas.drawText(series, left, top, mTextPaint);

        top = top + height + mTextSpace;
        canvas.drawText(image, left, top, mTextPaint);
    }

    private void drawRightTopText(Canvas canvas) {
        mTextPaint.setTextAlign(Align.RIGHT);

        String id = mDcm.getPatientID();
        String name = mDcm.getPatientName();
        String sex = mDcm.getPatientSex();
        String age = mDcm.getPatientAge();
        String birth = mDcm.getPatientBirth();
        String institution = mDcm.getInstitutionName();
        String studyDes = mDcm.getStudyDescription();
        String seriesDes = mDcm.getSeriesDescription();

        int height = measureTextHeight(id);

        int left = getWidth() - mTextMargin;
        int top = mTextMargin + height;

        canvas.drawText(id, left, top, mTextPaint);

        top = top + height + mTextSpace;
        canvas.drawText(name, left, top, mTextPaint);

        height = measureTextHeight(name);
        top = top + height + mTextSpace;

        canvas.drawText(sex, left, top, mTextPaint);

        height = measureTextHeight(sex);
        top = top + height + mTextSpace;

        canvas.drawText(age, left, top, mTextPaint);

        height = measureTextHeight(age);
        top = top + height + mTextSpace;

        canvas.drawText(birth, left, top, mTextPaint);

        height = measureTextHeight(birth);
        top = top + height + mTextSpace;

        canvas.drawText(institution, left, top, mTextPaint);

        height = measureTextHeight(institution);
        top = top + height + mTextSpace;

        canvas.drawText(studyDes, left, top, mTextPaint);

        height = measureTextHeight(studyDes);
        top = top + height + mTextSpace;

        canvas.drawText(seriesDes, left, top, mTextPaint);
    }

    private void drawLeftBottomText(Canvas canvas) {
        mTextPaint.setTextAlign(Align.LEFT);

        String wlw = "WL:" + mDcm.getWindowCenter() + " WW:" + mDcm.getWindowWidth();
        String pix = mImage.getSizeX() + "x" + mImage.getSizeY();

        int height;

        int left = mTextMargin;
        int top = getHeight() - mTextMargin;

        canvas.drawText(wlw, left, top, mTextPaint);

        height = measureTextHeight(wlw);
        top = top - mTextSpace - height;

        canvas.drawText(pix, left, top, mTextPaint);
    }

    private void drawRightBottomText(Canvas canvas) {
        mTextPaint.setTextAlign(Align.RIGHT);

        String date = mDcm.getStudyDate() + " " + mDcm.getStudyTime();

        int height;

        int left = getWidth() - mTextMargin;
        int top = getHeight() - mTextMargin;

        canvas.drawText(date, left, top, mTextPaint);

    }

    private int measureTextHeight(String text) {
        Rect rect = new Rect();
        mTextPaint.getTextBounds(text, 0, text.length(), rect);
        return rect.height();
    }

    private void drawMetric(Canvas canvas) {

        float width, height, perW, perH;
        if (mRotateAngle == IDCMRotateAngle.ANGLE_0 || mRotateAngle == IDCMRotateAngle.ANGLE_180) {
            width = (float) (100.0f / mVrw);
            height = (float) (100.0f / mVrh);

            perW = (float) (10.0f / mVrw);
            perH = (float) (10.0f / mVrh);
        } else {
            width = (float) (100.0f / mVrh);
            height = (float) (100.0f / mVrw);

            perW = (float) (10.0f / mVrh);
            perH = (float) (10.0f / mVrw);
        }

        int centerWidth = getWidth() / 2;
        int centerHeight = getHeight() / 2;

        PointF widthCenterPointF = new PointF(centerWidth, getHeight() - mMetricMargin * 4);
        PointF heightCenterPointF = new PointF(mMetricMargin, centerHeight);

        PointF widthStartPointF = new PointF(widthCenterPointF.x - width / 2, widthCenterPointF.y);
        PointF widthEndPointF = new PointF(widthCenterPointF.x + width / 2, widthCenterPointF.y);

        PointF heightStartPointF = new PointF(heightCenterPointF.x, heightCenterPointF.y - height / 2);
        PointF heightEndPointF = new PointF(heightCenterPointF.x, heightCenterPointF.y + height / 2);

        canvas.drawLine(widthStartPointF.x, widthStartPointF.y, widthEndPointF.x, widthEndPointF.y, mMetricPaint);
        canvas.drawLine(heightStartPointF.x, heightStartPointF.y, heightEndPointF.x, heightEndPointF.y, mMetricPaint);

        for (int i = 0; i <= 10; i++) {
            float mW = 5;
            if (i % 5 == 0) {
                mW = 8;
            }

            PointF wPointF = new PointF(widthStartPointF.x + i * perW, widthStartPointF.y);

            PointF hPointF = new PointF(heightStartPointF.x, heightStartPointF.y + i * perH);

            canvas.drawLine(wPointF.x, wPointF.y, wPointF.x, wPointF.y - mW, mMetricPaint);
            canvas.drawLine(hPointF.x, hPointF.y, hPointF.x + mW, hPointF.y, mMetricPaint);
        }

    }

    private void drawOrientation(Canvas canvas) {

        String[] orientation = mDcm.getOrientation();

        String[] rotateOri = new String[4];
        switch (mRotateAngle) {
        case ANGLE_0:
            rotateOri[0] = orientation[0];
            rotateOri[1] = orientation[1];
            rotateOri[2] = orientation[2];
            rotateOri[3] = orientation[3];
            break;
        case ANGLE_90:
            rotateOri[0] = orientation[3];
            rotateOri[1] = orientation[2];
            rotateOri[2] = orientation[0];
            rotateOri[3] = orientation[1];
            break;
        case ANGLE_180:
            rotateOri[0] = orientation[1];
            rotateOri[1] = orientation[0];
            rotateOri[2] = orientation[3];
            rotateOri[3] = orientation[2];
            break;
        case ANGLE_270:
            rotateOri[0] = orientation[2];
            rotateOri[1] = orientation[3];
            rotateOri[2] = orientation[1];
            rotateOri[3] = orientation[0];
            break;
        default:
            rotateOri[0] = orientation[0];
            rotateOri[1] = orientation[1];
            rotateOri[2] = orientation[2];
            rotateOri[3] = orientation[3];
            break;
        }

        int centerWidth = getWidth() / 2;
        int centerHeight = getHeight() / 2;

        int x, y;

        // left
        mOrientationPaint.setTextAlign(Align.LEFT);

        x = mTextMargin;
        y = centerHeight;
        canvas.drawText(rotateOri[0], x, y, mOrientationPaint);

        // right
        mOrientationPaint.setTextAlign(Align.RIGHT);

        x = getWidth() - mTextMargin;
        y = centerHeight;
        canvas.drawText(rotateOri[1], x, y, mOrientationPaint);

        // top
        mOrientationPaint.setTextAlign(Align.LEFT);

        x = centerWidth;
        y = mTextMargin * 6;
        canvas.drawText(rotateOri[2], x, y, mOrientationPaint);

        // bottom
        mOrientationPaint.setTextAlign(Align.LEFT);

        x = centerWidth;
        y = getHeight() - mTextMargin * 6;
        canvas.drawText(rotateOri[3], x, y, mOrientationPaint);

    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int reqWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int reqHeight = View.MeasureSpec.getSize(heightMeasureSpec);

        int minimumWidth = getSuggestedMinimumWidth();
        int minimumHeight = getSuggestedMinimumHeight();
        if (reqWidth < minimumWidth) {
            reqWidth = minimumWidth;
        }
        if (reqHeight < minimumHeight) {
            reqHeight = minimumHeight;
        }

        setMeasuredDimension(reqWidth, reqHeight);
    }

    /**
     * 队列请求生成所有的碎片要填满屏幕.
     */
    protected void renderTiles() {
        synchronized (mRenderTileRequests) {
            if (mImage == null || mImage.getSizeX() == 0 || mImage.getSizeY() == 0) {
                mTiles.clear();
                return;
            }

            updateCoordinates();

            if (mFullDrawBitmapCounter != mDrawBitmapCounter) {
                mFullBitmap = null;
            }

            // 删除之前的碎片，除了碎片集合.
            mRenderTileRequests.clear();

            if (mFullBitmap == null && mRenderFullDrawBitmapCounter != mDrawBitmapCounter) {
                WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                int displaySize = StrictMath.max(display.getWidth(), display.getHeight());
                int imageWidth = mImage.getSizeX();
                int imageHeight = mImage.getSizeY();
                float imageRatio = (float) imageWidth / (float) imageHeight;
                int fullSizeWidth;
                int fullSizeHeight;
                fullSizeWidth = display.getWidth();
                fullSizeHeight = (int) (fullSizeWidth / imageRatio + .5f);
                // if (imageHeight < imageWidth) {
                // fullSizeHeight = displaySize;
                // fullSizeWidth = (int) (fullSizeHeight * imageRatio + .5f);
                // } else {
                // fullSizeWidth = displaySize;
                // fullSizeHeight = (int) (fullSizeWidth / imageRatio + .5f);
                // }

                mFullImageRenderRequest = new RenderTileRequest(mDrawBitmap, mDrawBitmapCounter, fullSizeWidth,
                        fullSizeHeight, 0, 0, fullSizeWidth, fullSizeHeight, true);
                mRenderFullDrawBitmapCounter = mDrawBitmapCounter;
            }

            for (int scanY = 0; scanY < mTotalHeight; scanY += mTileSizeY) {
                if (scanY >= mBottomRightY || scanY + mTileSizeY <= mTopLeftY) {
                    continue;
                }
                int bottomRightY = StrictMath.min(mTotalHeight, scanY + mTileSizeY);

                for (int scanX = 0; scanX < mTotalWidth; scanX += mTileSizeX) {
                    if (scanX >= mBottomRightX || scanX + mTileSizeX <= mTopLeftX) {
                        continue;
                    }

                    int bottomRightX = StrictMath.min(mTotalWidth, scanX + mTileSizeX);

                    // 检查请求是否存在.
                    boolean bFound = false;
                    for (TileInfo tile : mTiles) {
                        if (tile.totalWidth == mTotalWidth && tile.totalHeight == mTotalHeight
                                && tile.topLeftX == scanX && tile.topLeftY == scanY
                                && tile.bottomRightX == bottomRightX && tile.bottomRightY == bottomRightY
                                && tile.drawBitmapCounter == mDrawBitmapCounter) {
                            bFound = true;
                            break;
                        }
                    }
                    if (bFound) {
                        continue;
                    }

                    mRenderTileRequests.add(new RenderTileRequest(mDrawBitmap, mDrawBitmapCounter, mTotalWidth,
                            mTotalHeight, scanX, scanY, bottomRightX, bottomRightY, false));
                }
            }
            mRenderTileRequests.notifyAll();
        }

    }

    /**
     * bitmap准备被渲染时调用.
     * 
     * @param totalWidth
     * @param totalHeight
     * @param topLeftX
     * @param topLeftY
     * @param bottomRightX
     * @param bottomRightY
     * @param bitmap
     */
    private void renderImageReady(long drawBitmapCounter, int totalWidth, int totalHeight, int topLeftX, int topLeftY,
            int bottomRightX, int bottomRightY, Bitmap bitmap, boolean bFullBitmap) {
        if (drawBitmapCounter != mDrawBitmapCounter) {
            return;
        }
        if (bFullBitmap) {
            if (bitmap != mFullBitmap) {
                mFullBitmap = bitmap;
                mFullDrawBitmapCounter = drawBitmapCounter;
                invalidate();
            }
            return;
        }
        if (totalWidth != mTotalWidth || totalHeight != mTotalHeight) {
            invalidate();
            return;
        }

        // 检查碎片是否存在.
        TileInfo tile = null;
        for (TileInfo tileInfo : mTiles) {
            if (tileInfo.topLeftX == topLeftX && tileInfo.topLeftY == topLeftY && tileInfo.bottomRightX == bottomRightX
                    && tileInfo.bottomRightY == bottomRightY && tileInfo.totalWidth == totalWidth
                    && tileInfo.totalHeight == totalHeight && tileInfo.drawBitmapCounter == drawBitmapCounter) {
                tile = tileInfo;
                break;
            }
        }
        if (tile == null) {
            tile = new TileInfo(drawBitmapCounter, totalWidth, totalHeight, topLeftX, topLeftY, bottomRightX,
                    bottomRightY, bitmap);
            mTiles.add(tile);
        }

        Rect invalidateRect = findTileDisplayPosition(tile);
        invalidateRect.offset(mBitmapOffsetX - mTopLeftX, mBitmapOffsetY - mTopLeftY);
        Log.d("invalidate", "left:" + invalidateRect.left + " top:" + invalidateRect.top + " right:"
                + invalidateRect.right + " buttom:" + invalidateRect.bottom);
        invalidate(invalidateRect);
    }

    /**
     * 渲染线程runnable.
     */
    private class RenderImage implements Runnable {
        private int m_renderBuffer[] = new int[4096];

        public void run() {
            Handler looperHandler = new Handler(DcmView.this.getContext().getMainLooper());

            // 循环知道请求到达.
            for (;;) {
                // 获取下一个渲染请求.
                RenderTileRequest renderRequest;
                synchronized (mRenderTileRequests) {
                    if (!mBTerminate && mRenderTileRequests.isEmpty() && mFullImageRenderRequest == null) {
                        try {
                            mRenderTileRequests.wait();
                        } catch (InterruptedException e) {

                        }
                    }
                    if (mBTerminate) {
                        return;
                    }
                    renderRequest = mFullImageRenderRequest;
                    mFullImageRenderRequest = null;
                    if (renderRequest == null && !mRenderTileRequests.isEmpty()) {
                        renderRequest = mRenderTileRequests.remove(0);
                    }
                    if (renderRequest == null) {
                        continue;
                    }
                }
                int requiredSize = renderRequest.drawBitmap.getBitmap(renderRequest.totalWidth,
                        renderRequest.totalHeight, renderRequest.topLeftX, renderRequest.topLeftY,
                        renderRequest.bottomRightX, renderRequest.bottomRightY, m_renderBuffer, 0);
                if (requiredSize == 0) {
                    continue;
                }
                if (m_renderBuffer.length < requiredSize) {
                    m_renderBuffer = new int[requiredSize];
                }
                renderRequest.drawBitmap.getBitmap(renderRequest.totalWidth, renderRequest.totalHeight,
                        renderRequest.topLeftX, renderRequest.topLeftY, renderRequest.bottomRightX,
                        renderRequest.bottomRightY, m_renderBuffer, m_renderBuffer.length);

                Bitmap renderBitmap = Bitmap.createBitmap(m_renderBuffer, renderRequest.bottomRightX
                        - renderRequest.topLeftX, renderRequest.bottomRightY - renderRequest.topLeftY,
                        Bitmap.Config.ARGB_8888);
                if (renderBitmap == null) {
                    return;
                }
                looperHandler
                        .post(new CallRenderImageReady(renderRequest.drawBitmapCounter, renderRequest.totalWidth,
                                renderRequest.totalHeight, renderRequest.topLeftX, renderRequest.topLeftY,
                                renderRequest.bottomRightX, renderRequest.bottomRightY, renderBitmap,
                                renderRequest.bFullBitmap));
            }
        }
    }

    /**
     * 在主线程渲染单个碎片runnabe.
     */
    private class CallRenderImageReady implements Runnable {
        private int     m_totalWidth, m_totalHeight, m_topLeftX, m_topLeftY, m_bottomRightX, m_bottomRightY;
        private long    m_drawBitmapCounter;
        private Bitmap  m_bitmap;
        private boolean m_bFullBitmap;

        public CallRenderImageReady(long drawBitmapCounter, int totalWidth, int totalHeight, int topLeftX,
                int topLeftY, int bottomRightX, int bottomRightY, Bitmap bitmap, boolean bFullBitmap) {
            m_drawBitmapCounter = drawBitmapCounter;
            m_totalWidth = totalWidth;
            m_totalHeight = totalHeight;
            m_topLeftX = topLeftX;
            m_topLeftY = topLeftY;
            m_bottomRightX = bottomRightX;
            m_bottomRightY = bottomRightY;
            m_bitmap = bitmap;
            m_bFullBitmap = bFullBitmap;
        }

        public void run() {
            DcmView.this.renderImageReady(m_drawBitmapCounter, m_totalWidth, m_totalHeight, m_topLeftX, m_topLeftY,
                    m_bottomRightX, m_bottomRightY, m_bitmap, m_bFullBitmap);
        }
    }

    /**
     * 得到碎片位置，不考虑图像位移.
     * 
     * @param tile
     *            碎片
     * @return the rect
     */
    private Rect findTileDisplayPosition(TileInfo tile) {
        int topLeftX = tile.topLeftX;
        int topLeftY = tile.topLeftY;
        int bottomRightX = tile.bottomRightX;
        int bottomRightY = tile.bottomRightY;

        if (tile.totalWidth != mTotalWidth || tile.totalHeight != mTotalHeight) {
            float ratioSizeX = (float) mTotalWidth / (float) tile.totalWidth;
            float ratioSizeY = (float) mTotalHeight / (float) tile.totalHeight;
            topLeftX = (int) ((float) topLeftX * ratioSizeX + 0.5f);
            topLeftY = (int) ((float) topLeftY * ratioSizeY + 0.5f);
            bottomRightX = (int) ((float) bottomRightX * ratioSizeX + 0.5f);
            bottomRightY = (int) ((float) bottomRightY * ratioSizeY + 0.5f);
        }

        return new Rect(topLeftX, topLeftY, bottomRightX, bottomRightY);

    }

    public void setRotateAngle(IDCMRotateAngle angle) {
        this.mRotateAngle = angle;
        this.mZoomScale = 1.0f;
        this.mCenterPointX = 0.5f;
        this.mCenterPointY = 0.5f;

        mOriginalZoomScale = getOriginalZoomScale();

        update();
    }

    public float getOriginalZoomScale() {
        float originalZoomScale = 1.0f;
        int imageWidth = mImage.getSizeX();
        int imageHeight = mImage.getSizeY();
        float imageRatio = (float) imageWidth / (float) imageHeight;
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        float viewRatio = (float) viewWidth / (float) viewHeight;

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        viewWidth = display.getWidth();
        viewHeight = display.getHeight();

        viewRatio = (float) viewWidth / (float) viewHeight;

        float baseImageWidth;
        switch (mRotateAngle) {
        case ANGLE_0:
        case ANGLE_180:
            if (imageRatio > viewRatio) {
                baseImageWidth = viewWidth;
            } else {
                baseImageWidth = (float) viewHeight * imageRatio;
            }
            originalZoomScale = baseImageWidth / imageWidth;
            break;
        case ANGLE_90:
        case ANGLE_270:
            viewRatio = (float) viewHeight / (float) viewWidth;

            if (imageRatio > viewRatio) {
                baseImageWidth = viewHeight;
            } else {
                baseImageWidth = (float) viewWidth * imageRatio;
            }
            originalZoomScale = baseImageWidth / imageWidth;
            break;
        default:
            break;
        }

        return originalZoomScale;
    }

    /**
     * 更新坐标、缩放变量.
     */
    private void updateCoordinates() {
        mBitmapOffsetX = 0;
        mBitmapOffsetY = 0;

        int imageWidth = mImage.getSizeX();
        int imageHeight = mImage.getSizeY();
        float imageRatio = (float) imageWidth / (float) imageHeight;
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        float viewRatio = (float) viewWidth / (float) viewHeight;

        float baseImageWidth = 0.0f, baseImageHeight = 0.0f; // bitmap size with
                                                             // zoom = 1
        switch (mRotateAngle) {
        case ANGLE_0:
            if (imageRatio > viewRatio) {
                baseImageWidth = viewWidth;
                baseImageHeight = (float) viewWidth / imageRatio;
            } else {
                baseImageHeight = viewHeight;
                baseImageWidth = (float) viewHeight * imageRatio;
            }

            baseImageWidth *= mZoomScale;
            baseImageHeight *= mZoomScale;

            mTotalWidth = (int) (baseImageWidth + .5f);
            mTotalHeight = (int) (baseImageHeight + .5f);

            // 横向坐标.
            if (viewWidth > mTotalWidth) {
                mBitmapOffsetX = (viewWidth - mTotalWidth) / 2;
                mCenterPointX = 0.5f;
                mTopLeftX = 0;
                mBottomRightX = mTotalWidth;
            } else {
                float centerPointX = baseImageWidth * mCenterPointX;
                mTopLeftX = (int) (centerPointX + .5f) - viewWidth / 2;
                if (mTopLeftX < 0) {
                    mTopLeftX = 0;
                    mCenterPointX = 0.5f * (float) viewWidth / (float) mTotalWidth;
                }
                mBottomRightX = mTopLeftX + viewWidth;
                if (mBottomRightX > mTotalWidth) {
                    mBottomRightX = mTotalWidth;
                    mTopLeftX = mBottomRightX - viewWidth;
                    mCenterPointX = 1f - 0.5f * (float) viewWidth / (float) mTotalWidth;
                }
            }

            // 纵向坐标.
            if (viewHeight > mTotalHeight) {
                mBitmapOffsetY = (viewHeight - mTotalHeight) / 2;
                mCenterPointY = 0.5f;
                mTopLeftY = 0;
                mBottomRightY = mTotalHeight;
            } else {
                float centerPointY = baseImageHeight * mCenterPointY;
                mTopLeftY = (int) (centerPointY + .5f) - viewHeight / 2;

                if (mTopLeftY < 0) {
                    mTopLeftY = 0;
                    mCenterPointY = 0.5f * (float) viewHeight / (float) mTotalHeight;
                }
                mBottomRightY = mTopLeftY + viewHeight;
                if (mBottomRightY > mTotalHeight) {
                    mBottomRightY = mTotalHeight;
                    mTopLeftY = mBottomRightY - viewHeight;
                    mCenterPointY = 1f - 0.5f * (float) viewHeight / (float) mTotalHeight;
                }
            }
            break;
        case ANGLE_90:
            viewRatio = (float) viewHeight / (float) viewWidth;

            if (imageRatio > viewRatio) {
                baseImageWidth = viewHeight;
                baseImageHeight = (float) viewHeight / imageRatio;
            } else {
                baseImageHeight = viewWidth;
                baseImageWidth = (float) viewWidth * imageRatio;
            }

            baseImageWidth *= mZoomScale;
            baseImageHeight *= mZoomScale;

            mTotalWidth = (int) (baseImageWidth + .5f);
            mTotalHeight = (int) (baseImageHeight + .5f);

            // 横向坐标.
            if (viewHeight > mTotalWidth) {
                mBitmapOffsetX = (viewHeight - mTotalWidth) / 2;
                mCenterPointX = 0.5f;
                mTopLeftX = 0;
                mBottomRightX = mTotalWidth;
            } else {
                float centerPointX = baseImageWidth * mCenterPointX;
                mTopLeftX = (int) (centerPointX + .5f) - viewHeight / 2;
                if (mTopLeftX < 0) {
                    mTopLeftX = 0;
                    mCenterPointX = 0.5f * (float) viewHeight / (float) mTotalWidth;
                }
                mBottomRightX = mTopLeftX + viewHeight;
                if (mBottomRightX > mTotalWidth) {
                    mBottomRightX = mTotalWidth;
                    mTopLeftX = mBottomRightX - viewHeight;
                    mCenterPointX = 1f - 0.5f * (float) viewHeight / (float) mTotalWidth;
                }
            }

            // 纵向坐标.
            if (viewWidth > mTotalHeight) {
                mBitmapOffsetY = (viewWidth - mTotalHeight) / 2;
                mCenterPointY = 0.5f;
                mTopLeftY = 0;
                mBottomRightY = mTotalHeight;
            } else {
                float centerPointY = baseImageHeight * mCenterPointY;
                mTopLeftY = (int) (centerPointY + .5f) - viewWidth / 2;

                if (mTopLeftY < 0) {
                    mTopLeftY = 0;
                    mCenterPointY = 0.5f * (float) viewWidth / (float) mTotalHeight;
                }
                mBottomRightY = mTopLeftY + viewWidth;
                if (mBottomRightY > mTotalHeight) {
                    mBottomRightY = mTotalHeight;
                    mTopLeftY = mBottomRightY - viewWidth;
                    mCenterPointY = 1f - 0.5f * (float) viewWidth / (float) mTotalHeight;
                }
            }

            break;
        case ANGLE_180:
            if (imageRatio > viewRatio) {
                baseImageWidth = viewWidth;
                baseImageHeight = (float) viewWidth / imageRatio;
            } else {
                baseImageHeight = viewHeight;
                baseImageWidth = (float) viewHeight * imageRatio;
            }

            baseImageWidth *= mZoomScale;
            baseImageHeight *= mZoomScale;

            mTotalWidth = (int) (baseImageWidth + .5f);
            mTotalHeight = (int) (baseImageHeight + .5f);

            // 横向坐标.
            if (viewWidth > mTotalWidth) {
                mBitmapOffsetX = (viewWidth - mTotalWidth) / 2;
                mCenterPointX = 0.5f;
                mTopLeftX = 0;
                mBottomRightX = mTotalWidth;
            } else {
                float centerPointX = baseImageWidth * mCenterPointX;
                mTopLeftX = (int) (centerPointX + .5f) - viewWidth / 2;
                if (mTopLeftX < 0) {
                    mTopLeftX = 0;
                    mCenterPointX = 0.5f * (float) viewWidth / (float) mTotalWidth;
                }
                mBottomRightX = mTopLeftX + viewWidth;
                if (mBottomRightX > mTotalWidth) {
                    mBottomRightX = mTotalWidth;
                    mTopLeftX = mBottomRightX - viewWidth;
                    mCenterPointX = 1f - 0.5f * (float) viewWidth / (float) mTotalWidth;
                }
            }

            // 纵向坐标.
            if (viewHeight > mTotalHeight) {
                mBitmapOffsetY = (viewHeight - mTotalHeight) / 2;
                mCenterPointY = 0.5f;
                mTopLeftY = 0;
                mBottomRightY = mTotalHeight;
            } else {
                float centerPointY = baseImageHeight * mCenterPointY;
                mTopLeftY = (int) (centerPointY + .5f) - viewHeight / 2;

                if (mTopLeftY < 0) {
                    mTopLeftY = 0;
                    mCenterPointY = 0.5f * (float) viewHeight / (float) mTotalHeight;
                }
                mBottomRightY = mTopLeftY + viewHeight;
                if (mBottomRightY > mTotalHeight) {
                    mBottomRightY = mTotalHeight;
                    mTopLeftY = mBottomRightY - viewHeight;
                    mCenterPointY = 1f - 0.5f * (float) viewHeight / (float) mTotalHeight;
                }
            }
            break;
        case ANGLE_270:
            viewRatio = (float) viewHeight / (float) viewWidth;

            if (imageRatio > viewRatio) {
                baseImageWidth = viewHeight;
                baseImageHeight = (float) viewHeight / imageRatio;
            } else {
                baseImageHeight = viewWidth;
                baseImageWidth = (float) viewWidth * imageRatio;
            }

            baseImageWidth *= mZoomScale;
            baseImageHeight *= mZoomScale;

            mTotalWidth = (int) (baseImageWidth + .5f);
            mTotalHeight = (int) (baseImageHeight + .5f);

            // 横向坐标.
            if (viewHeight > mTotalWidth) {
                mBitmapOffsetX = (viewHeight - mTotalWidth) / 2;
                mCenterPointX = 0.5f;
                mTopLeftX = 0;
                mBottomRightX = mTotalWidth;
            } else {
                float centerPointX = baseImageWidth * mCenterPointX;
                mTopLeftX = (int) (centerPointX + .5f) - viewHeight / 2;
                if (mTopLeftX < 0) {
                    mTopLeftX = 0;
                    mCenterPointX = 0.5f * (float) viewHeight / (float) mTotalWidth;
                }
                mBottomRightX = mTopLeftX + viewHeight;
                if (mBottomRightX > mTotalWidth) {
                    mBottomRightX = mTotalWidth;
                    mTopLeftX = mBottomRightX - viewHeight;
                    mCenterPointX = 1f - 0.5f * (float) viewHeight / (float) mTotalWidth;
                }
            }

            // 纵向坐标.
            if (viewWidth > mTotalHeight) {
                mBitmapOffsetY = (viewWidth - mTotalHeight) / 2;
                mCenterPointY = 0.5f;
                mTopLeftY = 0;
                mBottomRightY = mTotalHeight;
            } else {
                float centerPointY = baseImageHeight * mCenterPointY;
                mTopLeftY = (int) (centerPointY + .5f) - viewWidth / 2;

                if (mTopLeftY < 0) {
                    mTopLeftY = 0;
                    mCenterPointY = 0.5f * (float) viewWidth / (float) mTotalHeight;
                }
                mBottomRightY = mTopLeftY + viewWidth;
                if (mBottomRightY > mTotalHeight) {
                    mBottomRightY = mTotalHeight;
                    mTopLeftY = mBottomRightY - viewWidth;
                    mCenterPointY = 1f - 0.5f * (float) viewWidth / (float) mTotalHeight;
                }
            }

            break;

        default:
            break;
        }

        double rWidth = mDcm.getAbsoultWidth();
        double rHeight = mDcm.getAbsoultHeight();
        mVrw = rWidth / baseImageWidth;
        mVrh = rHeight / baseImageHeight;

    }

    /**
     * 碎片信息类.
     */
    private class TileInfo {
        public TileInfo(long drawBitmapCounter, int totalWidth, int totalHeight, int topLeftX, int topLeftY,
                int bottomRightX, int bottomRightY, Bitmap renderedBitmap) {
            this.drawBitmapCounter = drawBitmapCounter;
            this.totalWidth = totalWidth;
            this.totalHeight = totalHeight;
            this.topLeftX = topLeftX;
            this.topLeftY = topLeftY;
            this.bottomRightX = bottomRightX;
            this.bottomRightY = bottomRightY;
            this.bitmap = renderedBitmap;
        }

        public int    totalWidth, totalHeight, topLeftX, topLeftY, bottomRightX, bottomRightY;
        public long   drawBitmapCounter;
        public Bitmap bitmap;
    }

    /**
     * 碎片请求类.
     */
    private class RenderTileRequest {
        public DrawBitmap drawBitmap;
        public long       drawBitmapCounter;
        public int        totalWidth, totalHeight, topLeftX, topLeftY, bottomRightX, bottomRightY;
        public boolean    bFullBitmap;

        public RenderTileRequest(DrawBitmap drawBitmap, long drawBitmapCounter, int totalWidth, int totalHeight,
                int topLeftX, int topLeftY, int bottomRightX, int bottomRightY, boolean bFullBitmap) {
            this.drawBitmap = drawBitmap;
            this.drawBitmapCounter = drawBitmapCounter;
            this.totalWidth = totalWidth;
            this.totalHeight = totalHeight;
            this.topLeftX = topLeftX;
            this.topLeftY = topLeftY;
            this.bottomRightX = bottomRightX;
            this.bottomRightY = bottomRightY;
            this.bFullBitmap = bFullBitmap;
        }
    }

    private float   mDownX, mDownY, mMoveX, mMoveY;

    private Line    mDrawLine;
    private int     mPointInEdge   = 0;
    private boolean mBInTappedLine = false;
    private boolean mBMeasureLine  = false;
    private boolean mBLineEdit     = false;

    public void setMeasureLine(boolean measure) {
        this.mBMeasureLine = measure;
    }

    public void setLineEdit(boolean edit) {
        this.mBLineEdit = edit;
    }

    /**
     * 手势侦测类.
     */
    private class DicomViewGestureDetector implements ScaleGestureDetector.OnScaleGestureListener,
            GestureDetector.OnGestureListener {
        public boolean onDown(MotionEvent e) {
            if (!mBPanMoveEnable) {
                return true;
            }

            mDownX = e.getX();
            mDownY = e.getY();

            PointF dicomPointF = viewPointF2DicomPointF(new PointF(mDownX, mDownY));

            mPointInEdge = mLineStatck.pointInTappedLineEdge(dicomPointF.x, dicomPointF.y);
            mBInTappedLine = mLineStatck.pointInTappedLine(dicomPointF.x, dicomPointF.y);

            if (mBMeasureLine && mPointInEdge == 0 && !mBInTappedLine && e.getPointerCount() <= 1 && !mBLineEdit) {
                mDrawLine = new Line(DcmView.this.getContext());
                mDrawLine.updateVrwh(mVrw, mVrh);
                mDrawLine.initZoom(mZoomScale, mOriginalZoomScale);

                mDrawLine.setTapped(true);
                mDrawLine.touchDown(dicomPointF.x, dicomPointF.y);

                mLineStatck.clearLineTapped();
                invalidate();
            }

            return true;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        public void onLongPress(MotionEvent e) {
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!mBPanMoveEnable) {
                return false;
            }

            mMoveX = e2.getX();
            mMoveY = e2.getY();

            if (mDrawLine != null) {
                PointF dicomPointF = viewPointF2DicomPointF(new PointF(mMoveX, mMoveY));
                mDrawLine.touchMove(dicomPointF.x, dicomPointF.y);
            }

            PointF coordinateDistanceF = screenDistance2CoordinateDistance(distanceX, distanceY);

            if (mPointInEdge > 0) {
                Line tappedLine = mLineStatck.getTappedLine();
                tappedLine.setOffsetPointF(-coordinateDistanceF.x, -coordinateDistanceF.y, mPointInEdge);
            } else if (mBInTappedLine) {
                Line tappedLine = mLineStatck.getTappedLine();
                tappedLine.setOffsetPointF(-coordinateDistanceF.x, -coordinateDistanceF.y, -1);
            } else if (!mBMeasureLine && !mBLineEdit) {

                if (mTotalWidth == 0 || mTotalHeight == 0) {
                    return false;
                }
                // Convert pixels to float 0...1

                float unifiedDistanceX = coordinateDistanceF.x / (float) mTotalWidth;
                float unifiedDistanceY = coordinateDistanceF.y / (float) mTotalWidth;
                mCenterPointX += unifiedDistanceX;
                mCenterPointY += unifiedDistanceY;
                renderTiles();
            }

            invalidate();
            return true;
        }

        public void onShowPress(MotionEvent e) {
        }

        public boolean onSingleTapUp(MotionEvent e) {
            Line line = null;

            if (!mBScaleEnabled) {

                PointF dicomPointF = viewPointF2DicomPointF(new PointF(e.getX(), e.getY()));
                line = mLineStatck.getTappedLine(dicomPointF.x, dicomPointF.y);
            }
            if (line != null) {
                invalidate();
                if (mOnSingleTapTapListener != null && mBSingleTapEnable) {
                    mOnSingleTapTapListener.onSingleTapLine(DcmView.this);
                }
            } else {
                if (mOnSingleTapTapListener != null && mBSingleTapEnable) {

                    mLineStatck.clearLineTapped();
                    invalidate();
                    mOnSingleTapTapListener.onSingleTap(DcmView.this);
                }
            }

            return true;
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return mDrawBitmap != null;
        }

        public boolean onScale(ScaleGestureDetector detector) {
            mZoomScale *= detector.getScaleFactor();

            // 控制zoom.
            mZoomScale = Math.max(1.0f, Math.min(mZoomScale, 20.0f));

            updateCoordinates();

            updateLinesPointF();

            invalidate();
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
            renderTiles();
        }

    }

    private PointF viewPointF2DicomPointF(PointF viewPointF) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        switch (mRotateAngle) {
        case ANGLE_0:

            break;
        case ANGLE_90:
            float tempX = viewPointF.x;
            viewPointF.x = viewPointF.y;
            viewPointF.y = viewWidth - tempX;
            break;
        case ANGLE_180:
            viewPointF.x = viewWidth - viewPointF.x;
            viewPointF.y = viewHeight - viewPointF.y;
            break;
        case ANGLE_270:
            float tempY = viewPointF.y;
            viewPointF.y = viewPointF.x;
            viewPointF.x = viewHeight - tempY;
            break;

        default:
            break;
        }

        return new PointF(viewPointF.x - (mBitmapOffsetX - mTopLeftX), viewPointF.y - (mBitmapOffsetY - mTopLeftY));
    }

    private PointF screenDistance2CoordinateDistance(float distanceX, float distanceY) {
        switch (mRotateAngle) {
        case ANGLE_0:

            break;
        case ANGLE_90:
            float tempY = distanceY;
            distanceY = -distanceX;
            distanceX = tempY;
            break;
        case ANGLE_180:
            distanceX = -distanceX;
            distanceY = -distanceY;
            break;
        case ANGLE_270:
            float tempX = distanceX;
            distanceX = -distanceY;
            distanceY = tempX;
            break;

        default:
            break;
        }

        return new PointF(distanceX, distanceY);
    }

    private void updateLinesPointF() {

        mLineStatck.updateLinesPointFByZoomScale(mZoomScale, mOriginalZoomScale);
        mLineStatck.updateLinesLengthByRatio(mVrw, mVrh);
        mLineStatck.confirm();
    }

    public void translateAttributesWithRotateAngle() {
        switch (mRotateAngle) {
        case ANGLE_0:
            setRotateAngle(IDCMRotateAngle.ANGLE_90);
            break;
        case ANGLE_90:
            setRotateAngle(IDCMRotateAngle.ANGLE_180);
            break;
        case ANGLE_180:
            setRotateAngle(IDCMRotateAngle.ANGLE_270);
            break;
        case ANGLE_270:
            setRotateAngle(IDCMRotateAngle.ANGLE_0);
            break;
        default:
            break;
        }
    }

    public void resetDicom() {
        mZoomScale = 1.0f;
        mRotateAngle = IDCMRotateAngle.ANGLE_0;
        mCenterPointX = 0.5f;
        mCenterPointY = 0.5f;

        mOriginalZoomScale = getOriginalZoomScale();

        update();
    }

    public void initAttributes(SeriesAttribute attr) {
        mZoomScale = attr.zoom;
        mRotateAngle = attr.angle;
        mCenterPointX = attr.centerPointX;
        mCenterPointY = attr.centerPointY;

        mOriginalZoomScale = getOriginalZoomScale();

        update();
    }

    public void setLineStack(LineStack lineStack) {
        this.mLineStatck = lineStack;
    }

    public void clearLines() {
        mLineStatck.clearLines();
    }

    public void setDcmContent(IDCMContent dcm) {
        this.mDcm = dcm;
    }

    public void deleteTappedLine() {
        mLineStatck.deleteTapedLine();
    }

    public void updateAttributes(SeriesAttribute attr) {
        attr.zoom = mZoomScale;
        attr.angle = mRotateAngle;
        attr.centerPointX = mCenterPointX;
        attr.centerPointY = mCenterPointY;

    }

    public void update() {
        updateCoordinates();

        updateLinesPointF();

        renderTiles();

        invalidate();
    }
}
