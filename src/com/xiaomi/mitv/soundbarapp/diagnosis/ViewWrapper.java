package com.xiaomi.mitv.soundbarapp.diagnosis;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.xiaomi.mitv.soundbarapp.R;
import com.xiaomi.mitv.soundbarapp.diagnosis.data.Entry;
import com.xiaomi.mitv.soundbarapp.diagnosis.data.Node;
import com.xiaomi.mitv.soundbarapp.diagnosis.data.OrderedList;
import org.w3c.dom.Text;

import java.util.*;

/**
 * Created by ThinkPad User on 14-7-9.
 */
public class ViewWrapper {
    private Activity mContext;
    private DiagnosisFragment mFragment;
    private View mMainView;
    private ViewGroup mCategoryLayout;
    private ViewGroup mQuestionsLayout;
    private View mFixActionBar;
    private ViewGroup mFixViewLayout;
    private ViewGroup mBottomButtonLayout;
    private ViewGroup mCategoryList;
    private ViewGroup mQuestionsList;
    private TextView mBackButton;

    private TextView mFixedButton;
    private TextView mNextButton;

    private ViewPager mFixImageView;
    private TextView mTitleView;

    public View create(LayoutInflater inflater, ViewGroup container){
        mMainView =  inflater.inflate(R.layout.fragment_examine_main, container, false);
        View v = mMainView.findViewById(R.id.category_faq);
        return mMainView;
    }

