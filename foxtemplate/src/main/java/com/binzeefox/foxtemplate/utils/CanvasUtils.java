package com.binzeefox.foxtemplate.utils;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;

/**
 * 画布工具类
 *
 * 自定义view用到的工具类
 *
 * 待完善
 * @author binze
 * 2019/11/19 9:19
 */
public class CanvasUtils {

    /**
     * 矩阵转换点
     * @author binze 2019/11/19 9:20
     */
    public static PointF mapPointF(Matrix matrix, PointF point){
        float[] p = new float[]{point.x, point.y};
        matrix.mapPoints(p);
        return new PointF(p[0], p[1]);
    }

    /**
     * 矩阵转换矩形
     * @author binze 2019/11/19 9:20
     */
    public static RectF mapRectF(Matrix matrix, RectF rect){
        RectF rectF = new RectF(rect);
        matrix.mapRect(rectF);
        return rectF;
    }
}
