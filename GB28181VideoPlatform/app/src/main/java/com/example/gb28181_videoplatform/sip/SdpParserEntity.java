package com.example.gb28181_videoplatform.sip;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class SdpParserEntity {
    /**
     * 基础sdp_String 数据
     */
    public String baseString;
    /**
     * url参数
     */
    public Map<String, String> params;
    private static Vector vector_sdp = new Vector();
    public static String xml = "";

    public static String getXml() {
        return xml;
    }

    public static void setXml(String xml) {
        SdpParserEntity.xml = xml;
    }

    public Vector getVector_sdp() {
        return vector_sdp;
    }

    public void setVector_sdp(Vector vector_sdp) {
        this.vector_sdp = vector_sdp;
    }

    public String getBaseString() {
        return baseString;
    }

    public void setBaseString(String baseString) {
        this.baseString = baseString;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public static SdpParserEntity parse(String url) {
        xml = "";
        vector_sdp.clear();
        SdpParserEntity entity = new SdpParserEntity();
        url = url.trim();
        if (url.equals("")) {
            return entity;
        }
        String[] params = url.split("\r\n");
        entity.params = new HashMap<>();
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                if (keyValue[0].endsWith("y") || keyValue[0].endsWith("f")) {//过滤掉y和f字段
                    // TODONOTHING
                } else {
                    vector_sdp.add(param);
                }
                entity.params.put(keyValue[0], keyValue[1]);
            }
        }
        Log.e("gaozy", "vector_sdp.size()=======" + vector_sdp.size() + "");
        return entity;
    }

    public static String getnewSdp() {
        // 获取vec对应的String数组
        String[] arr = (String[]) vector_sdp.toArray(new String[0]);
        for (String param : arr) {
            xml = xml + param + "\r\n";
        }
        return xml;
    }
}
