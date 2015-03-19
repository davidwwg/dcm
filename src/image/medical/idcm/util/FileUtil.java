package image.medical.idcm.util;

import java.io.File;

import android.os.Environment;

public class FileUtil {

    public final static String DCM_THUMB_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/dtTemp/";

    public static void delete(File file) {
        if (!file.exists()) {
            return;
        }

        if (file.isFile()) {
            file.delete();
            return;
        }

        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }

            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }
}
