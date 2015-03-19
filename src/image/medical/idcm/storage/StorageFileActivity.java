package image.medical.idcm.storage;

import image.medical.idcm.R;
import image.medical.idcm.activity.BaseActivity;
import image.medical.idcm.adapter.FileAdapter;
import image.medical.idcm.cache.PageCache;
import image.medical.idcm.decode.DecodeError;
import image.medical.idcm.decode.DecodeTask;
import image.medical.idcm.decode.DecodeThreadPool;
import image.medical.idcm.decode.IDecodeProgress;
import image.medical.idcm.helper.ActivityConfig;
import image.medical.idcm.manager.ExchangeManager;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class StorageFileActivity extends BaseActivity implements OnClickListener, OnItemClickListener,
        OnItemLongClickListener {
    private ProgressBar    mProgressBar;
    private TextView       mDirectory;

    private View           mFooter, mCancel, mConfirm;
    private TextView       mSelected;

    private ListView       mListView;
    private FileAdapter    mAdapter;

    private String         mSdcardDir     = Environment.getExternalStorageDirectory().toString();
    private List<String>   mParentPaths   = new ArrayList<String>();

    private List<FileInfo> mFileInfos     = new ArrayList<FileInfo>();

    private boolean        mEditMode      = false;
    private List<String>   mSelectedPaths = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_directory);

        mDirectory = (TextView) this.findViewById(R.id.directory);
        mProgressBar = (ProgressBar) this.findViewById(R.id.progress_bar);

        mFooter = this.findViewById(R.id.edit_footer);
        mCancel = this.findViewById(R.id.cancel);
        mConfirm = this.findViewById(R.id.confirm);
        mSelected = (TextView) this.findViewById(R.id.selected);

        mCancel.setOnClickListener(this);
        mConfirm.setOnClickListener(this);
        mSelected.setOnClickListener(this);
        mFooter.setVisibility(View.GONE);

        mListView = (ListView) this.findViewById(R.id.file_directory_list);
        mAdapter = new FileAdapter(this, mFileInfos, mSelectedPaths);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        mParentPaths.add(mSdcardDir);

        updateDirectory(mSdcardDir);

        getSdcardFileDir(mSdcardDir);
    }

    private void updateDirectory(String path) {
        String directory = path.replace(mSdcardDir, "内存设备");
        mDirectory.setText(directory);
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hiddleProgressBar() {
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void getSdcardFileDir(final String path) {

        mFileInfos.clear();
        mAdapter.notifyDataSetChanged();

        showProgressBar();

        new Thread(new Runnable() {

            @Override
            public void run() {
                final List<FileInfo> fileInfos = new ArrayList<FileInfo>();

                File f = new File(path);

                File[] files = f.listFiles();
                List<File> fl = new ArrayList<File>();
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    if (file.isHidden()) {
                        continue;
                    }
                    String fName = file.getName();
                    String end = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();
                    if (!file.isDirectory() && !TextUtils.isEmpty(end) && !end.equals(fName.toLowerCase())
                            && !end.equals("dcm")) {
                        continue;
                    }
                    fl.add(file);
                }

                Collections.sort(fl, new CompratorByFileName());

                if (!path.equals(mSdcardDir)) {
                    FileInfo info = new FileInfo();
                    info.setFile(false);
                    info.setName("...");
                    info.setPath(path);
                    fileInfos.add(info);
                }
                for (int i = 0; i < fl.size(); i++) {
                    File file = fl.get(i);

                    FileInfo info = new FileInfo();
                    info.setName(file.getName());
                    info.setPath(file.getPath());
                    fileInfos.add(info);
                }

                Handler handle = new Handler(Looper.getMainLooper());
                handle.post(new Runnable() {

                    @Override
                    public void run() {
                        hiddleProgressBar();
                        mFileInfos.addAll(fileInfos);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();

    }

    private static class CompratorByFileName implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            Comparator<Object> cmp = Collator.getInstance(java.util.Locale.CHINA);
            return cmp.compare(lhs.getName(), rhs.getName());
        }

        @Override
        public boolean equals(Object o) {
            return true;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileInfo info = (FileInfo) mAdapter.getItem(position);
        String path = info.getPath();

        if (mEditMode) {

            if (!info.isFile()) {
                return;
            }

            if (mSelectedPaths.contains(path)) {
                mSelectedPaths.remove(path);
            } else {
                mSelectedPaths.add(path);
            }
            mAdapter.notifyDataSetChanged();

            checkAllSelected();
        } else {
            File file = new File(path);

            if (!info.isFile()) {
                mParentPaths.remove(mParentPaths.size() - 1);

                String parentPath = mParentPaths.get(mParentPaths.size() - 1);
                updateDirectory(parentPath);

                getSdcardFileDir(parentPath);
            } else if (file.isDirectory()) {
                String parentPath = info.getPath();
                mParentPaths.add(parentPath);

                updateDirectory(parentPath);

                getSdcardFileDir(parentPath);
            }

        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            if (mEditMode) {
                mSelectedPaths.clear();
                mEditMode = false;
                mAdapter.setStatus(mEditMode);
                mFooter.setVisibility(View.GONE);
                return true;
            } else {
                if (mParentPaths.size() > 1) {
                    mParentPaths.remove(mParentPaths.size() - 1);

                    String path = mParentPaths.get(mParentPaths.size() - 1);
                    updateDirectory(path);
                    getSdcardFileDir(path);
                    return true;
                }
            }

            break;
        default:
            break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        FileInfo info = (FileInfo) mAdapter.getItem(position);

        if (mEditMode) {
            return false;

        } else {
            if (!info.isFile()) {
                return false;
            }

            mEditMode = true;
            mAdapter.setStatus(mEditMode);

            String path = info.getPath();

            if (mSelectedPaths.contains(path)) {
                mSelectedPaths.remove(path);
            } else {
                mSelectedPaths.add(path);
            }

            mAdapter.notifyDataSetChanged();

            mFooter.setVisibility(View.VISIBLE);
            checkAllSelected();
            return true;
        }

    }

    public void checkAllSelected() {
        if (isSelectedAll()) {
            mSelected.setText(R.string.all_no_selected);
        } else {
            mSelected.setText(R.string.all_selected);
        }
    }

    private boolean isSelectedAll() {
        for (FileInfo info : mFileInfos) {
            if (info.isFile() && !mSelectedPaths.contains(info.getPath())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mCancel)) {

            mSelectedPaths.clear();
            mEditMode = false;
            mAdapter.setStatus(mEditMode);
            mFooter.setVisibility(View.GONE);

        } else if (v.equals(mConfirm)) {

            mEditMode = false;
            mAdapter.setStatus(mEditMode);
            mFooter.setVisibility(View.GONE);

            final List<String> paths = new ArrayList<String>();
            paths.addAll(mSelectedPaths);
            mSelectedPaths.clear();
            decodeFiles(paths);
        } else if (v.equals(mSelected)) {

            if (isSelectedAll()) {
                mSelected.setText(R.string.all_selected);
                mSelectedPaths.clear();
            } else {
                mSelected.setText(R.string.all_no_selected);
                mSelectedPaths.clear();
                for (FileInfo info : mFileInfos) {
                    if (info.isFile()) {
                        mSelectedPaths.add(info.getPath());
                    }
                }
            }

        }
        mAdapter.notifyDataSetChanged();
    }

    public void decodeFiles(List<String> paths) {
        // showLoadingDialog();

        final int maxValue = 100;
        final ProgressDialog pd;
        pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage(this.getResources().getString(R.string.decoding));
        pd.setCancelable(false);
        pd.show();

        pd.setMax(maxValue);

        DecodeTask task = new DecodeTask(this, paths, new IDecodeProgress() {

            @Override
            public void decoding(int progress) {
                pd.setProgress(progress);
            }

            @Override
            public int decodeStart() {
                return maxValue;
            }

            @Override
            public void decodeFinished(PageCache cache) {
                pd.dismiss();
                String jumpCode = ActivityConfig.getInstance().getActivityCode(IDCMListActivity.class);
                ExchangeManager.jump(jumpCode, cache, null, StorageFileActivity.this);
            }

            @Override
            public void decodeFailed(DecodeError error) {

            }
        });

        DecodeThreadPool.getInstance().excuteTask(task);

    }

    public class FileInfo {
        private boolean isFile = true;
        private String  path   = "";
        private String  name   = "";

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isFile() {
            return isFile;
        }

        public void setFile(boolean isFile) {
            this.isFile = isFile;
        }

    }
}
