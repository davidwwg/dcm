package image.medical.idcm.edit.views;

import image.medical.idcm.R;
import image.medical.idcm.model.IDCMContent;
import image.medical.idcm.model.IDCMSeries;
import image.medical.idcm.util.DCMUtil;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import com.imebra.dicom.DrawBitmap;
import com.imebra.dicom.Image;
import com.imebra.dicom.TransformsChain;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;

public class DcmSnap extends View {

    private Bitmap                    mBitmap;
    private float                     mBitmapOffsetX  = 0.0f, mBitmapOffsetY = 0.0f, mTotalWidth = 0.0f,
            mTotalHeight = 0.0f;

    private Paint                     mBackgroundPaint, mTextPaint;
    private boolean                   mBTerminate     = false;
    private Thread                    mRenderThread;
    final private List<RenderRequest> mRenderRequests = new ArrayList<RenderRequest>();

    private IDCMSeries                mSeries;

    public DcmSnap(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeParameters(context);

    }

    public DcmSnap(Context context) {
        super(context);
        initializeParameters(context);
    }

    private void initializeParameters(Context context) {
        if (mBackgroundPaint == null) {

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(Color.BLACK);
            mBackgroundPaint.setStyle(Paint.Style.FILL);
            mBackgroundPaint.setStrokeWidth(0);
        }

        if (mTextPaint == null) {
            mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
            mTextPaint.setColor(getResources().getColor(R.color.white));
            mTextPaint.setTextSize(getResources().getDimension(R.dimen.text_size_14));
            mTextPaint.setTextAlign(Align.CENTER);
            // mTextPaint.setAntiAlias(true);
        }
    }

