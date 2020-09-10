package com.binzeefox.foxframe.tools.dev;

import android.text.TextUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5工具类，不排除以后可能会改名或者整合到一个主司加密的大类
 * @author binze
 * 2019/11/19 9:32
 */
public class MD5Util {

    /**
     * 获取字符串的MD5摘要
     *
     * @param value 待摘要字符串
     * @return 字符串MD5码
     */
    public static String md5(String value) {
        if (TextUtils.isEmpty(value)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(value.getBytes());
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = 0 + temp;
                }
                result.append(temp);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
