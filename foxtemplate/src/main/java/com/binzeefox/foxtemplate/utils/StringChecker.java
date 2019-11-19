package com.binzeefox.foxtemplate.utils;

import java.util.regex.Pattern;

public class StringChecker {

    /**
     * 判断是否纯数字
     */
    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    /**
     * 判断字符是否是汉字
     * @author binze 2019/11/19 9:33
     */
    private static boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;// 根据字节码判断
    }

    /**
     * 判断字符串是否包含中文
     * @author binze 2019/11/19 9:33
     */
    public static boolean hasChinese(String str) {
        if (str == null)
            return false;
        for (char c : str.toCharArray()) {
            if (isChinese(c))
                return true;// 有一个中文字符就返回
        }
        return false;
    }
}
