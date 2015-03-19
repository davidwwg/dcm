package image.medical.idcm.model;

import image.medical.idcm.edit.LineStack;

import com.imebra.dicom.DataSet;
import com.imebra.dicom.Image;
import com.imebra.dicom.TransformsChain;

public class IDCMContent {

    private DataSet         dataSet;
    private Image           image;
    private TransformsChain chain;
    private String          thumbUrl          = "";

    /** 0010,0020 */
    private String          patientID         = "";
    /** 0010,0010 */
    private String          patientName       = "";
    /** 0010,0040 */
    private String          patientSex        = "";
    /** 0010,1010 */
    private String          patientAge        = "";
    /** 0010,0030 */
    private String          patientBirth      = "";
    /** 0008,0060 */
    private String          modality          = "";
    /** 0028,1050 */
    private String          windowCenter      = "";
    /** 0028,1051 */
    private String          windowWidth       = "";

    /** 0008,0020 */
    private String          studyDate         = "";
    /** 0008,0030 */
    private String          studyTime         = "";

    /** 0020,000D */
    private String          studyUid          = "";
    /** 0020,000E */
    private String          seriesUid         = "";
    /** 0020,0011 */
    private String          seriesNumber      = "";
    /** 0020,0013 */
    private String          instanceNumber    = "";

    /** 0008,0080 */
    private String          institutionName   = "";
    /** 0008,1030 */
    private String          studyDescription  = "";
    /** 0008,103E */
    private String          seriesDescription = "";

    private double          absoultWidth;
    private double          absoultHeight;

    /** 0:left | 1:right | 2:top | 3:buttom. */
    private String[]        orientation       = new String[] { "", "", "", "" };

    public LineStack        linestack         = new LineStack();

    public double getAbsoultWidth() {
        return absoultWidth;
    }

    public void setAbsoultWidth(double absoultWidth) {
        this.absoultWidth = absoultWidth;
    }

    public double getAbsoultHeight() {
        return absoultHeight;
    }

    public void setAbsoultHeight(double absoultHeight) {
        this.absoultHeight = absoultHeight;
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
        this.studyUid = dataSet.getString(0x0020, 0, 0x000D, 0);
        this.seriesUid = dataSet.getString(0x0020, 0, 0x000E, 0);
        this.seriesNumber = dataSet.getString(0x0020, 0, 0x0011, 0);
        this.instanceNumber = dataSet.getString(0x0020, 0, 0x0013, 0);

        this.patientID = dataSet.getString(0x0010, 0, 0x0020, 0);
        String patientName = dataSet.getString(0x0010, 0, 0x0010, 0);

        this.patientName = patientName.replace("^", " ").trim();
        this.patientSex = dataSet.getString(0x0010, 0, 0x0040, 0);
        this.patientAge = dataSet.getString(0x0010, 0, 0x1010, 0);
        this.patientBirth = dataSet.getString(0x0010, 0, 0x0030, 0);

        this.modality = dataSet.getString(0x0008, 0, 0x0060, 0);

        this.windowCenter = dataSet.getString(0x0028, 0, 0x1050, 0);
        this.windowWidth = dataSet.getString(0x0028, 0, 0x1051, 0);

        this.studyDate = dataSet.getString(0x0008, 0, 0x0020, 0);
        this.studyTime = dataSet.getString(0x0008, 0, 0x0030, 0);
        this.studyTime = studyTime.substring(0, 8);

        int rows = dataSet.getUnsignedLong(0x0028, 0, 0x0010, 0);
        int columns = dataSet.getUnsignedLong(0x0028, 0, 0x0011, 0);
        double rowsPS = dataSet.getDouble(0x0028, 0, 0x0030, 0);
        double columnsPS = dataSet.getDouble(0x0028, 0, 0x0030, 1);

        absoultWidth = columns * columnsPS;
        absoultHeight = rows * rowsPS;

        this.institutionName = dataSet.getString(0x0008, 0, 0x0080, 0);
        this.studyDescription = dataSet.getString(0x0008, 0, 0x1030, 0);
        this.seriesDescription = dataSet.getString(0x0008, 0, 0x103E, 0);

        String a0 = dataSet.getString(0x0020, 0, 0x0020, 0);
        String a1 = dataSet.getString(0x0020, 0, 0x0020, 0);

        double[] coordinate = new double[3];
        coordinate[0] = dataSet.getDouble(0x0020, 0, 0x0032, 0);
        coordinate[1] = dataSet.getDouble(0x0020, 0, 0x0032, 1);
        coordinate[2] = dataSet.getDouble(0x0020, 0, 0x0032, 2);

        double[] orientation = new double[6];

        orientation[0] = dataSet.getDouble(0x0020, 0, 0x0037, 0);
        orientation[1] = dataSet.getDouble(0x0020, 0, 0x0037, 1);
        orientation[2] = dataSet.getDouble(0x0020, 0, 0x0037, 2);
        orientation[3] = dataSet.getDouble(0x0020, 0, 0x0037, 3);
        orientation[4] = dataSet.getDouble(0x0020, 0, 0x0037, 4);
        orientation[5] = dataSet.getDouble(0x0020, 0, 0x0037, 5);

        computeAzimuth(coordinate, orientation);
    }

