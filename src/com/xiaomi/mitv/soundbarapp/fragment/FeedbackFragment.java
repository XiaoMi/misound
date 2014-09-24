package com.xiaomi.mitv.soundbarapp.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaomi.mitv.idata.client.app.AppData;
import com.xiaomi.mitv.idata.util.iDataCenterORM;
import com.xiaomi.mitv.soundbarapp.R;
import com.xiaomi.mitv.soundbar.provider.SoundBarORM;

public class FeedbackFragment extends BaseFragment {
    View mMainView;
	View mSubmitBtn;
	Toast mToast;
	EditText mMsg;
	EditText mPhone;
    public static FeedbackFragment newInstance() {
        return new FeedbackFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_feedback, container, false);
        return mMainView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((TextView)mMainView.findViewById(R.id.action_bar_text)).setText(R.string.fb_title);
        mSubmitBtn = findViewbyId(R.id.submit);
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	sendFeedback();
            }
        });
        
    	mMsg = findViewbyId(R.id.msg);
    	mPhone = findViewbyId(R.id.phone);
    }
    
    private void sendFeedback(){
        String str = getString(R.string.fb_tost);
    	if(mMsg.getText().length()==0){
            // Tell the user about what we did.
            if (mToast != null) {
                mToast.cancel();
            }
            String newstr = str.replace("xxx", getActivity().getText(R.string.fb_msg));
            mToast = Toast.makeText(getActivity(), newstr,
                    Toast.LENGTH_LONG);
            mToast.show();
            return;
    	}
    	if(mPhone.getText().length()==0){
            // Tell the user about what we did.
            if (mToast != null) {
                mToast.cancel();
            }

            mToast = Toast.makeText(getActivity(), getActivity().getText(R.string.fb_contact),
                    Toast.LENGTH_LONG);
            mToast.show();
            return;
    	}

    	
    	String tag = "feedback";
        String body = mMsg.getText().toString();
        body += "\n\n soundbar version="+ SoundBarORM.getSettingValue(this.getActivity(), SoundBarORM.dfuCurrentVersion) +
                "\n\n addr version="+ SoundBarORM.getSettingValue(this.getActivity(), SoundBarORM.addressName);

    	iDataCenterORM.getInstance(getActivity()).sendFeedback(this.getActivity().getPackageName(), AppData.appKey,
                tag, "", body,
    			mPhone.getText().toString(), "", "");
        mToast = Toast.makeText(getActivity(), R.string.fb_submit_finish,
                Toast.LENGTH_LONG);
        mToast.show();
        getActivity().onBackPressed();
    }
}
