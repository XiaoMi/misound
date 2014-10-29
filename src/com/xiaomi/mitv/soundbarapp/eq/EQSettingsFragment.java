package com.xiaomi.mitv.soundbarapp.eq;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.xiaomi.mitv.soundbarapp.R;
import com.xiaomi.mitv.soundbarapp.WrapperActivity;
import com.xiaomi.mitv.soundbarapp.fragment.BaseFragment;
import com.xiaomi.mitv.utils.Log;

import java.util.HashMap;

/**
 * Created by chenxuetong on 8/27/14.
 */
public class EQSettingsFragment extends BaseFragment implements View.OnClickListener {
    private View mMainView;
    private View mLoading;
    private EQManager mEQManager = new EQManager();

    public static EQSettingsFragment newInstance() {
        return new EQSettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.eq_base_setting, container, false);
        return mMainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((View)findViewbyId(R.id.actionbar)).setOnClickListener(this);
        ((TextView)findViewbyId(R.id.action_bar_text)).setText(R.string.eq_setting_title);
        mLoading = findViewbyId(R.id.loading);

        for(int id: ids()){
            ItemViewHolder holder = getItemView(id);
            EQItem item = mItemResources.get(id);
            holder.imageView.setImageResource(item.imgId);
            holder.nameView.setText(item.nameId);
            holder.container.setOnClickListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDeviceEQ();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.actionbar:
                getActivity().onBackPressed();
                break;
            case R.id.eq_style_standard:
            case R.id.eq_style_tv:
            case R.id.eq_style_movie:
            case R.id.eq_style_pop:
            case R.id.eq_style_rock:
            case R.id.eq_style_class:
            case R.id.eq_style_jazz:
                onSelectStyle(v.getId());
                break;
            case R.id.eq_style_custom:
                switch2UserEQSetting();
                break;
        }
    }

    private void switch2UserEQSetting() {
        WrapperActivity.go(getActivity(), WrapperActivity.FRAGMENT_USER_EQ, true);
    }

    private int[] ids(){
        return new int[]{R.id.eq_style_standard, R.id.eq_style_tv, R.id.eq_style_movie, R.id.eq_style_pop,
                R.id.eq_style_rock, R.id.eq_style_class, R.id.eq_style_jazz, R.id.eq_style_custom};
    }

    private void loadDeviceEQ(){
        runTask(new Runnable() {
            @Override
            public void run() {
                showLoading(true);
                EQManager manager = new EQManager();
                EQStyle style = manager.readSoundBarStyle(getActivity());
                final int id = manager.idOfStyle(style);

                updateUi(new Runnable() {
                    @Override
                    public void run() {
                        int set2Id = id;
                        if(set2Id == -1){
                            Log.logD("EQ", "load EQ failed, set to default!");
                            set2Id = R.id.eq_style_standard;
                        }
                        selectUIStyle(set2Id);
                    }
                });
                showLoading(false);
            }
        });
    }

    private void showLoading(final boolean show){
        updateUi(new Runnable() {
            @Override
            public void run() {
                mLoading.setVisibility(show?View.VISIBLE:View.GONE);
            }
        });
    }

    private void onSelectStyle(final int id){
        runTask(new Runnable() {
            @Override
            public void run() {
                try {
                    showLoading(true);
                    long begin = System.currentTimeMillis();
                    if(mEQManager.setUserEQ(getActivity(), id)){
                        updateUi(new Runnable() {
                            @Override
                            public void run() {
                                selectUIStyle(id);
                            }
                        });
                    }else{
                        Toast.makeText(getActivity(), "Failed ot set EQ!", Toast.LENGTH_LONG).show();
                    }
                    long wait = 1000-(System.currentTimeMillis()-begin);
                    if(wait>0){
                        try{Thread.sleep(wait);}catch (Exception e){}
                    }
                    showLoading(false);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void selectUIStyle(int id){
        for(int i : ids()) {
            ItemViewHolder h = getItemView(i);
            h.selector.setVisibility(i==id?View.VISIBLE:View.GONE);
        }
    }

    private static final class EQItem{
        int imgId;
        int nameId;

        EQItem(int imgId, int nameId){
            this.imgId = imgId;
            this.nameId = nameId;
        }
    }

    private ItemViewHolder getItemView(int id){
        View itemView = mMainView.findViewById(id);
        ItemViewHolder holder = new ItemViewHolder();
        holder.container = itemView;
        holder.imageView = (ImageView)itemView.findViewById(R.id.eq_image);
        holder.nameView = (TextView)itemView.findViewById(R.id.eq_name);
        holder.selector = (ImageView)itemView.findViewById(R.id.eq_selector);
        return holder;
    }

    private static final class ItemViewHolder{
        View container;
        ImageView imageView;
        TextView nameView;
        ImageView selector;
    }

    private SparseArray<EQItem> mItemResources = new SparseArray<EQItem>();
    {
        mItemResources.put(R.id.eq_style_standard, new EQItem(R.drawable.ic_eq_style_standard, R.string.eq_standar));
        mItemResources.put(R.id.eq_style_movie, new EQItem(R.drawable.ic_eq_style_movie, R.string.eq_movie));
        mItemResources.put(R.id.eq_style_tv, new EQItem(R.drawable.ic_eq_style_tv, R.string.eq_tv));
        mItemResources.put(R.id.eq_style_pop, new EQItem(R.drawable.ic_eq_style_pop, R.string.eq_pop));
        mItemResources.put(R.id.eq_style_rock, new EQItem(R.drawable.ic_eq_style_rock, R.string.eq_rock));
        mItemResources.put(R.id.eq_style_class, new EQItem(R.drawable.ic_eq_style_class, R.string.eq_class));
        mItemResources.put(R.id.eq_style_jazz, new EQItem(R.drawable.ic_eq_style_jazz, R.string.eq_jazz));
        mItemResources.put(R.id.eq_style_custom, new EQItem(R.drawable.ic_eq_style_custom, R.string.eq_custom));
    }
}

