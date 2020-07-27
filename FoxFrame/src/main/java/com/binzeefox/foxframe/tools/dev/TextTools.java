package com.binzeefox.foxframe.tools.dev;

import android.icu.util.Calendar;
import android.util.Patterns;
import android.webkit.URLUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 文字工具类
 * @author binze
 * 2019/12/13 16:40
 */
public class TextTools {

    /**
     * 判断是否是奇数
     * @author binze 2019/12/13 16:34
     * update 2019/12-18 参数类别改为long以支持长数字
     */
    public static boolean isObb(long num){
        return num % 2 == 1;
    }

    /**
     * 判断是否是奇数
     * @author binze 2019/12/13 16:34
     */
    public static boolean isObb(String numStr){
        if (!isInteger(numStr)) return false;
        return Long.parseLong(numStr) % 2 == 1;
    }

    /**
     * 判断是否纯数字
     */
    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    /**
     * 判断是否是双精度浮点数
     * @author binze 2019/12/3 15:30
     */
    public static boolean isDouble(String str){
        try{
            Double.parseDouble(str);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    /**
     * 判断字符是否是汉字
     * @author binze 2019/11/19 9:33
     */
    public static boolean isChinese(char c) {
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

    /**
     * 是合法网络Url
     * @author binze 2020/7/3 15:43
     */
    public static boolean isWebUrl(String url){
        return Patterns.WEB_URL.matcher(url).matches();
    }

    /**
     * 返回身份证工具类
     * @author binze 2019/12/13 15:54
     */
    public static IDCard idCard(String idStr){
        return new IDCard(idStr);
    }

    /**
     * 身份证工具
     * @author binze
     * 2019/12/13 15:50
     */
    public static final class IDCard{
        private static final Map<String, String> cCityMap = new HashMap<>();
        private final String idStr;
        static {
            cCityMap.put("11", "北京");
            cCityMap.put("12", "天津");
            cCityMap.put("13", "河北");
            cCityMap.put("14", "山西");
            cCityMap.put("15", "内蒙古");
            cCityMap.put("21", "辽宁");
            cCityMap.put("22", "吉林");
            cCityMap.put("23", "黑龙江");
            cCityMap.put("31", "上海");
            cCityMap.put("32", "江苏");
            cCityMap.put("33", "浙江");
            cCityMap.put("34", "安徽");
            cCityMap.put("35", "福建");
            cCityMap.put("36", "江西");
            cCityMap.put("37", "山东");
            cCityMap.put("41", "河南");
            cCityMap.put("42", "湖北");
            cCityMap.put("43", "湖南");
            cCityMap.put("44", "广东");
            cCityMap.put("45", "广西");
            cCityMap.put("46", "海南");
            cCityMap.put("50", "重庆");
            cCityMap.put("51", "四川");
            cCityMap.put("52", "贵州");
            cCityMap.put("53", "云南");
            cCityMap.put("54", "西藏");
            cCityMap.put("61", "陕西");
            cCityMap.put("62", "甘肃");
            cCityMap.put("63", "青海");
            cCityMap.put("64", "宁夏");
            cCityMap.put("65", "新疆");
            cCityMap.put("71", "台湾");
            cCityMap.put("81", "香港");
            cCityMap.put("82", "澳门");
            cCityMap.put("91", "境外");
        }

        /**
         * 私有化实例
         * @author binze 2019/12/13 16:36
         */
        private IDCard(String idStr){
            this.idStr = idStr;
        }

        /**
         * 是否合法
         * @author binze 2019/12/13 15:55
         */
        public boolean isLegal(){
//            Pattern lengthPattern = Pattern.compile("^\\d{17}(\\d|x)$");
            Pattern pattern = Pattern.compile("\\d{15}(\\d{2}[0-9xX])?");
            return pattern.matcher(idStr).matches();
        }

        /**
         * 获取该身份证城市名称
         * @author binze 2019/12/13 16:21
         */
        public String getCityName(){
            return cCityMap.get(idStr.substring(0, 2));
        }

        /**
         * 获取该身份证生日信息
         * @author binze 2019/12/13 16:22
         */
        public Date getBirthDay(){
            String birthday = idStr.substring(6, 14);

            int year; // 从中间变量birthday来截取
            year = Integer.parseInt(birthday.substring(0, 4)); // 字符串转成整数
            System.out.println(year);

            int month;
            System.out.println(birthday.substring(4, 6));
            month = Integer.parseInt(birthday.substring(4, 6));
            System.out.println(month);

            int day;
            day = Integer.parseInt(birthday.substring(6));
            System.out.println(day);
            Calendar calendar = Calendar.getInstance(Locale.CHINA);
            calendar.set(year, month, day);
            return calendar.getTime();
        }

        /**
         * 判断是否为男性
         * @author binze 2019/12/13 16:29
         */
        public boolean isMale(){
            String sex = "";
            sex = idStr.substring( idStr.length() -2 , idStr.length() -1);
            return isObb(sex);
        }
    }
}
