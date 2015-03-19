package image.medical.idcm.model;

import java.util.ArrayList;
import java.util.List;

public class IDCMStudy {
    /** 标识符 **/
    private String           studyUid = "";

    /** 序列组 **/
    private List<IDCMSeries> series   = new ArrayList<IDCMSeries>();

    public String getStudyUid() {
        return studyUid;
    }

    public void setStudyUid(String studyUid) {
        this.studyUid = studyUid;
    }

    public List<IDCMSeries> getSeries() {
        return series;
    }

    public void setSeries(List<IDCMSeries> series) {
        this.series = series;
    }
}
