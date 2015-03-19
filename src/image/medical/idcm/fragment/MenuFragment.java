package image.medical.idcm.fragment;

import image.medical.idcm.R;
import image.medical.idcm.storage.StorageFileActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

public class MenuFragment extends BaseFragment implements OnChildClickListener {

    private LayoutInflater     mInflater;
    private ExpandableListView mExpandableList;
    private MenuAdapter        mAdapter;

    private int[]              mGroups = new int[] { R.string.dicom_image, R.string.personal, R.string.about,
            R.string.system           };
    private int[][]            mChilds = new int[][] { { R.string.local_read }, { R.string.password_modify },
            { R.string.version_update, R.string.feedback, R.string.about_us },
            { R.string.serve_setting, R.string.clear_cache, R.string.logout } };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_menu, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mInflater = LayoutInflater.from(getActivity());

        mExpandableList = (ExpandableListView) getView().findViewById(R.id.menu);
        mExpandableList.setGroupIndicator(null);
        mAdapter = new MenuAdapter();
        mExpandableList.setAdapter(mAdapter);

        setControlls();

        mExpandableList.setOnChildClickListener(this);
    }

    private void setControlls() {

        mExpandableList.setOnGroupClickListener(new OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });

        int count = mAdapter.getGroupCount();
        for (int i = 0; i < count; i++) {
            mExpandableList.expandGroup(i);
        }
    }

    class MenuAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return mGroups.length;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mChilds[groupPosition].length;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mGroups[groupPosition];
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mChilds[groupPosition][childPosition];
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupViewHolder groupViewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_menu_group, parent, false);
                groupViewHolder = new GroupViewHolder();
                groupViewHolder.mGroupTitle = (TextView) convertView.findViewById(R.id.group_title);
                convertView.setTag(groupViewHolder);
            } else {
                groupViewHolder = (GroupViewHolder) convertView.getTag();
            }

            int title = (Integer) getGroup(groupPosition);
            groupViewHolder.mGroupTitle.setText(title);

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                ViewGroup parent) {
            ChildViewHolder childViewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_menu_child, parent, false);
                childViewHolder = new ChildViewHolder();
                childViewHolder.mChildName = (TextView) convertView.findViewById(R.id.child_name);
                convertView.setTag(childViewHolder);
            } else {
                childViewHolder = (ChildViewHolder) convertView.getTag();
            }

            int name = (Integer) getChild(groupPosition, childPosition);
            childViewHolder.mChildName.setText(name);

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        class GroupViewHolder {
            private TextView mGroupTitle;
        }

        class ChildViewHolder {
            private TextView mChildName;
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        if (groupPosition == 0 && childPosition == 0) {
            Intent intent = new Intent(getActivity(), StorageFileActivity.class);
            startActivity(intent);
        }
        return true;
    }
}
