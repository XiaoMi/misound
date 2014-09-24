package com.xiaomi.mitv.soundbarapp.diagnosis;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.JsonWriter;
import android.view.View;
import android.widget.Toast;
import com.xiaomi.mitv.soundbarapp.diagnosis.data.Entry;
import com.xiaomi.mitv.soundbarapp.diagnosis.data.Node;
import com.xiaomi.mitv.soundbarapp.diagnosis.data.QAElement;
import com.xiaomi.mitv.utils.IOUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;

/**
 * Created by chenxuetong on 7/9/14.
 [
 {id:"1",title:"音响连接", questions:[{id:"11", title:"手机和音响连接不上", fixes:[{id:"111",img:[1_1_1_1.png,1_1_1_2.png,1_1_1_3.png]},{id:"112",img:[1_1_2.png]},{id:"113",img:[1_1_3.png]},{id:"114",img:[1_1_4.png]}]}, {id:"12", title:"条形音响同低音炮连接不上", fixes:[{id:"121",img:[1_2_1.png]},{id:"122",img:[1_2_2.png]},{id:"123",img:[1_2_3.png]}]}, {id:"13", title:"音响不想被其他人连接", fixes:[{id:"131",img:[1_3_1.png]},{id:"132",img:[1_3_2_1.png,1_3_2_2.png]}]}]},
 {id:"2",title:"音响没有声音", questions:[{id:"21", title:"检查spdif线是否插好", fixes:[{id:"211",img:[2_1_1.png]},{id:"212",img:[2_1_2_1.png,2_1_2_2.png]},{id:"213",img:[2_1_3.png]}]}, {id:"22", title:"蓝牙连接音响无声音", fixes:[{id:"221",img:[2_2_1.png]},{id:"222",img:[2_2_2.png]},{id:"223",img:[2_2_3.png]}]},{id:"23", title:"模拟输入线无声音", fixes:[{id:"231",img:[2_3_1.png]},{id:"232",img:[2_3_2.png]},{id:"233",img:[2_3_3.png]}]}, {id:"24", title:"低音炮无声音", fixes:[{id:"241",img:[2_4_1.png]},{id:"242",img:[2_4_2.png]}]}]},
 {id:"3",title:"音响音质", questions:[{id:"31", title:"音响音质问题", fixes:[{id:"311",img:[3_1_1.png]},{id:"312",img:[3_1_2.png]},{id:"313",img:[3_1_3.png]}]}]},
 {id:"4",title:"音响升级", questions:[{id:"41", title:"手机客户端连接音响失败", fixes:[{id:"411",img:[4_1_1.png]},{id:"412",img:[4_1_2_1.png, 4_1_2_2.png]}]}]}
 ]
 */
public class Engine {
    private static final String TAG = "examine";

    private static final String CONF_DIR = "examine";
    private static final String CONF_FILE = "conf.json";

    private static final int ACTION_BACK = 0;
    private static final int ACTION_NEXT = 1;

    private HashMap<Integer, Entry> mEntries;
    private final Context mContext;
    private ViewWrapper mUiContainer;
    private ConfDecoder3 mDecoder;


    public Engine(Context context){
        mContext = context;
        mDecoder = new ConfDecoder3();
    }

    //for test
    Engine(String examineJson) throws JSONException{
        mContext = null;
        mDecoder = new ConfDecoder3();
        mEntries = mDecoder.decode(new JSONArray(examineJson));
    }

