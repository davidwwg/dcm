package image.medical.idcm.cache;

import image.medical.idcm.model.IDCMAbstract;
import image.medical.idcm.model.IDCMSeries;
import image.medical.idcm.model.IDCMStudy;

import java.util.ArrayList;
import java.util.List;

public class IDCMListData extends PageCache {

    public List<IDCMAbstract> abstracts = new ArrayList<IDCMAbstract>();

    public List<IDCMStudy>    studys    = new ArrayList<IDCMStudy>();

    public String getDefaultSeriesUid() {
        String defaultKey = "";
        if (abstracts.size() > 0) {
            defaultKey = abstracts.get(0).getId();
        }
        return defaultKey;
    }

    public List<IDCMSeries> getSeriesByStudyUid(String studyUid) {
        for (IDCMStudy study : studys) {
            if (study.getStudyUid().equals(studyUid)) {
                return study.getSeries();
            }
        }
        return null;
    }
}
