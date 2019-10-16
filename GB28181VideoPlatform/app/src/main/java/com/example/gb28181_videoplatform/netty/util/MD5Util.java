package com.example.gb28181_videoplatform.netty.util;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;


public class MD5Util {

    /**
     * 生成签名
     *
     * @param logistics_interface
     * @param key
     * @param charset
     * @return
     */
    public static String doSign(String logistics_interface, String key, String charset) {
        try {
            String sign = new String(
                    Base64.encodeBase64(code32((logistics_interface + key), charset).getBytes(charset)));
            return sign;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * MD5 32位加密
     *
     * @param origin  字符源
     * @param charset 字符编码
     * @return 32位密文
     */
    public static String code32(String origin, String charset) {
        String resultString = origin;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (charset == null) {
                resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
            } else {
                resultString = byteArrayToHexString(md.digest(resultString.getBytes(charset)));
            }
        } catch (Exception exception) {
        }
        return resultString;
    }

    private static String byteArrayToHexString(byte b[]) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }

        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n += 256;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    private static final String hexDigits[] =
            {
                    "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"
            };

    private static boolean isEmpty(Object str) {
        return str == null || str.toString().isEmpty();
    }
    // 加码验证

}
