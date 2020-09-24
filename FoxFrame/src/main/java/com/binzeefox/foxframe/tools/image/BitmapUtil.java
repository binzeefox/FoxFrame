package com.binzeefox.foxframe.tools.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.binzeefox.foxframe.core.FoxCore;
import com.binzeefox.foxframe.tools.dev.LogUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.annotation.NonNull;

/**
 * 位图工具类
 *
 * @author 狐彻
 * 2020/09/10 11:21
 */
public class BitmapUtil {
    private static final String TAG = "BitmapUtil";

    ///////////////////////////////////////////////////////////////////////////
    // 保存Bitmap至文件
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 保存至文件
     *
     * @param bm    要保存的Bitmap
     * @param format    保存格式，默认为PNG
     * @param quality   保存精度，0-100 的数字，100为原图
     * @author 狐彻 2020/09/10 11:25
     */
    public static void saveToFile(@NonNull Bitmap bm, @NonNull File target
            , Bitmap.CompressFormat format, int quality){
        if (target.exists()) {
            LogUtil.i(TAG, "saveToFile: 目标路径文件已存在");
            return;
        }
        try (FileOutputStream fos = new FileOutputStream(target)){
            bm.compress(format, quality, fos);
        } catch (IOException e) {
            LogUtil.e(TAG, "saveToFile: 保存文件失败", e);
        }
    }

    /**
     * 保存至文件
     *
     * @param bm    要保存的Bitmap
     * @param format    保存格式，默认为PNG
     * @author 狐彻 2020/09/10 11:25
     */
    public static void saveToFile(@NonNull Bitmap bm, @NonNull File target
            , Bitmap.CompressFormat format){
        saveToFile(bm, target, format, 100);
    }

    /**
     * 保存至文件
     *
     * @param bm    要保存的Bitmap
     * @param quality   保存精度，0-100 的数字，100为原图
     * @author 狐彻 2020/09/10 11:25
     */
    public static void saveToFile(@NonNull Bitmap bm, @NonNull File target
            , int quality){
        saveToFile(bm, target, Bitmap.CompressFormat.PNG, quality);
    }

    /**
     * 保存至文件
     *
     * @param bm    要保存的Bitmap
     * @author 狐彻 2020/09/10 11:25
     */
    public static void saveToFile(@NonNull Bitmap bm, @NonNull File target){
        saveToFile(bm, target, Bitmap.CompressFormat.PNG, 100);
    }

    ///////////////////////////////////////////////////////////////////////////
    // 保存Bitmap至文件 Finish
    // Bitmap与Drawable
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Bitmap获取Drawable
     *
     * @author 狐彻 2020/09/10 11:42
     */
    public static Drawable bitmapToDrawable(@NonNull Bitmap bm){
        return new BitmapDrawable(FoxCore.getApplication().getResources(), bm);
    }

    /**
     * Drawable转Bitmap
     *
     * @author 狐彻 2020/09/10 11:45
     */
    public static Bitmap drawableToBitmap(@NonNull Drawable drawable){
        if (drawable instanceof BitmapDrawable)
            return ((BitmapDrawable) drawable).getBitmap();

        //尺寸
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        //颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE
                ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;

        //创建BM
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);

        //建立画布，画上相应的drawable
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0,0,w,h);
        drawable.draw(canvas);
        return bitmap;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Bitmap操作
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 按百分比缩放图片
     *
     * @author 狐彻 2020/09/10 11:54
     */
    public static Bitmap scaleByPercent(@NonNull Bitmap source, float scale){
        int w = source.getWidth();
        int h = source.getHeight();
        return Bitmap.createScaledBitmap(source, (int) (w*scale), (int) (h*scale),true);
    }

    /**
     * 旋转
     *
     * @author 狐彻 2020/09/10 12:00
     */
    public static Bitmap rotate(Bitmap bm, int degrees){
        Bitmap bmRotate ;

        //生成旋转角度相应矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        try {
            bmRotate = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e){
            LogUtil.e(TAG, "rotate: 旋转图片失败", e);
            bmRotate = null;
            System.gc();
        }
        return bmRotate;
    }

    /**
     * 根据显示尺寸压缩图片
     *
     * @author 狐彻 2020/09/10 12:09
     */
    public static Bitmap compress(@NonNull Bitmap bitmap, Bitmap.CompressFormat format, int width, int height){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(format, 100, baos); //不压缩质量
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = true;  //只取信息
        BitmapFactory.decodeStream(bais, null, options);

        options.inSampleSize = calculateSampleSize(options, width, height);
        options.inMutable = true;
        try {
            options.inBitmap = Bitmap.createBitmap
                    (options.outWidth, options.outHeight, Bitmap.Config.RGB_565);
        } catch (OutOfMemoryError e ){
            LogUtil.e(TAG, "compress: 压缩失败", e);
            options.inBitmap = null;
            System.gc();
            return null;
        }

        options.inJustDecodeBounds = false; //要图
        bais.reset();
        Bitmap _result;
        try {
            _result = BitmapFactory.decodeStream(bais, null, options);
            return _result;
        } catch (OutOfMemoryError e){
            LogUtil.e(TAG, "compress: 生成压缩图失败", e);
            _result = null;
            System.gc();
            return null;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 私有方法
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 计算采样比例
     *
     * @author 狐彻 2020/09/10 12:20
     */
    private static int calculateSampleSize(BitmapFactory.Options options, int width, int height){
        int originalW = options.outWidth;
        int originalH = options.outHeight;

        int inSampleSize = 1;
        if (originalH > height || originalW > width){
            final int hRatio = Math.round((float) originalH / (float) height);
            final int wRatio = Math.round((float) originalW / (float) width);
            //根据比例选择inSampleSize值
            inSampleSize = Math.min(hRatio, wRatio);
        }
        return inSampleSize;
    }
}
