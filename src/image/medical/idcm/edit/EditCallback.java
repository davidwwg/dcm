package image.medical.idcm.edit;

import android.view.View;

public interface EditCallback {

    void cancel(View view);

    void delete(View view);

    void confirm(View view);

}
