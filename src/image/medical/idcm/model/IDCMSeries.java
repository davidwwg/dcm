package image.medical.idcm.model;

import image.medical.idcm.edit.SeriesAttribute;

import java.util.ArrayList;
import java.util.List;

public class IDCMSeries {
    /** 序列标识符 **/
    private String            seriesUid = "";

    /** 序列组 **/
    private List<IDCMContent> dcms      = new ArrayList<IDCMContent>();

    /** 序列组缓存属性 **/
    private SeriesAttribute   attribute = new SeriesAttribute();

    public String getSeriesUid() {
        return seriesUid;
    }

    public void setSeriesUid(String seriesUid) {
        this.seriesUid = seriesUid;
    }

    public List<IDCMContent> getDcms() {
        return dcms;
    }

    public void setDcms(List<IDCMContent> dcms) {
        this.dcms = dcms;
    }

    public SeriesAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(SeriesAttribute attribute) {
        this.attribute = attribute;
    }

}
