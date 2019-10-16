package com.example.gb28181_videoplatform.util;

import android.annotation.SuppressLint;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.util.Log;

import java.util.Arrays;

/**
 * 获得手机支持的MediaCodec编解码颜色格式
 */
public class MyMediaCodec {
    private static final String TAG = "MyMediaCodec";

    @SuppressLint("NewApi")
    public int getMediaCodecList() {
        //获取解码器列表
        int numCodecs = MediaCodecList.getCodecCount();
        MediaCodecInfo codecInfo = null;
        for (int i = 0; i < numCodecs && codecInfo == null; i++) {
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            if (!info.isEncoder()) {
                continue;
            }
            String[] types = info.getSupportedTypes();
            boolean found = false;
            //轮训所要的解码器
            for (int j = 0; j < types.length && !found; j++) {
                if (types[j].equals("video/avc")) {
                    System.out.println("found");
                    found = true;
                }
            }
            if (!found) {
                continue;
            }
            codecInfo = info;
        }
        Log.d(TAG, "found" + codecInfo.getName() + "supporting" + " video/avc");

        //检查所支持的colorspace
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType("video/avc");
        System.out.println("length-" + capabilities.colorFormats.length + "==" + Arrays.toString(capabilities.colorFormats));
        for (int i = 0; i < capabilities.colorFormats.length; i++) {
            int format = capabilities.colorFormats[i];
            switch (format) {
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
                case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
                    Log.e(TAG, "supported color format::" + format);
                    return format;
                default:
                    Log.d(TAG, "Skipping unsupported color format " + format);
                    break;
            }
        }
        return -1;
    }

}