    public boolean init(){
        try {
            String json = loadConfig();
            if(json != null){
                mEntries = mDecoder.decode(new JSONArray(json));
            }
            return mEntries!=null;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean bindView(ViewWrapper view){
        mUiContainer = view;
        go2(null, ACTION_NEXT);
        return true;
    }

    private String loadConfig() {
        AssetManager asm = mContext.getAssets();
        InputStream in = null;
        try {
            in = asm.open(CONF_DIR + File.separator + CONF_FILE);
            return new String(IOUtil.readInputAsBytes(in), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(in!=null)try {in.close();}catch (IOException e){}
        }
        return null;
    }

    private Drawable loadResource(String imageName){
        if(imageName == null) return null;

        AssetManager asm = mContext.getAssets();
        InputStream input = null;
        try {
            input = asm.open(CONF_DIR+File.separator+imageName);
            return new BitmapDrawable(mContext.getResources(), input);
        }catch (IOException e){
            e.printStackTrace();
            if(input!=null)try {input.close();}catch (IOException ei){}
        }
        return null;
    }

    //core function to switch the UI
    private void go2(Node node, int viaAction){
        if(node == null) { //top
            bindCategoryListLayout();
            return;
        }

        QAElement element = node.getElement();
        if(element.isCategory()){
            bindQuestionListLayout(node);
        }else if(element.isQuestion()){
            if(viaAction==ACTION_BACK){
                Node category = node.getParent();
                bindQuestionListLayout(category);
            }else {
                bindAnswerView(node);
            }
        }else if(element.isFix()){
            bindFixView(node);
        }
    }

    private void bindFixView(Node targetAnswer) {
        if(!targetAnswer.getElement().isFix()){
            throw new IllegalStateException("Not a Fix, but " + targetAnswer.getElement().getType());
        }

        QAElement targetData = targetAnswer.getElement();
        Node parent = targetAnswer.getParent();
        //find left and right
        Node left = null;
        Node right = null;
        List<Node> answerNodes = parent.getChildren();
        for(int i=0; i<answerNodes.size(); i++){
            Node item = answerNodes.get(i);
            if(item.getElement() == targetData){
                if(i>0) {
                    left = answerNodes.get(i - 1);
                }
                if(i<answerNodes.size()-1){
                    right = answerNodes.get(i+1);
                }
                break;
            }
        }
        ViewWrapper.FixViewHolder fixView = mUiContainer.getFixViewHolder();
        String[] imgs = targetData.getImagePath();
        ArrayList<Drawable> fix_imags = new ArrayList<Drawable>();
        for(String path: imgs){
            fix_imags.add(loadResource(path));
        }
        fixView.setImage(fix_imags);
        fixView.setFixStep(targetAnswer.getOrder());
        mUiContainer.showFixSuggestion(parent.getElement().getText());
        mUiContainer.hideBottomButtons();
        if(left==null){
            left = parent;
        }
        mUiContainer.showBackButton(left, mUIBackActionListener);
        if(right!=null){
            mUiContainer.showNextButton(right, mUINextActionListener, "没有，尝试其他方法");
        } else{
            mUiContainer.showNextButton(null, mFeedbackActionListener, "没有，我要反馈");
        }
        mUiContainer.showFixedButton(targetAnswer, mFixedListener);
    }

    private void bindAnswerView(Node question) {
        if(!question.getElement().isQuestion()){
            throw new IllegalStateException("Not a question, but " + question.getElement().getType());
        }
        List<Node> answerNodes = question.getChildren();
        Node first = answerNodes.get(0);
        bindFixView(first);
    }

    private void bindQuestionListLayout(Node category) {
        //TODO
        if(category.getChildren().size()==0){
            Toast.makeText(mContext, "正在开发中，敬请期待！", Toast.LENGTH_LONG).show();
            return;
        }

        if(!category.getElement().isCategory()){
            throw new IllegalStateException("Not a category, but " + category.getElement().getType());
        }
        List<Node> questionNode = category.getChildren();
        mUiContainer.setQuestions(questionNode, mUINextActionListener);
        mUiContainer.showQuestions(category.getElement().getText());
        mUiContainer.hideBottomButtons();
        mUiContainer.showBackButton(null, mUIBackActionListener);
    }

    private void bindCategoryListLayout(){
        mUiContainer.initCategory(mEntries, mUINextActionListener);
        mUiContainer.showCategories();
        mUiContainer.hideBottomButtons();
        mUiContainer.showBackButton(null, null);
    }

    private View.OnClickListener mUIBackActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            go2((Node)v.getTag(), ACTION_BACK);
        }
    };

    private View.OnClickListener mUINextActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            go2((Node)v.getTag(), ACTION_NEXT);
        }
    };

    private View.OnClickListener mFixedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mUiContainer.quitFragment();
        }
    };

    private View.OnClickListener mFeedbackActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mUiContainer.quit2Feedback();
        }
    };

    public HashMap<Integer, Entry> getEntries() {
        return mEntries;
    }

    /**
     [
     {id:"1",title:"音响连接", questions:[{id:"11", title:"手机和音响连接不上", fixes:[{id:"111",img:[1_1_1_1.png,1_1_1_2.png,1_1_1_3.png]},{id:"112",img:[1_1_2.png]},{id:"113",img:[1_1_3.png]},{id:"114",img:[1_1_4.png]}]}, {id:"12", title:"条形音响同低音炮连接不上", fixes:[{id:"121",img:[1_2_1.png]},{id:"122",img:[1_2_2.png]},{id:"123",img:[1_2_3.png]}]}, {id:"13", title:"音响不想被其他人连接", fixes:[{id:"131",img:[1_3_1.png]},{id:"132",img:[1_3_2_1.png,1_3_2_2.png]}]}]},
     {id:"2",title:"音响没有声音", questions:[{id:"21", title:"检查spdif线是否插好", fixes:[{id:"211",img:[2_1_1.png]},{id:"212",img:[2_1_2_1.png,2_1_2_2.png]},{id:"213",img:[2_1_3.png]}]}, {id:"22", title:"蓝牙连接音响无声音", fixes:[{id:"221",img:[2_2_1.png]},{id:"222",img:[2_2_2.png]},{id:"223",img:[2_2_3.png]}]},{id:"23", title:"模拟输入线无声音", fixes:[{id:"231",img:[2_3_1.png]},{id:"232",img:[2_3_2.png]},{id:"233",img:[2_3_3.png]}]}, {id:"24", title:"低音炮无声音", fixes:[{id:"241",img:[2_4_1.png]},{id:"242",img:[2_4_2.png]}]}]},
     {id:"3",title:"音响音质", questions:[{id:"31", title:"音响音质问题", fixes:[{id:"311",img:[3_1_1.png]},{id:"312",img:[3_1_2.png]},{id:"313",img:[3_1_3.png]}]}]},
     {id:"4",title:"音响升级", questions:[{id:"41", title:"手机客户端连接音响失败", fixes:[{id:"411",img:[4_1_1.png]},{id:"412",img:[4_1_2_1.png, 4_1_2_2.png]}]}]}
     ]
     */
    private static class ConfDecoder3{
        private HashMap<Integer, Entry> decode(JSONArray jCategoryList) throws JSONException {
            HashMap<Integer, Entry> ret = new HashMap<Integer, Entry>();

            for(int i=0; i<jCategoryList.length(); i++){
                JSONObject category = jCategoryList.getJSONObject(i);
                Node aCategory = parseCategory(category);
                int id = category.getInt("id");
                ret.put(id, new Entry(aCategory));
            }
            return ret;
        }

        //{id:"3",title:"音响音质", questions:[{id:"31", title:"音响音质问题", fixes:[{id:"311",img:[3_1_1.png]},{id:"312",img:[3_1_2.png]},{id:"313",img:[3_1_3.png]}]}]},
        private static Node parseCategory(JSONObject iCategory) throws JSONException {
            String title = iCategory.getString("title");
            JSONArray questions = iCategory.getJSONArray("questions");
            int id = iCategory.getInt("id");

            QAElement element = QAElement.createCategory(title);
            Node category = new Node(element, id);
            category.setParent(null);

            for(int i=0; i<questions.length(); i++){
                //{id:q1,order:1,value:[{id:a1,order:1},{id:a1,order:2},{id:a1,order:3},{id:a1,order:4},{id:a1,order:5},{id:a1,order:6}]}
                category.addNext(parseQuestion(questions.getJSONObject(i)));
            }
            return category;
        }

        //{id:"41", title:"手机客户端连接音响失败", fixes:[{id:"411",img:[4_1_1.png]},{id:"412",img:[4_1_2_1.png, 4_1_2_2.png]}]}
        private static Node parseQuestion(JSONObject jQuestion) throws JSONException {
            int id = jQuestion.getInt("id");
            String title = jQuestion.getString("title");
            JSONArray fixes = jQuestion.getJSONArray("fixes");

            QAElement element = QAElement.createQuestion(title);
            Node question = new Node(element, id);
            for(int i=0; i<fixes.length(); i++){
                JSONObject answer = fixes.getJSONObject(i);
                int aId = answer.getInt("id");
                JSONArray anwser_imgs = answer.getJSONArray("img");
                String[] imgs = new String[anwser_imgs.length()];
                for(int j=0; j<imgs.length; j++){
                    imgs[j] = anwser_imgs.getString(j);
                }
                QAElement qa = QAElement.createFix(imgs);
                question.addNext(new Node(qa, aId));
            }
            return question;
        }
    }


