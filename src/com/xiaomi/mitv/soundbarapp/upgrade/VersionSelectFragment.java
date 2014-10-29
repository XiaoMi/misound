package com.xiaomi.mitv.soundbarapp.upgrade;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.xiaomi.mitv.content.FirmwareVersion;
import com.xiaomi.mitv.soundbar.provider.SoundBarORM;
import com.xiaomi.mitv.soundbarapp.R;
import com.xiaomi.mitv.soundbarapp.provider.DataProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by chenxuetong on 7/31/14.
 */
public class VersionSelectFragment extends DialogFragment implements View.OnClickListener {
    private ViewGroup mMainView;
    private Spinner mVersionSpinner;
    private VersionLoader mLoader;
    private UpgradeFragment mUpgrade;

    private List<FirmwareVersion> mVersions = Collections.emptyList();
    private VersionAdapter mAdapter;

    public void setUpgradeHandler(UpgradeFragment handler){
        mUpgrade = handler;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainView = (ViewGroup)inflater.inflate(R.layout.upgrade_version_selection, container, false);
        return mMainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getDialog().setTitle(R.string.upgrade_version_selection_title);
        mVersionSpinner = (Spinner)mMainView.findViewById(R.id.upgrade_version_list);

        mAdapter = new VersionAdapter();
        mVersionSpinner.setAdapter(mAdapter);
        mVersionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onItemChecked(view);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mMainView.findViewById(R.id.upgrade_execute).setOnClickListener(this);

        mLoader = new VersionLoader();
        mLoader.execute();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mLoader != null){
            mLoader.cancel(false);
            mLoader = null;
        }
    }

    private void onItemChecked(View item){
        ViewHolder holder = (ViewHolder)item.getTag();
        FirmwareVersion version = holder.mVersion;
        holder.mVersionDesc.setText(version.description);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.upgrade_execute){
            ViewHolder holder = (ViewHolder)mVersionSpinner.getSelectedView().getTag();
            mVersions = Collections.emptyList();
            getFragmentManager().popBackStack();
            mUpgrade.forceUpgrade(holder.mVersion);
        }
    }

    private class ViewHolder{
        TextView mVersionName;
        TextView mVersionRelease;
        TextView mVersionDesc;
        FirmwareVersion mVersion;
    }

    private class VersionAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return mVersions.size();
        }

        @Override
        public Object getItem(int position) {
            if(position<0 || position>mVersions.size()) return null;
            return mVersions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final ViewGroup item = (ViewGroup)inflater.inflate(R.layout.upgrade_version_item, parent, false);
                ViewHolder holder = new ViewHolder();
                holder.mVersionName = (TextView)item.findViewById(R.id.version_name);
                holder.mVersionRelease = (TextView)item.findViewById(R.id.release_flag);
                holder.mVersionDesc = (TextView)mMainView.findViewById(R.id.version_desc);
                item.setTag(holder);
                convertView = item;
            }

            ViewHolder holder = (ViewHolder)convertView.getTag();
            FirmwareVersion version = mVersions.get(position);
            holder.mVersionName.setText("版本:"+version.codeName + "(" + version.code + ")");
            holder.mVersionRelease.setText(version.strFlag());
            holder.mVersion = version;
            return convertView;
        }
    }

    private class VersionLoader extends AsyncTask<Void,Void,List<FirmwareVersion>>{
        @Override
        protected void onPreExecute() {
            mMainView.findViewById(R.id.progress).setVisibility(View.VISIBLE);
            mMainView.findViewById(R.id.upgrade_execute).setVisibility(View.GONE);
            mMainView.findViewById(R.id.upgrade_version_list).setVisibility(View.GONE);
            mMainView.findViewById(R.id.version_desc).setVisibility(View.GONE);
        }

        @Override
        protected List<FirmwareVersion> doInBackground(Void... params) {
            List<FirmwareVersion> targets = new ArrayList<FirmwareVersion>();
            List<FirmwareVersion> versions = new DataProvider().listVersion();
            String currentVersion = SoundBarORM.getSettingValue(getActivity(), SoundBarORM.dfuCurrentVersion);
            for(FirmwareVersion v : versions){
                if(v.codeName.equals(currentVersion)){
                    continue;
                }
                boolean have = false;
                for(FirmwareVersion t : targets){
                    if(t.flag == v.flag){
                        have = true;
                        break;
                    }
                }
                if(!have){
                    targets.add(v);
                }
            }
            return targets;
        }

        @Override
        protected void onPostExecute(List<FirmwareVersion> versions) {
            if(isCancelled() || versions==null ) return;
            mVersions = versions;
            mAdapter.notifyDataSetChanged();
            mVersionSpinner.setSelection(0);
            mMainView.findViewById(R.id.progress).setVisibility(View.GONE);
            mMainView.findViewById(R.id.upgrade_execute).setVisibility(View.VISIBLE);
            mMainView.findViewById(R.id.upgrade_version_list).setVisibility(View.VISIBLE);
            mMainView.findViewById(R.id.version_desc).setVisibility(View.VISIBLE);
        }
    }
}
