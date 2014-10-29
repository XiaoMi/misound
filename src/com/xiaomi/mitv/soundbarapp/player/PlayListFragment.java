package com.xiaomi.mitv.soundbarapp.player;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.*;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.xiaomi.mitv.soundbarapp.R;
import com.xiaomi.mitv.soundbarapp.fragment.BaseFragment;
import com.xiaomi.mitv.soundbarapp.util.Worker;
import com.xiaomi.mitv.widget.LetterIndexSilderBar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by chenxuetong on 10/8/14.
 */
public class PlayListFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener, LetterIndexSilderBar.OnTouchingLetterChangedListener, View.OnClickListener,
        PlayerFragment.OnPlayerStateListener{
    private ListView mSongList;
    private LetterIndexSilderBar mIndexBar;
    private SortAdapter mAdapter;
    private ImageView mPlayModeSwitcher;
    private TextView mPlayModeHint;
    private Player mPlayer;
    private Worker mWorker;
    private Handler mAlumDecoderHandler;

    public static PlayListFragment newInstance() {
        return new PlayListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mWorker = new Worker("player_alum_worker");
        mAlumDecoderHandler = new Handler(mWorker.getLooper());
        return inflater.inflate(R.layout.play_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPlayModeSwitcher = findViewbyId(R.id.playlist_play_mode);
        mPlayModeHint = findViewbyId(R.id.playlist_play_mode_hint);
        mPlayModeSwitcher.setOnClickListener(this);
        ((View)findViewbyId(R.id.playlist_op_bar)).setOnClickListener(this);
        mSongList = findViewbyId(R.id.song_list);
        mIndexBar = findViewbyId(R.id.playlist_index_bar);
        mIndexBar.setOnTouchingLetterChangedListener(this);
        mAdapter = new SortAdapter(getActivity(), null);
        mSongList.setAdapter(mAdapter);
        mSongList.setOnItemClickListener(this);
        mSongList.setEmptyView((TextView)findViewbyId(R.id.playlist_empty));
        ((TextView)findViewbyId(R.id.action_bar_text)).setText(R.string.playlist_title);
        ((View)findViewbyId(R.id.actionbar)).setOnClickListener(this);

        Fragment f = getFragmentManager().findFragmentByTag("player");
        if(f==null){
            PlayerFragment pf = new PlayerFragment();
            pf.setStateListener(this);
            getFragmentManager().beginTransaction()
                    .add(R.id.main_player_container, pf, "player")
                    .commit();
        }

        mPlayer = new Player();
        mPlayer.init(getActivity(), null);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onDestroyView() {
        mPlayer.unInit();
        mWorker.quit();
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.playlist_op_bar){
            getActivity().finish();
            mPlayer.playRandom(getActivity(), getAllSongsId());
        } else if(v.getId()==R.id.actionbar){
            getActivity().finish();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ALBUM_ID},
                MusicUtils.SONG_FILTER , null, MusicUtils.SONG_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<SongModel> model = filledData(cursor);
        setSliderBarLetters(model);
        mAdapter.updateListView(model);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        long[] songs = getAllSongsId();
        SongModel song = (SongModel)mAdapter.getItem(position);
        mPlayer.play(songs, song.songId);
        mAdapter.updatePlayingFlag();
    }

    @Override
    public void onTouchingLetterChanged(String s) {
        char index = s.charAt(0);
        int pos = mAdapter.getPositionForSection(index);
        mSongList.setSelection(pos);
    }

    private long[] getAllSongsId(){
        List<SongModel> songs = mAdapter.mList;
        long[] ids = new long[songs.size()];
        for(int i=0; i<songs.size(); i++){
            ids[i] = songs.get(i).getSongId();
        }
        return ids;
    }

    private List<SongModel> filledData(Cursor cursor){
        List<SongModel> sortList = new ArrayList<SongModel>();
        CharacterParser characterParser = CharacterParser.getInstance();
        while(cursor.moveToNext()){
            String displayName = cursor.getString(1);
            SongModel songModel = new SongModel();
            songModel.setSongId(cursor.getLong(0));
            songModel.setSongName(displayName);
            songModel.setArtist(cursor.getString(2));
            songModel.setDuration(cursor.getLong(3));
            songModel.setAlumId(cursor.getLong(4));
            //汉字转换成拼音
            String pinyin = characterParser.getSelling(displayName);
            String sortString = pinyin.substring(0, 1).toUpperCase();

            // 正则表达式，判断首字母是否是英文字母
            if(sortString.matches("[A-Z]")){
                songModel.setSortLetters(sortString.toUpperCase());
            }else{
                songModel.setSortLetters("#");
            }

            sortList.add(songModel);
        }
        Collections.sort(sortList, new PinyinComparator());
        return sortList;
    }

    private void setSliderBarLetters(List<SongModel> songs){
        ArrayList<String> letters = new ArrayList<String>();
        for(SongModel m : songs){
            if(!letters.contains(m.getSortLetters())){
                letters.add(m.getSortLetters());
            }
        }
        mIndexBar.setLetters(letters.toArray(new String[0]));
    }

    private void initOpMenu(){
        PackageManager pm = getActivity().getPackageManager();
        List<ResolveInfo> infos = pm.queryIntentActivities(new Intent("android.intent.action.MIUI_MUSIC_PLAYER"), pm.MATCH_DEFAULT_ONLY);
        if(!infos.isEmpty()) {
            TextView op1 = findViewbyId(R.id.action_bar_op1);
            op1.setText("小米音乐");
            op1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{getActivity().startActivity(new Intent("android.intent.action.MIUI_MUSIC_PLAYER"));}catch (Exception e){}
                    getActivity().finish();
                }
            });
        }
    }

    @Override
    public void onMusicPlayState(boolean playing) {}

    @Override
    public void onMusicChanged() {
        mAdapter.updatePlayingFlag();
    }

    public class SortAdapter extends BaseAdapter implements SectionIndexer{
        private List<SongModel> mList = null;
        private Context mContext;

        public SortAdapter(Context context, List<SongModel> list) {
            mContext = context;
            mList = list;
        }

        /**
         * 当ListView数据发生变化时,调用此方法来更新ListView
         * @param list
         */
        public void updateListView(List<SongModel> list){
            mList = list;
            notifyDataSetChanged();
        }

        public int getCount() {
            return mList==null?0:mList.size();
        }

        public Object getItem(int position) {
            return mList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View view, ViewGroup arg2) {
            ViewHolder viewHolder = null;
            final SongModel content = mList.get(position);
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                viewHolder = new ViewHolder();
                view = inflater.inflate(R.layout.play_list_item, arg2, false);
                viewHolder.traceName = (TextView) view.findViewById(R.id.playlist_trace_name);
                viewHolder.artist = (TextView) view.findViewById(R.id.playlist_artist);
                viewHolder.headerLetter = (TextView) view.findViewById(R.id.playlist_headerLetter);
                viewHolder.alumLogo = (ImageView) view.findViewById(R.id.playlist_alum_logo);
                viewHolder.playingFlag = (ImageView) view.findViewById(R.id.playlist_playing_flag);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            //根据position获取分类的首字母的char ascii值
            int section = getSectionForPosition(position);

            //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
            if(position == getPositionForSection(section)){
                viewHolder.headerLetter.setVisibility(View.VISIBLE);
                viewHolder.headerLetter.setText(content.getSortLetters());
            }else{
                viewHolder.headerLetter.setVisibility(View.GONE);
            }

            viewHolder.artist.setText(mList.get(position).getArtist());
            viewHolder.traceName.setText(mList.get(position).getSongName());

            Bitmap alumLogo = content.getAlumLogo();
            if(alumLogo != null){
                viewHolder.alumLogo.setImageBitmap(alumLogo);
                viewHolder.alumLogo.getDrawable().setDither(true);
            }else {
                final ViewHolder finalHolder = viewHolder;
                mAlumDecoderHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setAlumLogo(position, finalHolder);
                    }
                });
            }

            SongModel song = mList.get(position);
            if(song.getSongId()==mPlayer.getCurrentAudioId()){
                viewHolder.playingFlag.setVisibility(View.VISIBLE);
            }else{
                viewHolder.playingFlag.setVisibility(View.GONE);
            }

            handler.removeCallbacks(indexClear);
            handler.postDelayed(indexClear, 200);
            return view;
        }

        public int getSectionForPosition(int position) {
            return mList.get(position).getSortLetters().charAt(0);
        }
        public int getPositionForSection(int section) {
            for (int i = 0; i < getCount(); i++) {
                String sortStr = mList.get(i).getSortLetters();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public Object[] getSections() {
            return null;
        }

        private void setAlumLogo(int pos, final ViewHolder views){
            if(mList.size()<=pos) return;
            SongModel song = mList.get(pos);
            Bitmap bm = MusicUtils.getArtwork(getActivity(), song.getSongId(), song.getAlumId(), false);
            if (bm == null) {
                bm = MusicUtils.getArtwork(getActivity(), song.getSongId(), -1);
            }
            if (bm != null) {
                song.setAlumLogo(bm);
                final Bitmap logo = bm;
                updateUi(new Runnable() {
                    @Override
                    public void run() {
                        views.alumLogo.setImageBitmap(logo);
                        views.alumLogo.getDrawable().setDither(true);
                    }
                });
            }
        }

        private void updatePlayingFlag(){
            mAdapter.notifyDataSetChanged();
        }

        Handler handler = new Handler();
        private Runnable indexClear = new Runnable() {
            @Override
            public void run() {
                int firstPos = mSongList.getFirstVisiblePosition();
                int lastPos = mSongList.getLastVisiblePosition();
                if(firstPos>0 || lastPos<mList.size()-1){
                    mIndexBar.setVisibility(View.VISIBLE);
                }else{
                    mIndexBar.setVisibility(View.GONE);
                }
            }
        };
    }


    private class ViewHolder{
        ImageView alumLogo;
        TextView headerLetter;
        TextView traceName;
        TextView artist;
        ImageView playingFlag;
    }

    public class SongModel {
        public String getArtist() {return artist;}
        public void setArtist(String artist) {this.artist = artist;}
        public String getSongName() {return songName;}
        public void setSongName(String songName) {this.songName = songName;}
        public String getSortLetters() {return sortLetters;}
        public void setSortLetters(String sortLetters) {this.sortLetters = sortLetters;}
        public long getSongId() {return songId;}
        public void setSongId(long songId) {this.songId = songId;}
        public long getDuration() {return duration;}
        public void setDuration(long duration) {this.duration = duration;}
        public long getAlumId() {return alumId;}
        public void setAlumId(long alumId) {this.alumId = alumId;}
        public Bitmap getAlumLogo(){return alumLogo==null?null:alumLogo.get();}
        public void setAlumLogo(Bitmap bitmap){ alumLogo=new WeakReference<Bitmap>(bitmap);}
        private String artist;
        private String songName;
        private String sortLetters;
        private long songId;
        private long duration;
        private long alumId;
        private WeakReference<Bitmap> alumLogo;
    }

    /**
     * Java汉字转换为拼音
     *
     */
    public static class CharacterParser {
        private static int[] pyvalue = new int[] {-20319, -20317, -20304, -20295, -20292, -20283, -20265, -20257, -20242, -20230, -20051, -20036, -20032,
                -20026, -20002, -19990, -19986, -19982, -19976, -19805, -19784, -19775, -19774, -19763, -19756, -19751, -19746, -19741, -19739, -19728,
                -19725, -19715, -19540, -19531, -19525, -19515, -19500, -19484, -19479, -19467, -19289, -19288, -19281, -19275, -19270, -19263, -19261,
                -19249, -19243, -19242, -19238, -19235, -19227, -19224, -19218, -19212, -19038, -19023, -19018, -19006, -19003, -18996, -18977, -18961,
                -18952, -18783, -18774, -18773, -18763, -18756, -18741, -18735, -18731, -18722, -18710, -18697, -18696, -18526, -18518, -18501, -18490,
                -18478, -18463, -18448, -18447, -18446, -18239, -18237, -18231, -18220, -18211, -18201, -18184, -18183, -18181, -18012, -17997, -17988,
                -17970, -17964, -17961, -17950, -17947, -17931, -17928, -17922, -17759, -17752, -17733, -17730, -17721, -17703, -17701, -17697, -17692,
                -17683, -17676, -17496, -17487, -17482, -17468, -17454, -17433, -17427, -17417, -17202, -17185, -16983, -16970, -16942, -16915, -16733,
                -16708, -16706, -16689, -16664, -16657, -16647, -16474, -16470, -16465, -16459, -16452, -16448, -16433, -16429, -16427, -16423, -16419,
                -16412, -16407, -16403, -16401, -16393, -16220, -16216, -16212, -16205, -16202, -16187, -16180, -16171, -16169, -16158, -16155, -15959,
                -15958, -15944, -15933, -15920, -15915, -15903, -15889, -15878, -15707, -15701, -15681, -15667, -15661, -15659, -15652, -15640, -15631,
                -15625, -15454, -15448, -15436, -15435, -15419, -15416, -15408, -15394, -15385, -15377, -15375, -15369, -15363, -15362, -15183, -15180,
                -15165, -15158, -15153, -15150, -15149, -15144, -15143, -15141, -15140, -15139, -15128, -15121, -15119, -15117, -15110, -15109, -14941,
                -14937, -14933, -14930, -14929, -14928, -14926, -14922, -14921, -14914, -14908, -14902, -14894, -14889, -14882, -14873, -14871, -14857,
                -14678, -14674, -14670, -14668, -14663, -14654, -14645, -14630, -14594, -14429, -14407, -14399, -14384, -14379, -14368, -14355, -14353,
                -14345, -14170, -14159, -14151, -14149, -14145, -14140, -14137, -14135, -14125, -14123, -14122, -14112, -14109, -14099, -14097, -14094,
                -14092, -14090, -14087, -14083, -13917, -13914, -13910, -13907, -13906, -13905, -13896, -13894, -13878, -13870, -13859, -13847, -13831,
                -13658, -13611, -13601, -13406, -13404, -13400, -13398, -13395, -13391, -13387, -13383, -13367, -13359, -13356, -13343, -13340, -13329,
                -13326, -13318, -13147, -13138, -13120, -13107, -13096, -13095, -13091, -13076, -13068, -13063, -13060, -12888, -12875, -12871, -12860,
                -12858, -12852, -12849, -12838, -12831, -12829, -12812, -12802, -12607, -12597, -12594, -12585, -12556, -12359, -12346, -12320, -12300,
                -12120, -12099, -12089, -12074, -12067, -12058, -12039, -11867, -11861, -11847, -11831, -11798, -11781, -11604, -11589, -11536, -11358,
                -11340, -11339, -11324, -11303, -11097, -11077, -11067, -11055, -11052, -11045, -11041, -11038, -11024, -11020, -11019, -11018, -11014,
                -10838, -10832, -10815, -10800, -10790, -10780, -10764, -10587, -10544, -10533, -10519, -10331, -10329, -10328, -10322, -10315, -10309,
                -10307, -10296, -10281, -10274, -10270, -10262, -10260, -10256, -10254};
        public static String[] pystr = new String[] {"a", "ai", "an", "ang", "ao", "ba", "bai", "ban", "bang", "bao", "bei", "ben", "beng", "bi", "bian",
                "biao", "bie", "bin", "bing", "bo", "bu", "ca", "cai", "can", "cang", "cao", "ce", "ceng", "cha", "chai", "chan", "chang", "chao", "che",
                "chen", "cheng", "chi", "chong", "chou", "chu", "chuai", "chuan", "chuang", "chui", "chun", "chuo", "ci", "cong", "cou", "cu", "cuan",
                "cui", "cun", "cuo", "da", "dai", "dan", "dang", "dao", "de", "deng", "di", "dian", "diao", "die", "ding", "diu", "dong", "dou", "du",
                "duan", "dui", "dun", "duo", "e", "en", "er", "fa", "fan", "fang", "fei", "fen", "feng", "fo", "fou", "fu", "ga", "gai", "gan", "gang",
                "gao", "ge", "gei", "gen", "geng", "gong", "gou", "gu", "gua", "guai", "guan", "guang", "gui", "gun", "guo", "ha", "hai", "han", "hang",
                "hao", "he", "hei", "hen", "heng", "hong", "hou", "hu", "hua", "huai", "huan", "huang", "hui", "hun", "huo", "ji", "jia", "jian",
                "jiang", "jiao", "jie", "jin", "jing", "jiong", "jiu", "ju", "juan", "jue", "jun", "ka", "kai", "kan", "kang", "kao", "ke", "ken",
                "keng", "kong", "kou", "ku", "kua", "kuai", "kuan", "kuang", "kui", "kun", "kuo", "la", "lai", "lan", "lang", "lao", "le", "lei", "leng",
                "li", "lia", "lian", "liang", "liao", "lie", "lin", "ling", "liu", "long", "lou", "lu", "lv", "luan", "lue", "lun", "luo", "ma", "mai",
                "man", "mang", "mao", "me", "mei", "men", "meng", "mi", "mian", "miao", "mie", "min", "ming", "miu", "mo", "mou", "mu", "na", "nai",
                "nan", "nang", "nao", "ne", "nei", "nen", "neng", "ni", "nian", "niang", "niao", "nie", "nin", "ning", "niu", "nong", "nu", "nv", "nuan",
                "nue", "nuo", "o", "ou", "pa", "pai", "pan", "pang", "pao", "pei", "pen", "peng", "pi", "pian", "piao", "pie", "pin", "ping", "po", "pu",
                "qi", "qia", "qian", "qiang", "qiao", "qie", "qin", "qing", "qiong", "qiu", "qu", "quan", "que", "qun", "ran", "rang", "rao", "re",
                "ren", "reng", "ri", "rong", "rou", "ru", "ruan", "rui", "run", "ruo", "sa", "sai", "san", "sang", "sao", "se", "sen", "seng", "sha",
                "shai", "shan", "shang", "shao", "she", "shen", "sheng", "shi", "shou", "shu", "shua", "shuai", "shuan", "shuang", "shui", "shun",
                "shuo", "si", "song", "sou", "su", "suan", "sui", "sun", "suo", "ta", "tai", "tan", "tang", "tao", "te", "teng", "ti", "tian", "tiao",
                "tie", "ting", "tong", "tou", "tu", "tuan", "tui", "tun", "tuo", "wa", "wai", "wan", "wang", "wei", "wen", "weng", "wo", "wu", "xi",
                "xia", "xian", "xiang", "xiao", "xie", "xin", "xing", "xiong", "xiu", "xu", "xuan", "xue", "xun", "ya", "yan", "yang", "yao", "ye", "yi",
                "yin", "ying", "yo", "yong", "you", "yu", "yuan", "yue", "yun", "za", "zai", "zan", "zang", "zao", "ze", "zei", "zen", "zeng", "zha",
                "zhai", "zhan", "zhang", "zhao", "zhe", "zhen", "zheng", "zhi", "zhong", "zhou", "zhu", "zhua", "zhuai", "zhuan", "zhuang", "zhui",
                "zhun", "zhuo", "zi", "zong", "zou", "zu", "zuan", "zui", "zun", "zuo"};
        private String resource;
        private static CharacterParser characterParser = new CharacterParser();

        public static CharacterParser getInstance() {
            return characterParser;
        }

        public String getResource() {
            return resource;
        }

        public void setResource(String resource) {
            this.resource = resource;
        }

        /** * 汉字转成ASCII码 * * @param chs * @return */
        private int getChsAscii(String chs) {
            int asc = 0;
            try {
                byte[] bytes = chs.getBytes("gb2312");
                if (bytes == null || bytes.length > 2 || bytes.length <= 0) {
                    throw new RuntimeException("illegal resource string");
                }
                if (bytes.length == 1) {
                    asc = bytes[0];
                }
                if (bytes.length == 2) {
                    int hightByte = 256 + bytes[0];
                    int lowByte = 256 + bytes[1];
                    asc = (256 * hightByte + lowByte) - 256 * 256;
                }
            } catch (Exception e) {
                System.out.println("ERROR:ChineseSpelling.class-getChsAscii(String chs)" + e);
            }
            return asc;
        }

        /** * 单字解析 * * @param str * @return */
        public String convert(String str) {
            String result = null;
            int ascii = getChsAscii(str);
            if (ascii > 0 && ascii < 160) {
                result = String.valueOf((char) ascii);
            } else {
                for (int i = (pyvalue.length - 1); i >= 0; i--) {
                    if (pyvalue[i] <= ascii) {
                        result = pystr[i];
                        break;
                    }
                }
            }
            return result;
        }

        /** * 词组解析 * * @param chs * @return */
        public String getSelling(String chs) {
            String key, value;
            StringBuilder buffer = new StringBuilder();

            for (int i = 0; i < chs.length(); i++) {
                key = chs.substring(i, i + 1);
                if (key.getBytes().length >= 2) {
                    value = (String) convert(key);
                    if (value == null) {
                        value = "unknown";
                    }
                } else {
                    value = key;
                }
                buffer.append(value);
            }
            return buffer.toString();
        }

        public String getSpelling() {
            return this.getSelling(this.getResource());
        }

        public boolean isAscii(String str){
            if(str.length() == 0 ) return false;

            char c = str.charAt(0);
            return (c>='a'&& c<='z') || (c>='A'&&c<'Z');
        }
    }

    public class PinyinComparator implements Comparator<SongModel> {
        public int compare(SongModel o1, SongModel o2) {
            //这里主要是用来对ListView里面的数据根据ABCDEFG...来排序
            if (o2.getSortLetters().equals("#")) {
                return -1;
            } else if (o1.getSortLetters().equals("#")) {
                return 1;
            } else {
                return o1.getSortLetters().compareTo(o2.getSortLetters());
            }
        }
    }
}
