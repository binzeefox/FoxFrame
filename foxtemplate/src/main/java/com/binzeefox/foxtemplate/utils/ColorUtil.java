package com.binzeefox.foxtemplate.utils;

import android.graphics.Color;
import android.util.Log;

import java.util.Arrays;

/**
 * 颜色工具类
 * @author binze
 * 2019/11/19 9:24
 */
public class ColorUtil {

    private static final String TAG = "ColorUtil";

    /**
     * 通过rgb获取"#ffffff"类型色值
     * @author binze 2019/11/19 9:25
     */
    public static String getColorHexStr(int r, int g, int b){
        int color = Color.rgb(r,g,b);
        String colorStr = "000000" + Integer.toHexString(color);
        return "#" + colorStr.substring(colorStr.length() - 6);
    }

    /**
     * 通过16进制色值获取颜色rgb
     *
     * @return int[r, g, b]
     * @author binze 2019/11/19 9:25
     */
    public static int[] getColorRgb(int color){
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0x00ff00) >> 8;
        int blue = (color & 0x0000ff);

        Log.d(TAG, "getColorRgb: rgb = " + Arrays.toString(new int[]{red, green, blue}));
        return new int[]{red, green, blue};
    }
}
