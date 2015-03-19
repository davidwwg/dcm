package image.medical.idcm.edit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.graphics.PointF;

public class LineStack implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -5023498363309702212L;

    private List<Line> mLines = new ArrayList<Line>();

    private Line mTappedLine = null;

    public List<Line> getLines() {
        return this.mLines;
    }

    public Line getTappedLine() {
        return this.mTappedLine;
    }

    public void setTappedLine(Line line) {
        this.mTappedLine = line;
    }

    public void addLine(Line line) {
        mLines.add(line);
    }

    public void removeLine(Line line) {
        mLines.remove(line);
    }

    public void popLine(Line line) {
        mLines.remove(line);
        mLines.add(0, line);
    }

    public Line getTappedLine(float x, float y) {

        for (Line line : mLines) {
            PointF pf = line.getRotatePoint(x, y);
            if (line.getRotateRect().contains(pf.x, pf.y)) {
                clearLineTapped();
                line.setTapped(true);
                mTappedLine = line;
                return line;
            }
        }
        return null;
    }

    public void clearLineTapped() {
        mTappedLine = null;
        for (Line line : mLines) {
            line.setTapped(false);
        }
    }

    public boolean pointInTappedLine(float x, float y) {

        if (mTappedLine == null) {
            return false;
        }
        PointF pf = mTappedLine.getRotatePoint(x, y);
        if (mTappedLine.getRotateRect().contains(pf.x, pf.y)) {
            return true;
        }

        return false;

    }

    public int pointInTappedLineEdge(float x, float y) {
        if (mTappedLine == null) {
            return 0;
        }

        return mTappedLine.getPointInEdge(x, y);
    }

    public void updateLinesPointFByZoomScale(float newZoom,
            float newOriginalZoom) {
        for (Line line : mLines) {
            line.updateZoom(newZoom, newOriginalZoom);
        }
    }

    public void updateLinesLengthByRatio(double vrw, double vrh) {
        for (Line line : mLines) {
            line.updateVrwh(vrw, vrh);
        }
    }

    public void confirm() {
        checkDeleted(true);

        for (Line line : mLines) {
            if (!line.getConfirm()) {
                line.setConfirm(true);
            }
            line.setLatestLinePointFConfirm();
        }
    }

    public void unConfirm() {
        checkDeleted(false);

        List<Line> unConfirmLines = new ArrayList<Line>();
        for (Line line : mLines) {
            if (!line.getConfirm()) {
                unConfirmLines.add(line);
            } else {
                line.resetLastLinePointFConfirm();
            }
        }

        for (Line line : unConfirmLines) {
            mLines.remove(line);
        }
    }

    public void checkDeleted(boolean confirm) {

        if (confirm) {
            List<Line> deletedLines = new ArrayList<Line>();

            for (Line line : mLines) {

                if (line.getDeleted()) {

                    deletedLines.add(line);
                }
            }

            for (Line line : deletedLines) {
                mLines.remove(line);
            }

        } else {
            for (Line line : mLines) {

                if (line.getDeleted()) {
                    line.setDeleted(false);
                }
            }
        }

    }

    public void deleteTapedLine() {
        for (Line line : mLines) {
            if (line.getTapped() && !line.getDeleted()) {
                line.setDeleted(true);
                return;
            }
        }
    }

    public void clearLines() {
        mLines.clear();
    }
}