    public void onActivityReady(Activity activity, DiagnosisFragment examineFragment){
        mContext = activity;
        mFragment = examineFragment;
        mCategoryLayout = (ViewGroup)mContext.findViewById(R.id.category_list_container);
        mQuestionsLayout = (ViewGroup)mContext.findViewById(R.id.question_list_container);
        mFixActionBar = mContext.findViewById(R.id.fix_action_bar);
        mFixViewLayout = (ViewGroup)mContext.findViewById(R.id.fix_container);
        mBottomButtonLayout = (ViewGroup)mContext.findViewById(R.id.bottom_button_container);

        mCategoryList = (ViewGroup)mContext.findViewById(R.id.category_list);
        mQuestionsList = (ViewGroup)mContext.findViewById(R.id.question_list);

        mBackButton = (TextView)mContext.findViewById(R.id.back);
        mFixedButton = (TextView)mContext.findViewById(R.id.fixed);
        mNextButton = (TextView)mContext.findViewById(R.id.next);

        mFixImageView = (ViewPager)mContext.findViewById(R.id.fix_image);
        mTitleView = ((TextView)mContext.findViewById(R.id.action_bar_text));
        mFixActionBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragment.goBack();
            }
        });

        mFixImageView.setOnPageChangeListener(new PagerChangeListener());
    }

    public boolean onActivityBackPressed() {
        try {
            if (!mBackButton.hasOnClickListeners()) return false;
            mBackButton.callOnClick();
            return true;
        }catch (Exception e){
            return false;
        }
    }


    public void showLoading(boolean yes) {
        if(yes) {
            mContext.findViewById(R.id.examine_loading).setVisibility(View.VISIBLE);
            mCategoryLayout.setVisibility(View.GONE);
            mQuestionsLayout.setVisibility(View.GONE);
            mFixViewLayout.setVisibility(View.GONE);
        }else{
            mContext.findViewById(R.id.examine_loading).setVisibility(View.GONE);
        }
    }

    public void hideAll(boolean hidden){
        int flag = hidden?View.GONE: View.VISIBLE;
        mCategoryLayout.setVisibility(flag);
        mQuestionsLayout.setVisibility(flag);
        mFixViewLayout.setVisibility(flag);
    }

    public void showCategories(){
        mTitleView.setText(R.string.main_entry_diagnose);
        mCategoryLayout.setVisibility(View.VISIBLE);
        mQuestionsLayout.setVisibility(View.GONE);
        mFixViewLayout.setVisibility(View.GONE);
    }

    public void showQuestions(String title){
        if(!TextUtils.isEmpty(title)) mTitleView.setText(title);
        mCategoryLayout.setVisibility(View.GONE);
        mQuestionsLayout.setVisibility(View.VISIBLE);
        mFixViewLayout.setVisibility(View.GONE);
    }

    public void showFixSuggestion(String title){
        if(!TextUtils.isEmpty(title)){
            ((TextView)mFixActionBar.findViewById(R.id.action_bar_text)).setText(title);
        }
        mCategoryLayout.setVisibility(View.GONE);
        mQuestionsLayout.setVisibility(View.GONE);
        mFixViewLayout.setVisibility(View.VISIBLE);
    }

    public void initCategory(HashMap<Integer, Entry> categories, View.OnClickListener listener){
        Set<Integer> keys = categories.keySet();
        for(Integer id : keys){
            View actionView = null;
            switch (id){
                case 1: actionView = mMainView.findViewById(R.id.category_connection);break;
                case 2: actionView = mMainView.findViewById(R.id.category_no_sound);break;
                case 3: actionView = mMainView.findViewById(R.id.category_quality);break;
                case 4: actionView = mMainView.findViewById(R.id.category_upgrade);break;
                default:continue;
            }
            actionView.setTag(categories.get(id).getRoot());
            actionView.setOnClickListener(listener);
        }
        mMainView.findViewById(R.id.category_faq).setOnClickListener(mFragment);
        mMainView.findViewById(R.id.category_bbs).setOnClickListener(mFragment);
    }

    public void setQuestions(List<Node> questions, View.OnClickListener listener){
        mQuestionsList.removeAllViews();
        LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for(Node node : questions){
            View view = li.inflate(R.layout.examine_button_layout, null, false);
            TextView button = (TextView)view.findViewById(android.R.id.text1);
            button.setText(node.getElement().getText());
            view.setTag(node);
            view.setOnClickListener(listener);
            mQuestionsList.addView(view);
        }
    }

    public FixViewHolder getFixViewHolder(){
        return new FixViewHolder();
    }

    public void showBackButton(Object tag, View.OnClickListener listener){
        mBackButton.setOnClickListener(listener);
        mBackButton.setTag(tag);
    }

    public void showFixedButton(Object tag, View.OnClickListener listener){
        mBottomButtonLayout.setVisibility(View.VISIBLE);
        mFixedButton.setVisibility(View.VISIBLE);
        mFixedButton.setOnClickListener(listener);
        mFixedButton.setTag(tag);
    }

    public void showNextButton(Object tag, View.OnClickListener listener, String text){
        mBottomButtonLayout.setVisibility(View.VISIBLE);
        mNextButton.setText(text);
        mNextButton.setVisibility(View.VISIBLE);
        mNextButton.setOnClickListener(listener);
        mNextButton.setTag(tag);
    }

    public void hideBottomButtons(){
        mBottomButtonLayout.setVisibility(View.GONE);
        mBackButton.setVisibility(View.GONE);
        mFixedButton.setVisibility(View.GONE);
        mNextButton.setVisibility(View.GONE);
    }

    public void quitFragment() {
        if (mFragment!=null) {
            mFragment.goBack();
        }
    }

    public void quit2Feedback() {
        if (mFragment!=null) {
            mFragment.go2Feedback();
        }
    }

    public class FixViewHolder{
        public void setImage(ArrayList<Drawable> drawable){
            ImageAdatper adatper = new ImageAdatper();
            adatper.setContent(drawable);
            mFixImageView.removeAllViews();
            mFixImageView.setAdapter(adatper);
            mFixImageView.setCurrentItem(0);
        }
        public void setFixStep(int step){
        }
    }


    private class PagerChangeListener implements ViewPager.OnPageChangeListener{
        @Override
        public void onPageScrolled(int i, float v, int i2) {}
        @Override
        public void onPageSelected(int i) {mFixImageView.setCurrentItem(i);}
        @Override
        public void onPageScrollStateChanged(int i) {}
    }


    private class ImageAdatper extends PagerAdapter {
        private ArrayList<ImageView> mContent = new ArrayList<ImageView>();

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((ImageView)object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView view = mContent.get(position);
            container.addView(view, 0);
            return view;
        }

        @Override
        public int getCount() {
            return mContent.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view==o;
        }

        public void setContent(ArrayList<Drawable> content) {
            for (Drawable d : content){
                ImageView view = new ImageView(mContext);
                view.setImageDrawable(d);
                view.setScaleType(ImageView.ScaleType.FIT_XY);
                mContent.add(view);
            }
        }
    }
}