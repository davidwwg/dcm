package image.medical.idcm.decode;

import image.medical.idcm.cache.IDCMListData;
import image.medical.idcm.model.IDCMAbstract;
import image.medical.idcm.model.IDCMAbstract.ImageUrl;
import image.medical.idcm.model.IDCMContent;
import image.medical.idcm.model.IDCMSeries;
import image.medical.idcm.model.IDCMStudy;
import image.medical.idcm.util.DCMUtil;
import image.medical.idcm.util.FileUtil;
import image.medical.idcm.util.ImageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;

import com.imebra.dicom.CodecFactory;
import com.imebra.dicom.DataSet;
import com.imebra.dicom.DrawBitmap;
import com.imebra.dicom.Image;
import com.imebra.dicom.Stream;
import com.imebra.dicom.StreamReader;
import com.imebra.dicom.TransformsChain;

public class DecodeTask implements Runnable {

    private static final String TAG = "DecodeTask";
    private Context             mContext;
    private List<String>        mPaths;
    private IDecodeProgress     mCallback;
    private int                 mMaxProgress;

    public DecodeTask(Context context, List<String> paths, IDecodeProgress callback) {
        this.mContext = context;
        this.mPaths = paths;
        this.mCallback = callback;
    }

    @Override
    public void run() {
        Handler looperhandler = new Handler(mContext.getMainLooper());

        if (mCallback != null) {
            mMaxProgress = mCallback.decodeStart();
        }

        // 清空之前的残留文件.
        FileUtil.delete(new File(FileUtil.DCM_THUMB_DIR));

        List<File> dcmFiles = new ArrayList<File>();
        for (String path : mPaths) {
            filterDcmFile(path, dcmFiles);
        }

        List<IDCMContent> dcms = new ArrayList<IDCMContent>();
        for (File file : dcmFiles) {
            try {
                Stream stream = new Stream();
                stream.openFileRead(file.getAbsolutePath());
                DataSet dataSet = CodecFactory.load(new StreamReader(stream), 256);

                IDCMContent dcm = new IDCMContent();
                dcm.setDataSet(dataSet);

                dcms.add(dcm);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (dcms.size() == 0) {
            looperhandler.post(new Runnable() {

                @Override
                public void run() {
                    mCallback.decodeFailed(new DecodeError("no dcm files!"));
                }
            });
        }

        List<IDCMAbstract> abstracts = new ArrayList<IDCMAbstract>();

        Map<String, Map<String, List<IDCMContent>>> dcmMaps = new HashMap<String, Map<String, List<IDCMContent>>>();

        for (IDCMContent dcm : dcms) {

            String studyUid = dcm.getStudyUid();
            String seriesUid = dcm.getSeriesUid();

            if (dcmMaps.containsKey(studyUid)) {

                Map<String, List<IDCMContent>> seriesMaps = dcmMaps.get(studyUid);

                if (seriesMaps.containsKey(seriesUid)) {
                    seriesMaps.get(seriesUid).add(dcm);
                } else {
                    List<IDCMContent> contents = new ArrayList<IDCMContent>();
                    contents.add(dcm);

                    seriesMaps.put(seriesUid, contents);
                }
            } else {

                Map<String, List<IDCMContent>> seriesMaps = new HashMap<String, List<IDCMContent>>();

                List<IDCMContent> contents = new ArrayList<IDCMContent>();
                contents.add(dcm);

                seriesMaps.put(seriesUid, contents);

                dcmMaps.put(studyUid, seriesMaps);
            }
        }

        List<IDCMStudy> idcmStudys = new ArrayList<IDCMStudy>();

        Iterator<Entry<String, Map<String, List<IDCMContent>>>> iter = dcmMaps.entrySet().iterator();

        int countMax = 0;
        float perMax = mMaxProgress / dcmMaps.size();

        while (iter.hasNext()) {

            decodeing(looperhandler, (int) (perMax * countMax));

            Map.Entry<String, Map<String, List<IDCMContent>>> entry = (Map.Entry<String, Map<String, List<IDCMContent>>>) iter
                    .next();
            String studyUid = entry.getKey();
            Map<String, List<IDCMContent>> seriedMaps = entry.getValue();

            List<Map.Entry<String, List<IDCMContent>>> sortSeries = new ArrayList<Map.Entry<String, List<IDCMContent>>>(
                    seriedMaps.entrySet());

            Collections.sort(sortSeries, new Comparator<Map.Entry<String, List<IDCMContent>>>() {

                @Override
                public int compare(Entry<String, List<IDCMContent>> lhs, Entry<String, List<IDCMContent>> rhs) {

                    String lNum = lhs.getValue().get(0).getSeriesNumber();
                    String rNum = rhs.getValue().get(0).getSeriesNumber();

                    try {
                        return Integer.parseInt(lNum) - Integer.parseInt(rNum);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return lNum.compareTo(rNum);
                }
            });

            IDCMAbstract abs = new IDCMAbstract();
            List<ImageUrl> imageUrls = new ArrayList<ImageUrl>();

            List<IDCMSeries> idcmSeries = new ArrayList<IDCMSeries>();

            Iterator<Map.Entry<String, List<IDCMContent>>> seriesIter = sortSeries.iterator();

            int countMin = 0;
            float perMin = perMax / sortSeries.size();

            while (seriesIter.hasNext()) {
                decodeing(looperhandler, (int) (perMin * countMin) + (int) (perMax * countMax));

                Map.Entry<String, List<IDCMContent>> seriesEntry = (Map.Entry<String, List<IDCMContent>>) seriesIter
                        .next();
                String seriedUid = seriesEntry.getKey();
                List<IDCMContent> contents = seriesEntry.getValue();

                Collections.sort(contents, new Comparator<IDCMContent>() {

                    @Override
                    public int compare(IDCMContent lhs, IDCMContent rhs) {

                        String lNum = lhs.getInstanceNumber();
                        String rNum = rhs.getInstanceNumber();

                        try {
                            return Integer.parseInt(lNum) - Integer.parseInt(rNum);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return lNum.compareTo(rNum);

                    }
                });

                String imageUrl = createThumbnail(contents.get(0));
                IDCMAbstract.ImageUrl image = abs.buildImageUrl(seriedUid, imageUrl, contents.size() + "");
                imageUrls.add(image);

                IDCMSeries series = new IDCMSeries();
                series.setSeriesUid(seriedUid);
                series.setDcms(contents);
                idcmSeries.add(series);

                countMin++;
            }

            IDCMContent sample = (IDCMContent) idcmSeries.get(0).getDcms().get(0);

            abs.setId(studyUid);
            abs.setImageUrls(imageUrls);
            abs.setModality(sample.getModality());
            abs.setPaitentBirth(sample.getPatientBirth());
            abs.setPaitentId(sample.getPatientID());
            abs.setPaitentName(sample.getPatientName());
            abs.setPaitentSex(sample.getPatientSex());
            abs.setSeriesNumber(seriedMaps.values().size() + "");
            abs.setStudyDate(sample.getStudyDate());

            abstracts.add(abs);

            IDCMStudy idcmStudy = new IDCMStudy();
            idcmStudy.setStudyUid(studyUid);
            idcmStudy.setSeries(idcmSeries);

            idcmStudys.add(idcmStudy);

            countMax++;
        }

        Collections.sort(abstracts, new Comparator<IDCMAbstract>() {

            @Override
            public int compare(IDCMAbstract lhs, IDCMAbstract rhs) {
                return rhs.getStudyDate().compareTo(lhs.getStudyDate());
            }
        });

        decodeing(looperhandler, mMaxProgress);

        final IDCMListData list = new IDCMListData();
        list.abstracts = abstracts;
        list.studys = idcmStudys;

        if (mCallback != null) {
            looperhandler.post(new Runnable() {

                @Override
                public void run() {

                    mCallback.decodeFinished(list);

                }
            });
        }
    }

    private void decodeing(Handler mainHandler, final int value) {
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                mCallback.decoding(value);

            }
        });
    }

    private void filterDcmFile(String path, List<File> dcmFiles) {

        File parentFile = new File(path);

        if (parentFile.isFile()) {
            dcmFiles.add(parentFile);
            return;
        }

        File[] childFiles = parentFile.listFiles();
        for (int i = 0; i < childFiles.length; i++) {
            File file = childFiles[i];
            if (file.isHidden()) {
                continue;
            }

            if (file.isDirectory()) {
                filterDcmFile(file.getAbsolutePath(), dcmFiles);
                continue;
            }

            String fileName = file.getName();
            String end = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
            if (!file.isDirectory() && !TextUtils.isEmpty(end) && !end.equals(fileName.toLowerCase())
                    && !end.equals("dcm")) {
                continue;
            }
            dcmFiles.add(file);
        }

    }

    private String createThumbnail(IDCMContent content) {
        TransformsChain chain = new TransformsChain();
        Image image = DCMUtil.getDcmImageTranfromChain(content.getDataSet(), chain);

        DrawBitmap drawBitmap = new DrawBitmap(image, chain);

        float ratio = (float) image.getSizeX() / (float) image.getSizeY();
        int renderBuffer[] = new int[4096];
        int requiredSize = drawBitmap.getBitmap(100, (int) (100 / ratio), 0, 0, 100, (int) (100 / ratio), renderBuffer,
                0);
        if (requiredSize == 0) {
            return "";
        }
        if (renderBuffer.length < requiredSize) {
            renderBuffer = new int[requiredSize];
        }

        drawBitmap.getBitmap(100, (int) (100 / ratio), 0, 0, 100, (int) (100 / ratio), renderBuffer,
                renderBuffer.length);

        Bitmap renderBitmap = Bitmap.createBitmap(renderBuffer, 100, (int) (100 / ratio), Bitmap.Config.ARGB_8888);
        UUID uuid = UUID.randomUUID();

        ImageUtil.saveBitmap(uuid.toString(), renderBitmap);
        renderBitmap.recycle();
        renderBitmap = null;

        String imageUrl = "file://" + FileUtil.DCM_THUMB_DIR + uuid.toString();

        content.setThumbUrl(imageUrl);

        return imageUrl;
    }

}
