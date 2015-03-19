package image.medical.idcm.util;

import com.imebra.dicom.ColorTransformsFactory;
import com.imebra.dicom.DataSet;
import com.imebra.dicom.Image;
import com.imebra.dicom.ModalityVOILUT;
import com.imebra.dicom.TransformsChain;
import com.imebra.dicom.VOILUT;
import com.imebra.dicom.Image.bitDepth;

public class DCMUtil {
    public static Image getDcmImageTranfromChain(DataSet dataSet,
            TransformsChain chain) {
        Image image = dataSet.getImage(0);

        if (ColorTransformsFactory.isMonochrome(image.getColorSpace())) {

            ModalityVOILUT modalityVOILUT = new ModalityVOILUT(dataSet);
            if (!modalityVOILUT.isEmpty()) {
                Image modalityImage = modalityVOILUT.allocateOutputImage(image,
                        image.getSizeX(), image.getSizeY());
                modalityVOILUT.runTransform(image, 0, 0, image.getSizeX(),
                        image.getSizeY(), modalityImage, 0, 0);
                image = modalityImage;
            }
        }

        if (ColorTransformsFactory.isMonochrome(image.getColorSpace())) {
            VOILUT voilut = new VOILUT(dataSet);
            int voilutId = voilut.getVOILUTId(0);
            if (voilutId != 0) {
                voilut.setVOILUT(voilutId);
            } else {
                voilut.applyOptimalVOI(image, 0, 0, image.getSizeX(),
                        image.getSizeY());
            }

            chain.addTransform(voilut);
        }
        return image;
    }
    
    public static Image getNegativeDcmImageTranfromChain(DataSet dataSet,
            TransformsChain chain) {
        Image image = dataSet.getImage(0);

        // Transform tf = ColorTransformsFactory.getTransform(
        // image.getColorSpace(), "MONOCHROME1");
        // Image image2 = tf.allocateOutputImage(image, image.getSizeX(),
        // image.getSizeY());
        // tf.runTransform(image, 0, 0, image.getSizeX(), image.getSizeY(),
        // image2, 0, 0);
        // image = image2;

        if (ColorTransformsFactory.isMonochrome(image.getColorSpace())) {

            ModalityVOILUT modalityVOILUT = new ModalityVOILUT(dataSet);
            if (!modalityVOILUT.isEmpty()) {
                Image modalityImage = modalityVOILUT.allocateOutputImage(image,
                        image.getSizeX(), image.getSizeY());
                modalityVOILUT.runTransform(image, 0, 0, image.getSizeX(),
                        image.getSizeY(), modalityImage, 0, 0);
                image = modalityImage;
            }
        }

        if (ColorTransformsFactory.isMonochrome(image.getColorSpace())) {
            VOILUT voilut = new VOILUT(dataSet);
            int voilutId = voilut.getVOILUTId(0);
            if (voilutId != 0) {
                voilut.setVOILUT(voilutId);
            } else {
                voilut.applyOptimalVOI(image, 0, 0, image.getSizeX(),
                        image.getSizeY());
            }

            chain.addTransform(voilut);
        }

        image.create(image.getSizeX(), image.getSizeY(), bitDepth.depthU8,
                "MONOCHROME1", image.getHighBit());

        return image;
    }
}