    private void computeAzimuth(double[] coordinate, double[] rcOrientation) {
        double[] imageConsinse = new double[3];
        imageConsinse[0] = rcOrientation[1] * rcOrientation[5] - rcOrientation[2] * rcOrientation[4];
        imageConsinse[1] = rcOrientation[2] * rcOrientation[3] - rcOrientation[0] * rcOrientation[5];
        imageConsinse[2] = rcOrientation[0] * rcOrientation[4] - rcOrientation[1] * rcOrientation[3];

        // x | y | z.
        String[][] allOrientation = new String[][] { { "L", "R" }, { "P", "A" }, { "S", "I" } };

        int rowMax = 0, cloMax = 3;
        for (int i = 0; i < 3; i++) {
            if (Math.abs(rcOrientation[i]) > Math.abs(rcOrientation[rowMax])) {
                rowMax = i;
            }

            if (Math.abs(rcOrientation[i + 3]) > Math.abs(rcOrientation[cloMax])) {
                cloMax = i + 3;
            }
        }

        if (rcOrientation[rowMax] == 0.0f || rcOrientation[cloMax] == 0.0f) {
            return;
        }

        if (rcOrientation[rowMax] >= 0) {
            orientation[0] = allOrientation[rowMax][1];
            orientation[1] = allOrientation[rowMax][0];
        } else {
            orientation[0] = allOrientation[rowMax][0];
            orientation[1] = allOrientation[rowMax][1];
        }

        if (rcOrientation[cloMax] >= 0) {
            orientation[2] = allOrientation[cloMax - 3][1];
            orientation[3] = allOrientation[cloMax - 3][0];
        } else {
            orientation[2] = allOrientation[cloMax - 3][0];
            orientation[3] = allOrientation[cloMax - 3][1];
        }

    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public TransformsChain getChain() {
        return chain;
    }

    public void setChain(TransformsChain chain) {
        this.chain = chain;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientSex() {
        return patientSex;
    }

    public void setPatientSex(String patientSex) {
        this.patientSex = patientSex;
    }

    public String getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(String patientAge) {
        this.patientAge = patientAge;
    }

    public String getPatientBirth() {
        return patientBirth;
    }

    public void setPatientBirth(String patientBirth) {
        this.patientBirth = patientBirth;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public String getWindowCenter() {
        return windowCenter;
    }

    public void setWindowCenter(String windowCenter) {
        this.windowCenter = windowCenter;
    }

    public String getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(String windowWidth) {
        this.windowWidth = windowWidth;
    }

    public String getStudyDate() {
        return studyDate;
    }

    public void setStudyDate(String studyDate) {
        this.studyDate = studyDate;
    }

    public String getStudyTime() {
        return studyTime;
    }

    public void setStudyTime(String studyTime) {
        this.studyTime = studyTime;
    }

    public String getStudyUid() {
        return studyUid;
    }

    public void setStudyUid(String studyUid) {
        this.studyUid = studyUid;
    }

    public String getSeriesUid() {
        return seriesUid;
    }

    public void setSeriesUid(String seriesUid) {
        this.seriesUid = seriesUid;
    }

    public String getSeriesNumber() {
        return seriesNumber;
    }

    public void setSeriesNumber(String seriesNumber) {
        this.seriesNumber = seriesNumber;
    }

    public String getInstanceNumber() {
        return instanceNumber;
    }

    public void setInstanceNumber(String instanceNumber) {
        this.instanceNumber = instanceNumber;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getStudyDescription() {
        return studyDescription;
    }

    public void setStudyDescription(String studyDescription) {
        this.studyDescription = studyDescription;
    }

    public String getSeriesDescription() {
        return seriesDescription;
    }

    public void setSeriesDescription(String seriesDescription) {
        this.seriesDescription = seriesDescription;
    }

    public String[] getOrientation() {
        return orientation;
    }

    public void setOrientation(String[] orientation) {
        this.orientation = orientation;
    }
}
