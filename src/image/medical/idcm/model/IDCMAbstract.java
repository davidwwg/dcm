package image.medical.idcm.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class IDCMAbstract {

    /** 标识符 **/
    private String         id           = "";

    /** 姓名 **/
    private String         paitentName  = "";

    /** 性别 **/
    private String         paitentSex   = "";

    /** 形态 **/
    private String         modality     = "";

    /** 代号 **/
    private String         paitentId    = "";

    /** 检查日期 **/
    private String         studyDate    = "";

    /** 生日 **/
    private String         paitentBirth = "";

    /** 序列数 **/
    private String         seriesNumber = "";

    /** 标识符 **/
    private List<ImageUrl> imageUrls    = new ArrayList<ImageUrl>();

    public void initWithAttributes(JSONObject o) {
        try {
            id = (String) o.get("id");
            paitentName = (String) o.getString("paitent_name");
            paitentSex = (String) o.getString("paitent_sex");
            modality = (String) o.getString("modality");
            paitentId = (String) o.getString("paitent_id");
            studyDate = (String) o.getString("study_date");
            paitentBirth = (String) o.getString("paitent_birth");
            seriesNumber = (String) o.getString("series_number");
            JSONArray ja = o.getJSONArray("urls");
            int length = ja.length();
            imageUrls.clear();
            for (int i = 0; i < length; i++) {
                ImageUrl image = new ImageUrl();
                image.initWithAttributes(ja.getJSONObject(i));

                imageUrls.add(image);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPaitentName() {
        return paitentName;
    }

    public void setPaitentName(String paitentName) {
        this.paitentName = paitentName;
    }

    public String getPaitentSex() {
        return paitentSex;
    }

    public void setPaitentSex(String paitentSex) {
        this.paitentSex = paitentSex;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public String getPaitentId() {
        return paitentId;
    }

    public void setPaitentId(String paitentId) {
        this.paitentId = paitentId;
    }

    public String getStudyDate() {
        return studyDate;
    }

    public void setStudyDate(String studyDate) {
        this.studyDate = studyDate;
    }

    public String getPaitentBirth() {
        return paitentBirth;
    }

    public void setPaitentBirth(String paitentBirth) {
        this.paitentBirth = paitentBirth;
    }

    public String getSeriesNumber() {
        return seriesNumber;
    }

    public void setSeriesNumber(String seriesNumber) {
        this.seriesNumber = seriesNumber;
    }

    public List<ImageUrl> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<ImageUrl> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public ImageUrl buildImageUrl(String id, String url, String number) {
        return new ImageUrl(id, url, number);
    }

    public class ImageUrl {
        /** 序列标识符 **/
        private String id;
        /** 首图Url **/
        private String url;
        /** 序列数目 **/
        private String number;

        public ImageUrl() {

        }

        public ImageUrl(String id, String url, String number) {
            this.id = id;
            this.url = url;
            this.number = number;
        }

        public void initWithAttributes(JSONObject o) {
            try {
                this.id = (String) o.get("id");
                this.url = (String) o.getString("url");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

    }
}