    public void initSeries(IDCMSeries series) {
        mSeries = series;

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for (IDCMContent dcm : mSeries.getDcms()) {
                    synchronized (mRenderRequests) {

                        TransformsChain chain = new TransformsChain();

                        Image image = null;

                        if (mSeries.getAttribute().negative) {
                            image = DCMUtil.getNegativeDcmImageTranfromChain(dcm.getDataSet(), chain);
                        } else {
                            image = DCMUtil.getDcmImageTranfromChain(dcm.getDataSet(), chain);
                        }

                        DrawBitmap drawBitmap = new DrawBitmap(image, chain);

                        render(image, drawBitmap);
                    }
                }
            }
        }).start();

    }

    public void play() {
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }

        mBTerminate = false;
        if (mRenderThread == null) {
            mRenderThread = new Thread(new RenderImage());
            mRenderThread.start();
        }

    }

    public void close() {
        synchronized (mRenderRequests) {
            mBTerminate = true;
            mRenderRequests.clear();
            mRenderRequests.notifyAll();
        }
        try {
            mRenderThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mRenderThread = null;

        reset();
    }

    private void reset() {
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }

        mBitmapOffsetX = 0.0f;
        mBitmapOffsetY = 0.0f;
        mTotalWidth = 0.0f;
        mTotalHeight = 0.0f;
    }

    public void render(Image image, DrawBitmap drawBitmap) {
        if (image == null || image.getSizeX() == 0 || image.getSizeY() == 0) {
            return;
        }

        RenderResponse response = updateCoordinates(image);

        RenderRequest renderRequest = new RenderRequest(drawBitmap, response.m_totalWidth, response.m_totalHeight, 0,
                0, response.m_totalWidth, response.m_totalHeight, response);

        mRenderRequests.add(renderRequest);
        mRenderRequests.notifyAll();

    }

    /**
     * 渲染线程runnable.
     */
    private class RenderImage implements Runnable {
        private int m_renderBuffer[] = new int[4096];

        public void run() {
            Handler looperHandler = new Handler(DcmSnap.this.getContext().getMainLooper());

            // 循环知道请求到达.
            for (;;) {
                // 获取下一个渲染请求.
                RenderRequest renderRequest = null;
                synchronized (mRenderRequests) {
                    if (!mBTerminate && mRenderRequests.isEmpty()) {
                        try {
                            mRenderRequests.wait();
                        } catch (InterruptedException e) {

                        }
                    }
                    if (mBTerminate) {
                        return;
                    }

                    if (!mRenderRequests.isEmpty()) {
                        renderRequest = mRenderRequests.remove(0);
                    }

                    if (renderRequest == null) {
                        continue;
                    }
                }
                int requiredSize = renderRequest.m_drawBitmap.getBitmap((int) renderRequest.m_totalWidth,
                        (int) renderRequest.m_totalHeight, (int) renderRequest.m_topLeftX,
                        (int) renderRequest.m_topLeftY, (int) renderRequest.m_bottomRightX,
                        (int) renderRequest.m_bottomRightY, m_renderBuffer, 0);
                if (requiredSize == 0) {
                    continue;
                }
                if (m_renderBuffer.length < requiredSize) {
                    m_renderBuffer = new int[requiredSize];
                }
                renderRequest.m_drawBitmap.getBitmap((int) renderRequest.m_totalWidth,
                        (int) renderRequest.m_totalHeight, (int) renderRequest.m_topLeftX,
                        (int) renderRequest.m_topLeftY, (int) renderRequest.m_bottomRightX,
                        (int) renderRequest.m_bottomRightY, m_renderBuffer, m_renderBuffer.length);

                Bitmap renderBitmap = Bitmap.createBitmap(m_renderBuffer, (int) renderRequest.m_bottomRightX
                        - (int) renderRequest.m_topLeftX, (int) renderRequest.m_bottomRightY
                        - (int) renderRequest.m_topLeftY, Bitmap.Config.ARGB_8888);
                if (renderBitmap == null) {
                    return;
                }

                renderRequest.m_response.m_bitmap = renderBitmap;
                looperHandler.post(new RenderImageReady(renderRequest.m_response));
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        RectF paintBackground = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());
        // canvas.drawRect(paintBackground, mBackgroundPaint);

        canvas.translate(mBitmapOffsetX, mBitmapOffsetY);
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, null, new RectF(0.0f, 0.0f, mTotalWidth, mTotalHeight), null);
        } else {
            canvas.translate(-mBitmapOffsetX, -mBitmapOffsetY);
            canvas.drawText("加载中...", getWidth() / 2, getHeight() / 2, mTextPaint);
        }
    }

    /**
     * 更新坐标、缩放变量.
     */
    private RenderResponse updateCoordinates(Image image) {
        float bitmapOffsetX = 0.0f, bitmapOffsetY = 0.0f, totalWidth = 0.0f, totalHeight = 0.0f;

        int imageWidth = image.getSizeX();
        int imageHeight = image.getSizeY();
        float imageRatio = (float) imageWidth / (float) imageHeight;
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        float viewRatio = (float) viewWidth / (float) viewHeight;

        float baseImageWidth = 0.0f, baseImageHeight = 0.0f; // bitmap size with
                                                             // zoom = 1
        if (imageRatio > viewRatio) {
            baseImageWidth = viewWidth;
            baseImageHeight = (float) viewWidth / imageRatio;
        } else {
            baseImageHeight = viewHeight;
            baseImageWidth = (float) viewHeight * imageRatio;
        }

        totalWidth = baseImageWidth;
        totalHeight = baseImageHeight;

        // 横向坐标.
        if (viewWidth > totalWidth) {
            bitmapOffsetX = (viewWidth - totalWidth) / 2;
        }

        // 纵向坐标.
        if (viewHeight > totalHeight) {
            bitmapOffsetY = (viewHeight - totalHeight) / 2;
        }

        return new RenderResponse(null, totalWidth, totalHeight, bitmapOffsetX, bitmapOffsetY);

    }

    class RenderRequest {
        public RenderResponse m_response;
        public DrawBitmap     m_drawBitmap;
        public float          m_totalWidth, m_totalHeight, m_topLeftX, m_topLeftY, m_bottomRightX, m_bottomRightY;

        public RenderRequest(DrawBitmap drawBitmap, float totalWidth, float totalHeight, float topLeftX,
                float topLeftY, float bottomRightX, float bottomRightY, RenderResponse response) {
            this.m_drawBitmap = drawBitmap;
            this.m_totalWidth = totalWidth;
            this.m_totalHeight = totalHeight;
            this.m_topLeftX = topLeftX;
            this.m_topLeftY = topLeftY;
            this.m_bottomRightX = bottomRightX;
            this.m_bottomRightY = bottomRightY;
            this.m_response = response;
        }
    }

    class RenderResponse {
        public Bitmap m_bitmap;
        public float  m_totalWidth, m_totalHeight, m_bitmapOffsetX, m_bitmapOffsetY;

        public RenderResponse(Bitmap bitmap, float totalWidth, float totalHeight, float bitmapOffsetX,
                float bitmapOffsetY) {
            this.m_bitmap = bitmap;
            this.m_totalWidth = totalWidth;
            this.m_totalHeight = totalHeight;
            this.m_bitmapOffsetX = bitmapOffsetX;
            this.m_bitmapOffsetY = bitmapOffsetY;
        }
    }

    class RenderImageReady implements Runnable {
        public RenderResponse m_response;

        public RenderImageReady(RenderResponse response) {
            m_response = response;
        }

        public void run() {

            if (m_response.m_bitmap == null) {
                return;
            }

            if (m_response.m_bitmap != mBitmap) {
                if (mBitmap != null) {
                    mBitmap.recycle();
                    mBitmap = null;
                }
                mBitmap = m_response.m_bitmap;
                mBitmapOffsetX = m_response.m_bitmapOffsetX;
                mBitmapOffsetY = m_response.m_bitmapOffsetY;
                mTotalWidth = m_response.m_totalWidth;
                mTotalHeight = m_response.m_totalHeight;

                invalidate();
            }

        }
    }

}
