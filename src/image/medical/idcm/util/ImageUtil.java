package image.medical.idcm.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;

public class ImageUtil {

    public static void saveBitmap(String name, Bitmap bitmap) {
        File file = new File(FileUtil.DCM_THUMB_DIR);
        if (!file.exists()) {
            file.mkdir();
        }

        File f = new File(FileUtil.DCM_THUMB_DIR + name);
        
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
