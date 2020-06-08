package com.binzeefox.foxframe.tools.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;

import com.binzeefox.foxframe.core.FoxCore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.annotation.Nullable;

/**
 * 位图工具类
 *
 * 2019-12-27 弃用，转为使用android P {@link android.graphics.ImageDecoder} 的 {@link ImageUtil}
 * @deprecated use {@link ImageUtil} instead
 */
public class BitmapUtil {
    private static final String TAG = "BitmapUtil";

    private Context mContext;   //上下文实例


    /**
     * 构造器
     * @author binze 2019/12/11 11:09
     */
    public BitmapUtil(Context context){
        mContext = context;
    }

    /**
     * 静态获取
     * @author binze 2019/12/11 11:09
     */
    public static BitmapUtil get(){
        return new BitmapUtil(FoxCore.getApplication());
    }

    /**
     * 静态获取
     * @author binze 2019/12/11 11:09
     */
    public static BitmapUtil get(Context context){
        return new BitmapUtil(context);
    }

    /**
     * 静态方法 直接从文件获取Bitmap
     * @param path  图片路径
     * @return  尚未处理过的bitmap
     */
    public static Bitmap getBitmapFromFileUnConditionally(String path) {
        return BitmapFactory.decodeFile(path);
    }

    /**
     * 静态方法 从文件获取Bitmap
     * @param path  图片路径
     * @param options   图片加载Options，Null则默认颜色格式为 RGB_565
     * @return  缩放过的图片
     */
    public static Bitmap getBitmapFromFile(String path, @Nullable BitmapFactory.Options options) {
        BitmapFactory.Options fOptions;
        if (options == null) {
            fOptions = new BitmapFactory.Options();
//            options.inJustDecodeBounds = true; //仅返回尺寸，不返回bitmap，在不需要图片实例的时候使用
            fOptions.inSampleSize = 1;   //分辨率分母，例如当为2时，位图的长宽均缩减为1/2，像素缩减为1/4
            fOptions.inPreferredConfig = Bitmap.Config.RGB_565; //颜色格式
            fOptions.inScaled = true;    //可否缩放
            fOptions.inTempStorage = new byte[100 * 1024];   //缓存大小
        } else fOptions = options;

        return BitmapFactory.decodeFile(path, fOptions);
    }

    /**
     * 静态方法 保存图片到文件
     * @return 保存路径
     */
    public static String saveBitmap(Bitmap bitmap, File file){
        if (file.exists()) file.delete();

        try (FileOutputStream out = new FileOutputStream(file)){
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            Log.i(TAG, "已经保存, 路径：" + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 静态方法 保存图片到文件
     * @return 保存路径
     */
    public static String saveBitmap(Bitmap bitmap, String path, String picName){
        File f = new File(path, picName);
        return saveBitmap(bitmap, f);
    }

    /**
     * 静态方法 保存图片到文件
     * @return 保存路径
     */
    public static String saveBitmap(Bitmap bitmap, String path){
        File f = new File(path);
        return saveBitmap(bitmap, f);
    }

    /**
     * 静态方法 旋转Bitmap
     * @param bm    图片
     * @param degrees   角度
     * @return  旋转后图片
     */
    public static Bitmap rotateBitmap(Bitmap bm, int degrees) {
        Bitmap returnBm = null;

        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            Log.w(TAG, "adjustBitmapRotation: 图片旋转失败，将返回原图" );
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    /**
     * 保存本地图片缓存
     */
    public String saveBitmapDiskCache(Bitmap bitmap, String name){
        File eCacheDir = mContext.getExternalCacheDir();
        String cachePath;
        if (eCacheDir != null) cachePath = eCacheDir.getPath();
        else cachePath = Environment.getExternalStorageDirectory().getPath() + "bitmap cache";

        return saveBitmap(bitmap, cachePath, name);
    }

    /**
     * 通过bitmap获取drawable
     *
     * 默认为平铺重复模式，左上角开始
     * @param bitmap    目标bitmap
     * @return  返回
     */
    public Drawable getDrawableFormBitmap(Bitmap bitmap){
        BitmapDrawable drawable = new BitmapDrawable(mContext.getResources(), bitmap);
        drawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        drawable.setGravity(Gravity.START| Gravity.TOP);
        return drawable;
    }
}
