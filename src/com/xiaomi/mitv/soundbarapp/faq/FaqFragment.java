package com.xiaomi.mitv.soundbarapp.faq;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.*;
import com.xiaomi.mitv.content.FaqData;
import com.xiaomi.mitv.soundbarapp.R;
import com.xiaomi.mitv.soundbarapp.fragment.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenxuetong on 7/3/14.
 */
public class FaqFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    private ListView mIndexView;
    private QAIndexAdapter mIndexAdapter;
    private DataLoader mLoader;

    public static FaqFragment newInstance(){
        return new FaqFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_faq_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mIndexView = (ListView)getActivity().findViewById(R.id.qa_item_list);
        mIndexAdapter = new QAIndexAdapter();
        mIndexView.setAdapter(mIndexAdapter);
        mIndexView.setOnItemClickListener(this);

        ((TextView)findViewbyId(R.id.action_bar_text)).setText(R.string.qa_title);
        ((View)findViewbyId(R.id.actionbar)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        mLoader = new DataLoader(getActivity());
        mLoader.execute();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mLoader!=null){
            mLoader.cancel(true);
            mLoader = null;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FaqData.Item item = (FaqData.Item)view.getTag();
        if(item == null){
            Toast.makeText(getActivity(), "null", Toast.LENGTH_SHORT).show();
            return;
        }
        final View indexView = getActivity().findViewById(R.id.qa_index_view);
        final View contentView = getActivity().findViewById(R.id.qa_detail_view);
        indexView.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);
        WebView content = ((WebView)getActivity().findViewById(R.id.qa_content));
        content.getSettings().setAllowContentAccess(true);
        content.loadDataWithBaseURL(null, item.getHtmlContent(), "text/html", "utf-8",  null);
    }

    @Override
    public boolean onBackPressed() {
        View indexView = getActivity().findViewById(R.id.qa_index_view);
        View contentView = getActivity().findViewById(R.id.qa_detail_view);
        if(indexView.getVisibility() == View.GONE) {
            indexView.setVisibility(View.VISIBLE);
            contentView.setVisibility(View.GONE);
            return true;
        }
        return super.onBackPressed();
    }

    public class QAIndexAdapter extends BaseAdapter{
        private List<FaqData.Item> objects = new ArrayList<FaqData.Item>();
        @Override
        public int getCount() {
            return objects.size();
        }

        @Override
        public Object getItem(int position) {
            if(position>objects.size()) return null;
            return objects.get(position);
        }

        @Override
        public long getItemId(int position) {
            return ((FaqData.Item)getItem(position)).getTimestamp();
        }

        /**
         * Populate new items in the list.
         */
        @Override public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.qa_index_list_item, parent, false);
            } else {
                view = convertView;
            }

            FaqData.Item item = (FaqData.Item)getItem(position);
            ((TextView)view.findViewById(R.id.qa_item_title)).setText(item.getTitle());
            view.setTag(item);
            return view;
        }


        public void setData(FaqData data) {
            objects.clear();
            if (data != null) {
                objects.addAll(data.getItems());
            }
            notifyDataSetInvalidated();
        }
    }

    private class DataLoader extends AsyncTask<Void,Void,FaqData> {
        private Activity mActivity;
        public DataLoader(Activity context) {
            mActivity = context;
        }

        private void postDataLoadingError(){
            Toast.makeText(mActivity, R.string.qa_loading_failure, Toast.LENGTH_LONG).show();
        }

        @Override
        protected FaqData doInBackground(Void... params) {
            final FaqDataStore store = new FaqDataStore(mActivity);
            FaqData qa = store.load();
            if (qa == null) {
                qa = store.loadRemote();
            }else{
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        store.refresh();
                    }
                }).start();
            }

            return qa;
        }

        @Override
        protected void onPostExecute(FaqData faqData) {
            if(isCancelled()) return;
            if(faqData == null){
                postDataLoadingError();
            }
            mIndexAdapter.setData(faqData);
        }
    }
}
