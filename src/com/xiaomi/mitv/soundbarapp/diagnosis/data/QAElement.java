package com.xiaomi.mitv.soundbarapp.diagnosis.data;

/**
 * Created by chenxuetong on 7/9/14.
 */
public abstract class QAElement{
    public static final String TYPE_CATEGORY = "category";
    public static final String TYPE_QUESTION = "question";
    public static final String TYPE_ANSWER = "answer";

    protected String mType;
    protected String mText;
    protected String[] mImagePath; //relative

    public String getType() { return mType; }
    public String getText() { return mText; }
    public void setText(String text) { this.mText = trim(text); }
    public String[] getImagePath() { return mImagePath; }
    public void setImagePath(String[] mImagePath) { this.mImagePath = mImagePath; }
    public boolean isCategory(){ return this instanceof Category; }
    public boolean isQuestion(){ return this instanceof Question; }
    public boolean isFix(){ return this instanceof Fix; }

    public static QAElement createCategory(String text) {
        QAElement e =  new Category();
        e.setText(text);
        return e;
    }

    public static QAElement createQuestion(String text) {
        QAElement e =  new Question();
        e.setText(text);
        return e;
    }

    public static QAElement createFix(String[] imgs) {
        QAElement e =  new Fix();
        e.setImagePath(imgs);
        return e;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof QAElement)) return false;
        QAElement q = (QAElement)o;
        return mType.equals(q.mType) && mText.equals(q.mText);
    }

    public static class Category extends QAElement{
        Category(){mType = TYPE_CATEGORY;}
    }
    public static class Question extends QAElement{
        Question(){mType= TYPE_QUESTION;}
    }
    public static class Fix extends QAElement{
        Fix(){mType= TYPE_ANSWER;}
    }

    private String trim(String text){
        return text.trim()
                .replaceFirst("\n", "").replaceFirst("\n", "")
                .replaceFirst("\t", "");
    }
}
