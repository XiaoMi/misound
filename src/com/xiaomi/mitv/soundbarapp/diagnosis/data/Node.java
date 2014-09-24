package com.xiaomi.mitv.soundbarapp.diagnosis.data;

import java.util.List;

/**
 * Created by chenxuetong on 7/9/14.
 */
public class Node implements Comparable<Node>{
    private Node mParent;
    private OrderedList<Node> mChildren = new OrderedList<Node>();
    private final QAElement mElement;
    private final int mOrder;

    public Node(QAElement e, int order){
        mElement = e;
        mOrder = order;
        if(mElement==null) throw  new IllegalArgumentException();
    }

    public Node getParent() {
        return mParent;
    }

    public void setParent(Node mPre) {
        this.mParent = mPre;
    }

    public int getOrder(){ return mOrder;}

    public List<Node> getChildren() {
        return mChildren;
    }

    public void addNext(Node node){
        node.setParent(this);
        mChildren.add(node);
    }

    public QAElement getElement() {
        return mElement;
    }

    @Override
    public int compareTo(Node another) {
        return mOrder -another.mOrder;
    }
}
