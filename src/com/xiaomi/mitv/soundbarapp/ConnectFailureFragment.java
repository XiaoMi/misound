package com.xiaomi.mitv.soundbarapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class ConnectFailureFragment extends Fragment {
    private OnReconnectListener mListener;

    public interface OnReconnectListener {
        void onReconnect();
    }

    public static ConnectFailureFragment newInstance(OnReconnectListener rl){
        ConnectFailureFragment ff = new ConnectFailureFragment();
        ff.mListener = rl;
        return ff;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_connect_failure, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().findViewById(R.id.button_reconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReconnect();
            }
        });
    }

    private void onReconnect(){
        if(mListener!=null) mListener.onReconnect();
    }
}
