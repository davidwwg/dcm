package image.medical.idcm.exchange;

import image.medical.idcm.app.AppController;
import image.medical.idcm.cache.PageCache;
import android.os.Parcel;
import android.os.Parcelable;

public class IDCMPageExchanger implements Parcelable {

    private String                                            key;
    public static final Parcelable.Creator<IDCMPageExchanger> CREATOR = new Creator<IDCMPageExchanger>() {

                                                                          @Override
                                                                          public IDCMPageExchanger[] newArray(int size) {
                                                                              return new IDCMPageExchanger[size];
                                                                          }

                                                                          @Override
                                                                          public IDCMPageExchanger createFromParcel(
                                                                                  Parcel source) {
                                                                              return new IDCMPageExchanger(source);
                                                                          }
                                                                      };

    public IDCMPageExchanger() {

    }

    public IDCMPageExchanger(Parcel source) {
        key = source.readString();
    }

    public void setPageCache(PageCache cache) {
        if (cache != null) {
            key = cache.hashCode() + "#" + cache.getClass().getName();
            AppController.addPageCache(cache);
        }
    }

    public PageCache getPageCache() {
        return AppController.getPageCache(key);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
    }

}
