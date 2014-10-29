package com.xiaomi.mitv.soundbarapp;

import com.xiaomi.mitv.soundbar.protocol.TraceInfo0x816;

/**
 * Created by chenxuetong on 9/25/14. 0c1daf1b5715
 */
public class BarInfoUtils {
    public static boolean haveSource(TraceInfo0x816 info){
        if (info==null) return false;
        return info.mAutoRouting.audio_source!=TraceInfo0x816.AudioRouting.Audio_source.none;
    }

    public static boolean sourceIsPhone(TraceInfo0x816 info){
        if (info==null) return false;
        int source = info.mAutoRouting.audio_source;
        return source == TraceInfo0x816.AudioRouting.Audio_source.AG1 ||
                source == TraceInfo0x816.AudioRouting.Audio_source.AG2;

    }

    public static boolean isA2dpConnected(TraceInfo0x816 info){
        if (info==null) return false;
        return info.mAutoRouting.primary_a2dp_stream_state == TraceInfo0x816.AudioRouting.A2dp_stream_state.open ||
               info.mAutoRouting.secondary_a2dp_stream_state == TraceInfo0x816.AudioRouting.A2dp_stream_state.open ||
               info.mAutoRouting.primary_a2dp_stream_state == TraceInfo0x816.AudioRouting.A2dp_stream_state.streaming ||
               info.mAutoRouting.secondary_a2dp_stream_state == TraceInfo0x816.AudioRouting.A2dp_stream_state.streaming;
    }
    public static boolean isA2dpPlaying(TraceInfo0x816 info){
        if (info==null) return false;
        return  info.mAutoRouting.primary_a2dp_stream_state == TraceInfo0x816.AudioRouting.A2dp_stream_state.streaming ||
                info.mAutoRouting.secondary_a2dp_stream_state == TraceInfo0x816.AudioRouting.A2dp_stream_state.streaming;
    }
}