//    private static class ConfDecoder{
//        private List<Entry> decode(JSONObject jsonObject) throws JSONException {
//            List<Entry> ret = new ArrayList<Entry>();
//
//            HashMap<String, QAElement> ele_map = parseElements(jsonObject.getJSONArray("items"));
//            JSONArray jEntries = jsonObject.getJSONArray("entries");
//
//            for(int i=0; i<jEntries.length(); i++){
//                JSONObject category = jEntries.getJSONObject(i);
//                Node aCategory = parseCategory(category, ele_map);
//                ret.add(new Entry(aCategory));
//            }
//            return ret;
//        }
//
//        ////{id:c1,order:1,value:[{id:q1,order:1,value:[a1,a2,a3,a4,a5,a6]},{id:q2,order:2,value:[a7,a8,a9,a10,a6]},{id:q3,order:3,value:[a11,a12]}]}
//        private static Node parseCategory(JSONObject iCategory, HashMap<String, QAElement> ele_map) throws JSONException {
//            String catId = iCategory.getString("id");
//            int order = iCategory.getInt("order");
//            JSONArray values = iCategory.getJSONArray("value");
//
//            if(!ele_map.containsKey(catId)) throw new JSONException("Not found category id: " + catId);
//            Node category = new Node(ele_map.get(catId), order);
//            category.setParent(null);
//
//            for(int i=0; i<values.length(); i++){
//                //{id:q1,order:1,value:[{id:a1,order:1},{id:a1,order:2},{id:a1,order:3},{id:a1,order:4},{id:a1,order:5},{id:a1,order:6}]}
//                JSONObject jQuestion = values.getJSONObject(i);
//                category.addNext(parseQuestion(jQuestion, ele_map));
//            }
//            return category;
//        }
//
//        //{id:q1,order:1,value:[{id:a1,order:1},{id:a1,order:2},{id:a1,order:3},{id:a1,order:4},{id:a1,order:5},{id:a1,order:6}]}
//        private static Node parseQuestion(JSONObject jQuestion, HashMap<String, QAElement> ele_map) throws JSONException {
//            String qId = jQuestion.getString("id");
//            int order = jQuestion.getInt("order");
//            JSONArray values = jQuestion.getJSONArray("value");
//
//            Node question = new Node(ele_map.get(qId), order);
//            for(int i=0; i<values.length(); i++){
//                JSONObject answer = values.getJSONObject(i);
//                String answerId = answer.getString("id");
//                int answer_order = answer.getInt("order");
//                question.addNext(new Node(ele_map.get(answerId), answer_order));
//            }
//            return question;
//        }
//
//        private static HashMap<String, QAElement> parseElements(JSONArray a) throws JSONException{
//            HashMap<String, QAElement> h = new HashMap<String, QAElement>();
//            for(int i=0; i<a.length(); i++){
//                QAElement e = toElement(a.getJSONObject(i));
//                h.put(e.getId(), e);
//            }
//            return h;
//        }

