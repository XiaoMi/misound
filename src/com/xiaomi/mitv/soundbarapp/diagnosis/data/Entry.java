package com.xiaomi.mitv.soundbarapp.diagnosis.data;

/**
 * Created by chenxuetong on 7/9/14.
 */
public class Entry implements Comparable<Entry>{
    private final Node mRoot;
    private Node mCurrentView;

    public Entry(Node root){
        mRoot = root;
        mCurrentView = mRoot;
    }

    public Node getRoot(){
        return mRoot;
    }

    public boolean isBegin(){
        return mCurrentView==mRoot;
    }

    public boolean isEnd(){
        return (mCurrentView!=null)?mCurrentView.getChildren().size()==0:true;
    }

    public Node pre(){
        if(isBegin()) return null;
        return mCurrentView.getParent();
    }

    @Override
    public int compareTo(Entry another) {
        return mRoot.compareTo(another.getRoot());
    }
}
