package image.medical.idcm.adapter;

import image.medical.idcm.R;
import image.medical.idcm.storage.StorageFileActivity.FileInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileAdapter extends BaseAdapter {

    private LayoutInflater mInflater      = null;

    private List<FileInfo> mFileInfos     = new ArrayList<FileInfo>();
    private boolean        mEditMode      = false;
    private List<String>   mSelectedPaths = new ArrayList<String>();

    public FileAdapter(Context context, List<FileInfo> infos, List<String> selectedInfos) {
        this.mInflater = LayoutInflater.from(context);
        this.mFileInfos = infos;
        this.mSelectedPaths = selectedInfos;
    }

    public void setStatus(boolean edit) {
        this.mEditMode = edit;
    }

    @Override
    public int getCount() {
        return mFileInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return mFileInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_file_row, null);
            viewHolder = new ViewHolder();
            viewHolder.mText = (TextView) convertView.findViewById(R.id.file_name);
            viewHolder.mFileImage = (ImageView) convertView.findViewById(R.id.file_type);
            viewHolder.mFileSelected = (ImageView) convertView.findViewById(R.id.file_selected);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        FileInfo info = (FileInfo) getItem(position);
        String path = info.getPath().toString();
        File file = new File(path);

        if (file.isDirectory()) {
            viewHolder.mFileImage.setImageResource(R.drawable.folder);
        } else {
            viewHolder.mFileImage.setImageResource(R.drawable.file);
        }

        viewHolder.mText.setText(info.getName());

        if (mEditMode && info.isFile()) {
            viewHolder.mFileSelected.setVisibility(View.VISIBLE);
            if (mSelectedPaths.contains(path)) {
                viewHolder.mFileSelected.setImageResource(R.drawable.selected_pressed);
            } else {
                viewHolder.mFileSelected.setImageResource(R.drawable.selected);
            }
        } else {
            viewHolder.mFileSelected.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    private class ViewHolder {
        private ImageView mFileImage;
        private TextView  mText;
        private ImageView mFileSelected;
    }
}