//        private static QAElement toElement(JSONObject o) throws JSONException{
//            String type = o.getString("type");
//            String id = o.getString("id");
//            String text = o.getString("text");
//            String image = o.optString("image", null);
//            return QAElement.create(id, type, text, image);
//        }
//    }

//    private static class ConfDecoder2{
//        private List<Entry> decode(JSONArray jCategoryList) throws JSONException {
//            List<Entry> ret = new ArrayList<Entry>();
//
//            for(int i=0; i<jCategoryList.length(); i++){
//                JSONObject category = jCategoryList.getJSONObject(i);
//                Node aCategory = parseCategory(category);
//                ret.add(new Entry(aCategory));
//            }
//            return ret;
//        }
//
//        //{desc:c2Text,order:1,children:[{desc:q1Text,order:1,children:[{desc:a1Text,order:1, image:b.jpg}]}]}
//        private static Node parseCategory(JSONObject iCategory) throws JSONException {
//            String desc = iCategory.getString("desc");
//            int order = iCategory.getInt("order");
//            JSONArray values = iCategory.getJSONArray("children");
//
//            QAElement element = QAElement.create(QAElement.TYPE_CATEGORY, desc, null);
//            Node category = new Node(element, order);
//            category.setParent(null);
//
//            for(int i=0; i<values.length(); i++){
//                //{id:q1,order:1,value:[{id:a1,order:1},{id:a1,order:2},{id:a1,order:3},{id:a1,order:4},{id:a1,order:5},{id:a1,order:6}]}
//                JSONObject jQuestion = values.getJSONObject(i);
//                category.addNext(parseQuestion(jQuestion));
//            }
//            return category;
//        }
//
//        //{"desc:c2Text",order:1,children:[{desc:a1Text,order:1, image:b.jpg}]}
//        private static Node parseQuestion(JSONObject jQuestion) throws JSONException {
//            String desc = jQuestion.getString("desc");
//            int order = jQuestion.getInt("order");
//            JSONArray values = jQuestion.getJSONArray("children");
//
//            QAElement element = QAElement.create(QAElement.TYPE_QUESTION, desc, null);
//            Node question = new Node(element, order);
//            for(int i=0; i<values.length(); i++){
//                JSONObject answer = values.getJSONObject(i);
//                String a_desc = answer.getString("desc");
//                int answer_order = answer.getInt("order");
//                String image = answer.optString("image", null);
//                QAElement qa = QAElement.create(QAElement.TYPE_ANSWER, a_desc, image);
//                question.addNext(new Node(qa, answer_order));
//            }
//            return question;
//        }
//    }

    //{desc:c2Text,order:1,children:[{desc:q1Text,order:1,children:[{desc:a1Text,order:1, image:b.jpg}]}]}
    public static class ConfEncoder2{
        private StringWriter mBuffer;
        private JsonWriter mWriter;
        public ConfEncoder2() throws IOException {
            mBuffer = new StringWriter();
            mWriter = new JsonWriter(mBuffer);
            mWriter.beginArray();
        }
        public ConfEncoder2 beginCategory(String desc, int order) throws IOException {
            mWriter.beginObject()
                    .name("desc").value(desc)
                    .name("order").value(order)
                    .name("children").beginArray();
            return this;
        }

        public ConfEncoder2 beginQuestion(String desc, int order) throws IOException{
            mWriter.beginObject()
                    .name("desc").value(desc)
                    .name("order").value(order)
                    .name("children").beginArray();
            return this;
        }
        public ConfEncoder2 addAnswer(String desc, int order, String img) throws IOException{
            mWriter.beginObject()
                    .name("desc").value(desc)
                    .name("order").value(order)
                    .name("image").value(img)
                    .endObject();
            return this;
        }

        public ConfEncoder2 endQuestion() throws IOException {
            mWriter.endArray().endObject();
            return this;
        }

        public ConfEncoder2 endCategory() throws IOException {
            mWriter.endArray().endObject();
            return this;
        }

        public String encode() throws IOException {
            mWriter.endArray();
            return mBuffer.toString();
        }
    }
}
