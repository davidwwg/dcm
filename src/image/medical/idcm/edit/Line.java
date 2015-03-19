package image.medical.idcm.edit;

import image.medical.idcm.R;
import image.medical.idcm.util.UiUtil;

import java.io.Serializable;
import java.text.DecimalFormat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

public class Line implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -4465134073233055261L;
    private static float EDGE_WIDTH = 10.f;
    private static float TAP_RANGE = 5.f;
    private Context mContext;

    private float x1, x2, y1, y2;

    private float cx1, cx2, cy1, cy2;

    private boolean mBMoved = false, mBConfirm = false, mBDeleted = false;
    private float mDicomZoom = 1.0f;
    private float mOriginalZoom = 1.0f;

    private double mVrw, mVrh;

    public Line(Context context) {
        this.mContext = context;

        EDGE_WIDTH = UiUtil.dip2px(mContext, 5);
        TAP_RANGE = UiUtil.dip2px(mContext, 5);
        LinePaint = linePaint();
        TextPaint = textPaint();
        EdgePaint = edgePaint();
        RectPaint = rectPaint();
    }

    public void updateVrwh(double vrw, double vrh) {
        this.mVrw = vrw;
        this.mVrh = vrh;
    }

    public void initZoom(float zoom, float originalZoom) {
        this.mDicomZoom = zoom;
        this.mOriginalZoom = originalZoom;
    }

    public void updateZoom(float zoom, float originalZoom) {

        float ratio = zoom * originalZoom / (mDicomZoom * mOriginalZoom);
        this.mDicomZoom = zoom;
        this.mOriginalZoom = originalZoom;
        updatePointFByZoomRatio(ratio);
    }

    public float getZoom() {
        return this.mDicomZoom;
    }

    public float getOriginalZoom() {
        return this.mOriginalZoom;
    }

    public void setConfirm(boolean confirm) {
        this.mBConfirm = confirm;
    }

    public boolean getConfirm() {
        return this.mBConfirm;
    }

    public boolean getMoved() {
        return this.mBMoved;
    }

    public void setDeleted(boolean delete) {
        this.mBDeleted = delete;
    }

    public boolean getDeleted() {
        return this.mBDeleted;
    }

    public void setLatestLinePointFConfirm() {

        this.cx1 = this.x1;
        this.cy1 = this.y1;
        this.cx2 = this.x2;
        this.cy2 = this.y2;

    }

    public void resetLastLinePointFConfirm() {

        this.x1 = this.cx1;
        this.y1 = this.cy1;
        this.x2 = this.cx2;
        this.y2 = this.cy2;

    }

    public void touchDown(float x1, float y1) {
        this.x1 = x1;
        this.y1 = y1;
    }

    public void touchMove(float x2, float y2) {
        mBMoved = true;
        this.x2 = x2;
        this.y2 = y2;
    }

    public void drawSelf(Canvas canvas) {
        if (!mBMoved) {
            return;
        }

        canvas.drawLine(x1, y1, x2, y2, LinePaint);

        if (mBTaped) {
            drawEdges(canvas, x1, y1, x2, y2);
        }

        drawLineLength(canvas, x1, y1, x2, y2);
    }

    private void drawEdges(Canvas canvas, float x1, float y1, float x2, float y2) {
        canvas.drawLine(x1 - EDGE_WIDTH, y1, x1 + EDGE_WIDTH, y1, EdgePaint);
        canvas.drawLine(x1, y1 - EDGE_WIDTH, x1, y1 + EDGE_WIDTH, EdgePaint);
        canvas.drawLine(x2 - EDGE_WIDTH, y2, x2 + EDGE_WIDTH, y2, EdgePaint);
        canvas.drawLine(x2, y2 - EDGE_WIDTH, x2, y2 + EDGE_WIDTH, EdgePaint);
    }

    private void drawLineLength(Canvas canvas, float x1, float y1, float x2,
            float y2) {
        float x, y;
        if (x1 > x2) {
            x = x1 + EDGE_WIDTH + 5;
            y = y1;
        } else {
            x = x2 + EDGE_WIDTH + 5;
            y = y2;
        }

        double dLength = calculateLength(x1, y1, x2, y2);

        DecimalFormat df = new DecimalFormat("#.0");

        String length = df.format(dLength) + " mm";

        Rect rect = new Rect();
        TextPaint.getTextBounds(length, 0, length.length(), rect);

        canvas.drawRect(rect.left + x - 5, rect.top + y - 5,
                rect.right + x + 5, rect.bottom + y + 5, RectPaint);

        canvas.drawText(length, x, y, TextPaint);
    }

    private double calculateLength(float x1, float y1, float x2, float y2) {

        return Math.sqrt((Math.abs(x1 - x2) * mVrw)
                * (Math.abs(x1 - x2) * mVrw) + (Math.abs(y1 - y2) * mVrh)
                * (Math.abs(y1 - y2) * mVrh));
    }

    public void updatePointFByZoomRatio(float ratio) {
        this.x1 = this.x1 * ratio;
        this.y1 = this.y1 * ratio;
        this.x2 = this.x2 * ratio;
        this.y2 = this.y2 * ratio;
    }

    public void setOffsetPointF(float offsetX, float offsetY, int edge) {
        if (edge == 1) {
            this.x1 = this.x1 + offsetX;
            this.y1 = this.y1 + offsetY;
        } else if (edge == 2) {
            this.x2 = this.x2 + offsetX;
            this.y2 = this.y2 + offsetY;
        } else {
            this.x1 = this.x1 + offsetX;
            this.y1 = this.y1 + offsetY;
            this.x2 = this.x2 + offsetX;
            this.y2 = this.y2 + offsetY;
        }
    }

    public int getPointInEdge(float x, float y) {
        RectF edge1 = new RectF(x1 - (EDGE_WIDTH + TAP_RANGE), y1
                - (EDGE_WIDTH + TAP_RANGE), x1 + (EDGE_WIDTH + TAP_RANGE), y1
                + (EDGE_WIDTH + TAP_RANGE));
        RectF edge2 = new RectF(x2 - (EDGE_WIDTH + TAP_RANGE), y2
                - (EDGE_WIDTH + TAP_RANGE), x2 + (EDGE_WIDTH + TAP_RANGE), y2
                + (EDGE_WIDTH + TAP_RANGE));
        if (edge1.contains(x, y)) {
            return 1;
        } else if (edge2.contains(x, y)) {
            return 2;
        }
        return 0;

    }

    public RectF getRotateRect() {
        PointF pointF = getRotatePoint(x2, y2);

        if (x1 <= pointF.x) {
            return new RectF(x1 - (EDGE_WIDTH + TAP_RANGE), y1
                    - (EDGE_WIDTH + TAP_RANGE), pointF.x
                    + (EDGE_WIDTH + TAP_RANGE), y1 + (EDGE_WIDTH + TAP_RANGE));
        } else {
            return new RectF(pointF.x - (EDGE_WIDTH + TAP_RANGE), pointF.y
                    - (EDGE_WIDTH + TAP_RANGE), x1 + (EDGE_WIDTH + TAP_RANGE),
                    pointF.y + (EDGE_WIDTH + TAP_RANGE));
        }

    }

    public PointF getRotatePoint(float x, float y) {

        double distance = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2)
                * (y1 - y2));

        float x0 = (float) ((x - x1) * (x2 - x1) / distance + (y - y1)
                * (y2 - y1) / distance + x1);
        float y0 = (float) (-(x - x1) * (y2 - y1) / distance + (y - y1)
                * (x2 - x1) / distance + y1);
        return new PointF(x0, y0);
    }

    private boolean mBTaped = false;

    public void setTapped(boolean tapped) {
        mBTaped = tapped;
    }

    public boolean getTapped() {
        return mBTaped;
    }

    public final Paint LinePaint;

    public Paint linePaint() {
        Paint linePaint = new Paint();
        linePaint.setStrokeWidth(3);
        linePaint.setColor(Color.GREEN);
        linePaint.setDither(true);
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        return linePaint;
    }

    public final Paint EdgePaint;

    public Paint edgePaint() {
        Paint edgePaint = new Paint();
        edgePaint.setStrokeWidth(5);
        edgePaint.setColor(mContext.getResources().getColor(R.color.red));
        edgePaint.setDither(true);
        edgePaint.setAntiAlias(true);
        edgePaint.setStyle(Paint.Style.STROKE);
        edgePaint.setStrokeJoin(Paint.Join.ROUND);
        edgePaint.setStrokeCap(Paint.Cap.ROUND);
        return edgePaint;
    }

    public final Paint TextPaint;

    public Paint textPaint() {
        Paint textPaint = new Paint();
        textPaint.setColor(mContext.getResources().getColor(
                R.color.line_text_color));
        textPaint.setTextSize(mContext.getResources().getDimension(
                R.dimen.text_size_10));
        textPaint.setAntiAlias(true);
        return textPaint;
    }

    public final Paint RectPaint;

    public Paint rectPaint() {
        Paint rectPaint = new Paint();
        rectPaint.setColor(mContext.getResources().getColor(
                R.color.line_rect_color));
        rectPaint.setAntiAlias(true);
        rectPaint.setStyle(Paint.Style.FILL);
        return rectPaint;
    }
}
