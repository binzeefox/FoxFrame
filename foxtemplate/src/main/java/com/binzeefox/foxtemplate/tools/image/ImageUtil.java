package com.binzeefox.foxtemplate.tools.image;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.PostProcessor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;

import com.binzeefox.foxtemplate.core.FoxCore;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

/**
 * 应用ImageDecoder的图片工具类
 *
 * @author binze
 * 2019/12/27 16:09
 */
@TargetApi(Build.VERSION_CODES.P)
public class ImageUtil {
    private static final String TAG = "ImageUtil";
    private Context mCtx;   //上下文实例

    public ImageUtil(@NonNull Context context) {
        mCtx = context.getApplicationContext();
    }

    public static ImageUtil get() {
        Context context = FoxCore.getApplication();
        if (context == null)
            throw new RuntimeException(new IllegalAccessError("call FoxCore.init() first!!!"));
        return new ImageUtil(FoxCore.getApplication());
    }

    /**
     * 解码
     *
     * @author binze
     * 2019/12/27 16:14
     */
    public Decoder decode(@NonNull ImageDecoder.Source source) {
        return new Decoder(source);
    }

    public Decoder decode(@IdRes int sourceId) {
        ImageDecoder.Source source = ImageDecoder.createSource(mCtx.getResources(), sourceId);
        return new Decoder(source);
    }

    public Decoder decode(@NonNull Uri uri) {
        ImageDecoder.Source source = ImageDecoder.createSource(mCtx.getContentResolver(), uri);
        return new Decoder(source);
    }

    public Decoder decode(@NonNull ByteBuffer buffer) {
        ImageDecoder.Source source = ImageDecoder.createSource(buffer);
        return new Decoder(source);
    }

    public Decoder decode(@NonNull File file) {
        ImageDecoder.Source source = ImageDecoder.createSource(file);
        return new Decoder(source);
    }

    /**
     * 负责解码的类
     *
     * @author binze 2019/12/27 16:16
     */
    public class Decoder {
        private ImageDecoder.Source source;
        //监听器数组
        private List<ImageDecoder.OnHeaderDecodedListener> decodeListenerList = new ArrayList<>();
        //不完全解码监听器。若触发并返回true，则展示不完整的图片，缺损区域将会是空白
        private ImageDecoder.OnPartialImageListener partialImageListener = null;
        //图片预处理
        private PostProcessor postProcessor = null;
        private int sizeIndex = -1; //尺寸设置监听器角标
        private int sampleSizeIndex = -1;   //清晰度监听器角标

        //真正的头加载监听器，在里面遍历调用定义的方法
        private ImageDecoder.OnHeaderDecodedListener mListener =
                new ImageDecoder.OnHeaderDecodedListener() {
                    @Override
                    public void onHeaderDecoded(@NonNull ImageDecoder decoder
                            , @NonNull ImageDecoder.ImageInfo info
                            , @NonNull ImageDecoder.Source source) {

                        //图片预处理
                        if (postProcessor != null)
                            decoder.setPostProcessor(postProcessor);
                        //不完整加载监听器
                        if (partialImageListener != null)
                            decoder.setOnPartialImageListener(partialImageListener);
                        //遍历已经添加的所有监听器
                        for (ImageDecoder.OnHeaderDecodedListener listener : decodeListenerList)
                            listener.onHeaderDecoded(decoder, info, source);
                    }
                };

        private Decoder(@NonNull ImageDecoder.Source source) {
            this.source = source;
        }

        /**
         * 获取圆角图片，会覆盖之前设置的postProcessor
         * @author binze 2019/12/27 17:35
         */
        public Decoder roundCorners(final float roundX, final float roundY){
            postProcessor = new PostProcessor() {
                @Override
                public int onPostProcess(@NonNull Canvas canvas) {
                    Path path = new Path();
                    path.setFillType(Path.FillType.INVERSE_EVEN_ODD);
                    int width = canvas.getWidth();
                    int height = canvas.getHeight();
                    path.addRoundRect(0 ,0, width, height, roundX, roundY
                            , Path.Direction.CW);
                    Paint paint = new Paint();
                    paint.setAntiAlias(true);
                    paint.setColor(Color.TRANSPARENT);
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                    canvas.drawPath(path, paint);
                    return PixelFormat.TRANSLUCENT;
                }
            };
            return this;
        }

        /**
         * 设置不完整加载监听器
         *
         * @author binze 2019/12/27 16:55
         */
        public Decoder partialImageListener(ImageDecoder.OnPartialImageListener listener) {
            partialImageListener = listener;
            return this;
        }

        /**
         * 设置头解码监听器
         *
         * @author binze 2019/12/27 16:35
         */
        public Decoder headDecodeListener(ImageDecoder.OnHeaderDecodedListener listener) {
            if (!decodeListenerList.contains(listener))
                decodeListenerList.add(listener);
            return this;
        }

        /**
         * 设置图片预处理方法
         *
         * @author binze 2019/12/27 17:23
         */
        public Decoder postProcessor(PostProcessor postProcessor) {
            this.postProcessor = postProcessor;
            return this;
        }

        /**
         * 设置图片压缩
         *
         * @author binze 2019/12/27 16:40
         */
        public Decoder setSampleSize(final int sampleSize) {
            if (sampleSizeIndex != -1)
                decodeListenerList.remove(sampleSizeIndex);
            sampleSizeIndex = decodeListenerList.size();
            decodeListenerList.add(new ImageDecoder.OnHeaderDecodedListener() {
                @Override
                public void onHeaderDecoded(@NonNull ImageDecoder decoder, @NonNull ImageDecoder.ImageInfo info, @NonNull ImageDecoder.Source source) {
                    decoder.setTargetSampleSize(sampleSize);
                }
            });
            return this;
        }

        /**
         * 设置图片尺寸
         *
         * @author binze 2019/12/27 16:48
         */
        public Decoder setSampleSize(final int width, final int height) {
            if (sizeIndex != -1)
                decodeListenerList.remove(sizeIndex);
            sizeIndex = decodeListenerList.size();
            decodeListenerList.add(new ImageDecoder.OnHeaderDecodedListener() {
                @Override
                public void onHeaderDecoded(@NonNull ImageDecoder decoder, @NonNull ImageDecoder.ImageInfo info, @NonNull ImageDecoder.Source source) {
                    decoder.setTargetSize(width, height);
                }
            });
            return this;
        }

        /**
         * 解码Drawable
         *
         * @author binze 2019/12/27 16:31
         */
        public Drawable decodeDrawable() throws IOException {
            return ImageDecoder.decodeDrawable(source, mListener);
        }

        /**
         * 解码Bitmap
         *
         * @author binze 2019/12/27 16:32
         */
        public Bitmap decodeBitmap() throws IOException {
            return ImageDecoder.decodeBitmap(source, mListener);
        }
    }
}
