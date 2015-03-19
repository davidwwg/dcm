package image.medical.idcm.cache;

import image.medical.idcm.model.IDCMSeries;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

public class IDCMEditData extends PageCache {

    public List<IDCMSeries> series = new ArrayList<IDCMSeries>();

    public String           key    = "";

    public int getKeyIndex() {
        int index = 0;
        if (TextUtils.isEmpty(key)) {
            return index;
        }

        int size = series.size();
        for (int i = 0; i < size; i++) {
            IDCMSeries s = series.get(i);
            if (s.getSeriesUid().equals(key)) {
                index = i;
                break;
            }
        }

        return index;
    }
}
